package net.thevpc.nuts.runtime.standalone.format.tson.parser.custom;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNListElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.*;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.DefaultNBufferedGenerator;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NBufferedGenerator;
import net.thevpc.nuts.util.NUnexpectedException;

public class TsonCustomParser {
    private NBufferedGenerator<NElementTokenImpl> lexer;
    private Object source;
    private NElementTokenInfo current;


    public TsonCustomParser(String reader) {
        this(new TsonCustomLexer(reader));
    }

    public TsonCustomParser(Reader reader) {
        this(new TsonCustomLexer(reader));
    }

    public TsonCustomParser(TsonCustomLexer lexer) {
        this.lexer = new DefaultNBufferedGenerator<>(lexer);
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
            NElement e = parseElement(new ArrayList<>());
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
        return parseElement(new ArrayList<>());
    }

    public NElement parseElement(List<NAffix> pendingAffixTokens) {
        return exprOrPairElement(pendingAffixTokens);
    }

    private NElement exprOrPairElement(List<NAffix> pendingAffixTokens) {
        pendingAffixTokens = new ArrayList<>(pendingAffixTokens);
        NElement first = exprElement(pendingAffixTokens);
        if (first == null) {
            return null;
        }
        pendingAffixTokens = new ArrayList<>();
        NElementTokenInfo t = peekToken();
        if (t != null && t.token != null && t.token.type() == NElementTokenType.COLON) {
            nextToken();
            NElement second = exprElement(pendingAffixTokens);
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

    private NBoundAffix tokenToBoundAffix(NElementTokenImpl x, NAffixAnchor anchor) {
        return DefaultNBoundAffix.of(tokenToAffix(x), anchor);
    }

    private List<NAffix> tokenToAffixes(NElementTokenInfo token) {
        return token.prefixes.stream().map(this::tokenToAffix).collect(Collectors.toList());
    }

    private NAffix tokenToAffix(NElementTokenImpl x) {
        switch (x.type()) {
            case SPACE:
                return DefaultNElementSpace.of(x.image());
            case NEWLINE:
                return DefaultNElementNewLine.of(x.image());
            case BLOCK_COMMENT: {
                return new NElementCommentImpl(NAffixType.BLOC_COMMENT, x.image(), x.image());
            }
            case LINE_COMMENT: {
                return new NElementCommentImpl(NAffixType.LINE_COMMENT, x.image(), x.image());
            }
            case COMMA:
            case SEMICOLON2:
                return new NElementCommentImpl(NAffixType.SEPARATOR, x.image(), x.image());
        }
        throw new NUnexpectedException(NMsg.ofC("unexpected token type"));
    }

    private List<NBoundAffix> tokensToBoundAffixes(List<NElementTokenImpl> prefixes, NAffixAnchor anchor) {
        return prefixes.stream().map(x -> tokenToBoundAffix(x, anchor)).collect(Collectors.toList());
    }

    private List<NAffix> tokensToAffixes(List<NElementTokenImpl> prefixes) {
        return prefixes.stream().map(x -> tokenToAffix(x)).collect(Collectors.toList());
    }

    private NElement exprElement(List<NAffix> pendingAffixTokens) {
        // shall read a list of element primitiveElement operator but never two successive elements
        List<NElement> all = new ArrayList<>();
        boolean wasOp = false;
        while (true) {
            NElementTokenInfo t = peekToken();
            if (t == null) {
                break;
            }
            if (t.token == null) {
                //this is comment but no next element
                if (!t.prefixes.isEmpty()) {
                    all.add(new NDefaultEmptyElement(tokensToBoundAffixes(t.prefixes, NAffixAnchor.START), null));
                }
                nextToken(); // consume it
                break;
            }
            if (all.isEmpty()) {
                if (isOp(t)) {
                    nextToken();
                    NElement e = new DefaultNOperatorSymbolElement(NOperatorSymbol.parse(t.token.image()).get(),
                            tokensToBoundAffixes(t.prefixes, NAffixAnchor.START), null
                    );
                    all.add(e);
                    wasOp = true;
                } else {
                    NElement operand = primitiveElement();
                    if (operand != null) {
                        all.add(operand);
                        wasOp = false;
                    } else {
                        break;
                    }
                }
            } else {
                if (isOp(t)) {
                    nextToken();
                    NElement e = new DefaultNOperatorSymbolElement(NOperatorSymbol.parse(t.token.image()).get(),
                            tokensToBoundAffixes(t.prefixes, NAffixAnchor.START), null
                    );
                    all.add(e);
                    wasOp = true;
                } else {
                    if (wasOp) {
                        NElement operand = primitiveElement();
                        if (operand != null) {
                            all.add(operand);
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
        NFlatExprElementBuilder b = NFlatExprElementBuilder.of();
        for (NElement nElement : all) {
            b.add(nElement);
        }
        return b.build();
    }

    private NElement primitiveElement() {
        List<NBoundAffix> affixes = new ArrayList<>();
        List<NElementDiagnostic> diagnostics = new ArrayList<>();
        while (true) {
            NElementTokenInfo t = peekToken();
            if (t == null) {
                break;
            } else {
                affixes.addAll(tokensToBoundAffixes(t.prefixes, NAffixAnchor.START));
                if (t.token == null) {
                    nextToken();
                    return new NDefaultEmptyElement(tokensToBoundAffixes(t.prefixes, NAffixAnchor.START), null);
                } else if (t.token.type() == NElementTokenType.AT) {
                    affixes.add(DefaultNBoundAffix.of(annotation(diagnostics), NAffixAnchor.START));
                } else {
                    break;
                }
            }
        }
        NElementTokenInfo t = peekToken();
        if (t == null) {
            return new NDefaultEmptyElement(affixes, null);
        }
        if (t.token == null) {
            affixes.addAll(tokensToBoundAffixes(t.prefixes, NAffixAnchor.START));
            return new NDefaultEmptyElement(affixes, null);
        }

        NElement base = null;
//        List<NElementTokenInfo> unexpectedTokens = new ArrayList<>();
        while (true) {
            base = null;
            t = peekToken();
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
                case BINARY_STREAM: {
                    nextToken();
                    base = (NElement) t.token.value();
                    NElementBuilder b = base.builder();
                    b.addAffixes(tokensToBoundAffixes(t.prefixes, NAffixAnchor.START));
                    b.addAffixes(readPostComments());
                    for (NElementDiagnostic diagnostic : diagnostics) {
                        b.addDiagnostic(diagnostic);
                    }
                    base = b.build();
                    break;
                }
                case LBRACE:
                    base = object(null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new ArrayList<>());
                    break;
                case LBRACK:
                    base = array(null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new ArrayList<>());
                    break;
                case NAME:
                    base = named(new ArrayList<>(), new ArrayList<>());
                    break;
                case LPAREN:
                    base = unnamed(null, new ArrayList<>(), new ArrayList<>());
                    break;
                case UNKNOWN: {
                    // TODO: handle error
                    // just ignore for now
                    NElementTokenInfo t2 = nextToken();
                    diagnostics.add(new DefaultNElementDiagnostic(t2.token, NMsg.ofC("unexpected token '%s'", t2.token.image())));
                    break;
                }
                case ORDERED_LIST: {
                    base = list(true, 0, new ArrayList<>(), new ArrayList<>());
                    break;
                }
                case UNORDERED_LIST: {
                    base = list(false, 0, new ArrayList<>(), new ArrayList<>());
                    break;
                }
                default: {
                    // TODO: handle error
                    // just ignore for now
                    NElementTokenInfo t2 = nextToken();
                    diagnostics.add(new DefaultNElementDiagnostic(t2.token, NMsg.ofC("unexpected token '%s'", t2.token.image())));
                    break;
                }
            }
            if (base != null) {
                break;
            }
        }

        if (base == null) {
            if (
                    !diagnostics.isEmpty()
                            || !affixes.isEmpty()
            ) {
                return new NDefaultEmptyElement(
                        affixes,
                        diagnostics
                );
            }
            return null;
        }
        // now read suffix nelements
        return base;
    }

    private List<NBoundAffix> readPostComments() {
        int x = 0;
        List<NElementTokenImpl> accepted = new ArrayList<>();
        while (true) {
            NElementTokenImpl e = lexer.peekAt(x);
            if (e == null) {
                break;
            }
            if (e.type() == NElementTokenType.SPACE || e.type() == NElementTokenType.BLOCK_COMMENT) {
                accepted.add(e);
                x++;
            } else if (e.type() == NElementTokenType.LINE_COMMENT) {
                accepted.add(e);
                break;
            } else if (e.type() == NElementTokenType.NEWLINE) {
                accepted.add(e);
                x++;
                break;
            } else {
                accepted.clear();
                break;
            }
        }
        for (NElementTokenImpl e : accepted) {
            lexer.next();
        }
        return tokensToBoundAffixes(accepted, NAffixAnchor.END);
    }

    private NElementAnnotation annotation(List<NElementDiagnostic> diagnostics) {
        List<NBoundAffix> boundAffixes = new ArrayList<>();
        nextToken(); // @
        NElementTokenInfo nameToken = nextToken();
        String name = nameToken == null || nameToken.token == null ? null : nameToken.token.image();
        List<NElement> params = new ArrayList<>();
        if (isToken(NElementTokenType.LPAREN)) {
            nextToken();
            while (!isToken(NElementTokenType.RPAREN)) {
                NElement e = exprOrPairElement(new ArrayList<>());
                if (e == null) {
                    diagnostics.add(new DefaultNElementDiagnostic(null, NMsg.ofC("missing annotation ')'")));
                    break;
                }
                params.add(e);
            }
            skipToken(NElementTokenType.RPAREN);
        }
        return new NElementAnnotationImpl(name, params, boundAffixes);
    }

    private List<NBoundAffix> bindAffixes(List<NAffix> affixes, NAffixAnchor anchor) {
        return affixes.stream().map(x -> DefaultNBoundAffix.of(x, anchor)).collect(Collectors.toList());
    }

    private NElement object(String name,
                            List<NElement> params,
                            List<NAffix> beforeLparAffixes,
                            List<NAffix> beforeRparAffixes,
                            List<NAffix> pendingAffixTokens,
                            List<NElementDiagnostic> diagnostics) {

        List<NElement> oelements = new ArrayList<>();
        List<NAffix> obeforeLparAffixes = new ArrayList<>();
        List<NAffix> obeforeRparAffixes = new ArrayList<>();
        readEnclosedAndSeparatedElements(NElementTokenType.RBRACE, oelements, obeforeLparAffixes, obeforeRparAffixes, diagnostics);

        List<NBoundAffix> boundAffixes = new ArrayList<>();

        boundAffixes.addAll(bindAffixes(pendingAffixTokens, NAffixAnchor.START));
        if (NBlankable.isBlank(name)) {
            boundAffixes.addAll(bindAffixes(beforeLparAffixes, NAffixAnchor.START));
        } else {
            boundAffixes.addAll(bindAffixes(beforeLparAffixes, NAffixAnchor.PRE_1));
        }
        boundAffixes.addAll(bindAffixes(beforeRparAffixes, NAffixAnchor.POST_1));
        boundAffixes.addAll(bindAffixes(obeforeLparAffixes, NAffixAnchor.PRE_2));
        boundAffixes.addAll(bindAffixes(obeforeRparAffixes, NAffixAnchor.POST_2));
        boundAffixes.addAll(readPostComments());
        return new DefaultNObjectElement(
                name, params, oelements, boundAffixes, diagnostics
        );
    }

    private NElement array(String name, List<NElement> params, List<NAffix> beforeLparAffixes,
                           List<NAffix> beforeRparAffixes, List<NAffix> pendingAffixTokens,
                           List<NElementDiagnostic> diagnostics) {

        List<NElement> oelements = new ArrayList<>();
        List<NAffix> obeforeLparAffixes = new ArrayList<>();
        List<NAffix> obeforeRparAffixes = new ArrayList<>();
        readEnclosedAndSeparatedElements(NElementTokenType.LBRACK, oelements, obeforeLparAffixes, obeforeRparAffixes, diagnostics);

        List<NBoundAffix> boundAffixes = new ArrayList<>();

        boundAffixes.addAll(bindAffixes(pendingAffixTokens, NAffixAnchor.START));
        if (NBlankable.isBlank(name)) {
            boundAffixes.addAll(bindAffixes(beforeLparAffixes, NAffixAnchor.START));
        } else {
            boundAffixes.addAll(bindAffixes(beforeLparAffixes, NAffixAnchor.PRE_1));
        }
        boundAffixes.addAll(bindAffixes(beforeRparAffixes, NAffixAnchor.POST_1));
        boundAffixes.addAll(bindAffixes(obeforeLparAffixes, NAffixAnchor.PRE_2));
        boundAffixes.addAll(bindAffixes(obeforeRparAffixes, NAffixAnchor.POST_2));
        boundAffixes.addAll(readPostComments());
        return new DefaultNArrayElement(
                name, params, oelements, boundAffixes, diagnostics
        );
    }

    private <T> List<T> copyAndClear(List<T> other) {
        if (other.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(other);
        other.clear();
        return result;
    }

    private void readEnclosedAndSeparatedElements(NElementTokenType stopToken, List<NElement> elements, List<NAffix> beforeLparAffixes
            , List<NAffix> beforeRparAffixes, List<NElementDiagnostic> diagnostics) {
        NElementTokenInfo lpar = nextToken();
        beforeLparAffixes.addAll(tokensToAffixes(lpar.prefixes));
        List<NAffix> pendingCommaSeparators = new ArrayList<>();
        while (true) {
            NElementTokenInfo p = peekToken();
            if (p == null) {
                diagnostics.add(new DefaultNElementDiagnostic(null, NMsg.ofC("missing " + stopToken.id())));
                break;
            }
            if (p.token == null) {
                diagnostics.add(new DefaultNElementDiagnostic(null, NMsg.ofC("missing " + stopToken.id())));
                pendingCommaSeparators.addAll(tokenToAffixes(p));
                break;
            }
            if (isToken(stopToken)) {
                break;
            }
            elements.add(exprOrPairElement(copyAndClear(pendingCommaSeparators)));
            if (isToken(NElementTokenType.COMMA) || isToken(NElementTokenType.SEMICOLON)) {
                NElementTokenInfo t = nextToken();
                pendingCommaSeparators.addAll(tokenToAffixes(t));
            }
        }
        NElementTokenInfo rpar = nextToken();
        beforeRparAffixes.addAll(copyAndClear(pendingCommaSeparators));
        beforeRparAffixes.addAll(tokenToAffixes(rpar));
    }

    private NListItemElement listItem(boolean ordered, int depth, List<NAffix> pendingAffixTokens, List<NElementDiagnostic> diagnostics) {
        NElementTokenInfo t = peekToken();
        NElementTokenType tt = ordered ? NElementTokenType.ORDERED_LIST : NElementTokenType.UNORDERED_LIST;
        if (t == null || t.token == null || t.token.type() != tt || t.token.level() <= depth) {
            return null;
        }
        t = nextToken();
        NElementType et = ordered ? NElementType.ORDERED_LIST : NElementType.UNORDERED_LIST;
        int currentDepth = t.token.level();
        NElementTokenInfo t2 = peekToken();
        List<NBoundAffix> itemAffixes = new ArrayList<>();
        itemAffixes.addAll(tokensToBoundAffixes(t.prefixes,NAffixAnchor.START));
        if (t2 == null) {
            return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, null, null,itemAffixes);
        }
        if (t2.token.type() == tt && t2.token.level() > currentDepth) {
            // there is no value;
            NElement li = list(ordered, currentDepth, new ArrayList<>(), diagnostics);
            return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, null, (NListElement) li,itemAffixes);
        } else {
            NElement p = exprOrPairElement(new ArrayList<>());
            //b1.value(p);
            NElementTokenInfo t3 = peekToken();
            if (t3 == null || t3.token == null) {
                return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, p, null,itemAffixes);
            }
            if (t3.token.type() == tt && t3.token.level() > currentDepth) {
                NElement li = list(ordered, currentDepth, new ArrayList<>(), diagnostics);
                return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, p, (NListElement) li,itemAffixes);
            }
            return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, p, null,itemAffixes);
        }
    }

    private NElement list(boolean ordered, int depth, List<NAffix> pendingAffixTokens, List<NElementDiagnostic> diagnostics) {
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
            NListItemElement i = listItem(listIsOrdered, depth, new ArrayList<>(), diagnostics);
            if (i != null) {
                if (i.depth() < minDepth) {
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

    private NElement unnamed(String seenName, List<NAffix> pendingAffixTokens, List<NElementDiagnostic> diagnostics) {
        List<NElement> elements = new ArrayList<>();
        List<NAffix> beforeLparAffixes = new ArrayList<>();
        List<NAffix> beforeRparAffixes = new ArrayList<>();
        readEnclosedAndSeparatedElements(NElementTokenType.RPAREN, elements, beforeLparAffixes, beforeRparAffixes, diagnostics);

        NElementTokenInfo t = peekToken();
        if (t != null && t.token != null) {
            if (t.token.type() == NElementTokenType.LBRACE) {
                List<NAffix> c = new ArrayList<>();
                c.addAll(pendingAffixTokens);
                c.addAll(beforeLparAffixes);
                return object(null, elements, c, beforeRparAffixes, Collections.emptyList(), diagnostics);
            }
            if (t.token.type() == NElementTokenType.LBRACK) {
                List<NAffix> c = new ArrayList<>();
                c.addAll(pendingAffixTokens);
                c.addAll(beforeLparAffixes);
                return array(null, elements, c, beforeRparAffixes, Collections.emptyList(), diagnostics);
            }
        }

        List<NBoundAffix> boundAffixes = new ArrayList<>();
        boundAffixes.addAll(pendingAffixTokens.stream().map(x -> DefaultNBoundAffix.of(x, NAffixAnchor.START)).collect(Collectors.toList()));
        boundAffixes.addAll(beforeRparAffixes.stream().map(x -> DefaultNBoundAffix.of(x, NAffixAnchor.POST_2)).collect(Collectors.toList()));
        boundAffixes.addAll(readPostComments());
        return new DefaultNUpletElement(
                seenName, elements,
                boundAffixes, diagnostics
        );
    }

    private NElement named(List<NAffix> pendingAffixTokens, List<NElementDiagnostic> diagnostics) {
        NElementTokenInfo nameToken = nextToken();
        String name = nameToken.token.image();
        pendingAffixTokens.addAll(tokensToAffixes(nameToken.prefixes));
        NElementTokenInfo t = peekToken();
        if (t == null) {
            return new DefaultNStringElement(
                    NElementType.NAME,
                    name,
                    name,
                    bindAffixes(pendingAffixTokens, NAffixAnchor.START),
                    diagnostics
            );
        }
        if (t.token == null) {
            List<NBoundAffix> boundAffixes = new ArrayList<>();
            boundAffixes.addAll(bindAffixes(pendingAffixTokens, NAffixAnchor.START));

            boundAffixes.addAll(tokensToBoundAffixes(t.prefixes, NAffixAnchor.END));
            return new DefaultNStringElement(
                    NElementType.NAME,
                    name,
                    name,
                    boundAffixes,
                    diagnostics
            );
        }

        switch (t.token.type()) {
            case LPAREN: {
                return unnamed(name, pendingAffixTokens, diagnostics);
            }
            case LBRACE: {
                return object(name, null, new ArrayList<>(), new ArrayList<>(), pendingAffixTokens, diagnostics);
            }
            case LBRACK: {
                return array(name, null, new ArrayList<>(), new ArrayList<>(), pendingAffixTokens, diagnostics);
            }
        }

        List<NBoundAffix> boundAffixes = new ArrayList<>(bindAffixes(pendingAffixTokens, NAffixAnchor.START));
        boundAffixes.addAll(readPostComments());
        return new DefaultNStringElement(
                NElementType.NAME,
                name,
                name,
                boundAffixes,
                diagnostics
        );
    }

    private NElementTokenInfo nextToken() {
        if (current != null) {
            NElementTokenInfo t = current;
            current = null;
            return t;
        }
        NElementTokenInfo ii = new NElementTokenInfo();
        while (true) {
            NElementTokenImpl t = lexer.next();
            if (t == null) return ii;
            switch (t.type()) {
                case SPACE:
                case NEWLINE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case COMMA:
                case SEMICOLON: {
                    ii.prefixes.add(t);
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

    private boolean isOp(NElementTokenImpl t) {
        if (t == null) return false;
        switch (t.type()) {
            case OP:
                return true;
        }
        return false;
    }

    private class NElementTokenInfo {
        NElementTokenImpl token;
        List<NElementTokenImpl> prefixes = new ArrayList<>();
//        List<NElementComment> comments = new ArrayList<>();
    }

}
