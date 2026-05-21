package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.runtime.standalone.text.NTextNodeWriterStringer;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NBlankable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractNText implements NText {

    public AbstractNText() {
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out);
        ss.writeNode(this);
        return out.toString();
    }

    @Override
    public String filteredText() {
        return immutable().filteredText();
    }

    @Override
    public int length() {
        return immutable().length();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(filteredText());
    }

    @Override
    public NTextBuilder builder() {
        return NTextBuilder.of().append(this);
    }

    @Override
    public boolean isPrimitive() {
        return this instanceof NPrimitiveText;
    }

    @Override
    public boolean isNormalized() {
        return this instanceof NNormalizedText;
    }

    @Override
    public List<NText> split(char c) {
        return split(String.valueOf(c), false);
    }

    @Override
    public List<NText> split(char c, boolean returnSeparator) {
        return split(String.valueOf(c), returnSeparator);
    }

    @Override
    public List<NText> split(String separator) {
        return split(separator, false);
    }

    @Override
    public NPrimitiveText[] toCharArray() {
        return toCharList().toArray(new NPrimitiveText[0]);
    }

    @Override
    public List<NPrimitiveText> toCharList() {
        return toCharStream().collect(Collectors.toList());
    }

    @Override
    public NText repeat(int times) {
        if (times <= 0) {
            return NText.ofBlank();
        }
        if (times == 1) {
            return this;
        }
        NTextBuilder b = NTextBuilder.of();
        for (int i = 0; i < times; i++) {
            b.append(this);
        }
        return b.build();
    }

    @Override
    public NText repeatln(int times) {
        if (times <= 0) {
            return NText.ofBlank();
        }
        if (times == 1) {
            return this;
        }
        NTextBuilder b = NTextBuilder.of();
        for (int i = 0; i < times; i++) {
            if (i > 0) {
                b.append(NText.ofNewLine());
            }
            b.append(this);
        }
        return b.build();
    }

    @Override
    public NText concat(NText other) {
        if (other == null) {
            return this;
        }
        return NText.ofList(this, other).simplify();
    }

    @Override
    public NText concat(NText... others) {
        List<NText> aa = new ArrayList<>();
        aa.add(this);
        if (others != null) {
            aa.addAll(Arrays.asList(others));
        }
        return NText.ofList(aa).simplify();
    }

    public NNormalizedText normalize() {
        return NTexts.of().normalize(this);
    }

    @Override
    public NNormalizedText normalize(NTextTransformConfig config) {
        return NTexts.of().normalize(this, config);
    }

    @Override
    public NNormalizedText normalize(NTextTransformer transformer, NTextTransformConfig config) {
        return NTexts.of().normalize(this, transformer, config);
    }

    @Override
    public boolean isString(String anyString) {
        return anyString != null && anyString.equals(filteredText());
    }

    @Override
    public boolean isNewLine() {
        Iterator<NPrimitiveText> it = toCharStream().iterator();
        if (!it.hasNext()) {
            return false;
        }
        String f = it.next().filteredText();
        if (f.equals("\n")) {
            return !it.hasNext();
        }
        if (!f.equals("\r")) {
            return false;
        }
        if (!it.hasNext()) {
            return true;
        }
        if (it.next().filteredText().equals("\n")) {
            return !it.hasNext();
        }
        return false;
    }

    public List<NPrimitiveText> toPrimitiveList() {
        NNormalizedText normalizeOne = normalize();
        List<NPrimitiveText> primitiveList = new ArrayList<>();
        if (normalizeOne instanceof NPrimitiveText || normalizeOne instanceof NTextPlain) {
            primitiveList.add((NPrimitiveText) normalizeOne);
        } else {
            primitiveList.addAll(((NTextList) normalizeOne).children().stream().map(x -> (NPrimitiveText) x).collect(Collectors.toList()));
        }
        return primitiveList;
    }

    public List<NText> split(Pattern regex, boolean returnSeparator) {
        List<NPrimitiveText> normalizedList = toPrimitiveList();
        // step 1: build offset map
        List<int[]> offsets = new ArrayList<>();
        List<NTextStyles> styles = new ArrayList<>();
        StringBuilder full = new StringBuilder();
        for (NText child : normalizedList) {
            int start = full.length();
            String t = child.filteredText();
            full.append(t);
            offsets.add(new int[]{start, full.length()});
            if (child instanceof NTextStyled) {
                styles.add(((NTextStyled) child).styles());
            } else {
                styles.add(null);
            }
        }

        // step 2: split the full string
        String s = full.toString();
        Matcher m = regex.matcher(s);

        List<NText> result = new ArrayList<>();
        int last = 0;
        while (m.find()) {
            if (m.start() > last) {
                result.add(slice(s, styles, offsets, last, m.start()));
            } else {
                result.add(NText.ofPlain(""));
            }
            if (returnSeparator) {
                result.add(slice(s, styles, offsets, m.start(), m.end()));
            }
            last = m.end();
        }
        // remainder
        if (last < s.length()) {
            result.add(slice(s, styles, offsets, last, s.length()));
        } else if (last == s.length() && last > 0 && !returnSeparator) {
            // trailing empty — drop it
        }

        return result;
    }

    // reconstruct a styled NText from a substring range [from, to)
    private NText slice(String full, List<NTextStyles> styles, List<int[]> offsets, int from, int to) {
        NTextBuilder tb = NTextBuilder.of();
        for (int i = 0; i < offsets.size(); i++) {
            int cStart = offsets.get(i)[0];
            int cEnd = offsets.get(i)[1];
            int overlapStart = Math.max(from, cStart);
            int overlapEnd = Math.min(to, cEnd);
            if (overlapStart < overlapEnd) {
                NTextStyles si = styles.get(i);
                if (si == null) {
                    tb.append(full.substring(overlapStart, overlapEnd));
                } else {
                    tb.append(full.substring(overlapStart, overlapEnd), si);
                }
            }
        }
        return tb.build();
    }

    public List<NText> split(String separator, boolean returnSeparator) {
        return split(Pattern.compile(Pattern.quote(separator)), returnSeparator);
    }

    public List<NText> splitLines(boolean returnSeparator) {
        return split(Pattern.compile("\\r?\\n"), returnSeparator);
    }

    @Override
    public List<NText> splitLines() {
        return splitLines(false);
    }
}
