package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.parser.AbstractNText;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextList;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextPlain;
import net.thevpc.nuts.runtime.standalone.text.parser.NTextListSimplifier;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class DefaultNTextBuilder extends AbstractNText implements NTextBuilder {

    private final List<NText> children = new ArrayList<>();
    private final NTexts txt;
    private NTextStyleGenerator styleGenerator;
    private boolean flattened = true;

    public DefaultNTextBuilder() {
        super();
        txt = NTexts.of();
    }

    @Override
    public Iterator<NText> iterator() {
        return Collections.unmodifiableList(getChildren()).iterator();
    }

    @Override
    public NTextType type() {
        return NTextType.BUILDER;
    }

    @Override
    public NTextStyleGenerator getStyleGenerator() {
        if (styleGenerator == null) {
            styleGenerator = new DefaultNTextStyleGenerator();
        }
        return styleGenerator;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public DefaultNTextBuilder setStyleGenerator(NTextStyleGenerator styleGenerator) {
        this.styleGenerator = styleGenerator;
        return this;
    }

    @Override
    public NTextBuilder appendCommand(NTerminalCmd command) {
        append(txt.ofCommand(command));
        return this;
    }

    @Override
    public NTextBuilder appendCode(String lang, String text) {
        append(txt.ofCode(lang, text));
        return this;
    }

    //
//    @Override
//    public NutsTextBuilder append(String text, NutsTextStyle... styles) {
//        return append(text1.text().forPlain(text), styles);
//    }
    @Override
    public NTextBuilder appendHashStyle(Object text) {
        return appendHashStyle(text, text);
    }

    @Override
    public NTextBuilder appendRandomStyle(Object text) {
        if (text == null) {
            return this;
        }
        return append(text, getStyleGenerator().random());
    }

    @Override
    public NTextBuilder appendHashStyle(Object text, Object hash) {
        if (text == null) {
            return this;
        }
        if (hash == null) {
            hash = text;
        }
        return append(text, getStyleGenerator().hash(hash));
    }

    @Override
    public NTextBuilder append(Object text, NTextStyle style) {
        return append(text, NTextStyles.of(style));
    }

    @Override
    public NTextBuilder append(Object text, NTextStyles styles) {
        if (text != null) {
            if (styles.size() == 0) {
                append(NText.of(text));
            } else {
                append(txt.ofStyled(NText.of(text), styles));
            }
        }
        return this;
    }

    @Override
    public NTextBuilder append(Object node) {
        if (node != null) {
            return append(NText.of(node));
        }
        return this;
    }

    @Override
    public NTextBuilder append(NText node) {
        if (node != null) {
            if(node instanceof NTextBuilder) {
                children.add(((NTextBuilder) node).build());
            }else{
                children.add(node);
            }
            flattened = false;
        }
        return this;
    }

    @Override
    public NText substring(int start, int end) {
        if (start < 0 || end < start || end > length()) {
            throw new IndexOutOfBoundsException("Invalid start or end");
        }
        DefaultNTextBuilder result = new DefaultNTextBuilder();
        int pos = 0;
        for (NText child : getChildren()) {
            int childLen = child.filteredText().length();
            int childStart = pos;
            int childEnd = pos + childLen;
            if (childEnd <= start) {
                // before range
            } else if (childStart >= end) {
                // after range
                break;
            } else {
                int subStart = Math.max(start - childStart, 0);
                int subEnd = Math.min(end - childStart, childLen);
                result.append(child.substring(subStart, subEnd)); // delegate to child
            }
            pos += childLen;
        }
        return result.build();
    }

    @Override
    public NTextBuilder delete(int start, int end) {
        if (start < 0 || end < start || end > filteredText().length()) {
            throw new IndexOutOfBoundsException("Invalid start or end");
        }
        int pos = 0;
        List<NText> newChildren = new ArrayList<>();
        for (NText child : children) {
            int childLen = child.filteredText().length();
            int childStart = pos;
            int childEnd = pos + childLen;

            if (childEnd <= start) {
                // Entirely before range → keep as is
                newChildren.add(child);
            } else if (childStart >= end) {
                // Entirely after range → keep as is
                newChildren.add(child);
            } else {
                // Overlap: compute remaining parts
                int leftEnd = Math.max(start - childStart, 0);
                int rightStart = Math.min(end - childStart, childLen);

                if (leftEnd > 0) {
                    newChildren.add(child.substring(0, leftEnd));
                }
                if (rightStart < childLen) {
                    newChildren.add(child.substring(rightStart, childLen));
                }
            }
            pos += childLen;
        }
        children.clear();
        children.addAll(newChildren);
        return this;
    }

    @Override
    public NTextBuilder appendJoined(Object separator, Collection<?> others) {
        if (others != null) {
            boolean first = true;
            for (Object other : others) {
                if (other != null) {
                    if (first) {
                        first = false;
                    } else {
                        if (separator != null) {
                            append(separator);
                        }
                    }
                    append(other);
                }
            }
        }
        return this;
    }
    //    @Override
//    public NutsTextBuilder append(NutsString str) {
//        if (str != null) {
//            NutsText n = ws.text().parser().parse(new StringReader(str.toString()));
//            if (n != null) {
//                append(n);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public NutsTextBuilder append(NutsFormattable str) {
//        if (str != null) {
//            append(ws.text().toText(str));
//        }
//        return this;
//    }


    @Override
    public NTextBuilder appendAll(NText[] others) {
        if (others != null) {
            for (NText node : others) {
                if (node != null) {
                    append(node);
                }
            }
        }
        return this;
    }

    @Override
    public NTextBuilder appendAll(Collection<?> others) {
        if (others != null) {
            for (Object node : others) {
                if (node != null) {
                    append(node);
                }
            }
        }
        return this;
    }

    @Override
    public NText build() {
        List<NText> all = new NTextListSimplifier().addAll(children).toList();
        if (all.isEmpty()) {
            return DefaultNTextPlain.EMPTY;
        }
        if (size() == 1) {
            return all.get(0);
        }
        if (!all.equals(children)) {
            return new DefaultNTextList(all.toArray(new NText[0]));
        }
        return this;
    }

    @Override
    public List<NText> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public NText subChildren(int from, int to) {
        if (from < 0) {
            from = 0;
        }
        if (to >= size()) {
            to = size() - 1;
        }
        if (to <= from) {
            return NText.ofBlank();
        }
        return NTextBuilder.of().appendAll(children.subList(from, to)).build();
    }

//    public NText substring(int from, int to) {
//        if (to <= from) {
//            return NText.ofPlain("");
//        }
//        int firstIndex = ensureCut(from);
//        if (firstIndex < 0) {
//            return NText.ofPlain("");
//        }
//        int secondIndex = ensureCut(to);
//        if (secondIndex < 0) {
//            //the cut is till the end
//            return NTextBuilder.of().appendAll(children.subList(firstIndex, children.size())).build();
//        }
//        return NTextBuilder.of().appendAll(children.subList(firstIndex, secondIndex)).build();
//    }

    @Override
    public NTextBuilder insert(int at, NText... newTexts) {
        return replaceChildren(at, at + 1, newTexts);
    }

    @Override
    public NTextBuilder replace(int from, int to, NText... newTexts) {
        if (to <= from) {
            return this;
        }
        int firstIndex = ensureCut(from);
        if (firstIndex < 0) {
            return this;
        }
        int secondIndex = ensureCut(to);
        if (secondIndex < 0) {
            //the cut is till the end
            replaceChildren(firstIndex, children.size(), newTexts);
        }
        replaceChildren(firstIndex, secondIndex, newTexts);
        return this;
    }

    @Override
    public NTextBuilder replaceChildren(int from, int to, NText... newTexts) {
        if (newTexts == null) {
            newTexts = new NText[0];
        } else {
            newTexts = Arrays.stream(newTexts).filter(x -> x != null && !x.isEmpty()).toArray(NText[]::new);
        }
        if (from < to) {
            children.subList(from, to).clear();
            if (newTexts.length > 0) {
                children.addAll(from, Arrays.asList(newTexts));
            }
        }
        return this;
    }

    @Override
    public int size() {
        return children.size();
    }

    public NText get(int index) {
        return children.get(index);
    }


    @Override
    public NTextBuilder flatten() {
        if (!flattened) {
            NText build = build();
            NText a = txt.transform(build, new NTextTransformConfig().setFlatten(true));
            this.children.clear();
            fill(a);
            flattened = true;
        }
        return this;
    }

    private void fill(NText z) {
        if (z != null) {
            if (z instanceof NTextList) {
                for (NText c : ((NTextList) z).getChildren()) {
                    fill(c);
                }
            } else if (z instanceof NTextPlain) {
                this.children.add(z);
            } else if (z instanceof NTextStyled) {
                if (((NTextStyled) z).getChild() instanceof NTextList) {
                    NText z2 = txt.transform(z, new NTextTransformConfig().setFlatten(true));
                }
                this.children.add(z);
            } else {
                throw new NUnsupportedOperationException(NMsg.ofPlain("expected plain or styled nodes"));
            }
        }
    }

    @Override
    public NTextBuilder removeAt(int index) {
        children.remove(index);
        return this;
    }

    @Override
    public NStream<NTextBuilder> lines() {
        DefaultNTextBuilder z = (DefaultNTextBuilder) copy().flatten();
        return NStream.ofIterator(
                new Iterator<NTextBuilder>() {
                    NTextBuilder n;

                    @Override
                    public boolean hasNext() {
                        n = z.readLine();
                        return n != null;
                    }

                    @Override
                    public NTextBuilder next() {
                        return n;
                    }
                }
        );
    }

    @Override
    public NTextBuilder newLine() {
        return append(NText.ofNewLine());
    }

    @Override
    public NTextBuilder readLine() {
        if (this.size() == 0) {
            return null;
        }
        List<NText> r = new ArrayList<>();
        while (this.size() > 0) {
            NText t = this.get(0);
            this.removeAt(0);
            if (isNewLine(t)) {
                break;
            }
            r.add(t);
        }
        return NTextBuilder.of().appendAll(r);
    }

    @Override
    public NTextBuilder clear() {
        children.clear();
        return this;
    }

    private boolean isNewLine(NText t) {
        if (t.type() == NTextType.PLAIN) {
            String txt = ((NTextPlain) t).getValue();
            return (txt.equals("\n") || txt.equals("\r\n"));
        }
        return false;
    }

    public NTextBuilder copy() {
        DefaultNTextBuilder c = new DefaultNTextBuilder();
        c.appendAll(children);
        c.flattened = flattened;
        return c;
    }

    public int ensureCut(int at) {
//        List<NutsText> newValues=new ArrayList<>();
        if (at <= 0) {
            return 0;
        }
        NTexts text = NTexts.of();
        int charPos = 0;
        int index = 0;
        while (index < children.size()) {
            NText c = children.get(index);
            int start = charPos;
            int len = c.length();
            int end = start + len;
            if (at < start) {
                //continue
            } else if (at == start) {
                return index;
            } else if (at == end) {
                if (index + 1 < children.size()) {
                    return index + 1;
                }
                return -1;
            } else if (at > start && at < end) {
                List<NText> rv = c.builder().flatten().getChildren();
                List<NText> rv2 = new ArrayList<>(rv.size() + 1);
                int toReturn = -1;
                for (int i = 0; i < rv.size(); i++) {
                    NText child = rv.get(i);
                    start = charPos;
                    len = child.length();
                    end = start + len;
                    if (at < start) {
                        rv2.add(child);
                    } else if (at == start) {
                        rv2.add(child);
                        toReturn = i + index;
                    } else if (at >= end) {
                        rv2.add(child);
                    } else {
                        if (child.type() == NTextType.PLAIN) {
                            NTextPlain p = (NTextPlain) child;
                            String tp = p.getValue();
                            String a = tp.substring(0, at - start);
                            String b = tp.substring(at - start);
                            rv2.add(text.ofPlain(a));
                            rv2.add(text.ofPlain(b));
                            toReturn = index + i + 1;
                        } else if (child.type() == NTextType.STYLED) {
                            NTextStyled p = (NTextStyled) child;
                            String tp = ((NTextPlain) p.getChild()).getValue();
                            String a = tp.substring(0, at - start);
                            String b = tp.substring(at - start);
                            rv2.add(text.ofStyled(a, p.getStyles()));
                            rv2.add(text.ofStyled(b, p.getStyles()));
                            toReturn = index + i + 1;
                        }
                    }
                    charPos = end;
                }
                replaceChildren(index, index + 1, rv2.toArray(new NText[0]));
                return toReturn;
            }
            charPos = end;
            index++;
        }
        return -1;
    }

    @Override
    public NText immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out);
        ss.writeNode(build());
        return new DefaultNTextList(children.toArray(new NText[0])).simplify();
    }

    @Override
    public NText simplify() {
        List<NText> all = new NTextListSimplifier().addAll(children).toList();
        children.clear();
        children.addAll(all);
        return this;
    }

    @Override
    public String filteredText() {
        StringBuilder sb = new StringBuilder();
        for (NText child : children) {
            sb.append(child.filteredText());
        }
        return sb.toString();
    }

    @Override
    public int length() {
        int count = 0;
        for (NText child : children) {
            count += child.length();
        }
        return count;
//        return immutable().textLength();
    }

    @Override
    public boolean isEmpty() {
        for (NText child : children) {
            if(!child.isEmpty()){
                return false;
            }
        }
        return true;
    }

    @Override
    public NTextBuilder builder() {
        return copy();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(filteredText());
    }

    @Override
    public List<NPrimitiveText> toCharList() {
        List<NPrimitiveText> all = new ArrayList<>();
        for (NText child : children) {
            all.addAll(child.toCharList());
        }
        return all;
    }

    @Override
    public List<NText> split(String separators, boolean keepSeparators) {
        List<NText> result = new ArrayList<>();
        for (NText child : flatten().getChildren()) {
            result.addAll(child.split(separators, keepSeparators));
        }
        return result;
    }

    @Override
    public NTextBuilder trimLeft() {
        for (int i = 0; i < children.size(); i++) {
            NText c = children.get(i).trimLeft();
            int l = c.length();
            if (l > 0) {
                children.set(i, c);
                break;
            } else {
                children.remove(i);
                i--;
            }
        }
        return this;
    }

    @Override
    public NTextBuilder trimRight() {
        for (int i = children.size() - 1; i >= 0; i--) {
            NText c = children.get(i).trimRight();  // delegate to child
            int l = c.length();
            if (l > 0) {
                children.set(i, c);
                break;
            } else {
                children.remove(i);
            }
        }
        return this;
    }

    @Override
    public NTextBuilder trim() {
        trimLeft();
        trimRight();
        return this;
    }
}
