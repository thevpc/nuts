//package net.thevpc.nuts.runtime.standalone.format.tson.parser;
//
//import net.thevpc.nuts.elem.NElementLine;
//import net.thevpc.nuts.elem.NElementLineSpec;
//import net.thevpc.nuts.text.NMsg;
//import net.thevpc.nuts.text.NNewLineMode;
//import net.thevpc.nuts.util.NIllegalArgumentException;
//
//import java.util.Objects;
//
//public class NElementLineSpecImpl implements NElementLineSpec {
//    private String prefix;
//    private String startPadding;
//    private String endPadding;
//    private String content;
//    private NNewLineMode newline;
//
//    public NElementLineSpecImpl(NElementLine line) {
//        this(line.prefix(),line.startPadding(),line.content(),line.endPadding(),line.newline());
//    }
//
//    public NElementLineSpecImpl(String prefix, String startPadding, String content, String endPadding, NNewLineMode newline) {
//        this.prefix = ensureWhites(prefix);
//        this.startPadding = ensureWhites(startPadding);
//        this.content = content == null ? "" : content;
//        this.endPadding = ensureWhites(endPadding);
//        this.newline = newline == null ? null : newline.normalize();
//    }
//
//    @Override
//    public String prefix() {
//        return prefix;
//    }
//
//    @Override
//    public String startPadding() {
//        return startPadding;
//    }
//
//    @Override
//    public String endPadding() {
//        return endPadding;
//    }
//
//    @Override
//    public String content() {
//        return content;
//    }
//
//    @Override
//    public NNewLineMode newline() {
//        return newline;
//    }
//
//    private String ensureWhites(String any) {
//        if (any == null) {
//            return "";
//        }
//        for (char c : any.toCharArray()) {
//            if (c != ' ' && c != '\t') {
//                throw new NIllegalArgumentException(NMsg.ofC("expected only spaces or tabs in '%s'"));
//            }
//        }
//        return any;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(prefix);
//        sb.append(startPadding);
//        sb.append(content);
//        sb.append(endPadding);
//        if (newline != null) {
//            sb.append(newline);
//        }
//        return sb.toString();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o == null || getClass() != o.getClass()) return false;
//        NElementLineSpecImpl that = (NElementLineSpecImpl) o;
//        return Objects.equals(prefix, that.prefix) && Objects.equals(startPadding, that.startPadding) && Objects.equals(endPadding, that.endPadding) && Objects.equals(content, that.content) && newline == that.newline;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(prefix, startPadding, endPadding, content, newline);
//    }
//}
