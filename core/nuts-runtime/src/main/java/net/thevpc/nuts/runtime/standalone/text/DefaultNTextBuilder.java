package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.runtime.standalone.text.parser.AbstractNText;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextList;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextPlain;
import net.thevpc.nuts.runtime.standalone.text.parser.NTextListSimplifier;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NUnsupportedOperationException;

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
            if (node instanceof NTextBuilder) {
                children.add(((NTextBuilder) node).build());
            } else {
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
                newChildren.add(child);
            } else if (childStart >= end) {
                newChildren.add(child);
            } else {
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
        if (all.size() == 1) {
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
    public NTextBuilder insert(int at, NText... items) {
        if (items == null || items.length == 0) {
            return this;
        }

        if (at < 0 || at > length()) {
            throw new IndexOutOfBoundsException("Invalid position: " + at);
        }

        if (at == length()) {
            append(items);
            return this;
        }

        int currentPos = 0;
        for (int i = 0; i < children.size(); i++) {
            NText part = children.get(i);
            int partLength = part.length();

            if (at < currentPos + partLength) {
                int offsetInPart = at - currentPos;

                if (offsetInPart == 0) {
                    children.addAll(i, Arrays.asList(items));
                } else if (offsetInPart == partLength) {
                    children.addAll(i + 1, Arrays.asList(items));
                } else {
                    NText left = part.substring(0, offsetInPart);
                    NText right = part.substring(offsetInPart, partLength);

                    children.set(i, left);
                    children.addAll(i + 1, Arrays.asList(items));
                    children.add(i + 1 + items.length, right);
                }
                return this;
            }

            currentPos += partLength;
        }
        children.addAll(Arrays.asList(items));
        return this;
    }

    @Override
    public NTextBuilder replace(int from, int to, NText... newTexts) {
        if (from < 0 || to < from || to > length()) {
            throw new IndexOutOfBoundsException("Invalid range: " + from + " to " + to);
        }

        int pos = 0;
        int i = 0;
        List<NText> result = new ArrayList<>();

        for (NText t : children) {
            int len = t.length();

            if (pos + len <= from) {
                result.add(t);
            } else if (pos <= from && from < pos + len) {
                int splitStart = from - pos;
                if (splitStart > 0) {
                    result.add(t.substring(0, splitStart));
                }
            }

            if (pos < to && to <= pos + len) {
                int splitEnd = to - pos;
                if (newTexts != null && newTexts.length > 0) {
                    result.addAll(Arrays.asList(newTexts));
                }
                if (splitEnd < len) {
                    result.add(t.substring(splitEnd, t.length()));
                }
            }
            if (pos >= to) {
                result.add(t);
            }

            pos += len;
            i++;
        }

        this.children.clear();
        this.children.addAll(result);
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
            if (!child.isEmpty()) {
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
    public NStream<NPrimitiveText> toCharStream() {
        if (children.isEmpty()) {
            return NStream.ofEmpty();
        }
        NStream<NPrimitiveText> s = children.get(0).toCharStream();
        for (int i = 1; i < children.size(); i++) {
            s = s.concat(children.get(i).toCharStream());
        }
        return s;
    }

    @Override
    public boolean isWhitespace() {
        boolean hasContent = false;
        for (NText child : children) {
            if (child.isEmpty()) {
                continue;
            }
            if (!child.isWhitespace()) {
                return false;
            }
            hasContent = true;
        }
        return hasContent;
    }

    @Override
    public List<NText> split(String separators, boolean keepSeparators) {
        List<NText> result = new ArrayList<>();
        NTextBuilder current = NTextBuilder.of();

        for (NText child : getChildren()) {
            List<NText> parts = child.split(separators, keepSeparators); // recursively split child
            for (NText part : parts) {
                String s = part.filteredText();
                if (keepSeparators && s.length() == 1 && separators.indexOf(s.charAt(0)) >= 0) {
                    if (current.length() > 0) {
                        result.add(current.build());
                        current = NTextBuilder.of();
                    }
                    result.add(part); // separator as own element
                } else {
                    current.append(part); // normal text
                }
            }
        }

        if (current.length() > 0) {
            result.add(current.build());
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
