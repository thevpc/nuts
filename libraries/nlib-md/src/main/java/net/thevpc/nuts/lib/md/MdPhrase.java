package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.lib.md.util.MdInlineHelper;

import java.util.Arrays;

public class MdPhrase extends MdParent {
    public MdPhrase(MdElement[] content) {
        super(content);
        if (!acceptPhrase(content)) {
            throw new IllegalArgumentException("expected inline elements");
        }
    }

    public static boolean acceptPhrase(MdElement[] content) {
        MdInlineHelper.InlineMode m = MdInlineHelper.detectInline(content);
        switch (m) {
            case NEWLINES:
            case MIDDLE_NEWLINE: {
                return false;
            }
            case END_NEWLINE: {
                if (content[content.length - 1].isText()) {
                    //ok
                } else {
                    return false;
                }
                break;
            }
        }
        return true;
    }

    public MdPhrase toInline() {
        if (isInline()) {
            return this;
        }
        //the only acceptable case is when it ends with new line text
        MdElement[] s = Arrays.copyOf(getChildren(), size());
        s[s.length - 1] = s[s.length - 1].asText().toInline();
        return new MdPhrase(s);
    }

    public MdPhrase toNewline() {
        if (!isInline()) {
            return this;
        }
        if (size()==0) {
            return new MdPhrase(new MdElement[]{new MdText("",false)});
        }
        //the only acceptable case is when it ends with new line text
        MdElement[] s = Arrays.copyOf(getChildren(), size());
        s[s.length - 1] = s[s.length - 1].asText().toNewline();
        return new MdPhrase(s);
    }

    @Override
    public MdElementType type() {
        return MdElementType.PHRASE;
    }

    @Override
    public boolean isInline() {
        if (size() == 0) {
            return true;
        }
        return true;
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
        return !isInline();
    }

    @Override
    public boolean isBlank() {
        return (getChildren() == null
                || getChildren().length == 0
                || Arrays.stream(getChildren()).allMatch(x -> NBlankable.isBlank(x)));
    }
}
