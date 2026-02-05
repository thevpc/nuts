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
                return DefaultNElementSeparator.of(x.image());
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
                    if (!all.isEmpty()) {
                        NElement e = all.get(all.size() - 1);
                        NElementBuilder b = e.builder();
                        b.addAffixes(
                                tokensToBoundAffixes(t.prefixes, NAffixAnchor.END)
                        );
                        all.set(all.size() - 1, b.build());
                    } else {
                        all.add(new NDefaultEmptyElement(tokensToBoundAffixes(t.prefixes, NAffixAnchor.START), null));
                    }
                }
                nextToken(); // consume it
                break;
            }
            boolean doBreak = false;
            switch (t.token.type()) {
                case RBRACE:
                case RPAREN:
                case RBRACK:
                case COMMA:
                case SEMICOLON: {
                    doBreak = true;
                    break;
                }
            }
            if (doBreak) {
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
        List<NAffix> affixes = new ArrayList<>();
        List<NElementDiagnostic> diagnostics = new ArrayList<>();
        NElementTokenInfo t = null;
        while (true) {
            t = peekToken();
            if (t == null) {
                break;
            } else {
                if (t.token == null) {
                    nextToken();
                    affixes.addAll(tokensToAffixes(t.prefixes));
                    return new NDefaultEmptyElement(bindAffixes(affixes, NAffixAnchor.START), null);
                } else if (t.token.type() == NElementTokenType.AT) {
                    affixes.addAll(tokensToAffixes(t.prefixes));
                    affixes.add(annotation(diagnostics));
                    diagnostics.clear();
                } else {
                    break;
                }
            }
        }
        if (t == null) {
            return new NDefaultEmptyElement(bindAffixes(affixes, NAffixAnchor.START), null);
        }
        if (t.token == null) {
            return new NDefaultEmptyElement(bindAffixes(affixes, NAffixAnchor.START), null);
        }
        NElement base = null;
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
                    affixes.addAll(tokensToAffixes(t.prefixes));
                    b.addAffixes(bindAffixes(affixes, NAffixAnchor.START));
                    affixes.clear();
                    b.addAffixes(readPostComments());
                    for (NElementDiagnostic diagnostic : diagnostics) {
                        b.addDiagnostic(diagnostic);
                    }
                    base = b.build();
                    break;
                }
                case LBRACE: {
                    base = object(null, null, new ArrayList<>(affixes), new ArrayList<>(), Collections.emptyList(), new ArrayList<>());
                    affixes.clear();
                    break;
                }
                case LBRACK: {
                    base = array(null, null, new ArrayList<>(affixes), new ArrayList<>(), Collections.emptyList(), new ArrayList<>());
                    affixes.clear();
                    break;
                }
                case NAME: {
                    base = named(new ArrayList<>(affixes), new ArrayList<>());
                    affixes.clear();
                    break;
                }
                case LPAREN: {
                    base = unnamed(null, new ArrayList<>(affixes), new ArrayList<>());
                    affixes.clear();
                    break;
                }
                case UNKNOWN: {
                    // TODO: handle error
                    NElementTokenInfo t2 = nextToken();
                    diagnostics.add(new DefaultNElementDiagnostic(t2.token, NMsg.ofC("unexpected token '%s'", t2.token.image())));
                    break;
                }
                case ORDERED_LIST: {
                    base = list(true, 0, new ArrayList<>(affixes), new ArrayList<>());
                    affixes.clear();
                    break;
                }
                case UNORDERED_LIST: {
                    base = list(false, 0, new ArrayList<>(affixes), new ArrayList<>());
                    affixes.clear();
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
                        bindAffixes(affixes, NAffixAnchor.START),
                        diagnostics
                );
            }
            return null;
        }
        // now read suffix nelements
        return base;
    }

    private List<NBoundAffix> readPostComments() {
        NElementTokenInfo e = peekToken();
        if (e == null) {
            return new ArrayList<>();
        } else if (e.token == null) {
            if (e.prefixes.size() > 0) {
                List<NBoundAffix> r = tokensToBoundAffixes(e.prefixes, NAffixAnchor.END);
                nextToken();
                return r;
            }
            nextToken();
            return new ArrayList<>();
        } else {
            List<NElementTokenImpl> accepted = new ArrayList<>();
            List<NElementTokenImpl> prefixes = e.prefixes;
            for (int i = 0; i < prefixes.size(); i++) {
                NElementTokenImpl nt = prefixes.get(i);
                if (nt.type() == NElementTokenType.SPACE || nt.type() == NElementTokenType.BLOCK_COMMENT) {
                    accepted.add(nt);
                } else if (nt.type() == NElementTokenType.LINE_COMMENT) {
                    accepted.add(nt);
                    break;
                } else if (nt.type() == NElementTokenType.NEWLINE) {
                    accepted.add(nt);
                    break;
                } else {
                    accepted.clear();
                    break;
                }
            }
            if (accepted.size() > 0) {
                for (int i = 0; i < accepted.size(); i++) {
                    e.prefixes.remove(0);
                }
            }
            return tokensToBoundAffixes(accepted, NAffixAnchor.END);
        }
    }

    private NElementAnnotation annotation(List<NElementDiagnostic> diagnostics) {
        List<NBoundAffix> boundAffixes = new ArrayList<>();
        NElementTokenInfo atToken = nextToken();
        String name = null;
        List<NElement> params = null;
        if (isToken(NElementTokenType.NAME)) {
            NElementTokenInfo nameToken = nextToken();
            name = nameToken.token.image();
        }
        List<NAffix> beforeLpar = new ArrayList<>();
        List<NAffix> beforeRpar = new ArrayList<>();
        if (isToken(NElementTokenType.LPAREN)) {
            params = new ArrayList<>();
            readEnclosedAndSeparatedElements(NElementTokenType.RPAREN, params, beforeLpar, beforeRpar, diagnostics);
        }
        boundAffixes.addAll(bindAffixes(beforeLpar, NAffixAnchor.PRE_1));
        boundAffixes.addAll(bindAffixes(beforeRpar, NAffixAnchor.PRE_2));
        return new NElementAnnotationImpl(name, params, boundAffixes);
    }

    private List<NBoundAffix> bindAffixes(List<NAffix> affixes, NAffixAnchor anchor) {
        return affixes.stream().map(x -> DefaultNBoundAffix.of(x, anchor)).collect(Collectors.toList());
    }

    private NElement object(String name,
                            List<NElement> params,
                            List<NAffix> pendingAffixTokens, List<NAffix> beforeLparAffixes,
                            List<NAffix> beforeRparAffixes,
                            List<NElementDiagnostic> diagnostics) {

        beforeLparAffixes = new ArrayList<>(beforeLparAffixes);// make a copy because it will be updated
        beforeRparAffixes = new ArrayList<>(beforeRparAffixes);// make a copy because it will be updated
        List<NAffix> startAffixes = new ArrayList<>(pendingAffixTokens);// make a copy because it will be updated
        List<NElement> oelements = new ArrayList<>();
        List<NAffix> beforeLbraceAffixes = new ArrayList<>();
        List<NAffix> beforeRbraceAffixes = new ArrayList<>();
        readEnclosedAndSeparatedElements(NElementTokenType.RBRACE, oelements, beforeLbraceAffixes, beforeRbraceAffixes, diagnostics);

        List<NBoundAffix> boundAffixes = new ArrayList<>();

        if (NBlankable.isBlank(params)) {
            beforeLparAffixes.addAll(beforeRparAffixes);
            beforeRparAffixes.clear();
            beforeLparAffixes.addAll(beforeLbraceAffixes);
            beforeLbraceAffixes.clear();
        }
        if (NBlankable.isBlank(name)) {
            startAffixes.addAll(beforeLparAffixes);
            beforeLparAffixes.clear();
        }

        boundAffixes.addAll(bindAffixes(startAffixes, NAffixAnchor.START));
        boundAffixes.addAll(bindAffixes(beforeLparAffixes, NAffixAnchor.PRE_2));
        boundAffixes.addAll(bindAffixes(beforeRparAffixes, NAffixAnchor.PRE_3));
        boundAffixes.addAll(bindAffixes(beforeLbraceAffixes, NAffixAnchor.PRE_4));
        boundAffixes.addAll(bindAffixes(beforeRbraceAffixes, NAffixAnchor.PRE_5));
        boundAffixes.addAll(readPostComments());
        return new DefaultNObjectElement(
                name, params, oelements, boundAffixes, diagnostics
        );
    }

    private NElement array(String name, List<NElement> params, List<NAffix> pendingAffixTokens, List<NAffix> beforeLparAffixes,
                           List<NAffix> beforeRparAffixes,
                           List<NElementDiagnostic> diagnostics) {

        beforeLparAffixes = new ArrayList<>(beforeLparAffixes);// make a copy because it will be updated
        beforeRparAffixes = new ArrayList<>(beforeRparAffixes);// make a copy because it will be updated
        List<NAffix> startAffixes = new ArrayList<>(pendingAffixTokens);// make a copy because it will be updated
        List<NElement> oelements = new ArrayList<>();
        List<NAffix> beforeLbracketAffixes = new ArrayList<>();
        List<NAffix> beforeRbracketAffixes = new ArrayList<>();
        readEnclosedAndSeparatedElements(NElementTokenType.RBRACK, oelements, beforeLbracketAffixes, beforeRbracketAffixes, diagnostics);

        List<NBoundAffix> boundAffixes = new ArrayList<>();

        if (NBlankable.isBlank(params)) {
            beforeLparAffixes.addAll(beforeRparAffixes);
            beforeRparAffixes.clear();
            beforeLparAffixes.addAll(beforeLbracketAffixes);
            beforeLbracketAffixes.clear();
        }
        if (NBlankable.isBlank(name)) {
            startAffixes.addAll(beforeLparAffixes);
            beforeLparAffixes.clear();
        }

        boundAffixes.addAll(bindAffixes(startAffixes, NAffixAnchor.START));
        boundAffixes.addAll(bindAffixes(beforeLparAffixes, NAffixAnchor.PRE_2));
        boundAffixes.addAll(bindAffixes(beforeRparAffixes, NAffixAnchor.PRE_3));
        boundAffixes.addAll(bindAffixes(beforeLbracketAffixes, NAffixAnchor.PRE_4));
        boundAffixes.addAll(bindAffixes(beforeRbracketAffixes, NAffixAnchor.PRE_5));
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
        itemAffixes.addAll(bindAffixes(pendingAffixTokens, NAffixAnchor.START));
        itemAffixes.addAll(tokensToBoundAffixes(t.prefixes, NAffixAnchor.START));
        if (t2 == null) {
            return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, null, null, itemAffixes);
        }
        if (t2.token.type() == tt && t2.token.level() > currentDepth) {
            // there is no value;
            NElement li = list(ordered, currentDepth, new ArrayList<>(), diagnostics);
            return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, null, (NListElement) li, itemAffixes);
        } else {
            NElement p = exprOrPairElement(new ArrayList<>());
            //b1.value(p);
            NElementTokenInfo t3 = peekToken();
            if (t3 == null || t3.token == null) {
                return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, p, null, itemAffixes);
            }
            if (t3.token.type() == tt && t3.token.level() > currentDepth) {
                NElement li = list(ordered, currentDepth, new ArrayList<>(), diagnostics);
                return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, p, (NListElement) li, itemAffixes);
            }
            return new DefaultNListItemElement(et, t.token.image(), t.token.variant(), currentDepth, p, null, itemAffixes);
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
        b.addAffixes(bindAffixes(pendingAffixTokens, NAffixAnchor.START));
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
                if(seenName==null) {
                    List<NAffix> c = new ArrayList<>();
                    c.addAll(pendingAffixTokens);
                    c.addAll(beforeLparAffixes);
                    return object(seenName, elements, Collections.emptyList(), c, beforeRparAffixes, diagnostics);
                }else{
                    return object(seenName, elements, pendingAffixTokens, beforeLparAffixes, beforeRparAffixes, diagnostics);
                }
            }
            if (t.token.type() == NElementTokenType.LBRACK) {
                if(seenName==null) {
                    List<NAffix> c = new ArrayList<>();
                    c.addAll(pendingAffixTokens);
                    c.addAll(beforeLparAffixes);
                    return array(seenName, elements, Collections.emptyList(), c, beforeRparAffixes, diagnostics);
                }else{
                    return array(seenName, elements, pendingAffixTokens, beforeLparAffixes, beforeRparAffixes, diagnostics);
                }
            }
        }

        List<NBoundAffix> boundAffixes = new ArrayList<>();
        boundAffixes.addAll(pendingAffixTokens.stream().map(x -> DefaultNBoundAffix.of(x, NAffixAnchor.START)).collect(Collectors.toList()));
        boundAffixes.addAll(beforeLparAffixes.stream().map(x -> DefaultNBoundAffix.of(x, NAffixAnchor.START)).collect(Collectors.toList()));
        boundAffixes.addAll(beforeRparAffixes.stream().map(x -> DefaultNBoundAffix.of(x, NAffixAnchor.PRE_3)).collect(Collectors.toList()));
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
            nextToken();
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
                return object(name, null, pendingAffixTokens, new ArrayList<>(), new ArrayList<>(), diagnostics);
            }
            case LBRACK: {
                return array(name, null, pendingAffixTokens, new ArrayList<>(), new ArrayList<>(), diagnostics);
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
