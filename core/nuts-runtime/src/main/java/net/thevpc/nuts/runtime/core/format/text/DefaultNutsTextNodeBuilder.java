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
    private List<NutsText> all = new ArrayList<>();
    private NutsSession session;
    private NutsTextNodeWriteConfiguration writeConfiguration;
    private NutsTextStyleGenerator styleGenerator;

    public DefaultNutsTextNodeBuilder(NutsSession session) {
        this.session = session;
        text1 = session.getWorkspace().formats();
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
    public NutsTextNodeBuilder appendCommand(NutsTerminalCommand command) {
        all.add(text1.text().forCommand(command));
        return this;
    }

    @Override
    public NutsTextNodeBuilder appendCode(String lang, String text) {
        all.add(text1.text().forCode(lang, text));
        return this;
    }

//
//    @Override
//    public NutsTextNodeBuilder append(String text, NutsTextNodeStyle... styles) {
//        return append(text1.text().forPlain(text), styles);
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
        return append(text, NutsTextNodeStyles.of(style));
    }

    @Override
    public NutsTextNodeBuilder append(Object text, NutsTextNodeStyles styles) {
        if (text != null) {
            if (styles.size() == 0) {
                all.add(session.getWorkspace().formats().text().toText(text));
            } else {
                all.add(text1.text().forStyled(session.getWorkspace().formats().text().toText(text), styles));
            }
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder append(Object node) {
        if (node != null) {
            return append(session.getWorkspace().formats().text().toText(node));
        }
        return this;
    }

    @Override
    public NutsTextNodeBuilder append(NutsText node) {
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
//            NutsText n = ws.formats().text().parser().parse(new StringReader(str.toString()));
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
//            append(ws.formats().text().toText(str));
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
    public NutsText build() {
        return text1.text().forList(all);
    }

    @Override
    public int size() {
        return all.size();
    }

    @Override
    public NutsTextParser parser() {
        return new DefaultNutsTextNodeParser(session);
    }

    @Override
    public NutsString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NutsTextNodeWriterStringer ss = new NutsTextNodeWriterStringer(out, session.getWorkspace());
        ss.writeNode(build(), getConfiguration());
        return new NutsImmutableString(session.getWorkspace(), out.toString());
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
    public NutsText toNode() {
        return build();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }
}
