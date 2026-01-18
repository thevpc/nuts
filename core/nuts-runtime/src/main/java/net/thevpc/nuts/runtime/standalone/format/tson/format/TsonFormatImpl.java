package net.thevpc.nuts.runtime.standalone.format.tson.format;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.io.WriterOutputStream;
import net.thevpc.nuts.runtime.standalone.format.tson.util.Kmp;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TsonFormatImpl implements TsonFormat, Cloneable {
    public static final HashSet<String> FORMAT_NUMBER_TYPES = new HashSet<>(Arrays.asList("hex", "dec", "bin", "oct"));

    private DefaultTsonFormatConfig config;

    public TsonFormatImpl(DefaultTsonFormatConfig config) {
        this.config = (config == null ? new DefaultTsonFormatConfig().setCompact(false) : config.copy());
    }

    @Override
    public String format(NElement element) {
        StringBuilder sb = new StringBuilder();
        try (Kmp.AppendableWriter w = Kmp.AppendableWriter.of(sb)) {
            formatElement(element, config.isShowComments(), config.isShowAnnotations(), w);
        }
        return sb.toString();
    }

    public void format(NElement element, Writer sb) {
        formatElement(element, config.isShowComments(), config.isShowAnnotations(), sb);
    }

    public void formatAnnotation(NElementAnnotation a, boolean showComments, boolean showAnnotations, Writer sb) {
        final List<NElement> params = a.params();
        try {
            sb.append('@');
            sb.append(a.name());
            if (params != null) {
                sb.append('(');
                int i = 0;
                for (NElement p : params) {
                    if (i > 0) {
                        sb.append(',');
                        sb.append(config.afterComma);
                    }
                    //cannot have embedded annotations
                    formatElement(p, showComments, showAnnotations, sb);
                    i++;
                }
                sb.append(')');
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void formatElement(NElement element, boolean showComments, boolean showAnnotations, Writer sb) {
        try {
            if (showComments) {
                NElementComments c = element.comments();
                if (c != null && !c.isEmpty()) {
                    List<NElementComment> leadingComments = c.leadingComments();
                    if (!leadingComments.isEmpty()) {
                        boolean wasSLC = false;
                        for (NElementComment lc : leadingComments) {
                            switch (lc.type()) {
                                case MULTI_LINE: {
                                    sb.append(formatMultiLineComments(lc.text(), !config.isIndentBraces()));
                                    sb.append(config.afterMultiLineComments);
                                    wasSLC = false;
                                    break;
                                }
                                case SINGLE_LINE: {
                                    if (!wasSLC) {
                                        sb.append("\n");
                                    }
                                    sb.append(formatSingleLineComments(lc.text()));
                                    wasSLC = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            List<NElementAnnotation> ann = element.annotations();
            NElementAnnotation formatAnnotation = null;

            if (ann != null && !ann.isEmpty()) {
                for (NElementAnnotation a : ann) {
                    if ("format".equals(a.name())) {
                        formatAnnotation = a;
                        if (showAnnotations) {
                            if (!config.showFormatNumber) {
                                NElementAnnotationBuilder ab = a.builder();
                                List<NElement> params = ab.params();
                                for (int i = params.size() - 1; i >= 0; i--) {
                                    NElement o = params.get(i);
                                    if (o.type() == NElementType.NAME || o.type().isAnyString() && FORMAT_NUMBER_TYPES.contains(o.asStringValue().get())) {
                                        ab.removeAt(i);
                                    }
                                }
                                if (ab.size() != 0) {
                                    formatAnnotation(a, showComments, true, sb);
                                    sb.append(config.afterAnnotation);
                                }
                            } else {
                                formatAnnotation(a, showComments, true, sb);
                                sb.append(config.afterAnnotation);
                            }
                        }
                    } else {
                        if (showAnnotations) {
                            formatAnnotation(a, showComments, true, sb);
                            sb.append(config.afterAnnotation);
                        }
                    }
                }
                sb.append(config.afterAnnotations);
            }
            formatElementCore(element, formatAnnotation, sb);
            if (showComments) {
                NElementComments c = element.comments();
                if (c != null && !c.isEmpty()) {
                    List<NElementComment> trailingComments = c.trailingComments();
                    if (!trailingComments.isEmpty()) {
                        boolean wasSLC = false;
                        for (NElementComment lc : trailingComments) {
                            switch (lc.type()) {
                                case MULTI_LINE: {
                                    sb.append(formatMultiLineComments(lc.text(), false));
                                    sb.append(config.afterMultiLineComments);
                                    wasSLC = false;
                                    break;
                                }
                                case SINGLE_LINE: {
                                    if (!wasSLC) {
                                        sb.append("\n");
                                    }
                                    sb.append(formatSingleLineComments(lc.text()));
                                    wasSLC = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void formatElementCore(NElement element, NElementAnnotation format, Writer writer) {
        try {
//        sb.ensureCapacity(sb.length() + 10);
            switch (element.type()) {
                case NULL:
                    writer.append("null");
                    return;
                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                case BIG_INT:
                case FLOAT:
                case DOUBLE:
                case BIG_DECIMAL:
                case BIG_COMPLEX:
                case FLOAT_COMPLEX:
                case DOUBLE_COMPLEX: {
                    writer.append(((NNumberElement) element).toString());
                    return;
                }
                case BOOLEAN:
                    writer.append(String.valueOf(element.asBooleanValue().get()));
                    return;
                case LOCAL_DATETIME:
                    writer.append(String.valueOf(element.asLocalDateTimeValue().get()));
                    return;
                case LOCAL_DATE:
                    writer.append(String.valueOf(element.asLocalDateValue().get()));
                    return;
                case INSTANT:
                    writer.append(String.valueOf(element.asInstantValue().get()));
                    return;
                case LOCAL_TIME:
                    writer.append(String.valueOf(element.asLocalTimeValue().get()));
                    return;
                case CHAR: {
                    writer.append(Kmp.TsonUtils.toSmpStr(element.asCharValue().get()));
                    return;
                }
                case DOUBLE_QUOTED_STRING:
                case SINGLE_QUOTED_STRING:
                case BACKTICK_STRING:
                case TRIPLE_DOUBLE_QUOTED_STRING:
                case TRIPLE_SINGLE_QUOTED_STRING:
                case TRIPLE_BACKTICK_STRING:
                case LINE_STRING: {
//                String s = element.getString();
//                StringBuilder sb = new StringBuilder(s.length() * 2);
//                TsonUtils.toQuotedStr(s, element.toStr().layout(), sb);
                    //TsonUtils.toDblStr(s, writer);
                    writer.append(element.asString().get().literalString());
                    return;
                }
                case NAME: {
                    writer.append(element.asStringValue().get());
                    return;
                }
                case PAIR: {
                    NPairElement t = element.asPair().get();
                    format(t.key(), writer);
                    String vs = format(t.value());
                    writer.append(config.afterKey);
                    if (config.indent.length() > 0 && vs.indexOf("\n") > 0) {
                        writer.append(":\n").append(Kmp.TsonUtils.indent(vs, config.indent));
                    } else {
                        writer.append(':').append(config.beforeValue).append(vs);
                    }
                    return;
                }
                case UNARY_OPERATOR:
                case BINARY_OPERATOR:
                case TERNARY_OPERATOR:
                case NARY_OPERATOR:
                {
                    NExprElement t = element.asTernaryOperator().get();
                    List<NElement> operands = t.operands();
                    List<NOperatorSymbol> operatorSymbols = t.operatorSymbols();
                    switch (t.position()) {
                        case PREFIX: {
                            String opSymbol = operatorSymbols.get(0).lexeme();
                            writer.append(opSymbol);
                            for (NElement operand : operands) {
                                writer.append(" ");
                                format(operand, writer);
                            }
                            break;
                        }
                        case SUFFIX: {
                            String opSymbol = operatorSymbols.get(0).lexeme();
                            for (NElement operand : operands) {
                                format(operand);
                                writer.append(" ");
                            }
                            writer.append(opSymbol);
                            break;
                        }
                        case INFIX: {
                            NStringBuilder sb = new NStringBuilder();
                            for (int i = 0; i < operands.size(); i++) {
                                if (i > 0) {
                                    if (i < operatorSymbols.size()) {
                                        sb.append(operatorSymbols.get(i));
                                        sb.append(" ");
                                    } else {
                                        sb.append(operatorSymbols.size() - 1);
                                        sb.append(" ");
                                    }
                                }
                                NElement operand = operands.get(i);
                                format(operand);
                            }
                            break;
                        }
                    }
                    break;
                }
                case OPERATOR_SYMBOL: {
                    NOperatorSymbolElement t = element.asOperatorSymbol().get();
                    writer.append(t.symbol().lexeme());
                    break;
                }
                case FLAT_EXPR: {
                    NFlatExprElement t = (NFlatExprElement) element;
                    int index = 0;
                    for (NElement e : t) {
                        if (index > 0) {
                            writer.append(" ");
                        }
                        format(e, writer);
                        index++;
                    }
                    break;
                }

                case UPLET:
                case NAMED_UPLET: {
                    NUpletElement list = element.asUplet().get();
                    if (list.isNamed()) {
                        writer.append(list.name().orNull());
                    }
                    listToString(config.indentList, list.params(), '(', ')', writer, ListType.PARAMS);
                    return;
                }
                case ARRAY:
                case NAMED_PARAMETRIZED_ARRAY:
                case PARAMETRIZED_ARRAY:
                case NAMED_ARRAY: {
                    NArrayElement list = element.asArray().get();
                    String n = NStringUtils.trimToNull(list.name().orNull());
                    boolean hasName = false;
                    if (n != null) {
                        writer.append(n);
                        hasName = n.length() > 0;
                    }
                    List<NElement> params = list.params().orNull();
                    if (params != null) {
                        if (!hasName || params.size() > 0) {
                            listToString(config.indentList, params, '(', ')', writer, ListType.PARAMS);
                        }
                    }
                    listToString(config.indentBrackets ? IndentMode.ALWAYS : IndentMode.NEVER, list, '[', ']', writer, ListType.PARAMS);
                    return;
                }
                case OBJECT:
                case NAMED_PARAMETRIZED_OBJECT:
                case NAMED_OBJECT:
                case PARAMETRIZED_OBJECT: {
                    NObjectElement list = element.asObject().get();
                    String n = NStringUtils.trimToNull(list.name().orNull());
                    boolean hasName = false;
                    if (n != null) {
                        writer.append(n);
                        hasName = n.length() > 0;
                    }
                    List<NElement> params = list.params().orNull();
                    if (params != null) {
                        if (!hasName || params.size() > 0) {
                            listToString(config.indentList, params, '(', ')', writer, ListType.PARAMS);
                        }
                    }
                    listToString(config.indentBraces ? IndentMode.ALWAYS : IndentMode.NEVER, list, '{', '}', writer, ListType.OBJECT);
                    return;
                }
                case BINARY_STREAM: {
                    NBinaryStreamElement list = (NBinaryStreamElement) element;
                    writer.write("^[");
                    char[] c = new char[1024];
                    NInputStreamProvider p = list.value();
                    try (OutputStream base64Out = Base64.getEncoder().wrap(new WriterOutputStream(writer, StandardCharsets.UTF_8))) {
                        try (InputStream in = p.getInputStream()) {
                            byte[] buffer = new byte[4096];
                            int n;
                            while ((n = in.read(buffer)) != -1) {
                                base64Out.write(buffer, 0, n);
                            }
                        }
                    }
                    writer.write("]");
                    return;
                }
                case CHAR_STREAM: {
                    NCharStreamElement list = (NCharStreamElement) element;
                    String blockIdentifier = list.getBlocIdentifier();
                    if (blockIdentifier == null) {
                        blockIdentifier = "";
                    }
                    String n = blockIdentifier;
                    for (char c : n.toCharArray()) {
                        switch (c) {
                            case '}':
                            case '{': {
                                throw new IllegalArgumentException("Invalid StopWord CharStream : " + n);
                            }
                        }
                    }
                    writer.write("^" + n + "{");
                    String stop = "^" + n + "}";
                    Kmp kmp = Kmp.compile(stop);
                    char[] c = new char[1024];
                    NReaderProvider rp = list.value();
                    try (Reader r = rp.getReader()) {
                        int x;
                        while ((x = r.read(c)) > 0) {
                            for (int i = 0; i < x; i++) {
                                if (kmp.next(c[i])) {
                                    throw new IllegalArgumentException("Invalid StopWord CharStream StopWord detected in content : " + n);
                                }
                            }
                            writer.write(c, 0, x);
                        }
                    }
                    return;
                }
            }
            throw new IllegalArgumentException("Format Tson : Unexpected type " + element.type());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

//    private void formatAppendUnit(String ll, TsonNumber element, Writer writer) throws IOException {
//        writer.append(ll);
//        String unit = TsonUtils.trimToNull(element.numberSuffix());
//        if (unit != null) {
//            if (unit.charAt(0) != '%' && unit.charAt(0) != '_') {
//                writer.append('_');
//            }
//            writer.append(unit);
//        }
//    }


//    private String decodeRadixPrefix(TsonNumberLayout l) {
//        switch (l) {
//            case BINARY:
//                return "0b";
//            case OCTAL:
//                return "0";
//            case HEXADECIMAL:
//                return "0x";
//            case DECIMAL:
//                return "";
//            default:
//                return "";
//        }
//    }

    private void listToString(boolean indent, Iterable<NElement> it, char start, char end, Writer out, ListType listType) throws IOException {
        IndentMode indentMode = indent ? IndentMode.OPTIMIZE : IndentMode.NEVER;
        listToString(indentMode, it, start, end, out, listType);
    }

    private void listToString(IndentMode indent, Iterable<NElement> it, char start, char end, Writer out, ListType listType) throws IOException {
        if (it == null) {
            return;
        }
        if (indent == null) {
            indent = IndentMode.OPTIMIZE;
        }
        switch (indent) {
            case ALWAYS: {
                listToStringIndented(it, start, end, out, listType);
                break;
            }
            case NEVER: {
                listToStringNotIndented(it, start, end, out, listType);
                break;
            }
            case OPTIMIZE: {
                listToStringIndentOptimized(it, start, end, out, listType);
            }
        }
    }

    private void listToStringIndentOptimized(Iterable<NElement> it, char start, char end, Writer out, ListType listType) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Writer w2 = new PrintWriter(os);
        listToStringNotIndented(it, start, end, w2, listType);
        w2.flush();
        String s = os.toString();
        if (s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0 || (config.getLineLength() >= 0 && s.length() > config.getLineLength())) {
            listToStringIndented(it, start, end, out, listType);
        } else {
            out.append(s);
        }
    }

    private void listToStringIndented(Iterable<NElement> it, char start, char end, Writer out, ListType listType) throws IOException {
        List<NElement> a = new ArrayList<>();
        for (NElement e : it) {
            a.add(e);
        }
        if (a.isEmpty()) {
            out.append(start).append(end);
            return;
        }
        out.append(start).append('\n');
        StringBuilder sb2 = new StringBuilder();

        try (Kmp.AppendableWriter w = Kmp.AppendableWriter.of(sb2)) {
            int i = 0;

            switch (listType) {
                case OBJECT: {
                    for (NElement tsonElement : a) {
                        if (acceptObjectElement(tsonElement)) {
                            if (i > 0) {
                                sb2.append(",\n");
                            }
                            format(tsonElement, w);
                            i++;
                        }
                    }
                    break;
                }
                case ARRAY: {
                    for (NElement tsonElement : a) {
                        if (acceptArrayElement(tsonElement)) {
                            if (i > 0) {
                                sb2.append(",\n");
                            }
                            format(tsonElement, w);
                            i++;
                        }
                    }
                    break;
                }
                case PARAMS: {
                    for (NElement tsonElement : a) {
                        if (acceptParamElement(tsonElement)) {
                            if (i > 0) {
                                sb2.append(",\n");
                            }
                            format(tsonElement, w);
                            i++;
                        }
                    }
                    break;
                }
                case MATRIX: {
                    for (NElement tsonElement : a) {
//                        if (acceptParamElement(tsonElement)) {
                        if (i > 0) {
                            sb2.append(";\n");
                        }
                        format(tsonElement, w);
                        i++;
//                        }
                    }
                    if (i < 2) {
                        sb2.append(";\n");
                    }
                    break;
                }
            }
        }
        out.append(Kmp.TsonUtils.indent(sb2.toString(), config.indent));
        out.append('\n').append(end);
    }

    private boolean acceptObjectElement(NElement tsonElement) {
        if (config.ignoreObjectNullFields) {
            if (tsonElement.isNull()) {
                return false;
            }
            if (tsonElement.type() == NElementType.PAIR && tsonElement.asPair().get().value().isNull()) {
                return false;
            }
        }
        if (config.ignoreObjectEmptyArrayFields) {
            if (tsonElement.type() == NElementType.ARRAY && tsonElement.toArray().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void listToStringNotIndented(Iterable<NElement> it, char start, char end, Writer out, ListType type) throws
            IOException {
        out.append(start);
        int i = 0;
        switch (type) {
            case OBJECT: {
                for (NElement tsonElement : it) {
                    if (acceptObjectElement(tsonElement)) {
                        if (i > 0) {
                            out.append(',').append(config.afterComma);
                        }
                        format(tsonElement, out);
                        i++;
                    }
                }
                break;
            }
            case PARAMS: {
                for (NElement tsonElement : it) {
                    if (acceptParamElement(tsonElement)) {
                        if (i > 0) {
                            out.append(',').append(config.afterComma);
                        }
                        format(tsonElement, out);
                        i++;
                    }
                }
                break;
            }
            case ARRAY: {
                for (NElement tsonElement : it) {
                    if (acceptArrayElement(tsonElement)) {
                        if (i > 0) {
                            out.append(',').append(config.afterComma);
                        }
                        format(tsonElement, out);
                        i++;
                    }
                }
                break;
            }
            case MATRIX: {
                for (NElement tsonElement : it) {
                    //if (acceptArrayElement(tsonElement)) {
                    if (i > 0) {
                        out.append(';').append(config.afterComma);
                    }
                    format(tsonElement, out);
                    i++;
                    //}
                }
                if (i < 2) {
                    out.append(';').append(config.afterComma);
                }
                break;
            }
        }
        out.append(end);
    }

    private boolean acceptParamElement(NElement tsonElement) {
        return true;
    }

    private boolean acceptArrayElement(NElement tsonElement) {
        return true;
    }

    @Override
    public TsonFormatBuilder builder() {
        return new TsonFormatImplBuilder().setConfig(config);
    }

    private enum IndentMode {
        ALWAYS,
        NEVER,
        OPTIMIZE,
    }

    private enum ListType {
        MATRIX,
        PARAMS,
        ARRAY,
        OBJECT,
    }

    public static String formatMultiLineComments(String str, boolean compact) {
        if (str != null) {
            if (compact) {
                List<String> lines = Kmp.TsonUtils.lines(str);
                if (lines.isEmpty()) {
                    return "/* */";
                }
                if (lines.size() == 1) {
                    return "/* " + lines.get(0) + " */";
                }
                return "/*\n"
                        + Kmp.TsonUtils.indent(str, "* ")
                        + "*/\n";
            } else {
                return "/*\n"
                        + Kmp.TsonUtils.indent(str, "* ")
                        + "*/\n";
            }
        }
        return "";
    }

    public static String formatSingleLineComments(String str) {
        if (str != null) {
            return Kmp.TsonUtils.indent(str, "// ") + "\n";
        }
        return "";
    }

}
