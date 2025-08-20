package net.thevpc.nuts.text;

import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NTextBuilderPlain implements NTextBuilder {
    private StringBuilder sb=new StringBuilder();

    public NTextBuilderPlain() {
    }
    public NTextBuilderPlain(String data) {
        if(data!=null){
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
    public NTextType getType() {
        return NTextType.BUILDER;
    }

    @Override
    public NTextBuilder builder() {
        return new NTextBuilderPlain(sb.toString());
    }

    @Override
    public NTextStyleGenerator getStyleGenerator() {
        return new NTextStyleGenerator() {
            private boolean includePlain=false;
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
                this.includePlain=includePlain;
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
    public NTextBuilder setStyleGenerator(NTextStyleGenerator styleGenerator) {
        return this;
    }

    @Override
    public NTextBuilder appendCommand(NTerminalCmd command) {
        return this;
    }

    @Override
    public NTextBuilder appendCode(String lang, String text) {
        sb.append(text==null?"":text);
        return this;
    }

    @Override
    public NTextBuilder appendHashStyle(Object text) {
        sb.append(text==null?"":text);
        return this;
    }

    @Override
    public NTextBuilder appendRandomStyle(Object text) {
        sb.append(text==null?"":text);
        return this;
    }

    @Override
    public NTextBuilder appendHashStyle(Object text, Object hash) {
        sb.append(text==null?"":text);
        return this;
    }

    @Override
    public NTextBuilder append(Object text, NTextStyle style) {
        sb.append(text==null?"":text);
        return this;
    }

    @Override
    public NTextBuilder append(Object text, NTextStyles styles) {
        sb.append(text==null?"":text);
        return this;
    }

    @Override
    public NTextBuilder append(Object node) {
        sb.append(node==null?"":node);
        return this;
    }

    @Override
    public NTextBuilder append(NText node) {
        sb.append(node==null?"":node);
        return this;
    }

    @Override
    public NTextBuilder appendJoined(Object separator, Collection<?> others) {
        if(others!=null){
            boolean first=true;
            for (Object other : others) {
                if(first){
                    first=false;
                }else{
                    append(separator);
                }
                append(other);
            }
        }
        return this;
    }

    @Override
    public NTextBuilder appendAll(Collection<?> others) {
        if(others!=null){
            for (Object other : others) {
                append(other);
            }
        }
        return this;
    }

    @Override
    public NText build() {
        return new ImmutableNTextPlain(sb.toString());
    }

    @Override
    public List<NText> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public NText subChildren(int from, int to) {
        if(from==0 && to==1){
            return new ImmutableNTextPlain(sb.toString());
        }
        return new ImmutableNTextPlain("");
    }

    @Override
    public NText substring(int from, int to) {
        return new ImmutableNTextPlain(sb.substring(from,to));
    }

    @Override
    public NTextBuilder insert(int at, NText... newTexts) {
        StringBuilder sb2=new StringBuilder();
        for (NText newText : newTexts) {
            sb2.append(newText);
        }
        sb.insert(at,sb2);
        return this;
    }

    @Override
    public NTextBuilder replace(int from, int to, NText... newTexts) {
        StringBuilder sb2=new StringBuilder();
        for (NText newText : newTexts) {
            sb2.append(newText);
        }
        sb.replace(from,to,sb2.toString());
        return this;
    }

    @Override
    public NTextBuilder replaceChildren(int from, int to, NText... newTexts) {
        return this;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public NText get(int index) {
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public Iterable<NText> items() {
        return Collections.emptyList();
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
        throw new IllegalArgumentException(NMsg.ofC(NI18n.of("not supported method %s"),"lines()").toString());
    }

    @Override
    public NTextBuilder readLine() {
        String line = NStringUtils.readLine(sb);
        if(line==null){
            return null;
        }
        return new NTextBuilderPlain(line);
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
    public int textLength() {
        return sb.length();
    }

    @Override
    public boolean isEmpty() {
        return sb.length()==0;
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(sb.toString());
    }

    @Override
    public NText simplify() {
        return this;
    }

    private static class ImmutableNTextPlain implements NTextPlain {
        private final String nString;

        public ImmutableNTextPlain(String nString) {
            this.nString = nString;
        }

        @Override
        public String getValue() {
            return nString.toString();
        }

        @Override
        public NTextType getType() {
            return NTextType.PLAIN;
        }

        @Override
        public NText immutable() {
            return this;
        }

        @Override
        public String filteredText() {
            return nString;
        }

        @Override
        public int textLength() {
            return nString.length();
        }

        @Override
        public boolean isEmpty() {
            return nString.isEmpty();
        }

        @Override
        public NTextBuilder builder() {
            NTextBuilderPlain b = new NTextBuilderPlain();
            b.sb.append(nString.toString());
            return b;
        }

        @Override
        public NText simplify() {
            return this;
        }

        @Override
        public boolean isBlank() {
            return nString.trim().isEmpty();
        }
    }
}
