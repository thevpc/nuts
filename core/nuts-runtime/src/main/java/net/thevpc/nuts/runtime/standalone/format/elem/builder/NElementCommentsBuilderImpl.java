package net.thevpc.nuts.runtime.standalone.format.elem.builder;

import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NElementComments;
import net.thevpc.nuts.runtime.standalone.format.elem.item.NElementCommentsImpl;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.*;

public class NElementCommentsBuilderImpl {
    private List<NElementComment> leadingComments = new ArrayList<>();
    private List<NElementComment> trailingComments = new ArrayList<>();

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


    public NElementCommentsBuilderImpl() {
    }

    public NElementCommentsBuilderImpl(NElementComment[] leadingComments, NElementComment[] trailingComments) {
        this.leadingComments.addAll(Arrays.asList(_trim(leadingComments)));
        this.trailingComments.addAll(Arrays.asList(_trim(trailingComments)));
    }

    public NElementCommentsBuilderImpl addLeading(NElementComment... leadingComments) {
        this.leadingComments.addAll(Arrays.asList(_trim(leadingComments)));
        return this;
    }

    public NElementCommentsBuilderImpl addTrailing(NElementComment... trailingComments) {
        this.trailingComments.addAll(Arrays.asList(_trim(trailingComments)));
        return this;
    }

    public NElementCommentsBuilderImpl removeLeadingCommentAt(int index) {
        if (index >= 0 && index < leadingComments.size()) {
            leadingComments.remove(index);
        }
        return this;
    }

    public NElementCommentsBuilderImpl removeLeading(NElementComment comment) {
        for (Iterator<NElementComment> iterator = leadingComments.iterator(); iterator.hasNext(); ) {
            NElementComment currentComment = iterator.next();
            if (Objects.equals(currentComment, comment)) {
                iterator.remove();
                break;
            }
        }
        return this;
    }

    public NElementCommentsBuilderImpl removeTrailingCommentAt(int index) {
        if (index >= 0 && index < trailingComments.size()) {
            trailingComments.remove(index);
        }
        return this;
    }

    public NElementCommentsBuilderImpl removeTrailingComment(NElementComment comment) {
        for (Iterator<NElementComment> iterator = trailingComments.iterator(); iterator.hasNext(); ) {
            NElementComment currentComment = iterator.next();
            if (Objects.equals(currentComment, comment)) {
                iterator.remove();
                break;
            }
        }
        return this;
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

    public List<NElementComment> trailingComments() {
        return Collections.unmodifiableList(trailingComments);
    }

    public List<NElementComment> leadingComments() {
        return Collections.unmodifiableList(leadingComments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NElementCommentsBuilderImpl that = (NElementCommentsBuilderImpl) o;
        return Objects.deepEquals(leadingComments, that.leadingComments) && Objects.deepEquals(trailingComments, that.trailingComments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Objects.hashCode(leadingComments), Objects.hashCode(trailingComments));
    }

    public boolean isBlank() {
        return leadingComments.isEmpty() && trailingComments.isEmpty();
    }


    public NElementComments build() {
        return new NElementCommentsImpl(leadingComments.toArray(new NElementComment[0]), trailingComments.toArray(new NElementComment[0]));
    }

    public NElementCommentsBuilderImpl clear() {
        leadingComments.clear();
        trailingComments.clear();
        return this;
    }

    public NElementCommentsBuilderImpl addComments(NElementComments comments) {
        if(comments!=null) {
            for (NElementComment e : comments.leadingComments()) {
                addLeading(e);
            }
            for (NElementComment e : comments.trailingComments()) {
                addTrailing(e);
            }
        }
        return this;
    }
}
