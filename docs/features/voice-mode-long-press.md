# Voice Mode Long-Press Gesture

## Goal

Add a gesture to switch voice-typing backends per invocation:

- **Tap** the dictation (mic) key: dictate with the current/default backend (Rambler / agentic dictation).
- **Long-press** the mic key: dictate with the *other* backend (standard voice typing) for that invocation only.

The gesture inverts whatever mode is currently effective. It does not change the
global default (which is owned by Gboard's official selector).

## Background (from Phase 0 RE, Gboard 18.0.3.954559732)

### Backends

- Standard voice typing: `com/.../libs/voiceime/VoiceImeExtension` (+ `VoiceInputHandler`).
- Agentic dictation ("Rambler"): `com/.../libs/agenticdictation/extension/AgenticDictationExtension`
  (obfuscated `fbl`), `AgenticDictationIme`, `AgenticVoiceInputHandlerWrapper` (`fbt`).
- A third "muse" variant is gated by `pref_key_switch_to_muse`.

### The routing decision (per-activation, not cached)

`AgenticDictationExtension.fn()` (onActivate, `fbl.java:533`) does:

```java
if (!mqk.a(context)) return false;   // "Agentic Dictation is disabled, not activating."
```

`mqk.a(context)` = `qhy.I(context).x(R.string.0x7f140a0d, false)` — a persistent
preference read on every call. Other gates that read the same selector:
`mqk.b`, `fbf.b` (`mqh.a && mqk.a`), `mev.A`, and the live pref-change listener `fbh`.

Because the selector is read at activation time, a runtime override of `mqk.a`'s
return value flips the whole routing consistently for that activation.

### The mic key

- Provided by `VoiceIconAccessPointProviderModule` (obfuscated `fax`), widget key
  kind `mif.i` = `WIDGET_VOICE_KEY`, via `AccessPointUtil` (`mhf`).
- The access point binds the key with `softKeyView.c(def)` (`mhf.k`).
- **`SoftKeyView.c(def)` is `r(def, 0L)` (`SoftKeyView.java:397`)** — the exact
  method the repo's long-press framework already hooks (`soft_key_bind`).

Conclusion: the existing SoftKey metadata hook **does** see the mic key; it simply
has no rule for it yet. The gesture can reuse the long-press framework instead of a
new pipeline.

## Design

### 1. Override the selector return (`mqk.a`)

The repo already patches `mqk.a`'s return to *observe* it
(`GboardRambler1803OfficialSelectorPatch.applySelectionReadObserver` →
`RAMBLER_RUNTIME_UPDATE_OFFICIAL_SELECTION`, void).

Change the read observer to *substitute* the value:

```text
before:  invoke-static {vResult}, ...updateOfficialSelection(Z)V
         return vResult

after:   invoke-static {vResult}, ...applyOfficialSelectionOverride(Z)Z
         move-result vResult
         return vResult
```

`applyOfficialSelectionOverride(stock)` records the official selection (as today)
and returns the inverted value when an invocation override is armed, else `stock`.
The write observer keeps using `updateOfficialSelection`.

### 2. Override state (runtime)

Primary mechanism: a **ThreadLocal scope** (like the existing
`enterVoiceSettingsScope`) because the mic activation and `mqk.a` read happen in
the same call path. A one-shot with latch/clear/TTL is kept only if on-device
timing shows the read happens asynchronously.

New runtime methods (in `GboardRambler1803OfficialSelectionRuntime`):

- `enterStandardOverride()` / `exitStandardOverride()` (scope, nestable)
- `boolean applyOfficialSelectionOverride(boolean stock)`

### 3. Gesture

Extend the long-press framework:

- Add a voice-key rule to `GboardLongPressQuickActions1803Policy` so the mic key's
  metadata gets a custom long-press action appended (identified by the voice key id
  / resource entry name `key_pos_header_voice`, GlobeDrag-style).
- On the long-press event, instead of an `InputConnection` editing action: arm the
  standard override **and** dispatch the normal voice-start action so dictation
  begins in standard mode for that invocation.
- The override scope ends when the invocation ends (or on cancel), per the
  lifecycle rules.

### 4. Settings

- New toggle `pref_advanced_voice_long_press_inverts_mode` (default off).
- Surface in `GboardAdvancedVoiceSettingsFeature`.
- No change to the global default backend.

### 5. Tests

- `GboardRambler1803OfficialSelectionRuntimeTest`: override arm/exit nesting,
  `applyOfficialSelectionOverride` true/false paths, interaction with
  voice-settings scope and default-selection suppression.
- Long-press policy test: voice key produces the custom action; other keys
  unaffected.
- `RuntimeAbiVerifierTest`: new/changed `RuntimeCallId`s.
- Kotlin shape test for the selector-read observer substitution.

## Sequencing

1. **A — selector override**: runtime state + `applyOfficialSelectionOverride` +
   Kotlin read-observer substitution + unit tests. Independently reviewable.
2. **B — gesture**: voice-key rule in the long-press policy + arm/dispatch.
3. **C — settings**: toggle + UI.
4. **D — tests** as above.

## Risks / open items

- Confirm the voice key's stable identifier (key id vs resource entry name) for the
  policy rule, and whether the mic already has a long-press/popup
  (`for-al-rambler` layout, `fbf` widget-mode menu).
- Confirm the exact voice-start dispatch used by a tap, so the long-press can
  reproduce it (`kgl` defines voice action `-10042`).
- Lifecycle: ensure the override cannot leak into the next invocation after a
  cancel (clear-on-cancel / TTL).
- Build/test needs Morphe GitHub Packages credentials (`gpr.user`/`gpr.key` or
  `GITHUB_ACTOR`/`GITHUB_TOKEN`).
