package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.env.NSpeedQualifier;

public class NSpeedQualifiers {
    public static NSpeedQualifier avg(NSpeedQualifier... all) {
        if (all == null || all.length == 0) {
            return NSpeedQualifier.NORMAL;
        }
        long x = -1;
        for (NSpeedQualifier q : all) {
            if (q != null) {
                if (x < 0) {
                    x = q.ordinal();
                } else {
                    x += q.ordinal();
                }
            }
        }
        if (x < 0) {
            return NSpeedQualifier.NORMAL;
        }
        return NSpeedQualifier.values()[(int) (x % NSpeedQualifier.values().length)];
    }

    public static NSpeedQualifier max(NSpeedQualifier... all) {
        if (all == null || all.length == 0) {
            return NSpeedQualifier.NORMAL;
        }
        NSpeedQualifier x = null;
        for (NSpeedQualifier q : all) {
            if (q != null) {
                if (x == null) {
                    x = q;
                } else if (q.ordinal() > x.ordinal()) {
                    x = q;
                }
            }
        }
        if (x == null) {
            return NSpeedQualifier.NORMAL;
        }
        return x;
    }

    public static NSpeedQualifier min(NSpeedQualifier... all) {
        if (all == null || all.length == 0) {
            return NSpeedQualifier.NORMAL;
        }
        NSpeedQualifier x = null;
        for (NSpeedQualifier q : all) {
            if (q != null) {
                if (x == null) {
                    x = q;
                } else if (q.ordinal() < x.ordinal()) {
                    x = q;
                }
            }
        }
        if (x == null) {
            return NSpeedQualifier.NORMAL;
        }
        return x;
    }
}
