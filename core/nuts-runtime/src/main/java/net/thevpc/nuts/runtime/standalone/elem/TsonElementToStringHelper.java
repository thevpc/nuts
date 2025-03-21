package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class TsonElementToStringHelper {
    public static String leadingCommentsAndAnnotations(NElement any, boolean compact) {
        StringBuilder sb = new StringBuilder();
        for (NElementComment c : any.comments().leadingComments()) {
            sb.append(commentsToString(c, compact));
        }
        //sb.append(any.comments());
        sb.append(any.annotations().stream().map(x -> x.toString(compact)).collect(Collectors.joining(compact ? " " : "\n")));
        if (sb.length() > 0) {
            sb.append(compact ? " " : "\n");
        }
        return sb.toString();
    }

    public static String trailingComments(NElement any, boolean compact) {
        StringBuilder sb = new StringBuilder();
        for (NElementComment c : any.comments().trailingComments()) {
            sb.append(commentsToString(c, false));
        }
        if (sb.length() > 0) {
            sb.insert(0, "\n");
        }
        return sb.toString();
    }

    private static String commentsToString(NElementComment c, boolean compact) {
        StringBuilder sb = new StringBuilder();
        switch (c.type()) {
            case SINGLE_LINE: {
                for (String s : NStringUtils.split(c.text(), "\n", false, false)) {
                    sb.append("// ").append(NStringUtils.trimRight(s)).append("\n");
                }
                break;
            }
            case MULTI_LINE: {
                List<String> lines = NStringUtils.split(c.text(), "\n", false, false);
                if (compact && lines.size() == 1) {
                    sb.append("/* ").append(lines.get(0)).append(" */");
                } else {
                    if (!lines.isEmpty()) {
                        sb.append("/*");
                        for (String line : lines) {
                            sb.append("\n* ").append(NStringUtils.trimRight(line));
                        }
                        sb.append("*/\n");
                    }
                }
            }
            break;
        }
        return sb.toString();
    }

    public static void appendChildren(List<NElement> children, boolean compact, SemiCompactInfo semiCompactInfo, NStringBuilder sb) {
        if (compact) {
            sb.append(children.stream().map(x -> x.toString(compact)).collect(Collectors.joining(", ")));
        } else {
            if (children.size() == 0) {
                //
            } else {
                if (semiCompactInfo == null) {
                    semiCompactInfo = new SemiCompactInfo();
                }
                if (children.stream().allMatch(x -> isSimple(x, compact)) && children.size() <= semiCompactInfo.maxChildren) {
                    boolean first = true;
                    for (NElement child : children) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(new NStringBuilder(child.toString(compact)));
                    }
                } else {
                    for (NElement child : children) {
                        sb.append("\n");
                        sb.append(new NStringBuilder(child.toString(compact)).indent("  "));
                    }
                    sb.append("\n");
                }
            }
        }
    }

    private static boolean isSimple(NElement any, boolean compact) {
        if (any.isPrimitive()) {
            return isSingleLine(any, compact);
        }
        if (any.type() == NElementType.PAIR) {
            NPairElement k = any.asPair().get();
            if (k.key().type() == NElementType.STRING || k.key().type() == NElementType.NAME || k.key().type() == NElementType.REGEX) {
                if (k.value().isPrimitive()) {
                    return isSingleLine(any, compact);
                }
            }
        }
        return false;
    }

    private static boolean isSingleLine(NElement any, boolean compact) {
        return new NStringBuilder(any.toString(compact)).length() > 0;
    }

    public static void appendUplet(String name, List<NElement> params, boolean compact, NStringBuilder sb) {
        if (name != null) {
            sb.append(name);
        }
        if (params != null) {
            sb.append("(");
            appendChildren(params, compact, new SemiCompactInfo().setMaxChildren(10), sb);
            sb.append(")");
        }
    }

    public static class SemiCompactInfo {
        int maxChildren;

        public int getMaxChildren() {
            return maxChildren;
        }

        public SemiCompactInfo setMaxChildren(int maxChildren) {
            this.maxChildren = maxChildren;
            return this;
        }
    }
}
