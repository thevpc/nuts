package net.thevpc.nuts.runtime.standalone.elem.writer;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NCharReader;
import net.thevpc.nuts.io.NStringWriter;
import net.thevpc.nuts.runtime.standalone.elem.builder.NBoundAffixList;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class DefaultTsonWriter {
    protected NStringWriter w;
    private NAffixType[] acceptablePost = new NAffixType[]{NAffixType.NEWLINE, NAffixType.SPACE, NAffixType.BLOC_COMMENT, NAffixType.LINE_COMMENT};
    private NAffixType[] acceptablePre = NAffixType.values();
    private NAffixType[] acceptableWrapSep = {NAffixType.SPACE, NAffixType.SEPARATOR, NAffixType.NEWLINE, NAffixType.BLOC_COMMENT, NAffixType.LINE_COMMENT};

    public DefaultTsonWriter(NStringWriter writer) {
        this.w = writer;
    }

    public void writeAffix(NAffix affix) {
        switch (affix.type()) {
            case LINE_COMMENT:
            case BLOC_COMMENT: {
                writeComment((NElementComment) affix);
                break;
            }
            case SPACE: {
                write(affix.toString());
                break;
            }
            case ANNOTATION: {
                NElementAnnotation a = (NElementAnnotation) affix;
                writeAnnotation(a);
                break;
            }
            case SEPARATOR: {
                write(affix.toString());
                break;
            }
            case NEWLINE: {
                write(((NElementNewLine) affix).value().value());
                break;
            }
        }
    }

    private void writeAnnotation(NElementAnnotation a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        write("@");
        if (!NBlankable.isBlank(a.name())) {
            writeBoundedString(a.name(), 1, a.affixes());
        }
        if (a.params().isPresent()) {
            writeBoundedString("(", 2, a.affixes());
            for (NElement e : a.params().get()) {
                write(e);
            }
            writeBoundedString(")", 3, a.affixes());
        }
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    public void write(NElement element) {
        switch (element.type()) {
            case NULL:
            case BOOLEAN:

            case INSTANT:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME:

            case UBYTE:
            case ULONG:
            case UINT:
            case USHORT:
            case INT:
            case BYTE:
            case BIG_INT:
            case BIG_DECIMAL:
            case BIG_COMPLEX:
            case DOUBLE:
            case FLOAT:
            case FLOAT_COMPLEX:
            case LONG:
            case DOUBLE_COMPLEX:
            case SHORT: {
                writePrimitive((NPrimitiveElement) element);
                break;
            }
            case CHAR_STREAM: {
                NCharStreamElement a = (NCharStreamElement) element;
                writeCharStream(a);
                break;
            }
            case BINARY_STREAM: {
                NBinaryStreamElement a = (NBinaryStreamElement) element;
                writeBinaryStream(a);
                break;
            }
            case EMPTY: {
                NEmptyElement a = (NEmptyElement) element;
                writeEmpty(a);
                break;
            }
            case PAIR: {
                NPairElement a = (NPairElement) element;
                writePair(a);
                break;
            }
            case OPERATOR_SYMBOL: {
                NOperatorSymbolElement a = (NOperatorSymbolElement) element;
                writeOperatorSymbolElement(a);
                break;
            }
            case CHAR: {
                writeQuotedString("'", (NStringElement) element);
                break;
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                writeQuotedString("'''", (NStringElement) element);
                break;
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                writeQuotedString("\"\"\"", (NStringElement) element);
                break;
            }
            case TRIPLE_BACKTICK_STRING: {
                writeQuotedString("```", (NStringElement) element);
                break;
            }
            case SINGLE_QUOTED_STRING: {
                writeQuotedString("'", (NStringElement) element);
                break;
            }
            case DOUBLE_QUOTED_STRING: {
                writeQuotedString("\"", (NStringElement) element);
                break;
            }
            case BACKTICK_STRING: {
                writeQuotedString("`", (NStringElement) element);
                break;
            }
            case LINE_STRING:
            case BLOCK_STRING: {
                writeLineOrBlockString((NStringElement) element);
                break;
            }
            case UNARY_OPERATOR:
            case BINARY_OPERATOR:
            case TERNARY_OPERATOR:
            case NARY_OPERATOR: {
                NOperatorElement a = (NOperatorElement) element;
                writeAnyOperatorElement(a);
                break;
            }
            case CUSTOM: {
                NCustomElement a = (NCustomElement) element;
                writeCustomElement(a);
                break;
            }
            case NAME: {
                writeName((NStringElement) element);
                break;
            }
            case UPLET:
            case NAMED_UPLET: {
                writeUplet((NUpletElement) element);
                break;
            }
            case FRAGMENT: {
                writeFragment((NFragmentElement) element);
                break;
            }
            case OBJECT:
            case NAMED_OBJECT:
            case PARAM_OBJECT:
            case FULL_OBJECT: {
                writeObject((NObjectElement) element);
                break;
            }
            case ARRAY:
            case NAMED_ARRAY:
            case PARAM_ARRAY:
            case FULL_ARRAY: {
                writeArray((NArrayElement) element);
                break;
            }
            case FLAT_EXPR: {
                writeFlatExpr((NFlatExprElement) element);
                break;
            }
            case UNORDERED_LIST:
            case ORDERED_LIST: {
                writeList((NListElement) element);
                break;
            }
        }
    }

    private void writeUplet(NUpletElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        String name = a.name().orNull();
        if (name != null) {
            writeBoundedString(name, 1, a.affixes());
        }
        writeBoundedString("(", 2, a.affixes());
        List<NElement> params = a.params();
        for (int i = 0; i < params.size(); i++) {
            NElement e = params.get(i);
            if (i > 0) {
                write(a.affixes(), NAffixAnchor.SEP_1, acceptableWrapSep);
            }
            write(e);
        }
        writeBoundedString(")", 3, a.affixes());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeFragment(NFragmentElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        List<NElement> params = a.children();
        for (int i = 0; i < params.size(); i++) {
            NElement e = params.get(i);
            if (i > 0) {
                write(a.affixes(), NAffixAnchor.SEP_1, acceptableWrapSep);
            }
            write(e);
        }
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeFlatExpr(NFlatExprElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        List<NElement> children = a.children();
        for (int i = 0; i < children.size(); i++) {
            NElement e = children.get(i);
            if (i > 0) {
                write(a.affixes(), NAffixAnchor.SEP_1, acceptableWrapSep);
            }
            write(e);
        }
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeObject(NObjectElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        String name = a.name().orNull();
        if (name != null) {
            writeBoundedString(name, 1, a.affixes());
        }
        if (a.params().isPresent()) {
            writeBoundedString("(", 2, a.affixes());
            List<NElement> get = a.params().get();
            for (int i = 0; i < get.size(); i++) {
                NElement e = get.get(i);
                if (i > 0) {
                    write(a.affixes(), NAffixAnchor.SEP_1, acceptableWrapSep);
                }
                write(e);
            }
            writeBoundedString(")", 3, a.affixes());
        }
        writeBoundedString("{", 4, a.affixes());
        List<NElement> children = a.children();
        for (int i = 0; i < children.size(); i++) {
            NElement e = children.get(i);
            if (i > 0) {
                write(a.affixes(), NAffixAnchor.SEP_2, acceptableWrapSep);
            }
            write(e);
        }
        writeBoundedString("}", 5, a.affixes());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeArray(NArrayElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        String name = a.name().orNull();
        if (name != null) {
            writeBoundedString(name, 1, a.affixes());
        }
        if (a.params().isPresent()) {
            writeBoundedString("(", 2, a.affixes());
            List<NElement> get = a.params().get();
            for (int i = 0; i < get.size(); i++) {
                NElement e = get.get(i);
                if (i > 0) {
                    write(a.affixes(), NAffixAnchor.SEP_1, acceptableWrapSep);
                }
                write(e);
            }
            writeBoundedString(")", 3, a.affixes());
        }
        writeBoundedString("[", 4, a.affixes());
        List<NElement> children = a.children();
        for (int i = 0; i < children.size(); i++) {
            NElement e = children.get(i);
            if (i > 0) {
                write(a.affixes(), NAffixAnchor.SEP_2, acceptableWrapSep);
            }
            write(e);
        }
        writeBoundedString("]", 5, a.affixes());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeList(NListElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        List<NListItemElement> items = a.items();
        for (int i = 0; i < items.size(); i++) {
            NListItemElement item = items.get(i);
            if (i > 0) {
                write(a.affixes(), NAffixAnchor.SEP_1, acceptableWrapSep);
            }
            writeListItem(item);
        }
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeListItem(NListItemElement item) {
        write(item.affixes(), NAffixAnchor.START, acceptableWrapSep);
        NElement v = item.value().orNull();
        String bullet = item.marker();
        writeBoundedString(bullet, 1, item.affixes());
        if (v != null) {
            writeBoundedElement(v, 2, item.affixes());
        }
        NListElement sl = item.subList().orNull();
        if (sl != null) {
            writeBoundedElement(sl, 3, item.affixes());
        }
        write(item.affixes(), NAffixAnchor.END, acceptableWrapSep);
    }


    private void writeCustomElement(NCustomElement a) {
        NStringElement s = (NStringElement) NPrimitiveElementBuilder.of()
                .setString(a.value() == null ? null : a.value().toString())
                .copyFrom(a)
                .build();
        writeQuotedString("\"", s);
    }

    private void writeAnyOperatorElement(NOperatorElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        List<NOperatorSymbol> operatorSymbols = a.operatorSymbols();
        switch (a.position()) {
            case PREFIX: {
                for (int i = 0; i < operatorSymbols.size(); i++) {
                    writeBoundedString(a.operatorSymbol(i).get().lexeme(), 1, a.affixes());
                }
                for (NElement operand : a.operands()) {
                    writeBoundedElement(operand, 2, a.affixes());
                }
                break;
            }
            case INFIX: {
                List<NElement> operands = a.operands();
                for (int i = 0; i < operands.size(); i++) {
                    NElement operand = operands.get(i);
                    if (i > 0) {
                        if (i < operatorSymbols.size()) {
                            writeBoundedString(operatorSymbols.get(i).lexeme(), 1, a.affixes());
                        } else if (!operatorSymbols.isEmpty()) {
                            writeBoundedString(operatorSymbols.get(operatorSymbols.size() - 1).lexeme(), 1, a.affixes());
                        } else {
                            writeBoundedString("?", 1, a.affixes());
                        }
                    }
                    writeBoundedElement(operand, 2, a.affixes());
                }
                break;
            }
            case POSTFIX: {
                for (NElement operand : a.operands()) {
                    writeBoundedElement(operand, 2, a.affixes());
                }
                for (NOperatorSymbol s : operatorSymbols) {
                    writeBoundedString(s.lexeme(), 1, a.affixes());
                }
                break;
            }
        }
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeCharStream(NCharStreamElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        writeBoundedString("^" + a.blocIdentifier() + "{", 1, a.affixes());
        try (Reader reader = a.value().getReader()) {
            char[] b = new char[1024];
            int c;
            while ((c = reader.read(b)) >= 0) {
                write(b, 0, c);
            }
        } catch (IOException e) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to execute toString on CharStream"));
        }
        writeBoundedString("^" + a.blocIdentifier() + "}", 2, a.affixes());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeBinaryStream(NBinaryStreamElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        writeBoundedString("^" + a.blocIdentifier() + "[", 1, a.affixes());
        try (InputStream reader = a.value().getInputStream()) {
            try (OutputStream out = asBinaryOutputStream(a.blocIdentifier())) {
                byte[] b = new byte[1024];
                int c;
                while ((c = reader.read(b)) >= 0) {
                    out.write(b, 0, c);
                }
            }
        } catch (IOException e) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to execute toString on CharStream"));
        }
        writeBoundedString("]", 2, a.affixes());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeLineOrBlockString(NStringElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        List<NElementLine> lines = a.lines();
        for (int i = 0; i < lines.size(); i++) {
            NElementLine line = lines.get(i);
            write(line.prefix());
            writeBoundedString(line.startMarker(), 1, a.affixes());
            write(line.startPadding());
            write(line.content());
            if (line.newline() != null) {
                write(line.newline().value());
            }
        }
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeComment(NElementComment a) {
        write(a.raw());
    }

    private void writeName(NStringElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        write(a.stringValue());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeQuotedString(String quotes, NStringElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        NCharReader sb = new NCharReader(new StringReader(a.stringValue()));
        int qlength = quotes.length();
        write(quotes);
        if (qlength == 1) {
            char q = quotes.charAt(0);
            while (true) {
                int s = sb.peek();
                if (s < 0) {
                    break;
                }
                if (s == q) {
                    write(q);
                    write(sb.read(qlength));
                } else {
                    int c = sb.read();
                    if (c < 0) {
                        break;
                    }
                    write((char) c);
                }
            }
        } else {
            char q = quotes.charAt(0);
            while (true) {
                String s = sb.peek(qlength);
                if (s == null || s.length() == 0) {
                    break;
                }
                if (s.equals(quotes)) {
                    write(q);
                    write(sb.read(qlength));
                } else {
                    int c = sb.read();
                    if (c < 0) {
                        break;
                    }
                    write((char) c);
                }
            }
        }
        write(quotes);
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeOperatorSymbolElement(NOperatorSymbolElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        write(a.symbol().lexeme());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writeEmpty(NElement element) {
        write(element.affixes(), NAffixAnchor.START, acceptablePre);
        write(element.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writePrimitive(NPrimitiveElement element) {
        write(element.affixes(), NAffixAnchor.START, acceptablePre);
        switch (element.type()) {
            case NULL: {
                write("null");
                break;
            }
            case UINT:
            case UBYTE:
            case USHORT:
            case ULONG:
            case BYTE:
            case LONG:
            case BIG_DECIMAL:
            case BIG_INT:
            case SHORT:
            case INT:
            case FLOAT:
            case DOUBLE: {
                NNumberElement r = element.asNumber().get();
                String img = r.image();
                if (!NBlankable.isBlank(img)) {
                    write(img);
                    return;
                } else {
                    NNumberLayout layout = r.numberLayout();
                    String suffix = r.numberSuffix();
                    switch (layout) {
                        case DECIMAL: {
                            write(String.valueOf(element.asNumberValue().get()));
                            break;
                        }
                        case HEXADECIMAL: {
                            write(element.asBigIntValue().get().toString(16));
                            break;
                        }
                        case OCTAL: {
                            write(element.asBigIntValue().get().toString(8));
                            break;
                        }
                        case BINARY: {
                            write(element.asBigIntValue().get().toString(2));
                            break;
                        }
                    }
                    if (!NBlankable.isBlank(suffix)) {
                        write(suffix);
                    }
                }
                break;
            }
            case INSTANT:
            case LOCAL_TIME:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case BOOLEAN:
            default: {
                write(String.valueOf(element.value()));
                break;
            }
        }
        write(element.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private void writePair(NPairElement a) {
        write(a.affixes(), NAffixAnchor.START, acceptablePre);
        write(a.key().toString());
        write(a.affixes(), NAffixAnchor.PRE_1, acceptableWrapSep);
        write(":");
        write(a.affixes(), NAffixAnchor.POST_1, acceptableWrapSep);
        write(a.value().toString());
        write(a.affixes(), NAffixAnchor.END, acceptablePost);
    }

    private OutputStream asBinaryOutputStream(String type) {
        return Base64.getEncoder().wrap(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                DefaultTsonWriter.this.write(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                DefaultTsonWriter.this.write(new String(b, off, len));
            }
        });
    }


    private void write(NBoundAffix a) {
        writeAffix(a.affix());
    }

    private NAffixAnchor pre(int index) {
        switch (index) {
            case 1:
                return NAffixAnchor.PRE_1;
            case 2:
                return NAffixAnchor.PRE_2;
            case 3:
                return NAffixAnchor.PRE_3;
            case 4:
                return NAffixAnchor.PRE_4;
            case 5:
                return NAffixAnchor.PRE_5;
        }
        throw new IndexOutOfBoundsException("index=" + index);
    }

    private NAffixAnchor pos(int index) {
        switch (index) {
            case 1:
                return NAffixAnchor.POST_1;
            case 2:
                return NAffixAnchor.POST_2;
            case 3:
                return NAffixAnchor.POST_3;
            case 4:
                return NAffixAnchor.POST_4;
            case 5:
                return NAffixAnchor.POST_5;
        }
        throw new IndexOutOfBoundsException("index=" + index);
    }

    private void writeBoundedString(String str, int index, List<NBoundAffix> list) {
        write(list, pre(index), acceptableWrapSep);
        write(str);
        write(list, pos(index), acceptableWrapSep);
    }


    private void writeBoundedElement(NElement str, int index, List<NBoundAffix> list) {
        write(list, pre(index), acceptableWrapSep);
        write(str);
        write(list, pos(index), acceptableWrapSep);
    }

    private void write(List<NBoundAffix> list, NAffixAnchor anchor, NAffixType[] acceptableTypes) {
        for (NBoundAffix p : NBoundAffixList.filter(list, anchor, acceptableTypes)) {
            write(p);
        }
    }

    private void write(char[] buffer, int offset, int len) {
        w.write(buffer, offset, len);
    }

    private void write(String text) {
        w.write(text);
    }

    private void write(char c) {
        w.write(c);
    }

    public static String formatTsonCompact(NElement e) {
        NElement ee = e.transform(new NElementTransform() {
            @Override
            public List<NElement> postTransform(NElementTransformContext context) {
                NElement e = context.element();
                e = e.builder()
                        .removeAffixIf(q -> {
                            NAffixType type = q.affix().type();
                            switch (type) {
                                case SPACE:
                                case NEWLINE:
                                    return true;
                            }
                            return false;
                        })
                        .build();
                return Arrays.asList(e);
            }
        }).get(0);
        return formatTson(ee);
    }


    public static String formatTson(NElement e) {
        NStringBuilder sb = new NStringBuilder();
        DefaultTsonWriter w = new DefaultTsonWriter(sb.asStringWriter());
        w.write(e);
        return sb.toString();
    }

    public static String formatTson(NAffix e) {
        NStringBuilder sb = new NStringBuilder();
        DefaultTsonWriter w = new DefaultTsonWriter(sb.asStringWriter());
        w.writeAffix(e);
        return sb.toString();
    }


}
