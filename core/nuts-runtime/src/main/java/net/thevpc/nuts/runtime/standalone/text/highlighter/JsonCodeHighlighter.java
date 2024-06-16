package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.NCodeHighlighter;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;

public class JsonCodeHighlighter implements NCodeHighlighter {
    private NWorkspace ws;

    public JsonCodeHighlighter(NWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String getId() {
        return "json";
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt, NSession session) {
        return txt.ofPlain(text);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        String s = context.getConstraints();
        if(s==null){
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        switch (s){
            case "json":
            case "application/json":
            case "text/json":
            {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    @Override
    public NText stringToText(String text, NTexts txt, NSession session) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            switch (ar.peekChar()) {
                case '{':
                case '}':
                case '[':
                case ']':
                case ':': {
                    all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
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
                    NText[] d = StringReaderExtUtils.readNumber(session, ar);
                    if(d!=null) {
                        all.addAll(Arrays.asList(d));
                    }else{
                        all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
                    }
                    break;
                }
                case '/':{
                    if(ar.peekChars("//")) {
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(session,ar)));
                    }else if(ar.peekChars("/*")){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(session,ar)));
                    }else{
                        all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
                    }
                    break;
                }
                default: {
                    if(Character.isWhitespace(ar.peekChar())){
                        all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(session,ar)));
                    }else {
                        NText[] d = StringReaderExtUtils.readJSIdentifier(session, ar);
                        if (d != null) {
                            if (d.length == 1 && d[0].getType() == NTextType.PLAIN) {
                                String txt2 = ((NTextPlain) d[0]).getText();
                                switch (txt2) {
                                    case "true":
                                    case "false": {
                                        d[0] = txt.ofStyled(d[0], NTextStyle.bool());
                                        break;
                                    }
                                }
                            }
                            all.addAll(Arrays.asList(d));
                        } else {
                            all.add(txt.ofStyled(String.valueOf(ar.nextChar()), NTextStyle.separator()));
                        }
                    }
                    break;
                }
            }
        }
        return txt.ofList(all.toArray(new NText[0]));
    }
}
