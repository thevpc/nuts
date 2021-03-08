package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsTextStyleGenerator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;

public class DefaultNutsTextNodeBuilder implements NutsTextNodeBuilder {

    NutsFormatManager text1;
    private List<NutsTextNode> all = new ArrayList<>();
    private NutsWorkspace ws;
    private NutsTextNodeWriteConfiguration writeConfiguration;
    private NutsTextStyleGenerator styleGenerator;

    public DefaultNutsTextNodeBuilder(NutsWorkspace ws) {
        text1 = ws.formats();
        this.ws = ws;
    }

    @Override
    public NutsTextStyleGenerator getStyleGenerator() {
        if (styleGenerator == null) {
            styleGenerator = new DefaultNutsTextStyleGenerator();
        }
        return styleGenerator;
    }

    @Override
    public DefaultNutsTextNodeBuilder setStyleGenerator(NutsTextStyleGenerator styleGenerator) {
        this.styleGenerator = styleGenerator;
        return this;
    }

    @Override
    public NutsTextNodeWriteConfiguration getConfiguration() {
        return writeConfiguration;
    }

    @Override
    public NutsTextNodeBuilder setConfiguration(NutsTextNodeWriteConfiguration writeConfiguration) {
        this.writeConfiguration = writeConfiguration;
        return this;
    }

    @Override
    public NutsTextNodeBuilder appendCommand(String command, String args) {
        all.add(text1.text().command(command, args));
        return this;
    }

    @Override
    public NutsTextNodeBuilder appendCode(String lang, String text) {
        all.add(text1.text().code(lang, text));
        return this;
    }

//
//    @Override
//    public NutsTextNodeBuilder append(String text, NutsTextNodeStyle... styles) {
//        return append(text1.text().plain(text), styles);
//    }
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
        return append(text, getStyleGenerator().hash(hash));
    }

    @Override
    public NutsTextNodeBuilder appendRandom(Object text) {
        if (text == null) {
            return this;
        }
        return append(text, getStyleGenerator().random());
    }

    @Override
    public NutsTextNodeBuilder append(Object text, NutsTextNodeStyle style) {
        return append(text,NutsTextNodeStyles.of(style));
    }
    
    @Override
    public NutsTextNodeBuilder append(Object text, NutsTextNodeStyles styles) {
        if (text != null) {
            if (styles.size() == 0) {
                all.add(ws.formats().text().nodeFor(text));
            } else {
                all.add(text1.text().styled(ws.formats().text().nodeFor(text), styles));
            }
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder append(Object node) {
        if (node != null) {
            return append(ws.formats().text().nodeFor(node));
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
    public NutsTextNodeBuilder appendAll(Collection<?> others) {
        if (others != null) {
            for (Object node : others) {
                if (node != null) {
                    append(node);
                }
            }
        }
        return this;
    }
    //    @Override
//    public NutsTextNodeBuilder append(NutsString str) {
//        if (str != null) {
//            NutsTextNode n = ws.formats().text().parser().parse(new StringReader(str.toString()));
//            if (n != null) {
//                append(n);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public NutsTextNodeBuilder append(NutsFormattable str) {
//        if (str != null) {
//            append(ws.formats().text().nodeFor(str));
//        }
//        return this;
//    }

    @Override
    public NutsTextNodeBuilder appendJoined(Object separator, Collection<?> others) {
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
    public NutsTextNode build() {
        return text1.text().list(all);
    }

    @Override
    public int size() {
        return all.size();
    }

    @Override
    public NutsTextNodeParser parser() {
        return new DefaultNutsTextNodeParser(ws);
    }

    @Override
    public NutsString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NutsTextNodeWriterStringer ss = new NutsTextNodeWriterStringer(out, ws);
        ss.writeNode(build(), getConfiguration());
        return new NutsImmutableString(ws, out.toString());
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
