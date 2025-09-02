package net.thevpc.nuts.text;

import java.util.*;

import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringUtils;

public class NTextBuilderPlain implements NTextBuilder {

    private StringBuilder sb = new StringBuilder();

    public NTextBuilderPlain() {
    }

    public NTextBuilderPlain(String data) {
        if (data != null) {
            sb.append(data);
        }
    }

    @Override
    public Iterator<NText> iterator() {
        return Collections.unmodifiableList(getChildren()).iterator();
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public NTextType type() {
        return NTextType.BUILDER;
    }

    @Override
    public NTextBuilder builder() {
        return new NTextBuilderPlain(sb.toString());
    }

    @Override
    public NTextStyleGenerator getStyleGenerator() {
        return new NTextStyleGenerator() {
            private boolean includePlain = false;

            @Override
            public NTextStyles hash(Object i) {
                return NTextStyles.of();
            }

            @Override
            public NTextStyles hash(int i) {
                return NTextStyles.of();
            }

            @Override
            public NTextStyles random() {
                return NTextStyles.of();
            }

            @Override
            public boolean isIncludePlain() {
                return includePlain;
            }

            @Override
            public NTextStyleGenerator setIncludePlain(boolean includePlain) {
                this.includePlain = includePlain;
                return this;
            }

            @Override
            public boolean isIncludeBold() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeBold(boolean includeBold) {
                return this;
            }

            @Override
            public boolean isIncludeBlink() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeBlink(boolean includeBlink) {
                return this;
            }

            @Override
            public boolean isIncludeReversed() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeReversed(boolean includeReversed) {
                return this;
            }

            @Override
            public boolean isIncludeItalic() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeItalic(boolean includeItalic) {
                return this;
            }

            @Override
            public boolean isIncludeUnderlined() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeUnderlined(boolean includeUnderlined) {
                return this;
            }

            @Override
            public boolean isIncludeStriked() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeStriked(boolean includeStriked) {
                return this;
            }

            @Override
            public boolean isIncludeForeground() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeForeground(boolean includeForeground) {
                return this;
            }

            @Override
            public boolean isIncludeBackground() {
                return false;
            }

            @Override
            public NTextStyleGenerator setIncludeBackground(boolean includeBackground) {
                return this;
            }

            @Override
            public boolean isUseThemeColors() {
                return false;
            }

            @Override
            public boolean isUsePaletteColors() {
                return false;
            }

            @Override
            public boolean isUseTrueColors() {
                return false;
            }

            @Override
            public NTextStyleGenerator setUseThemeColors() {
                return this;
            }

            @Override
            public NTextStyleGenerator setUsePaletteColors() {
                return this;
            }

            @Override
            public NTextStyleGenerator setUseTrueColors() {
                return this;
            }
        };
    }

    @Override
    public boolean isNormalized() {
        return false;
    }

    @Override
    public NNormalizedText normalize() {
        return (NNormalizedText) build();
    }

    @Override
    public NNormalizedText normalize(NTextTransformConfig config) {
        return (NNormalizedText) build();
    }

    @Override
    public NNormalizedText normalize(NTextTransformer transformer, NTextTransformConfig config) {
        return (NNormalizedText) build();
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public NTextBuilder setStyleGenerator(NTextStyleGenerator styleGenerator) {
        return this;
    }

    @Override
    public NTextBuilder appendCommand(NTerminalCmd command) {
        return this;
    }

    @Override
    public NTextBuilder appendCode(String lang, String text) {
        sb.append(text == null ? "" : text);
        return this;
    }

    @Override
    public NTextBuilder appendHashStyle(Object text) {
        sb.append(text == null ? "" : text);
        return this;
    }

    @Override
    public NTextBuilder appendRandomStyle(Object text) {
        sb.append(text == null ? "" : text);
        return this;
    }

    @Override
    public NTextBuilder appendHashStyle(Object text, Object hash) {
        sb.append(text == null ? "" : text);
        return this;
    }

    @Override
    public NTextBuilder append(Object text, NTextStyle style) {
        sb.append(text == null ? "" : text);
        return this;
    }

    @Override
    public NTextBuilder append(Object text, NTextStyles styles) {
        sb.append(text == null ? "" : text);
        return this;
    }

    @Override
    public NTextBuilder append(Object node) {
        sb.append(node == null ? "" : node);
        return this;
    }

    @Override
    public NTextBuilder append(NText node) {
        sb.append(node == null ? "" : node);
        return this;
    }

    @Override
    public NTextBuilder appendJoined(Object separator, Collection<?> others) {
        if (others != null) {
            boolean first = true;
            for (Object other : others) {
                if (first) {
                    first = false;
                } else {
                    append(separator);
                }
                append(other);
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
            for (Object other : others) {
                append(other);
            }
        }
        return this;
    }

    @Override
    public NTextBuilder delete(int start, int end) {
        sb.delete(start, end);
        return this;
    }

    @Override
    public NText build() {
        return new ImmutableNTextPlain(sb.toString());
    }

    @Override
    public List<NText> getChildren() {
        return Arrays.asList(build());
    }

    @Override
    public NText subChildren(int from, int to) {
        if (from == 0 && to == 1) {
            return new ImmutableNTextPlain(sb.toString());
        }
        return new ImmutableNTextPlain("");
    }

    @Override
    public NText substring(int start, int end) {
        return new ImmutableNTextPlain(sb.substring(start, end));
    }

    @Override
    public NTextBuilder insert(int at, NText... newTexts) {
        StringBuilder sb2 = new StringBuilder();
        for (NText newText : newTexts) {
            sb2.append(newText);
        }
        sb.insert(at, sb2);
        return this;
    }

    @Override
    public NTextBuilder replace(int from, int to, NText... newTexts) {
        StringBuilder sb2 = new StringBuilder();
        for (NText newText : newTexts) {
            sb2.append(newText);
        }
        sb.replace(from, to, sb2.toString());
        return this;
    }

    @Override
    public NTextBuilder replaceChildren(int from, int to, NText... newTexts) {
        return this;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public NText get(int index) {
        if (index == 0) {
            return build();
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public NTextBuilder flatten() {
        NTextBuilderPlain b = new NTextBuilderPlain();
        sb.append(sb);
        return b;
    }

    @Override
    public NTextBuilder removeAt(int index) {
        return this;
    }

    @Override
    public NStream<NTextBuilder> lines() {
        throw new IllegalArgumentException(NMsg.ofC(NI18n.of("not supported method %s"), "lines()").toString());
    }

    @Override
    public NTextBuilder readLine() {
        String line = NStringUtils.readLine(sb);
        if (line == null) {
            return null;
        }
        return new NTextBuilderPlain(line);
    }

    @Override
    public NTextBuilder newLine() {
        return append("\n");
    }

    @Override
    public NText immutable() {
        return new ImmutableNTextPlain(sb.toString());
    }

    @Override
    public String filteredText() {
        return sb.toString();
    }

    @Override
    public int length() {
        return sb.length();
    }

    @Override
    public boolean isEmpty() {
        return sb.length() == 0;
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(sb.toString());
    }

    @Override
    public NText simplify() {
        return this;
    }


    @Override
    public NPrimitiveText[] toCharArray() {
        return toCharList().toArray(new NPrimitiveText[0]);
    }

    @Override
    public List<NPrimitiveText> toCharList() {
        List<NPrimitiveText> all = new ArrayList<>();
        for (char child : toString().toCharArray()) {
            all.add(new ImmutableNTextPlain(String.valueOf(child)));
        }
        return all;
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
    public List<NText> split(String separator, boolean returnSeparator) {
        StringTokenizer st = new StringTokenizer(toString(), separator, true);
        List<NText> all = new ArrayList<>();
        while (st.hasMoreElements()) {
            all.add(new ImmutableNTextPlain(st.nextToken()));
        }
        return all;
    }

    @Override
    public NTextBuilder clear() {
        sb.delete(0, sb.length());
        return this;
    }

    @Override
    public NTextBuilder trim() {
        NStringUtils.trim(sb);
        return this;
    }

    @Override
    public NTextBuilder trimLeft() {
        NStringUtils.trimLeft(sb);
        return this;
    }

    @Override
    public NTextBuilder trimRight() {
        NStringUtils.trimRight(sb);
        return this;
    }

    @Override
    public NText repeat(int times) {
        if (times <= 0) {
            return NText.ofPlain("");
        }
        if (times == 1) {
            return this;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < times; i++) {
            b.append(sb.toString());
        }
        return new ImmutableNTextPlain(b.toString());
    }


    @Override
    public NText concat(NText other) {
        if (other == null) {
            return this;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.sb);
        sb.append(other);
        return new ImmutableNTextPlain(sb.toString());
    }

    @Override
    public NText concat(NText... others) {
        if (others == null) {
            return this;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.sb);
        for (NText other : others) {
            sb.append(other);
        }
        return new ImmutableNTextPlain(sb.toString());
    }

    private static class ImmutableNTextPlain implements NTextPlain {

        private final String str;

        public ImmutableNTextPlain(String str) {
            this.str = str == null ? "" : str;
        }

        @Override
        public String getValue() {
            return str.toString();
        }

        @Override
        public NTextType type() {
            return NTextType.PLAIN;
        }

        @Override
        public NText immutable() {
            return this;
        }

        @Override
        public String filteredText() {
            return str;
        }

        @Override
        public int length() {
            return str.length();
        }

        @Override
        public boolean isEmpty() {
            return str.isEmpty();
        }

        @Override
        public NTextBuilder builder() {
            NTextBuilderPlain b = new NTextBuilderPlain();
            b.sb.append(str);
            return b;
        }

        @Override
        public NText simplify() {
            return this;
        }

        @Override
        public boolean isBlank() {
            return str.trim().isEmpty();
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }


        @Override
        public List<NPrimitiveText> toCharList() {
            List<NPrimitiveText> all = new ArrayList<>();
            for (char child : str.toCharArray()) {
                all.add(new ImmutableNTextPlain(String.valueOf(child)));
            }
            return all;
        }

        @Override
        public NText substring(int start, int end) {
            return new ImmutableNTextPlain(this.str.substring(start, end));
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
        public List<NText> split(String separator, boolean returnSeparator) {
            StringTokenizer st = new StringTokenizer(str, separator, true);
            List<NText> all = new ArrayList<>();
            while (st.hasMoreElements()) {
                all.add(new ImmutableNTextPlain(st.nextToken()));
            }
            return all;
        }

        @Override
        public NText trim() {
            return new ImmutableNTextPlain(this.str.trim());
        }

        @Override
        public NText trimLeft() {
            return new ImmutableNTextPlain(NStringUtils.trimLeft(this.str));
        }

        @Override
        public NText trimRight() {
            return new ImmutableNTextPlain(NStringUtils.trimRight(this.str));
        }

        @Override
        public NText repeat(int times) {
            if (times <= 0) {
                return NText.ofPlain("");
            }
            if (times == 1) {
                return this;
            }
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < times; i++) {
                b.append(str);
            }
            return new ImmutableNTextPlain(b.toString());
        }

        @Override
        public NText concat(NText other) {
            if (other == null) {
                return this;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(other);
            return new ImmutableNTextPlain(sb.toString());
        }

        @Override
        public NText concat(NText... others) {
            if (others == null) {
                return this;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(str);
            for (NText other : others) {
                sb.append(other);
            }
            return new ImmutableNTextPlain(sb.toString());
        }

        @Override
        public boolean isNormalized() {
            return true;
        }

        @Override
        public NNormalizedText normalize() {
            return this;
        }

        @Override
        public NNormalizedText normalize(NTextTransformConfig config) {
            return this;
        }

        @Override
        public NNormalizedText normalize(NTextTransformer transformer, NTextTransformConfig config) {
            return this;
        }

        @Override
        public NPrimitiveText[] toCharArray() {
            return toCharList().toArray(new NPrimitiveText[0]);
        }
    }
}
