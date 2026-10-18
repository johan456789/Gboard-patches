import android.content.Context;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;
import java.util.Collection;

final class gtv {
    public final Context c;

    gtv(Context context) {
        c = context;
    }

    public wfr a(EditorInfo editorInfo, boolean incognito) {
        return new wfr();
    }
}

final class obb {
    public final pvl c;

    obb(pvl keyboardType) {
        c = keyboardType;
    }
}

final class pvl {
    public final String m;

    pvl(String name) {
        m = name;
    }
}

final class wfr extends ArrayList<Object> {
    static int copyCalls;

    wfr() {
    }

    wfr(Collection<?> values) {
        super(values);
    }

    public static wfr o(Collection<?> values) {
        copyCalls++;
        return new wfr(values);
    }
}
