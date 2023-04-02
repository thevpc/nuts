package net.thevpc.nuts.toolbox.nsh.cmds.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NumberRangeList {
    private List<NumberRange> all;

    public NumberRangeList(List<NumberRange> all) {
        this.all = all;
    }

    public static NumberRangeList parse(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        List<NumberRange> ret = new LinkedList<>();
        for (String s : str.split(",")) {
            int i = s.indexOf("..");
            NumberRange numberRange = new NumberRange();
            if (i < 0) {
                String r = s.trim();
                if (r.length() > 0) {
                    numberRange.setFrom(Long.parseLong(r));
                    numberRange.setTo(numberRange.getFrom());
                    ret.add(numberRange);
                }
            } else {
                String f = s.substring(0, i).trim();
                String t = s.substring(i + 2).trim();
                if (f.length() > 0 || t.length() > 0) {
                    numberRange.setFrom(f.isEmpty() ? null : Long.parseLong(f));
                    numberRange.setTo(f.isEmpty() ? null : Long.parseLong(t));
                    ret.add(numberRange);
                }
            }
        }
        if (ret.isEmpty()) {
            return null;
        }
        return new NumberRangeList(ret);
    }

    public NumberRange toRange() {
        Long from = null;
        Long to = null;
        for (NumberRange r : all) {
            long ff = r.getFrom() == null ? 0 : r.getFrom();
            long tt = r.getTo() == null ? 0 : r.getTo();
            if (from == null) {
                from = ff;
            } else if (from > 0) {
                if (ff > 0) {
                    from = Math.min(from, ff);
                } else {
                    from = 0L;
                }
            } else if (from < 0) {
                if (ff < 0) {
                    from = Math.min(from, ff);
                } else {
                    from = 0L;
                }
            } else {
                from = 0L;
            }
            if (to == null) {
                to = tt;
            } else if (to > 0) {
                if (tt > 0) {
                    to = Math.max(to, tt);
                } else {
                    to = 0L;
                }
            } else if (to < 0) {
                if (tt < 0) {
                    to = Math.max(to, tt);
                } else {
                    to = 0L;
                }
            } else {
                to = 0L;
            }
        }
        from = from == null ? 0L : from;
        to = to == null ? 0L : to;
        if (from == 1) {
            from = 0L;
        }
        if (to == -1) {
            to = 0L;
        }
        return new NumberRange(from, to);
    }
}
