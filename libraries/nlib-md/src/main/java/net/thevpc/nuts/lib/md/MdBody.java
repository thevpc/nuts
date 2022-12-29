package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.NBlankable;

import java.util.Arrays;

public class MdBody extends MdParent {
    public MdBody(MdElement[] content) {
        super(content);
    }

    @Override
    public MdElementType type() {
        return MdElementType.BODY;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                if (!isInline()) {
                    sb.append("\n");
                }
            }
            sb.append(getChild(i));
        }
        return sb.toString();
    }

    @Override
    public boolean isEndWithNewline() {
        return true;
    }

    @Override
    public boolean isBlank() {
        return (getChildren() == null
                || getChildren().length == 0
                || Arrays.stream(getChildren()).allMatch(x -> NBlankable.isBlank(x)));
    }
}
