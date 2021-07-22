package net.thevpc.nuts.lib.md;

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

}
