//package net.thevpc.nuts.runtime.standalone.format.tson.parser;
//
//import net.thevpc.nuts.elem.NElementComment;
//
//public class CommentsHelper {
//    private static ThreadLocal<Data> CURR = new ThreadLocal<>();
//
//    private static class Data {
//        private TsonStreamParserConfig config;
//        private TsonParserVisitor visitor;
//        private Object source;
//
//        public Data(TsonStreamParserConfig config, TsonParserVisitor visitor, Object source) {
//            this.config = config;
//            this.visitor = visitor;
//            this.source = source;
//        }
//    }
//
//    public static void init(TsonStreamParserConfig config, TsonParserVisitor visitor, Object source) {
//        CURR.set(new Data(config, visitor, source));
//    }
//
//    public static void onComments(String image) {
//        Data c = CURR.get();
//        if (!c.config.isSkipComments()) {
//            NElementComment rc = (!c.config.isRawComments())
//                    ? parseComments(image)
//                    : NElementComment.ofMultiLine(image);
//            c.visitor.visitComments(rc);
//        }
//    }
//
//
//    public static NElementComment parseComments(String c) {
//        if (c == null) {
//            return null;
//        }
//        if (c.startsWith("/*")) {
//            return NElementComment.ofMultiLine(escapeMultiLineComments(c));
//        }
//        if (c.startsWith("//")) {
//            return NElementComment.ofSingleLine(escapeSingleLineComments(c));
//        }
//        throw new IllegalArgumentException("unsupported comments " + c);
//    }
//
//    public static String escapeSingleLineComments(String c) {
//        if (c == null) {
//            return null;
//        }
//        if (c.startsWith("//")) {
//            return c.substring(2);
//        }
//        throw new IllegalArgumentException("unsupported comments " + c);
//    }
//
//    public static String escapeMultiLineComments(String c) {
//        if (c == null) {
//            return null;
//        }
//        int line = 0;
//        String[] lines = c.trim().split("\n");
//        StringBuilder sb = new StringBuilder();
//        for (String s : lines) {
//            s = s.trim();
//            if (line == 0) {
//                if (s.startsWith("/*")) {
//                    s = s.substring(2);
//                }
//            }
//            if (line == lines.length - 1) {
//                if (s.endsWith("*/")) {
//                    s = s.substring(0, s.length() - 2);
//                }
//            }
//            if (s.equals("*")) {
//                s = s.substring(1);
//            } else if (s.equals("**")) {
//                s = s.substring(1);
//            } else if (s.startsWith("*") && s.length() > 1 && Character.isWhitespace(s.charAt(1))) {
//                s = s.substring(2).trim();
//            } else if (s.startsWith("**") && s.length() > 2 && Character.isWhitespace(s.charAt(1))) {
//                s = s.substring(2).trim();
//            }
//            if (s.length() > 1 && s.charAt(0) == '*' && s.charAt(1) == ' ') {
//                s = s.substring(2);
//            }
//            s = s.trim();
//            if (line == lines.length - 1) {
//                if (s.length() > 0) {
//                    if (line > 0) {
//                        sb.append("\n");
//                    }
//                    sb.append(s.trim());
//                }
//            } else {
//                if (line > 0) {
//                    sb.append("\n");
//                }
//                sb.append(s.trim());
//            }
//            line++;
//        }
//        return sb.toString().trim();
//    }
//
//}
