package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TsonComments implements Comparable<TsonComments> {
    public static final TsonComments BLANK = new TsonComments();
    private TsonComment[] leadingComments;
    private TsonComment[] trailingComments;

    private static TsonComment[] _trim(TsonComment[] all) {
        List<TsonComment> ok = new ArrayList<>();
        if (all != null) {
            for (TsonComment a : all) {
                if (a != null) {
                    if (a.text().length() > 0) {
                        ok.add(a);
                    }
                }
            }
        }
        return ok.toArray(new TsonComment[0]);
    }

    public TsonComments() {
        this.leadingComments = new TsonComment[0];
        this.trailingComments = new TsonComment[0];
    }

    public TsonComments(TsonComment[] leadingComments, TsonComment[] trailingComments) {
        this.leadingComments = _trim(leadingComments);
        this.trailingComments = _trim(trailingComments);
    }

    public static TsonComments concat(TsonComments a, TsonComments b) {
        if (a == null && b == null) {
            return null;
        }
        if (a != null) {
            return a.concat(b);
        }
        return b;
    }

    public static TsonComments ofMultiLine(String a) {
        return new TsonComments(new TsonComment[]{TsonComment.ofMultiLine(a)}, null);
    }

    public static TsonComments ofSingleLine(String a) {
        return new TsonComments(new TsonComment[]{TsonComment.ofSingleLine(a)}, null);
    }

    public TsonComments concat(TsonComments other) {
        if (other != null && !other.isEmpty()) {
            List<TsonComment> a = new ArrayList<>();
            List<TsonComment> b = new ArrayList<>();
            a.addAll(Arrays.asList(leadingComments()));
            a.addAll(Arrays.asList(other.leadingComments()));
            b.addAll(Arrays.asList(trailingComments()));
            b.addAll(Arrays.asList(other.trailingComments()));
            return new TsonComments(
                    a.toArray(new TsonComment[0]),
                    b.toArray(new TsonComment[0])
            );
        }
        return this;
    }

    public TsonComments addLeading(TsonComment ...other) {
        if (other != null) {
            List<TsonComment> a = new ArrayList<>();
            List<TsonComment> b = new ArrayList<>();
            a.addAll(Arrays.asList(leadingComments()));
            b.addAll(Arrays.asList(trailingComments()));
            a.addAll(Arrays.asList(_trim(other)));
            return new TsonComments(
                    a.toArray(new TsonComment[0]),
                    b.toArray(new TsonComment[0])
            );
        }
        return this;
    }

    public TsonComments addTrailing(TsonComment ...other) {
        if (other != null) {
            List<TsonComment> a = new ArrayList<>();
            List<TsonComment> b = new ArrayList<>();
            a.addAll(Arrays.asList(leadingComments()));
            b.addAll(Arrays.asList(trailingComments()));
            b.addAll(Arrays.asList(_trim(other)));
            return new TsonComments(
                    a.toArray(new TsonComment[0]),
                    b.toArray(new TsonComment[0])
            );
        }
        return this;
    }

    public TsonComments addLeading(TsonComment other) {
        if (other != null) {
            List<TsonComment> a = new ArrayList<>();
            List<TsonComment> b = new ArrayList<>();
            a.addAll(Arrays.asList(leadingComments()));
            b.addAll(Arrays.asList(trailingComments()));
            a.add(other);
            return new TsonComments(
                    a.toArray(new TsonComment[0]),
                    b.toArray(new TsonComment[0])
            );
        }
        return this;
    }

    public TsonComments addTrailing(TsonComment other) {
        if (other != null) {
            List<TsonComment> a = new ArrayList<>();
            List<TsonComment> b = new ArrayList<>();
            a.addAll(Arrays.asList(leadingComments()));
            b.addAll(Arrays.asList(trailingComments()));
            b.add(other);
            return new TsonComments(
                    a.toArray(new TsonComment[0]),
                    b.toArray(new TsonComment[0])
            );
        }
        return this;
    }

    public TsonComment[] getComments() {
        return leadingComments;
    }

    public TsonComment[] trailingComments() {
        return trailingComments;
    }

    public TsonComment[] leadingComments() {
        return leadingComments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TsonComments that = (TsonComments) o;
        return Objects.deepEquals(leadingComments, that.leadingComments) && Objects.deepEquals(trailingComments, that.trailingComments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(leadingComments), Arrays.hashCode(trailingComments));
    }

    public boolean isEmpty() {
        return leadingComments.length == 0 && trailingComments.length == 0;
    }

    @Override
    public int compareTo(TsonComments o) {
        int i = TsonApiUtils.compareArrays(leadingComments(), o.leadingComments());
        if (i != 0) {
            return i;
        }
        i = TsonApiUtils.compareArrays(trailingComments(), o.trailingComments());
        if (i != 0) {
            return i;
        }
        return 0;
    }
}
