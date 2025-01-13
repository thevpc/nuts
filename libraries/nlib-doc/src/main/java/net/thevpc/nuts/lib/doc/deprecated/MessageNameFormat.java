/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.deprecated;

import net.thevpc.nuts.util.NBlankable;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class MessageNameFormat {

    private String message;
    private MessagePart part;


    public MessageNameFormat(String message) {
        if (message == null) {
            message = "";
        }
        this.message = message;
        try {
            //parse here ...
            part = new MessageParser(new StringReader(message)).parse();
        } catch (IOException ex) {
            //should never happen
            throw new IllegalArgumentException(ex);
        }
    }



    private static class MessageParser {

        private StringReader r;

        public MessageParser(StringReader r) {
            this.r = r;
        }

        private MessagePart parse() throws IOException {
            List<MessagePart> all = new ArrayList<>();
            while (true) {
                r.mark(1);
                int c = r.read();
                if (c == -1) {
                    break;
                }
                if (c == '$') {
                    r.reset();
                    all.add(readDollarVar());
                } else {
                    r.reset();
                    StringBuilder sb = new StringBuilder();
                    while (true) {
                        r.mark(1);
                        c = r.read();
                        if (c == '\\') {
                            c = r.read();
                            switch (c) {
                                case -1: {
                                    sb.append('\\');
                                    break;
                                }
                                case '\\': {
                                    sb.append('\\');
                                    break;
                                }
                                case 'n': {
                                    sb.append('\n');
                                    break;
                                }
                                case 't': {
                                    sb.append('\t');
                                    break;
                                }
                                case '$': {
                                    sb.append('$');
                                    break;
                                }
                                default: {
                                    sb.append((char) c);
                                    break;
                                }
                            }
                        } else if (c == '$') {
                            r.reset();
                            break;
                        } else if (c == -1) {
                            break;
                        } else {
                            sb.append((char) c);
                        }
                    }
                    all.add(new MessagePartFixed(sb.toString()));
                }
            }
            if (all.isEmpty()) {
                return null;
            }
            if (all.size() == 1) {
                return all.get(0);
            }
            return new MessagePartList(all);
        }

        private Number readLiteralNumber() throws IOException {
            consumeWhites();
            r.mark(1);
            int c = r.read();
            StringBuilder sb = new StringBuilder();
            int sign = 1;
            if (c == '-') {
                sign = -1;
                //this is
                c = r.read();
            }
            if (c == '.' || c >= '0' && c <= '9') {
                sb.append((char) c);
                //now read until it is no more parsable as number
                while (true) {
                    r.mark(1);
                    c = r.read();
                    if (c == -1) {
                        String s = sb.toString();
                        if (s.indexOf('.') >= 0 || s.indexOf('E') >= 0) {
                            return Double.parseDouble(s);
                        }
                        return Integer.parseInt(s);
                    } else if (c == '.' || c >= '0' && c <= '9' || c == 'E' || c == '+') {
                        String s = sb.toString() + ((char) c);
                        if (s.indexOf('.') >= 0 || s.indexOf('E') >= 0) {
                            if (isValidDouble(s)) {
                                sb.append((char) c);
                            } else {
                                r.reset();
                                s = sb.toString();
                                if (s.indexOf('.') >= 0 || s.indexOf('E') >= 0) {
                                    return Double.parseDouble(s);
                                }
                                return Integer.parseInt(s) * sign;
                            }
                        } else {
                            if (isValidInt(s)) {
                                sb.append((char) c);
                            } else {
                                r.reset();
                                s = sb.toString();
                                return Integer.parseInt(s) * sign;
                            }
                        }
                    } else {
                        r.reset();
                        String s = sb.toString();
                        if (s.indexOf('.') >= 0 || s.indexOf('E') >= 0) {
                            return Double.parseDouble(s);
                        }
                        return Integer.parseInt(s);
                    }
                }
            } else {
                r.reset();
                return null;
            }
        }

        private String readName() throws IOException {
            int c;
            //read name
            StringBuilder n = new StringBuilder();
            while (true) {
                r.mark(1);
                c = r.read();
                if (c == -1) {
                    break;
                } else if (c != '$' && Character.isJavaIdentifierPart(c)) {
                    n.append((char) c);
                } else {
                    r.reset();
                    break;
                }
            }
            return n.toString();
        }

        private MessagePart readDollarVar() throws IOException {
            r.mark(1);
            int c = r.read();
            if (c != '$') {
                throw new IllegalArgumentException("Expected $");
            }
            r.mark(1);
            c = r.read();
            if (c != '{') {
                r.reset();
                return new MessagePartExpr(new NameExprNode(readName()));
            } else {
                ExprNode layout = readLayoutNode(true);
                consumeWhites();
                r.mark(1);
                int acc = r.read();
                if (acc == -1 || acc == '}') {
                    //this is ok;
                } else {
                    r.reset();
                }
                return new MessagePartExpr(layout);
            }
        }

        private void consumeWhites() throws IOException {
            while (true) {
                r.mark(1);
                int c = r.read();
                if (c == -1) {
                    break;
                } else if (Character.isWhitespace(c)) {
                    //consumre
                } else {
                    r.reset();
                    break;
                }
            }
        }

        private ExprNode readLayoutNode(boolean acceptLiterals) throws IOException {
            consumeWhites();
            if (acceptLiterals) {
                Number p = readLiteralNumber();
                if (p != null) {
                    return new LiteralExprNode(p);
                }
                String s = readLiteralString();
                if (s != null) {
                    return new LiteralExprNode(s);
                }
            }
            String n = readName();
            consumeWhites();
            r.mark(1);
            int c = r.read();
            if (c == -1) {
                return new NameExprNode(n);
            } else if (c == '(') {
                FunctionExprNode f = new FunctionExprNode(n);
                ExprNode a = readLayoutNode(true);
                if (a != null) {
                    f.args.add(a);
                    while (true) {
                        consumeWhites();
                        r.mark(1);
                        c = r.read();
                        if (c == -1) {
                            break;
                        } else if (c == ',') {
                            consumeWhites();
                        } else if (c == ')') {
                            break;
                        } else {
                            r.reset();
                            consumeWhites();
                        }
                        a = readLayoutNode(true);
                        if (a != null) {
                            f.args.add(a);
                        } else {
                            break;
                        }
                    }
                }
                return f;
            } else {
                r.reset();
                return new NameExprNode(n);
            }
        }

        private String readLiteralString() throws IOException {
            StringBuilder sb = new StringBuilder();
            r.mark(1);
            int c = r.read();
            if (c != '\'' && c != '"' && c != '`') {
                r.reset();
                return null;
            }
            int start = c;
            boolean more = true;
            while (more) {
                c = r.read();
                switch (c) {
                    case -1: {
                        more = false;
                        break;
                    }
                    case '\\': {
                        c = r.read();
                        switch (c) {
                            case -1: {
                                sb.append('\\');
                                more = false;
                                break;
                            }
                            case 'n': {
                                sb.append('\n');
                                break;
                            }
                            case 't': {
                                sb.append('\t');
                                break;
                            }
                            case 'f': {
                                sb.append('\f');
                                break;
                            }
                            default: {
                                sb.append((char) c);
                            }
                        }
                        break;
                    }
                    default: {
                        if (c == start) {
                            more = false;
                        } else {
                            sb.append((char) c);
                        }
                        break;
                    }
                }
            }
            return sb.toString();
        }

        private boolean isValidInt(String s) {
            try {
                Integer.parseInt(s);
                return true;
            } catch (Exception any) {
                return false;
            }
        }

        private boolean isValidDouble(String s) {
            try {
                Double.parseDouble(s);
                return true;
            } catch (Exception any) {
                return false;
            }
        }

    }

    public static interface Function {

        Object eval(ExprNode[] args, MessageNameFormat format, java.util.function.Function<String,Object> provider, MessageNameFormatContext messageNameFormatContext);
    }

    public static interface ExprNode {

        Object format(MessageNameFormat format, java.util.function.Function<String,Object> provider, MessageNameFormatContext messageNameFormatContext);

    }

    public static class LiteralExprNode implements ExprNode {

        Object literal;

        public LiteralExprNode(Object literal) {
            this.literal = literal;
        }

        @Override
        public Object format(MessageNameFormat format, java.util.function.Function<String,Object> f, MessageNameFormatContext messageNameFormatContext) {
            return literal;
        }

        @Override
        public String toString() {
            return _StringUtils.literalToString(literal);
        }

    }

    public static class NameExprNode implements ExprNode {

        private String name;

        public NameExprNode(String name) {
            this.name = name;
        }

        @Override
        public Object format(MessageNameFormat format, java.util.function.Function<String,Object> provider, MessageNameFormatContext messageNameFormatContext) {
            return provider.apply(name);
        }

        @Override
        public String toString() {
            return String.valueOf(name);
        }

    }

    public static class FunctionExprNode implements ExprNode {

        private String name;
        private List<ExprNode> args = new ArrayList<>();

        public FunctionExprNode(String name) {
            this.name = name;
        }

        @Override
        public Object format(MessageNameFormat format, java.util.function.Function<String,Object> provider, MessageNameFormatContext messageNameFormatContext) {
            return format(args, format, provider, messageNameFormatContext);
        }

        public Object format(List<ExprNode> args, MessageNameFormat format, java.util.function.Function<String,Object> provider, MessageNameFormatContext messageNameFormatContext) {
            Function f = messageNameFormatContext.getFunction(name);
            if (f != null) {
                return f.eval(args.toArray(new ExprNode[args.size()]), format, provider, messageNameFormatContext);
            }
            StringBuilder sb = new StringBuilder(name);
            if (args.size() > 0) {
                sb.append("(");
                for (int i = 0; i < args.size(); i++) {
                    ExprNode n = args.get(i);
                    if (i > 0) {
                        sb.append(i);
                    }
                    sb.append(n.format(format, provider, messageNameFormatContext));
                }
                sb.append(")");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(name);
            sb.append('(');
            sb.append(args.stream().map(Object::toString).collect(Collectors.joining(",")));
            sb.append(')');
            return sb.toString();
        }

    }

    public String format(Map<String, Object> map, MessageNameFormatContext messageNameFormatContext) {
        return format(map == null ? null : new StringToObjectMap(map), messageNameFormatContext);
    }

    public String format(java.util.function.Function<String,Object> provider, MessageNameFormatContext messageNameFormatContext) {
        StringBuilder sb = new StringBuilder(message.length() + 1);
        if (part != null) {
            part.format(this, provider, sb, messageNameFormatContext);
        }
        return sb.toString();
    }

    private static interface MessagePart {

        void format(MessageNameFormat format, java.util.function.Function<String,Object> stringToObject, StringBuilder sb, MessageNameFormatContext messageNameFormatContext);
    }

    private static class MessagePartExpr implements MessagePart {

        private final ExprNode layout;

        public MessagePartExpr(ExprNode layout) {
            this.layout = layout;
        }

        @Override
        public void format(MessageNameFormat format, java.util.function.Function<String,Object> stringToObject, StringBuilder sb, MessageNameFormatContext messageNameFormatContext) {
            Object o = layout.format(format, stringToObject, messageNameFormatContext);
            sb.append(String.valueOf(o));
        }

        @Override
        public String toString() {
            return "${" + layout + '}';
        }
    }

    private static class MessagePartList implements MessagePart {

        List<MessagePart> all;// = new ArrayList<MessagePart>();

        public MessagePartList(List<MessagePart> all) {
            this.all = all;
        }

        @Override
        public void format(MessageNameFormat format, java.util.function.Function<String,Object> f, StringBuilder sb, MessageNameFormatContext messageNameFormatContext) {
            for (MessagePart part : all) {
                part.format(format, f, sb, messageNameFormatContext);
            }
        }
    }

    private static class MessagePartFixed implements MessagePart {

        private final String value;

        public MessagePartFixed(String value) {
            this.value = value;
        }

        public void format(MessageNameFormat format, java.util.function.Function<String,Object> f, StringBuilder sb, MessageNameFormatContext messageNameFormatContext) {
            sb.append(value);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    public static DateFormat resolveDateFormat(String dateFormatString, Locale loc, String defaultDateFormatString) {
        if (NBlankable.isBlank(dateFormatString)) {
            dateFormatString = defaultDateFormatString;
        }
        if (dateFormatString.equalsIgnoreCase("short")) {
            return DateFormat.getDateInstance(DateFormat.SHORT, loc);
        } else if (dateFormatString.equalsIgnoreCase("medium")) {
            return DateFormat.getDateInstance(DateFormat.DEFAULT, loc);
        } else if (dateFormatString.equalsIgnoreCase("default")) {
            return DateFormat.getDateInstance(DateFormat.DEFAULT, loc);
        } else if (dateFormatString.equalsIgnoreCase("long")) {
            return DateFormat.getDateInstance(DateFormat.LONG, loc);
        } else if (dateFormatString.equalsIgnoreCase("full")) {
            return DateFormat.getDateInstance(DateFormat.FULL, loc);
        }
        try {
            return new SimpleDateFormat(dateFormatString);
        } catch (Exception ex) {

        }
        try {
            return new SimpleDateFormat(defaultDateFormatString);
        } catch (Exception ex) {

        }
        return new SimpleDateFormat("yyyy-MM-mm HH:mm:ss");
    }

}
