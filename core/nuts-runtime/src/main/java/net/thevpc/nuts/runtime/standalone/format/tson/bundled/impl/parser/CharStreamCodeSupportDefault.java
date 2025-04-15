package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser;

public class CharStreamCodeSupportDefault extends AbstractCharStreamCodeSupport {
    private int brackets;
    private int braces;
    private int parens;
    private String state = "";

    @Override
    public void reset() {
        brackets = 0;
        braces = 0;
        parens = 0;
        state = "";
    }

    @Override
    public String getErrorMessage(){
        StringBuilder sb=new StringBuilder();
        if(parens<0){
            if(sb.length()>0){
                sb.append(". ");
            }
            sb.append("Too many ')'");
        }
        if(parens>0){
            if(sb.length()>0){
                sb.append(". ");
            }
            sb.append("missing '('");
        }
        if(braces<0){
            if(sb.length()>0){
                sb.append(". ");
            }
            sb.append("Too many '}'");
        }
        if(braces>0){
            if(sb.length()>0){
                sb.append(". ");
            }
            sb.append("missing '{'");
        }
        if(brackets<0){
            if(sb.length()>0){
                sb.append(". ");
            }
            sb.append("Too many ']'");
        }
        if(brackets>0){
            if(sb.length()>0){
                sb.append(". ");
            }
            sb.append("missing '['");
        }
        switch (state){
            case "\"":{
                if(sb.length()>0){
                    sb.append(". ");
                }
                sb.append("missing '\"'");
            }
            case "'":{
                if(sb.length()>0){
                    sb.append(". ");
                }
                sb.append("missing '\\''");
            }
            case "/*":
            case "/*_*":
                {
                if(sb.length()>0){
                    sb.append(". ");
                }
                sb.append("missing '*/'");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isValid() {
        if (parens == 0 && braces == 0 && brackets == 0){
          switch (state){
              case "":
              case "/":return true;
              default:return false;
          }
        }
        return  false;
    }

    @Override
    public void next(char c) {
        switch (state) {
            case "": {
                switch (c) {
                    case '(': {
                        parens++;
                        break;
                    }
                    case ')': {
                        parens--;
                        if (parens < 0) {
                            throw new IllegalArgumentException("Invalid char " + c);
                        }
                        break;
                    }
                    case '{': {
                        braces++;
                        break;
                    }
                    case '}': {
                        braces--;
                        if (braces < 0) {
                            throw new IllegalArgumentException("Invalid char " + c);
                        }
                        break;
                    }
                    case '[': {
                        brackets++;
                        break;
                    }
                    case ']': {
                        brackets--;
                        if (brackets < 0) {
                            throw new IllegalArgumentException("Invalid char " + c);
                        }
                        break;
                    }
                    case '"': {
                        state = "\"";
                        break;
                    }
                    case '\'': {
                        state = "'";
                        break;
                    }
                    case '\\': {
                        state = "\\";
                        break;
                    }
                    case '/': {
                        state = "/?";
                        break;
                    }
                }
                break;
            }
            case "\"": {
                switch (c) {
                    case '"': {
                        state = "";
                        break;
                    }
                    case '\\': {
                        state = "\"\\";
                        break;
                    }
                }
                break;
            }
            case "\\\"": {
                state = "\"";
                break;
            }
            case "'": {
                switch (c) {
                    case '\'': {
                        state = "";
                        break;
                    }
                    case '\\': {
                        state = "\"\\";
                        break;
                    }
                }
                break;
            }
            case "\\'": {
                state = "'";
                break;
            }
            case "/": {
                switch (c) {
                    case '/': {
                        state = "//";
                        break;
                    }
                    case '*': {
                        state = "/*";
                        break;
                    }
                    default: {
                        state = "";
                        break;
                    }
                }
                break;
            }
            case "//": {
                switch (c) {
                    case '\n':
                    case '\r':
                        {
                        state = "";
                        break;
                    }
                }
                break;
            }
            case "/*": {
                switch (c) {
                    case '*':
                        {
                        state = "/*_*";
                        break;
                    }
                }
                break;
            }
            case "/*_*": {
                switch (c) {
                    case '/':
                        {
                        state = "";
                        break;
                    }
                    default:
                        {
                        state = "/*";
                        break;
                    }
                }
                break;
            }
        }
    }
}
