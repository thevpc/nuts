package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.*;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DefaultNutsTextNodeBuilder implements NutsTextNodeBuilder {
    NutsTextFormatManager text1;
    private List<NutsTextNode> all = new ArrayList<>();
    private NutsWorkspace ws;
    private NutsTextNodeWriteConfiguration writeConfiguration;
    private static List<NutsTextNodeStyle[]> allCombinations=new ArrayList<>();
    static{
        for (int i = NutsTextNodeStyle.PRIMARY1.ordinal(); i <= NutsTextNodeStyle.PRIMARY9.ordinal(); i++) {
            allCombinations.add(new NutsTextNodeStyle[]{NutsTextNodeStyle.values()[i]});
        }
        for (int i = NutsTextNodeStyle.SECONDARY1.ordinal(); i <= NutsTextNodeStyle.SECONDARY9.ordinal(); i++) {
            allCombinations.add(new NutsTextNodeStyle[]{NutsTextNodeStyle.values()[i]});
        }
        for (int i = NutsTextNodeStyle.UNDERLINED.ordinal(); i <= NutsTextNodeStyle.REVERSED.ordinal(); i++) {
            allCombinations.add(new NutsTextNodeStyle[]{NutsTextNodeStyle.values()[i]});
        }
        for (int i = NutsTextNodeStyle.PRIMARY1.ordinal(); i <= NutsTextNodeStyle.PRIMARY9.ordinal(); i++) {
            for (int j = NutsTextNodeStyle.UNDERLINED.ordinal(); j < NutsTextNodeStyle.REVERSED.ordinal(); j++) {
                allCombinations.add(new NutsTextNodeStyle[]{
                        NutsTextNodeStyle.values()[i],
                        NutsTextNodeStyle.values()[j],
                });
            }
        }
        for (int i = NutsTextNodeStyle.SECONDARY1.ordinal(); i <= NutsTextNodeStyle.SECONDARY9.ordinal(); i++) {
            for (int j = NutsTextNodeStyle.UNDERLINED.ordinal(); j < NutsTextNodeStyle.REVERSED.ordinal(); j++) {
                allCombinations.add(new NutsTextNodeStyle[]{
                        NutsTextNodeStyle.values()[i],
                        NutsTextNodeStyle.values()[j],
                });
            }
        }
    }

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
    public NutsTextNodeBuilder appendPlain(String text) {
        all.add(text1.factory().plain(text));
        return this;
    }

    @Override
    public NutsTextNodeBuilder appendStyled(String text, NutsTextNodeStyle... decos) {
        return appendStyled(text1.factory().plain(text), decos);
    }

    @Override
    public NutsTextNodeBuilder appendStyled(NutsTextNode text, NutsTextNodeStyle... decos) {
        all.add(text1.factory().styled(text,decos));
        return this;
    }

    @Override
    public NutsTextNode build() {
        return text1.factory().list(all);
    }

    @Override
    public NutsString toNutsString() {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        NutsTextNodeWriterStringer ss=new NutsTextNodeWriterStringer(out);
        ss.writeNode(build(),getWriteConfiguration());
        return new NutsString(out.toString());
    }

    @Override
    public String toString() {
        return toNutsString().toString();
    }

    @Override
    public NutsTextNodeBuilder appendHashedStyle(Object text) {
        return appendHashedStyle(text,text);
    }

    @Override
    public NutsTextNodeBuilder appendHashedStyle(Object text, Object hash) {
        if(text==null){
            return this;
        }
        if(hash==null){
            hash=text;
        }
        int a=Math.abs(hash.hashCode())%allCombinations.size();
        NutsTextNode v;
        if(text instanceof NutsTextNode) {
            v = (NutsTextNode) text;
        }else if(text instanceof NutsStringBase){
            v=ws.formats().text().parser().parse(new StringReader(text.toString()));
        }else if(text instanceof CharSequence){
            v=ws.formats().text().factory().plain((String) text);
        }else{
            v=ws.formats().text().factory().plain(String.valueOf(text));
        }
        return appendStyled(v,allCombinations.get(a));
    }
}
