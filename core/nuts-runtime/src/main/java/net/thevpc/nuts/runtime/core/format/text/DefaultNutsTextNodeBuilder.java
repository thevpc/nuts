package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNutsTextStyleGenerator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;

public class DefaultNutsTextNodeBuilder implements NutsTextBuilder {

    NutsTextManager text1;
    private List<NutsText> all = new ArrayList<>();
    private NutsSession session;
    private NutsTextWriteConfiguration writeConfiguration;
    private NutsTextStyleGenerator styleGenerator;

    public DefaultNutsTextNodeBuilder(NutsSession session) {
        this.session = session;
        text1 = session.getWorkspace().text();
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
    public NutsTextWriteConfiguration getConfiguration() {
        return writeConfiguration;
    }

    @Override
    public NutsTextBuilder setConfiguration(NutsTextWriteConfiguration writeConfiguration) {
        this.writeConfiguration = writeConfiguration;
        return this;
    }

    @Override
    public NutsTextBuilder appendCommand(NutsTerminalCommand command) {
        all.add(text1.forCommand(command));
        return this;
    }

    @Override
    public NutsTextBuilder appendCode(String lang, String text) {
        all.add(text1.forCode(lang, text));
        return this;
    }

//
//    @Override
//    public NutsTextBuilder append(String text, NutsTextStyle... styles) {
//        return append(text1.text().forPlain(text), styles);
//    }
    @Override
    public NutsTextBuilder appendHash(Object text) {
        return appendHash(text, text);
    }

    @Override
    public NutsTextBuilder appendHash(Object text, Object hash) {
        if (text == null) {
            return this;
        }
        if (hash == null) {
            hash = text;
        }
        return append(text, getStyleGenerator().hash(hash));
    }

    @Override
    public NutsTextBuilder appendRandom(Object text) {
        if (text == null) {
            return this;
        }
        return append(text, getStyleGenerator().random());
    }

    @Override
    public NutsTextBuilder append(Object text, NutsTextStyle style) {
        return append(text, NutsTextStyles.of(style));
    }

    @Override
    public NutsTextBuilder append(Object text, NutsTextStyles styles) {
        if (text != null) {
            if (styles.size() == 0) {
                all.add(session.getWorkspace().text().toText(text));
            } else {
                all.add(text1.forStyled(session.getWorkspace().text().toText(text), styles));
            }
        }
        return this;
    }

    @Override
    public NutsTextBuilder append(Object node) {
        if (node != null) {
            return append(session.getWorkspace().text().toText(node));
        }
        return this;
    }

    @Override
    public NutsTextBuilder append(NutsText node) {
        if (node != null) {
            all.add(node);
        }
        return this;
    }

    @Override
    public NutsTextBuilder appendAll(Collection<?> others) {
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
    public NutsTextBuilder appendJoined(Object separator, Collection<?> others) {
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
        if(all.size()==0){
            return text1.forPlain("");
        }
        if(all.size()==1){
            return all.get(0);
        }
        return text1.forList(all).simplify();
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
    public NutsText toText() {
        return build();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }
}
