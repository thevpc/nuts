package net.thevpc.nuts.runtime.standalone.format.tson.parser.custom;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementAnnotation;
import net.thevpc.nuts.elem.NElementBuilder;
import net.thevpc.nuts.elem.NElementComment;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.elem.NExprElementBuilder;
import net.thevpc.nuts.elem.NListElement;
import net.thevpc.nuts.elem.NListItemElement;
import net.thevpc.nuts.elem.NObjectElementBuilder;
import net.thevpc.nuts.elem.NOperatorSymbol;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNListElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNListItemElementBuilder;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementToken;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;

public class TsonCustomParser {
    private TsonCustomLexer lexer;
    private Object source;
    private NElementTokenInfo current;


    public TsonCustomParser(String reader) {
        this(new TsonCustomLexer(reader));
    }

    public TsonCustomParser(Reader reader) {
        this(new TsonCustomLexer(reader));
    }

    public TsonCustomParser(TsonCustomLexer lexer) {
        this.lexer = lexer;
    }

    public Object source() {
        return source;
    }

    public void source(Object source) {
        this.source = source;
    }

    public NElement parseDocument() {
        List<NElement> elements = new ArrayList<>();
        while (true) {
            NElement e = parseElement();
            if (e == null) {
                break;
            }
            elements.add(e);
        }
        if (elements.isEmpty()) {
            return null;
        }
        if (elements.size() == 1) {
            return elements.get(0);
        }
        return NElement.ofObject(elements.toArray(new NElement[0]));
    }

    public NElement parseElement() {
        return exprOrPairElement();
    }

    private NElement exprOrPairElement() {
        NElement first = exprElement();
        if (first == null) {
            return null;
        }
        NElementTokenInfo t = peekToken();
        if (t != null && t.token != null && t.token.type() == NElementTokenType.COLON) {
            nextToken();
            NElement second = exprElement();
            return NElement.ofPair(first, second);
        }
        return first;
    }

    private NElement withComments(NElement e, List<NElementComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return e;
        }
        return e.builder().addLeadingComments(comments.toArray(new NElementComment[0])).build();
    }

    private NElement exprElement() {
        // shall read a list of element primitiveElement operator but never two successive elements
        List<NElement> all = new ArrayList<>();
        boolean wasOp = false;
        while (true) {
            NElementTokenInfo t = peekToken();
            if (all.isEmpty()) {
                if (isOp(t)) {
                    NElement e = withComments(NElement.ofOpSymbol(NOperatorSymbol.parse(t.token.type().name()).get()), t.comments);
                    all.add(e);
                    wasOp = true;
                } else {
                    NElement operand = primitiveElement();
                    if (operand != null) {
                        all.add(withComments(operand, t.comments));
                        wasOp = false;
                    } else {
                        break;
                    }
                }
            } else {
                if (isOp(t)) {
                    all.add(withComments(NElement.ofOpSymbol(NOperatorSymbol.parse(t.token.type().name()).get()), t.comments));
                    wasOp = true;
                } else {
                    if (wasOp) {
                        NElement operand = primitiveElement();
                        if (operand != null) {
                            all.add(withComments(operand, t.comments));
                            wasOp = false;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        NExprElementBuilder b = NExprElementBuilder.of();
        for (NElement nElement : all) {
            b.addElement(nElement);
        }
        return b.build();
    }

    private NElement primitiveElement() {
        List<NElementAnnotation> annotations = new ArrayList<>();
        while (isToken(NElementTokenType.AT)) {
            annotations.add(annotation());
        }
        NElement base = null;
        List<NElementTokenInfo> unexpectedTokens = new ArrayList<>();
        while (true) {
            base = null;
            NElementTokenInfo t = peekToken();
            if (t == null) {
                break;
            }
            if (t.token == null) {
                break;
            }

            switch (t.token.type()) {
                case NULL:
                case TRUE:
                case FALSE:
                case DATE:
                case INSTANT:
                case DATETIME:
                case TIME:
                case NUMBER:
                case DOUBLE_QUOTED_STRING:
                case SINGLE_QUOTED_STRING:
                case LINE_STRING:
                case BACKTICK_STR:
                case TRIPLE_DOUBLE_QUOTED_STRING:
                case TRIPLE_SINGLE_QUOTED_STRING:
                case TRIPLE_BACKTICK_STRING:
                case CHAR_STREAM:
                case BINARY_STREAM:
                    nextToken();
                    base = (NElement) t.token.value();
                    break;
                case LBRACE:
                    base = object(null, null);
                    break;
                case LBRACK:
                    base = array(null, null);
                    break;
                case NAME:
                    base = named();
                    break;
                case LPAREN:
                    base = unnamed();
                    break;
                case UNKNOWN: {
                    // TODO: handle error
                    // just ignore for now
                    unexpectedTokens.add(nextToken());
                    break;
                }
                case ORDERED_LIST: {
                    base = list(true, 0);
                    break;
                }
                case UNORDERED_LIST: {
                    base = list(false, 0);
                    break;
                }
                default: {
                    // TODO: handle error
                    // just ignore for now
                    unexpectedTokens.add(nextToken());
                    break;

                }
            }
            if (base != null) {
                break;
            }
        }

        if (base == null) {
            return null;
        }

        if (!annotations.isEmpty() || !unexpectedTokens.isEmpty()) {
            // NElement doesn't have a direct "withAnnotations" but some builders do
            // For now, we just return the base.
            NElementBuilder builder = base.builder().addAnnotations(annotations);
            for (NElementTokenInfo unexpectedToken : unexpectedTokens) {
                // builder.addError(NMsg.ofC("unexpected token %s", unexpectedToken));
            }
            base = builder.build();
        }
        return base;
    }

    private NElementAnnotation annotation() {
        nextToken(); // @
        NElementTokenInfo nameToken = nextToken();
        String name = nameToken == null || nameToken.token == null ? null : nameToken.token.image();
        List<NElement> params = new ArrayList<>();
        if (isToken(NElementTokenType.LPAREN)) {
            nextToken();
            while (!isToken(NElementTokenType.RPAREN)) {
                params.add(exprOrPairElement());
                if (isToken(NElementTokenType.COMMA) || isToken(NElementTokenType.SEMICOLON)) {
                    nextToken();
                } else {
                    break;
                }
            }
            skipToken(NElementTokenType.RPAREN);
        }
        return NElement.ofAnnotation(name, params.toArray(new NElement[0]));
    }

    private NElement object(String name, NElement[] params) {
        NObjectElementBuilder builder = NElement.ofObjectBuilder(name);
        if (params != null) {
            builder.addAll(Arrays.asList(params));
        }
        skipToken(NElementTokenType.LBRACE);
        while (!isToken(NElementTokenType.RBRACE)) {
            NElement e = exprOrPairElement();
            if (e != null) {
                builder.add(e);
            }
            if (isToken(NElementTokenType.COMMA) || isToken(NElementTokenType.SEMICOLON)) {
                nextToken();
            } else {
                break;
            }
        }
        skipToken(NElementTokenType.RBRACE);
        return builder.build();
    }

    private NElement array(String name, NElement[] params) {
        NArrayElementBuilder builder = NElement.ofArrayBuilder(name);
        if (params != null) {
            builder.addAll(Arrays.asList(params));
        }
        skipToken(NElementTokenType.LBRACK);
        while (!isToken(NElementTokenType.RBRACK)) {
            NElement e = exprOrPairElement();
            if (e != null) {
                builder.add(e);
            }
            if (isToken(NElementTokenType.COMMA) || isToken(NElementTokenType.SEMICOLON)) {
                nextToken();
            } else {
                break;
            }
        }
        skipToken(NElementTokenType.RBRACK);
        return builder.build();
    }

    private NElement unnamed() {
        skipToken(NElementTokenType.LPAREN);
        List<NElement> elements = new ArrayList<>();
        while (!isToken(NElementTokenType.RPAREN)) {
            elements.add(exprOrPairElement());
            if (isToken(NElementTokenType.COMMA) || isToken(NElementTokenType.SEMICOLON)) {
                nextToken();
            } else {
                break;
            }
        }
        skipToken(NElementTokenType.RPAREN);
        NElementTokenInfo t = peekToken();
        if (t != null && t.token != null) {
            if (t.token.type() == NElementTokenType.LBRACE) {
                return withComments(object(null, elements.toArray(new NElement[0])), t.comments);
            }
            if (t.token.type() == NElementTokenType.LBRACK) {
                return withComments(array(null, elements.toArray(new NElement[0])), t.comments);
            }
        }
        return NElement.ofUplet(elements.toArray(new NElement[0]));
    }

    private NListItemElement listItem(boolean ordered, int depth) {
        NElementTokenInfo t = peekToken();
        NElementTokenType tt = ordered ? NElementTokenType.ORDERED_LIST : NElementTokenType.UNORDERED_LIST;
        if (t == null || t.token==null || t.token.type() != tt || t.token.level() <= depth) {
            return null;
        }
        t = nextToken();
        int currentDepth = t.token.level();
        DefaultNListItemElementBuilder b1 = new DefaultNListItemElementBuilder(currentDepth);
        NElementTokenInfo t2 = peekToken();
        if (t2 == null) {
            return b1.build();
        }
        if (t2.token.type() == tt && t2.token.level() > currentDepth) {
            // there is no value;
            NElement li = list(ordered, currentDepth);
            b1.subList((NListElement) li);
        } else {
            NElement p = exprOrPairElement();
            b1.value(p);
            NElementTokenInfo t3 = peekToken();
            if (t3 == null || t3.token == null) {
                return b1.build();
            }
            if (t3.token.type() == tt && t3.token.level() > currentDepth) {
                NElement li = list(ordered, currentDepth);
                b1.subList((NListElement) li);
                return b1.build();
            }
        }
        return b1.build();
    }

    private NElement list(boolean ordered, int depth) {
        NElementTokenInfo t = peekToken();
        if (t == null) {
            return null;
        }
        boolean listIsOrdered = t.token.type() == NElementTokenType.ORDERED_LIST;
        int currentDepth = t.token.level();
        if (ordered != listIsOrdered || currentDepth < depth) {
            return null;
        }
        List<NListItemElement> sub = new ArrayList<>();
        int minDepth = currentDepth;
        while (true) {
            NListItemElement i = listItem(listIsOrdered, depth);
            if (i != null) {
                if(i.depth()<minDepth) {
                    minDepth = i.depth();
                }
                sub.add(i);
            } else {
                break;
            }
        }
        DefaultNListElementBuilder b = new DefaultNListElementBuilder(t.token.type() == NElementTokenType.ORDERED_LIST ? NElementType.ORDERED_LIST : NElementType.UNORDERED_LIST, minDepth);
        for (NListItemElement e : sub) {
            b.addItem(e);
        }
        return b.build();
    }

    private NElement named() {
        NElementTokenInfo nameToken = nextToken();
        String name = nameToken.token.image();
        if (isToken(NElementTokenType.LPAREN)) {
            nextToken();
            List<NElement> params = new ArrayList<>();
            while (!isToken(NElementTokenType.RPAREN)) {
                params.add(exprOrPairElement());
                if (isToken(NElementTokenType.COMMA) || isToken(NElementTokenType.SEMICOLON)) {
                    nextToken();
                } else {
                    break;
                }
            }
            skipToken(NElementTokenType.RPAREN);
            // Check if followed by { or [
            if (isToken(NElementTokenType.LBRACE)) {
                return object(name, params.toArray(new NElement[0])); // Should handle name/params
            } else if (isToken(NElementTokenType.LBRACK)) {
                return array(name, params.toArray(new NElement[0])); // Should handle name/params
            }
            return NElement.ofUplet(name, params.toArray(new NElement[0]));
        }
        return NElement.ofString(name, NElementType.NAME);
    }

    private NElementTokenInfo nextToken() {
        if (current != null) {
            NElementTokenInfo t = current;
            current = null;
            return t;
        }
        NElementTokenInfo ii = new NElementTokenInfo();
        while (true) {
            NElementToken t = lexer.next();
            if (t == null) return ii;
            switch (t.type()) {
                case WHITESPACE: {
                    ii.prefixes.add(t);
                    break;
                }
                case LINE_COMMENT:
                case BLOCK_COMMENT: {
                    ii.prefixes.add(t);
                    ii.comments.add((NElementComment) t.value());
                    break;
                }
                default: {
                    ii.token = t;
                    return ii;
                }
            }
        }
    }

    private NElementTokenInfo peekToken() {
        if (current == null) {
            current = nextToken();
        }
        return current;
    }

    private boolean isToken(NElementTokenType type) {
        NElementTokenInfo t = peekToken();
        return t != null && t.token != null && t.token.type() == type;
    }

    private void skipToken(NElementTokenType type) {
        NElementTokenInfo t = nextToken();
        if (t == null || t.token == null || t.token.type() != type) {
            // Error handling
        }
    }

    private boolean isOp(NElementTokenInfo t) {
        if (t == null) return false;
        return isOp(t.token);
    }

    private boolean isOp(NElementToken t) {
        if (t == null) return false;
        switch (t.type()) {
            case OP:
                return true;
        }
        return false;
    }

    private class NElementTokenInfo {
        NElementToken token;
        List<NElementToken> prefixes = new ArrayList<>();
        List<NElementComment> comments = new ArrayList<>();
    }

}
