package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NutsSpeedQualifier;

public class NutsSpeedQualifiers {
    public static NutsSpeedQualifier avg(NutsSpeedQualifier... all) {
        if (all == null || all.length == 0) {
            return NutsSpeedQualifier.NORMAL;
        }
        long x = -1;
        for (NutsSpeedQualifier q : all) {
            if (q != null) {
                if (x < 0) {
                    x = q.ordinal();
                } else {
                    x += q.ordinal();
                }
            }
        }
        if (x < 0) {
            return NutsSpeedQualifier.NORMAL;
        }
        return NutsSpeedQualifier.values()[(int) (x % NutsSpeedQualifier.values().length)];
    }

    public static NutsSpeedQualifier max(NutsSpeedQualifier... all) {
        if (all == null || all.length == 0) {
            return NutsSpeedQualifier.NORMAL;
        }
        NutsSpeedQualifier x = null;
        for (NutsSpeedQualifier q : all) {
            if (q != null) {
                if (x == null) {
                    x = q;
                } else if (q.ordinal() > x.ordinal()) {
                    x = q;
                }
            }
        }
        if (x == null) {
            return NutsSpeedQualifier.NORMAL;
        }
        return x;
    }

    public static NutsSpeedQualifier min(NutsSpeedQualifier... all) {
        if (all == null || all.length == 0) {
            return NutsSpeedQualifier.NORMAL;
        }
        NutsSpeedQualifier x = null;
        for (NutsSpeedQualifier q : all) {
            if (q != null) {
                if (x == null) {
                    x = q;
                } else if (q.ordinal() < x.ordinal()) {
                    x = q;
                }
            }
        }
        if (x == null) {
            return NutsSpeedQualifier.NORMAL;
        }
        return x;
    }
}
