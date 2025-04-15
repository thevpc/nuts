package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NElementComments;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NElementCommentsImpl implements NElementComments {
    public static final NElementComments BLANK = new NElementCommentsImpl();
    private NElementComment[] leadingComments;
    private NElementComment[] trailingComments;

    private static NElementComment[] _trim(NElementComment[] all) {
        List<NElementComment> ok = new ArrayList<>();
        if (all != null) {
            for (NElementComment a : all) {
                if (a != null) {
                    if (!a.text().isEmpty()) {
                        ok.add(a);
                    }
                }
            }
        }
        return ok.toArray(new NElementComment[0]);
    }

    public static NElementComments ofLeading(NElementComment... all) {
        return new NElementCommentsImpl(all, null);
    }

    public static NElementComments ofTrailing(NElementComment... all) {
        return new NElementCommentsImpl(null, all);
    }

    public static NElementComments ofTrailing(NElementComment[] leading, NElementComment[] trailing) {
        return new NElementCommentsImpl(leading, trailing);
    }

    public NElementCommentsImpl() {
        this.leadingComments = new NElementComment[0];
        this.trailingComments = new NElementComment[0];
    }

    public NElementCommentsImpl(NElementComment[] leadingComments, NElementComment[] trailingComments) {
        this.leadingComments = _trim(leadingComments);
        this.trailingComments = _trim(trailingComments);
    }

    public static NElementComments concat(NElementCommentsImpl a, NElementCommentsImpl b) {
        if (a == null && b == null) {
            return null;
        }
        if (a != null) {
            return a.concat(b);
        }
        return b;
    }

    @Override
    public String toString() {
        NStringBuilder sb = new NStringBuilder();
        for (NElementComment leadingComment : leadingComments) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append(leadingComment);
        }
        for (NElementComment ec : trailingComments) {
            sb.append("\n");
            sb.append(ec);
        }
        return sb.toString();
    }

    public NElementComments concat(NElementComments other) {
        if (other != null) {
            List<NElementComment> a = new ArrayList<>();
            List<NElementComment> b = new ArrayList<>();
            a.addAll(leadingComments());
            a.addAll(other.leadingComments());
            b.addAll(trailingComments());
            b.addAll(other.trailingComments());
            return new NElementCommentsImpl(
                    a.toArray(new NElementComment[0]),
                    b.toArray(new NElementComment[0])
            );
        }
        return this;
    }

    @Override
    public List<NElementComment> trailingComments() {
        return Arrays.asList(trailingComments);
    }

    @Override
    public List<NElementComment> leadingComments() {
        return Arrays.asList(leadingComments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NElementCommentsImpl that = (NElementCommentsImpl) o;
        return Objects.deepEquals(leadingComments, that.leadingComments) && Objects.deepEquals(trailingComments, that.trailingComments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(leadingComments), Arrays.hashCode(trailingComments));
    }

    public boolean isBlank() {
        return (
                (leadingComments.length == 0 || Arrays.stream(leadingComments).allMatch(NElementComment::isBlank))
                        && (trailingComments.length == 0 || Arrays.stream(trailingComments).allMatch(NElementComment::isBlank))
        );
    }

    public boolean isEmpty() {
        return leadingComments.length == 0 && trailingComments.length == 0;
    }

    @Override
    public int compareTo(NElementComments o) {
        int i = compareLists(leadingComments(), o.leadingComments());
        if (i != 0) {
            return i;
        }
        i = compareLists(trailingComments(), o.trailingComments());
        if (i != 0) {
            return i;
        }
        return 0;
    }

    private static <T extends Comparable<T>> int compareLists(List<T> a1, List<T> a2) {
        for (int j = 0; j < Math.max(a1.size(), a2.size()); j++) {
            if (j >= a1.size()) {
                return -1;
            }
            if (j >= a2.size()) {
                return 1;
            }
            int i = a1.get(j).compareTo(a2.get(j));
            if (i != 0) {
                return i;
            }
        }
        return 0;
    }
}
