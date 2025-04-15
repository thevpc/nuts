package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.format;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.CharStreamCodeSupports;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.TsonNumberHelper;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.AppendableWriter;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.util.Kmp;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TsonFormatImpl implements TsonFormat, Cloneable {
    public static final HashSet<String> FORMAT_NUMBER_TYPES = new HashSet<>(Arrays.asList("hex", "dec", "bin", "oct"));

    private DefaultTsonFormatConfig config;

    public TsonFormatImpl(DefaultTsonFormatConfig config) {
        this.config = (config == null ? new DefaultTsonFormatConfig().setCompact(false) : config.copy());
    }

    @Override
    public String format(TsonElement element) {
        StringBuilder sb = new StringBuilder();
        try (AppendableWriter w = AppendableWriter.of(sb)) {
            formatElement(element, config.isShowComments(), config.isShowAnnotations(), w);
        }
        return sb.toString();
    }

    public void format(TsonElement element, Writer sb) {
        formatElement(element, config.isShowComments(), config.isShowAnnotations(), sb);
    }

    @Override
    public String format(TsonDocument document) {
        StringBuilder sb = new StringBuilder();
        try (AppendableWriter w = AppendableWriter.of(sb)) {
            formatDocument(document, w);
        }
        return sb.toString();
    }

    public void formatDocument(TsonDocument document, Writer sb) {
        TsonDocumentHeader h = document.getHeader();
        TsonElement elem = document.getContent();
        formatAnnotation(h.builder().toAnnotation(), config.isShowComments(), config.isShowAnnotations(), sb);
        try {
            if (config.compact) {
                sb.append(' ');
            } else {
                sb.append('\n');
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        formatElement(elem, config.isShowComments(), config.isShowAnnotations(), sb);
    }

    public void formatAnnotation(TsonAnnotation a, boolean showComments, boolean showAnnotations, Writer sb) {
        final TsonElementList params = a.params();
        try {
            sb.append('@');
            if (a.name() != null) {
                sb.append(a.name());
            }
            if (params != null) {
                sb.append('(');
                int i = 0;
                for (TsonElement p : params) {
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

    public void formatElement(TsonElement element, boolean showComments, boolean showAnnotations, Writer sb) {
        try {
            if (showComments) {
                TsonComments c = element.comments();
                if (c != null && !c.isEmpty()) {
                    TsonComment[] leadingComments = c.leadingComments();
                    if (leadingComments.length > 0) {
                        boolean wasSLC = false;
                        for (TsonComment lc : leadingComments) {
                            switch (lc.type()) {
                                case MULTI_LINE: {
                                    sb.append(TsonUtils.formatMultiLineComments(lc.text(),!config.isIndentBraces()));
                                    sb.append(config.afterMultiLineComments);
                                    wasSLC = false;
                                    break;
                                }
                                case SINGLE_LINE: {
                                    if (!wasSLC) {
                                        sb.append("\n");
                                    }
                                    sb.append(TsonUtils.formatSingleLineComments(lc.text()));
                                    wasSLC = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            List<TsonAnnotation> ann = element.annotations();
            TsonAnnotation formatAnnotation = null;

            if (ann != null && !ann.isEmpty()) {
                for (TsonAnnotation a : ann) {
                    if ("format".equals(a.name())) {
                        formatAnnotation = a;
                        if (showAnnotations) {
                            if (!config.showFormatNumber) {
                                TsonAnnotationBuilder ab = a.builder();
                                List<TsonElement> params = ab.params();
                                for (int i = params.size() - 1; i >= 0; i--) {
                                    TsonElement o = params.get(i);
                                    if (o.type() == TsonElementType.NAME || o.type().isString() && FORMAT_NUMBER_TYPES.contains(o.stringValue())) {
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
                TsonComments c = element.comments();
                if (c != null && !c.isEmpty()) {
                    TsonComment[] trailingComments = c.trailingComments();
                    if (trailingComments.length > 0) {
                        boolean wasSLC = false;
                        for (TsonComment lc : trailingComments) {
                            switch (lc.type()) {
                                case MULTI_LINE: {
                                    sb.append(TsonUtils.formatMultiLineComments(lc.text(),false));
                                    sb.append(config.afterMultiLineComments);
                                    wasSLC = false;
                                    break;
                                }
                                case SINGLE_LINE: {
                                    if (!wasSLC) {
                                        sb.append("\n");
                                    }
                                    sb.append(TsonUtils.formatSingleLineComments(lc.text()));
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

    public void formatElementCore(TsonElement element, TsonAnnotation format, Writer writer) {
        try {
//        sb.ensureCapacity(sb.length() + 10);
            switch (element.type()) {
                case NULL:
                    writer.append("null");
                    return;
                case BYTE:
                case SHORT:
                case INTEGER:
                case LONG:
                case BIG_INTEGER:
                case FLOAT:
                case DOUBLE:
                case BIG_DECIMAL:
                case BIG_COMPLEX:
                case FLOAT_COMPLEX:
                case DOUBLE_COMPLEX: {
                    writer.append(new TsonNumberHelper((TsonNumber) element).toString());
                    return;
                }
                case BOOLEAN:
                    writer.append(String.valueOf(element.booleanValue()));
                    return;
                case LOCAL_DATETIME:
                    writer.append(String.valueOf(element.localDateTimeValue()));
                    return;
                case LOCAL_DATE:
                    writer.append(String.valueOf(element.localDateValue()));
                    return;
                case LOCAL_TIME:
                    writer.append(String.valueOf(element.localTimeValue()));
                    return;
                case REGEX: {
                    writer.append(TsonUtils.toRegex(element.regexValue().toString()));
                    return;
                }
                case CHAR: {
                    writer.append(TsonUtils.toSmpStr(element.charValue()));
                    return;
                }
                case DOUBLE_QUOTED_STRING:
                case SINGLE_QUOTED_STRING:
                case ANTI_QUOTED_STRING:
                case TRIPLE_DOUBLE_QUOTED_STRING:
                case TRIPLE_SINGLE_QUOTED_STRING:
                case TRIPLE_ANTI_QUOTED_STRING:
                case LINE_STRING:
                {
//                String s = element.getString();
//                StringBuilder sb = new StringBuilder(s.length() * 2);
//                TsonUtils.toQuotedStr(s, element.toStr().layout(), sb);
                    //TsonUtils.toDblStr(s, writer);
                    writer.append(element.toStr().literalString());
                    return;
                }
                case NAME: {
                    writer.append(element.stringValue());
                    return;
                }
                case ALIAS: {
                    writer.append("&").append(element.stringValue());
                    return;
                }
                case PAIR: {
                    TsonPair t = element.toPair();
                    format(t.key(), writer);
                    String vs = format(t.value());
                    writer.append(config.afterKey);
                    if (config.indent.length() > 0 && vs.indexOf("\n") > 0) {
                        writer.append(":\n").append(TsonUtils.indent(vs, config.indent));
                    } else {
                        writer.append(':').append(config.beforeValue).append(vs);
                    }
                    return;
                }
                case OP: {
                    TsonOp t = element.toOp();
                    String op = t.opName();
                    format(t.first(), writer);
                    String vs = format(t.second());
                    writer.append(config.afterKey);
                    if (config.indent.length() > 0 && vs.indexOf("\n") > 0) {
                        writer.append(op).append("\n").append(TsonUtils.indent(vs, config.indent));
                    } else {
                        writer.append(op).append(config.beforeValue).append(vs);
                    }
                    return;
                }
                case UPLET:
                case NAMED_UPLET: {
                    TsonUplet list = element.toUplet();
                    if (list.isNamed()) {
                        writer.append(list.name());
                    }
                    listToString(config.indentList, list.params(), '(', ')', writer, ListType.PARAMS);
                    return;
                }
                case ARRAY:
                case NAMED_PARAMETRIZED_ARRAY:
                case PARAMETRIZED_ARRAY:
                case NAMED_ARRAY: {
                    TsonArray list = element.toArray();
                    String n = TsonUtils.nullIfBlank(list.name());
                    boolean hasName = false;
                    if (n != null) {
                        writer.append(n);
                        hasName = n.length() > 0;
                    }
                    TsonElementList params = list.params();
                    if (params != null) {
                        if (!hasName || params.size() > 0) {
                            listToString(config.indentList, params, '(', ')', writer, ListType.PARAMS);
                        }
                    }
                    listToString(config.indentBrackets?IndentMode.ALWAYS : IndentMode.NEVER, list, '[', ']', writer, ListType.PARAMS);
                    return;
                }
                case MATRIX:
                case NAMED_MATRIX:
                case PARAMETRIZED_MATRIX:
                case NAMED_PARAMETRIZED_MATRIX: {
                    TsonMatrix list = element.toMatrix();
                    String n = TsonUtils.nullIfBlank(list.name());
                    boolean hasName = false;
                    if (n != null) {
                        writer.append(n);
                        hasName = n.length() > 0;
                    }
                    TsonElementList params = list.params();
                    if (params != null) {
                        if (!hasName || params.size() > 0) {
                            listToString(config.indentList, params, '(', ')', writer, ListType.PARAMS);
                        }
                    }
                    listToString(config.indentBrackets?IndentMode.ALWAYS : IndentMode.NEVER, (Iterable) list.rows(), '[', ']', writer, ListType.MATRIX);
                    return;
                }
                case OBJECT:
                case NAMED_PARAMETRIZED_OBJECT:
                case NAMED_OBJECT:
                case PARAMETRIZED_OBJECT: {
                    TsonObject list = element.toObject();
                    String n = TsonUtils.nullIfBlank(list.name());
                    boolean hasName = false;
                    if (n != null) {
                        writer.append(n);
                        hasName = n.length() > 0;
                    }
                    TsonElementList params = list.params();
                    if (params != null) {
                        if (!hasName || params.size() > 0) {
                            listToString(config.indentList, params, '(', ')', writer, ListType.PARAMS);
                        }
                    }
                    listToString(config.indentBraces?IndentMode.ALWAYS : IndentMode.NEVER, list, '{', '}', writer, ListType.OBJECT);
                    return;
                }
                case BINARY_STREAM: {
                    TsonBinaryStream list = element.toBinaryStream();
                    writer.write("^[");
                    char[] c = new char[1024];
                    try (Reader r = list.getBase64Value()) {
                        int x;
                        while ((x = r.read(c)) > 0) {
                            writer.write(c, 0, x);
                        }
                    }
                    writer.write("]");
                    return;
                }
                case CHAR_STREAM: {
                    TsonCharStream list = element.toCharStream();
                    switch (list.getStreamType()) {
                        case "": {
                            //code
                            writer.write("^{");
                            char[] c = new char[1024];
                            CharStreamCodeSupport cscs = CharStreamCodeSupports.of("");
                            try (Reader r = list.value()) {
                                int x;
                                while ((x = r.read(c)) > 0) {
                                    writer.write(c, 0, x);
                                    cscs.next(c, 0, x);
                                }
                            }
                            if (!cscs.isValid()) {
                                throw new IllegalArgumentException("Invalid Code CharStream : " + cscs.getErrorMessage());
                            }
                            writer.write("}");
                        }
                        default: {
                            String n = list.getStreamType();
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
                            try (Reader r = list.value()) {
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

    private void formatAppendUnit(String ll, TsonNumber element, Writer writer) throws IOException {
        writer.append(ll);
        String unit = TsonUtils.trimToNull(element.numberSuffix());
        if (unit != null) {
            if (unit.charAt(0) != '%' && unit.charAt(0) != '_') {
                writer.append('_');
            }
            writer.append(unit);
        }
    }


    private String decodeRadixPrefix(TsonNumberLayout l) {
        switch (l) {
            case BINARY:
                return "0b";
            case OCTAL:
                return "0";
            case HEXADECIMAL:
                return "0x";
            case DECIMAL:
                return "";
            default:
                return "";
        }
    }

    private void listToString(boolean indent, Iterable<TsonElement> it, char start, char end, Writer out, ListType listType) throws IOException {
        IndentMode indentMode = indent ? IndentMode.OPTIMIZE : IndentMode.NEVER;
        listToString(indentMode, it, start, end, out, listType);
    }

    private void listToString(IndentMode indent, Iterable<TsonElement> it, char start, char end, Writer out, ListType listType) throws IOException {
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

    private void listToStringIndentOptimized(Iterable<TsonElement> it, char start, char end, Writer out, ListType listType) throws IOException {
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

    private void listToStringIndented(Iterable<TsonElement> it, char start, char end, Writer out, ListType listType) throws IOException {
        List<TsonElement> a = new ArrayList<>();
        for (TsonElement e : it) {
            a.add(e);
        }
        if (a.isEmpty()) {
            out.append(start).append(end);
            return;
        }
        out.append(start).append('\n');
        StringBuilder sb2 = new StringBuilder();

        try (AppendableWriter w = AppendableWriter.of(sb2)) {
            int i = 0;

            switch (listType) {
                case OBJECT: {
                    for (TsonElement tsonElement : a) {
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
                    for (TsonElement tsonElement : a) {
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
                    for (TsonElement tsonElement : a) {
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
                    for (TsonElement tsonElement : a) {
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
        out.append(TsonUtils.indent(sb2.toString(), config.indent));
        out.append('\n').append(end);
    }

    private boolean acceptObjectElement(TsonElement tsonElement) {
        if (config.ignoreObjectNullFields) {
            if (tsonElement.isNull()) {
                return false;
            }
            if (tsonElement.type() == TsonElementType.PAIR && tsonElement.toPair().value().isNull()) {
                return false;
            }
        }
        if (config.ignoreObjectEmptyArrayFields) {
            if (tsonElement.type() == TsonElementType.ARRAY && tsonElement.toArray().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void listToStringNotIndented(Iterable<TsonElement> it, char start, char end, Writer out, ListType type) throws
            IOException {
        out.append(start);
        int i = 0;
        switch (type) {
            case OBJECT: {
                for (TsonElement tsonElement : it) {
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
                for (TsonElement tsonElement : it) {
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
                for (TsonElement tsonElement : it) {
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
                for (TsonElement tsonElement : it) {
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

    private boolean acceptParamElement(TsonElement tsonElement) {
        return true;
    }

    private boolean acceptArrayElement(TsonElement tsonElement) {
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
}
