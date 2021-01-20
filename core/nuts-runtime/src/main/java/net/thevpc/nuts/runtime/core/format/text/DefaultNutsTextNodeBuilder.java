package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DefaultNutsTextNodeBuilder implements NutsTextNodeBuilder {
    private static List<NutsTextNodeStyle[]> allCombinations = new ArrayList<>();

    static {
        for (int i = 1; i <= 255; i++) {
            allCombinations.add(new NutsTextNodeStyle[]{NutsTextNodeStyle.primary(i)});
        }
        for (int i = 1; i <= 255; i++) {
            allCombinations.add(new NutsTextNodeStyle[]{NutsTextNodeStyle.secondary(i)});
        }
        NutsTextNodeStyle[] ss = {
                NutsTextNodeStyle.underlined(),
                NutsTextNodeStyle.striked(),
                NutsTextNodeStyle.reversed(),
                NutsTextNodeStyle.bold(),
                NutsTextNodeStyle.blink(),
        };
        for (NutsTextNodeStyle s : ss) {
            allCombinations.add(new NutsTextNodeStyle[]{s});
        }

        for (int i = 1; i <= 255; i++) {
            for (int j = 0; j < ss.length; j++) {
                allCombinations.add(new NutsTextNodeStyle[]{
                        NutsTextNodeStyle.primary(i),
                        ss[j]
                });
            }
        }
        for (int i = 1; i <= 255; i++) {
            for (int j = 0; j < ss.length; j++) {
                allCombinations.add(new NutsTextNodeStyle[]{
                        NutsTextNodeStyle.secondary(i),
                        ss[j]
                });
            }
        }
    }

    NutsTextFormatManager text1;
    private List<NutsTextNode> all = new ArrayList<>();
    private NutsWorkspace ws;
    private NutsTextNodeWriteConfiguration writeConfiguration;

    public DefaultNutsTextNodeBuilder(NutsWorkspace ws) {
        text1 = ws.formats().text();
        this.ws = ws;
    }

    @Override
    public NutsTextNodeWriteConfiguration getWriteConfiguration() {
        return writeConfiguration;
    }

    @Override
    public NutsTextNodeBuilder setWriteConfiguration(NutsTextNodeWriteConfiguration writeConfiguration) {
        this.writeConfiguration = writeConfiguration;
        return this;
    }

    @Override
    public NutsTextNodeBuilder appendCommand(String command, String args) {
        all.add(text1.factory().command(command, args));
        return this;
    }

    @Override
    public NutsTextNodeBuilder appendCode(String lang, String text) {
        all.add(text1.factory().code(lang, text));
        return this;
    }


    @Override
    public NutsTextNodeBuilder append(String text, NutsTextNodeStyle... styles) {
        return append(text1.factory().plain(text), styles);
    }

    @Override
    public NutsTextNodeBuilder appendHash(Object text) {
        return appendHash(text, text);
    }

    @Override
    public NutsTextNodeBuilder appendHash(Object text, Object hash) {
        if (text == null) {
            return this;
        }
        if (hash == null) {
            hash = text;
        }
        int a = Math.abs(hash.hashCode()) % allCombinations.size();
        NutsTextNode v;
        if (text instanceof NutsTextNode) {
            v = (NutsTextNode) text;
        } else if (text instanceof NutsString) {
            v = ws.formats().text().parser().parse(new StringReader(text.toString()));
        } else {
            v = ws.formats().text().factory().plain(String.valueOf(text));
        }
        return append(v, allCombinations.get(a));
    }

    @Override
    public NutsTextNodeBuilder append(NutsTextNode text, NutsTextNodeStyle... styles) {
        if (styles.length == 0) {
            all.add(text);
        } else {
            all.add(text1.factory().styled(text, styles));
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder append(Object node) {
        if (node != null) {
            return append(ws.formats().text().of(node));
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder append(NutsTextNode node) {
        if (node != null) {
            all.add(node);
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder append(NutsString str) {
        if (str != null) {
            NutsTextNode n = ws.formats().text().parser().parse(new StringReader(str.toString()));
            if (n != null) {
                append(n);
            }
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder append(NutsFormattable str) {
        if (str != null) {
            append(ws.formats().text().factory().nodeFor(str));
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder appendJoined(NutsString separator, List<NutsString> others) {
        if (others != null) {
            boolean first = true;
            for (NutsString other : others) {
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
    public NutsTextNode build() {
        return text1.factory().list(all);
    }

    @Override
    public int size() {
        return all.size();
    }

    @Override
    public NutsString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NutsTextNodeWriterStringer ss = new NutsTextNodeWriterStringer(out, ws);
        ss.writeNode(build(), getWriteConfiguration());
        return new NutsImmutableString(ws,out.toString());
    }

    @Override
    public String toString() {
        return immutable().toString();
    }

    @Override
    public String filteredText() {
        return immutable().filteredText();
    }

    @Override
    public int textLength() {
        return immutable().textLength();
    }

    @Override
    public NutsTextNode toNode() {
        return build();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }
}
