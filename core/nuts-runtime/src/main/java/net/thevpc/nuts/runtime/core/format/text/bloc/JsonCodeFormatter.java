package net.thevpc.nuts.runtime.core.format.text.bloc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringReaderExt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.NutsCodeFormat;

public class JsonCodeFormatter implements NutsCodeFormat {
    private NutsWorkspace ws;
    private NutsTextManager factory;

    public JsonCodeFormatter(NutsWorkspace ws) {
        this.ws = ws;
        factory = ws.formats().text();
    }


    @Override
    public NutsText tokenToNode(String text, String nodeType,NutsSession session) {
        factory.setSession(session);
        return factory.forPlain(text);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<String> criteria) {
        String s = criteria.getConstraints();
        return "json".equals(s) ? NutsComponent.DEFAULT_SUPPORT : NutsComponent.NO_SUPPORT;
    }

    @Override
    public NutsText textToNode(String text, NutsSession session) {
        factory.setSession(session);
        List<NutsText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            switch (ar.peekChar()) {
                case '{':
                case '}':
                case ':': {
                    all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    break;
                }
                case '\'': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSSimpleQuotes(session, ar)));
                    break;
                }
                case '"': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readJSDoubleQuotesString(session, ar)));
                    break;
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readNumber(session, ar)));
                    break;
                }
                case '.':
                case '-':{
                    NutsText[] d = StringReaderExtUtils.readNumber(session, ar);
                    if(d!=null) {
                        all.addAll(Arrays.asList(d));
                    }else{
                        all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                case '/':{
                    if(ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(session,ar)));
                    }else if(ar.peekChars("/*")){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(session,ar)));
                    }else{
                        all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                    }
                    break;
                }
                default: {
                    if(Character.isWhitespace(ar.peekChar())){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session,ar)));
                    }else {
                        NutsText[] d = StringReaderExtUtils.readJSIdentifier(session, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NutsTextNodeType.PLAIN) {
                                String txt = ((NutsTextPlain) d[0]).getText();
                                switch (txt) {
                                    case "true":
                                    case "false": {
                                        d[0] = factory.forStyled(d[0], NutsTextNodeStyle.keyword());
                                        break;
                                    }
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(factory.forStyled(String.valueOf(ar.nextChar()), NutsTextNodeStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return factory.forList(all.toArray(new NutsText[0]));
    }
}
