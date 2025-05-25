package net.thevpc.nuts.runtime.standalone.xtra.shell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineShellOptions;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractWinNShellHelper implements NShellHelper {
    public AbstractWinNShellHelper() {
    }

    public String newlineString() {
        return "\r\n";
    }


    public String[] parseCmdLineArr(String commandLineString, boolean lenient) {
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
                        case ' ':
                        case '\t':
                        {
                            //ignore
                            break;
                        }
                        case '\r':
                        case '\n': //support multiline commands
                        {
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
                            if(lenient) {
                                args.add(sb.toString());
                                sb.delete(0, sb.length());
                                status = IN_DBQUOTED_WORD;
                            }else{
                                throw new NParseException(NMsg.ofC("illegal char %s", c));
                            }
                            break;
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
                if(!lenient) {
                    throw new NParseException(NMsg.ofPlain("expected \""));
                }
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
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

    public String escapeArgument(String arg, NCmdLineShellOptions options) {
        if(arg == null || arg.isEmpty()){
            return "\"\"";
        }
        if(options==null){
            options=new NCmdLineShellOptions();
        }
        NCmdLineFormatStrategy s = options.getFormatStrategy();
        if(s==null|| s== NCmdLineFormatStrategy.DEFAULT){
            s= NCmdLineFormatStrategy.SUPPORT_QUOTES;
        }
        int ii = arg.indexOf('=');
        if(ii>=0) {
            String q = arg.substring(0, ii);
            if (options.isExpectEnv()) {
                if (isEnv(q)) {
                    return q + "=" + escapeArgument(arg.substring(ii + 1), options.copy().setExpectEnv(false).setExpectOption(false));
                } else {
                    options.setExpectEnv(false);
                }
            }
            if (options.isExpectOption()) {
                if (isVarOrOption(q)) {
                    return q + "=" + escapeArgument(arg.substring(ii + 1), options.copy().setExpectEnv(false).setExpectOption(false));
                }else{
                    // continue
                }
            }else{
                // continue
            }
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
                            throw new NIllegalArgumentException(NMsg.ofPlain("unsupported new line in arguments"));
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
                            return escapeArgument(arg,options.copy().setFormatStrategy(NCmdLineFormatStrategy.REQUIRE_QUOTES));
                        }
                        case '\n':
                        case '\r':
                        {
                            throw new NIllegalArgumentException(NMsg.ofPlain("unsupported new line in arguments"));
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
                            throw new NIllegalArgumentException(NMsg.ofPlain("unsupported new line in arguments"));
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
                throw new NUnsupportedEnumException(s);
            }
        }
    }

    private boolean isVarOrOption(String arg){
        return Pattern.compile("[-+]{0,3}(//)?[!~]?[a-zA-Z][a-zA-Z0-9_]*").matcher(arg).matches();
    }

    private boolean isEnv(String arg){
        return Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*").matcher(arg).matches();
    }

    @Override
    public String escapeArguments(String[] args, NCmdLineShellOptions options) {
        if(options==null){
            options=new NCmdLineShellOptions();
        }else{
            options=options.copy();
        }
        StringBuilder sb = new StringBuilder();
        boolean expectEnv=true;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(escapeArgument(arg, options));
        }
        return sb.toString();
    }
}
