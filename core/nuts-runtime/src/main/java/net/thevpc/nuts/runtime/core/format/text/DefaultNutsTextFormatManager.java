package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.DefaultNutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatPropertiesTheme;
import net.thevpc.nuts.NutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatThemeWrapper;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.io.*;

public class DefaultNutsTextFormatManager implements NutsTextFormatManager {
    private NutsWorkspace ws;
    private NutsTextFormatTheme styleTheme;

    public DefaultNutsTextFormatManager(NutsWorkspace ws) {
        this.ws = ws;
        String y = ws.config().options().getTheme();
        if(!CoreStringUtils.isBlank(y)){
            if("default".equals(y)){
                //default always refers to the this implementation
                styleTheme=new DefaultNutsTextFormatTheme(ws);
            }else {
                styleTheme = new NutsTextFormatThemeWrapper(new NutsTextFormatPropertiesTheme(y, null, ws));
            }
        }else{
            styleTheme=new DefaultNutsTextFormatTheme(ws);
        }
    }

    @Override
    public NutsTextFormatTheme getTheme() {
        return styleTheme;
    }

    @Override
    public NutsTextNodeFactory factory() {
        return new DefaultNutsTextNodeFactory(ws,styleTheme);
    }

    @Override
    public NutsTextNodeBuilder builder() {
        return new DefaultNutsTextNodeBuilder(ws);
    }

    @Override
    public NutsTextNode parse(String t) {
        return t==null?factory().blank():parser().parse(new StringReader(t));
    }

    @Override
    public NutsTextNodeParser parser() {
        return new DefaultNutsTextNodeParser(ws);
    }


//    /**
//     * transform plain text to formatted text so that the result is rendered as
//     * is
//     *
//     * @param text text
//     * @return escaped text
//     */    @Override
//    public String escapeText(String text) {
//        if (text == null) {
//            return "";
//        }
//        NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(text));
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        NutsTextNodeWriter w = new NutsTextNodeWriterStringer(out, ws)
//                .setWriteConfiguration(
//                        new NutsTextNodeWriteConfiguration()
//                        .setFiltered(true)
//                );
//        w.writeNode(node);
//        return out.toString();
//    }

//    @Override
//    public String escapeCodeText(String text) {
//        if (text == null) {
//            return "";
//        }
//        //TODO...
//        return text;
//    }

}
