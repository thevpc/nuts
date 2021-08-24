package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.List;

public class DefaultCommandLineWindowsCmd implements NutsCommandLineBashFamilySupport{
    //special chars : &()[]{}^=;!'+,`~
    public DefaultCommandLineWindowsCmd() {
    }

    protected String[] parseCommandLineArrBash(String commandLineString,NutsSession session) {
        if (commandLineString == null) {
            return new String[0];
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_DBQUOTED_WORD = 3;
        int status = START;
        char[] charArray = commandLineString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (status) {
                case START: {
                    switch (c) {
                        case ' ': {
                            //ignore
                            break;
                        }
                        case '"': {
                            status = IN_DBQUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '^': {
                            status = IN_WORD;
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            status = IN_WORD;
                            break;
                        }
                    }
                    break;
                }
                case IN_WORD: {
                    switch (c) {
                        case ' ': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            break;
                        }
                        case '"': {
                            throw new NutsParseException(session, NutsMessage.cstyle("illegal char %s", c));
                        }
                        case '^': {
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            break;
                        }
                    }
                    break;
                }
                case IN_DBQUOTED_WORD: {
                    switch (c) {
                        case '"': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '^': {
                            i = readEscaped(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                }
            }
        }
        switch (status) {
            case START: {
                break;
            }
            case IN_WORD: {
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
            }
            case IN_DBQUOTED_WORD: {
                throw new NutsParseException(session, NutsMessage.cstyle("expected \""));
            }
        }
        return args.toArray(new String[0]);
    }


    public int readEscaped(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case '%':
            case '\"':
            case ':':
            case ';':
            case '^':
            case '=':
            case ',':
            case ' ':
            case '\t':
            case '<':
            case '>':
            case '&':
            case '(':
            case ')':
            case '!':
            case '\'':
            case '`':
            case '~':
            {
                sb.append(c);
                break;
            }
            default: {
                sb.append('^').append(c);
                break;
            }
        }
        return i;
    }

    public String escapeArgument(String arg, NutsCommandLineFormatStrategy s, NutsSession session) {
        if(arg == null || arg.isEmpty()){
            return "\"\"";
        }
        if(s==null|| s==NutsCommandLineFormatStrategy.DEFAULT){
            s=NutsCommandLineFormatStrategy.SUPPORT_QUOTES;
        }
        switch (s){
            case NO_QUOTES:{
                StringBuilder sb = new StringBuilder();
                for (char c : arg.toCharArray()) {
                    switch (c){
                        //special chars : &()[]{}^=;!'+,`~
                        case '\"':
                        case ':':
                        case ';':
                        case '^':
                        case '=':
                        case ',':
                        case ' ':
                        case '\t':
                        case '<':
                        case '>':
                        case '&':
                        case '(':
                        case ')':
                        case '!':
                        case '\'':
                        case '`':
                        case '~':
                        {
                            sb.append("^").append(c);
                            break;
                        }
                        case '\n':
                        case '\r':
                        {
                            throw new NutsIllegalArgumentException(session,NutsMessage.plain("unsupported new line in arguments"));
                        }
                        default:
                        {
                            sb.append(c);
                            break;
                        }
                    }
                }
                return sb.toString();
            }
            case SUPPORT_QUOTES:{
                StringBuilder sb=new StringBuilder();
                for (char c : arg.toCharArray()) {
                    switch (c){
                        case '\"':
                        case ':':
                        case ';':
                        case '^':
                        case '=':
                        case ',':
                        case ' ':
                        case '\t':
                        case '<':
                        case '>':
                        case '&':
                        case '(':
                        case ')':
                        case '!':
                        case '\'':
                        case '`':
                        case '~':
                        {
                            return escapeArgument(arg,NutsCommandLineFormatStrategy.REQUIRE_QUOTES,session);
                        }
                        case '\n':
                        case '\r':
                        {
                            throw new NutsIllegalArgumentException(session,NutsMessage.plain("unsupported new line in arguments"));
                        }
                        default:
                        {
                            sb.append(c);
                            break;
                        }
                    }
                }
                return sb.toString();
            }
            case REQUIRE_QUOTES:{
                StringBuilder sb=new StringBuilder();
                sb.append("\"");
                for (char c : arg.toCharArray()) {
                    switch (c){
                        case '^':
                        case '\"':
                        {
                            sb.append("^").append(c);
                            break;
                        }
                        case '\n':
                        case '\r':
                        {
                            throw new NutsIllegalArgumentException(session,NutsMessage.plain("unsupported new line in arguments"));
                        }
                        default:
                        {
                            sb.append(c);
                            break;
                        }
                    }
                }
                sb.append("\"");
                return sb.toString();
            }
            default:{
                throw new NutsUnsupportedEnumException(session,s);
            }
        }
    }
}
