package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TsonFormatSanitizerAction implements NElementFormatterAction {
    private boolean strict;

    public TsonFormatSanitizerAction(boolean strict) {
        this.strict = strict;
    }

    @Override
    public void apply(NElementFormatContext context) {
        NElementBuilder b = context.builder();
        String startAffixes = lastStringOfBoundAffixes(b.affixes(), NAffixAnchor.START);
        switch (b.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case PARAM_OBJECT:
            case FULL_OBJECT: {
                NObjectElementBuilder eb = (NObjectElementBuilder) b;
                List<NElement> nElements = eb.params().orNull();
                if (nElements != null) {
                    for (int i = 1; i < nElements.size(); i++) {
                        if (isCollision(nElements.get(i - 1), nElements.get(i))) {
                            eb.setParamAt(i - 1, nElements.get(i - 1).builder().addSpaceAffix(" ", NAffixAnchor.END).build());
                        }
                    }
                }
                nElements = eb.children();
                for (int i = 1; i < nElements.size(); i++) {
                    if (isCollision(nElements.get(i - 1), nElements.get(i))) {
                        eb.setAt(i - 1, nElements.get(i - 1).builder().addSpaceAffix(" ", NAffixAnchor.END).build());
                    }
                }
                break;
            }
            case ARRAY:
            case NAMED_ARRAY:
            case PARAM_ARRAY:
            case FULL_ARRAY: {
                NArrayElementBuilder eb = (NArrayElementBuilder) b;
                List<NElement> nElements = eb.params().orNull();
                if (nElements != null) {
                    eb.setParams(addSpaces(nElements));
                }
                eb.setChildren(addSpaces(eb.children()));
                break;
            }
            case NAMED_UPLET: {
                NUpletElementBuilder eb = (NUpletElementBuilder) b;
                if (isCollision(startAffixes, eb.name().orNull())) {
                    eb.addSpaceAffix(" ", NAffixAnchor.START);
                }
                List<NElement> nElements = eb.params();
                if (nElements != null) {
                    eb.setParams(addSpaces(nElements));
                }
                break;
            }
            case UPLET: {
                NUpletElementBuilder eb = (NUpletElementBuilder) b;
                List<NElement> nElements = eb.params();
                if (nElements != null) {
                    eb.setParams(addSpaces(nElements));
                }
                break;
            }
            case FLAT_EXPR: {
                NFlatExprElementBuilder eb = (NFlatExprElementBuilder) b;
                eb.setChildren(addSpaces(eb.children()));
                break;
            }
            case UNARY_OPERATOR:
            case BINARY_OPERATOR:
            case TERNARY_OPERATOR:
            case NARY_OPERATOR: {
                NOperatorElementBuilder eb = (NOperatorElementBuilder) b;
                eb.setChildren(addSpaces(eb.children()));
                break;
            }
            case ORDERED_LIST:
            case UNORDERED_LIST: {
                NListElementBuilder eb = (NListElementBuilder) b;
                List<NListItemElement> items = eb.items();
                for (int i = 0; i < items.size(); i++) {
                    NListItemElement currentItem = items.get(i);

                    // --- 1. Internal Junction: Marker vs Content ---
                    // If marker and value/sublist touch, we use the Symbol anchor (POST_1)
                    if (currentItem.marker() != null) {
                        NElement content = currentItem.value().isPresent()
                                ? currentItem.value().get()
                                : currentItem.subList().orNull();

                        if (content != null && isCollision(currentItem.marker(), content)) {
                            // Add space to POST_1 (The anchor for the list symbol/marker)
                            eb.setItemAt(i, currentItem.builder()
                                    .addAffixSpace(" ", NAffixAnchor.POST_1)
                                    .build());
                            currentItem = eb.get(i); // Refresh reference
                        }
                    }

                    // --- 2. External Junction: Between Items ---
                    // Relevant for inline lists where items touch each other
                    if (i > 0) {
                        NListItemElement prevItem = items.get(i - 1);
                        if (isCollision(lastStringOfElement(prevItem), firstStringOfElement(currentItem))) {
                            // Add space to the end of the previous item (POST_2 or END)
                            eb.setItemAt(i - 1, prevItem.builder()
                                    .addAffixSpace(" ", NAffixAnchor.END)
                                    .build());
                        }
                    }
                }
                break;
            }
            default: {
                // do nothing
                return;
            }
        }
    }
    private List<NElement> addSpaces(List<NElement> oldList){
        List<NElement> newList=new ArrayList<>();
        for (int i = 0; i < oldList.size(); i++) {
            if(!newList.isEmpty()){
                NElement last = newList.get(newList.size() - 1);
                if (isCollision(last, oldList.get(i))) {
                    newList.set(newList.size() - 1, last.builder().addSpaceAffix(" ", NAffixAnchor.END).build());
                }
            }
            newList.add(oldList.get(i));
        }
        return newList;
    }

    public static boolean isNameOrDigit(char c) {
        return isName(c) || isDigit(c);
    }

    public static boolean isName(char c) {
        return Character.isUnicodeIdentifierPart(c) || c == '_' || c == '$';
    }

    public static boolean isFirstOrderSep(char c) {
        switch (c) {
            case ' ':
            case '\n':
            case '\r':
            case ',':
            case ';':
            case ']':
            case '}':
            case ')':
            case ':':
                return true;
        }
        if (Character.isWhitespace(c)) {
            return true;
        }
        return false;
    }

    public static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    public static boolean isCloseSep(char c) {
        switch (c) {
            case ')':
            case ']':
            case '}':
                return true;
        }
        return false;
    }

    public static boolean isOpenSep(char c) {
        switch (c) {
            case '(':
            case '[':
            case '{':
                return true;
        }
        return false;
    }

    public static boolean isOp(char c) {
        switch (c) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '^':
            case '#':
            case '.':
            case '&':
            case '?':
            case '|':
            case '!':
            case '\\':
            case '=':
            case '%':
            case '<':
            case '>':
                return true;
        }
        return false;
    }

    private static boolean isQuoteOrLineStr(char c) {
        return c == '\'' || c == '"' || c == '`' || c == '¶';
    }

    private static boolean isQuote(char c) {
        return c == '\'' || c == '"' || c == '`';
    }

    public boolean isCollision(NElement first, NElement second) {
        return collisionTypeToBoolean(resolveCollision(first, second));
    }

    public CollisionType resolveCollision(NElement first, NElement second) {
        if (first == null || second == null) {
            return CollisionType.NO_COLLISION;
        }
        String before = lastStringOfElement(first);
        String after = firstStringOfElement(second);
        return resolveCollision(before, after);
    }

    public boolean isCollision(String first, NElement second) {
        return collisionTypeToBoolean(resolveCollision(first, second));
    }

    public static CollisionType resolveCollision(String first, NElement second) {
        if (first == null || second == null) {
            return CollisionType.NO_COLLISION;
        }
        String before = first;
        String after = firstStringOfElement(second);
        return resolveCollision(before, after);
    }

    public enum CollisionType {
        FATAL,
        UNPRETTY,
        NO_COLLISION
    }

    private boolean collisionTypeToBoolean(CollisionType c) {
        boolean b = strict ? (c == CollisionType.FATAL) : (c != CollisionType.NO_COLLISION);
        if (b) {
            return true;
        }
        return false;
    }

    public boolean isCollision(String before, String after) {
        return collisionTypeToBoolean(resolveCollision(before, after));
    }

    public static CollisionType resolveCollision(String before, String after) {
        if (NStringUtils.isEmpty(before) || NStringUtils.isEmpty(after)) {
            return CollisionType.NO_COLLISION;
        }
        char bc = before.charAt(before.length() - 1);
        char ac = after.charAt(0);
        if (Character.isWhitespace(bc) || Character.isWhitespace(ac)) {
            return CollisionType.NO_COLLISION;
        }
        // If either is a separator (space, comma, brace), no collision possible
        if (isCloseSep(bc) && (isQuote(ac) || isNameOrDigit(ac))) {
            return CollisionType.UNPRETTY;
        }
        if (isFirstOrderSep(ac) || isFirstOrderSep(bc)) return CollisionType.NO_COLLISION;

        // Logic: If both are Alphanumeric or both are Operators, they will glue.
        if (isNameOrDigit(bc) && isNameOrDigit(ac)) {
            return CollisionType.FATAL;
        }
        if (isQuote(bc) && isNameOrDigit(ac)) {
            return CollisionType.UNPRETTY;
        }
        if (isCloseSep(bc) && isNameOrDigit(ac)) {
            return CollisionType.UNPRETTY;
        }
        if (ac == bc) {
            return CollisionType.FATAL;
        }
        if (isOp(bc) && isOp(ac)) {
            return  CollisionType.FATAL;
        }
        if (isQuote(ac) || isQuote(bc)) {
            return CollisionType.UNPRETTY;
        }
        return CollisionType.NO_COLLISION;
    }


    private static String firstStringOfAffixes(List<NBoundAffix> list) {
        if (!list.isEmpty()) {
            NBoundAffix a = list.get(0);
            switch (a.affix().type()) {
                case SPACE: {
                    return ((NElementSpace) a.affix()).value();
                }
                case NEWLINE: {
                    return ((NElementNewLine) a.affix()).value().value();
                }
                case LINE_COMMENT: {
                    //even though is something else doesn't matter
                    return "//";
                }
                case BLOC_COMMENT: {
                    return "/*";
                }
                case SEPARATOR: {
                    return ((NElementSeparator) a.affix()).value();
                }
                case ANNOTATION: {
                    return "@";
                }
            }
        }
        return null;
    }

    public static String firstStringOfElement(NElement e) {
        List<NBoundAffix> list = e.affixes().stream().filter(x -> x.anchor() == NAffixAnchor.START).collect(Collectors.toList());
        String ss = firstStringOfAffixes(list);
        if (ss != null) {
            return ss;
        }
        // there is no affixes
        switch (e.type()) {
            case UPLET:
            case PARAM_OBJECT:
            case PARAM_ARRAY: {
                return "(";
            }
            case NAMED_UPLET: {
                return ((NUpletElement) e).name().get();
            }

            case NAMED_OBJECT:
            case FULL_OBJECT: {
                return ((NObjectElement) e).name().get();
            }
            case OBJECT: {
                return "{";
            }
            case CHAR_STREAM:
            case BINARY_STREAM: {
                return "^";
            }
            case FULL_ARRAY:
            case NAMED_ARRAY: {
                return ((NArrayElement) e).name().get();
            }
            case ARRAY: {
                return "[";
            }
            case PAIR: {
                NElement value = ((NPairElement) e).key();
                return firstStringOfElement(value);
            }
            case FLAT_EXPR: {
                NFlatExprElement e1 = (NFlatExprElement) e;
                if (!e1.children().isEmpty()) {
                    return firstStringOfElement(e1.children().get(0));
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case DOUBLE_QUOTED_STRING: {
                return "\"";
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                return "\"\"\"";
            }
            case CHAR:
            case SINGLE_QUOTED_STRING: {
                return "'";
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                return "'''";
            }
            case BACKTICK_STRING: {
                return "`";
            }
            case TRIPLE_BACKTICK_STRING: {
                return "```";
            }
            case BLOCK_STRING: {
                return "¶¶";
            }
            case LINE_STRING: {
                return "¶";
            }
            case UNARY_OPERATOR: {
                NUnaryOperatorElement e1 = (NUnaryOperatorElement) e;
                switch (e1.position()) {
                    case PREFIX: {
                        return e1.operatorSymbol().lexeme();
                    }
                    case POSTFIX: {
                        String s = firstStringOfElement(e1.operand());
                        if (!NBlankable.isBlank(s)) {
                            return s;
                        }
                        return e1.operatorSymbol().lexeme();
                    }
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case BINARY_OPERATOR: {
                NBinaryOperatorElement e1 = (NBinaryOperatorElement) e;
                switch (e1.position()) {
                    case PREFIX: {
                        return e1.operatorSymbol().lexeme();
                    }
                    case POSTFIX: {
                        String s = firstStringOfElement(e1.firstOperand());
                        if (s != null) {
                            return s;
                        }
                        s = firstStringOfElement(e1.secondOperand());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol().lexeme();
                    }
                    case INFIX: {
                        String s = firstStringOfElement(e1.firstOperand());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol().lexeme();
                    }
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case TERNARY_OPERATOR:
            case NARY_OPERATOR: {
                NOperatorElement e1 = (NOperatorElement) e;
                switch (e1.position()) {
                    case PREFIX: {
                        return e1.operatorSymbol(0).get().lexeme();
                    }
                    case INFIX: {
                        String s = firstStringOfElement(e1.operand(0).get());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol(e1.operatorSymbols().size() - 1).get().lexeme();
                    }
                    case POSTFIX: {
                        String s = firstStringOfElement(e1.operand(0).get());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol(0).get().lexeme();
                    }
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case OPERATOR_SYMBOL: {
                return ((NOperatorSymbolElement) e).symbol().lexeme();
            }
            case ORDERED_LIST:
            case UNORDERED_LIST: {
                NListElement e1 = (NListElement) e;
                return e1.items().get(0).marker();
            }
            case BIG_COMPLEX:
            case BIG_INT:
            case BYTE:
            case NULL:
            case BOOLEAN:
            case INT:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME:
            case INSTANT:
            case LONG:
            case DOUBLE_COMPLEX:
            case FLOAT:
            case SHORT:
            case UBYTE:
            case DOUBLE:
            case USHORT:
            case FLOAT_COMPLEX:
            case ULONG:
            case EMPTY:
            case UINT:
            case BIG_DECIMAL:
            case CUSTOM:
            case NAME:
            case FRAGMENT:
            {
                return DefaultTsonWriter.formatTson(e);
            }
        }
        return DefaultTsonWriter.formatTson(e);
    }

    public static String firstStringOfElement(NListItemElement e) {
        return e.marker();
    }

    public static String lastStringOfElement(NListItemElement i) {
        if (i.subList().isPresent()) {
            return lastStringOfElement(i.subList().get());
        }
        if (i.value().isPresent()) {
            return lastStringOfElement(i.value().get());
        }
        //after list item symbol
        String s = lastStringOfBoundAffixes(i.affixes(), NAffixAnchor.POST_1);
        if (!NStringUtils.isEmpty(s)) {
            return s;
        }
        return i.marker();
    }

    public static String lastStringOfBoundAffixes(List<NBoundAffix> affixes, NAffixAnchor anchor) {
        for (int i = affixes.size() - 1; i >= 0; i--) {
            NBoundAffix a = affixes.get(i);
            if (anchor == null || a.anchor() == anchor) {
                String u = lastStringOfAffix(a.affix());
                if (!NStringUtils.isEmpty(u)) {
                    return u;
                }
            }
        }
        return null;
    }


    public static String lastStringOfAffix(NAffix e) {
        switch (e.type()) {
            case SPACE:
                return ((NElementSpace) e).value();
            case NEWLINE:
                return ((NElementNewLine) e).value().value();
            case SEPARATOR:
                return ((NElementSeparator) e).value();
            case LINE_COMMENT:
                return "\n";
            case BLOC_COMMENT:
                return "*/";
            case ANNOTATION: {
                NElementAnnotation ea = (NElementAnnotation) e;
                if (ea.affixes() != null) {
                    String s = lastStringOfBoundAffixes(ea.affixes(), NAffixAnchor.END);
                    if (!NStringUtils.isEmpty(s)) {
                        return s;
                    }
                }
                List<NElement> p = ea.params().orNull();
                if (p != null) {
                    return ")";
                }
                if (!ea.name().isEmpty()) {
                    return ea.name().substring(ea.name().length() - 2, ea.name().length() - 1);
                }
                return "@";
            }
        }
        return null;
    }

    public static String lastStringOfElement(NElement e) {
        String ss = lastStringOfBoundAffixes(e.affixes(), NAffixAnchor.END);
        if (!NStringUtils.isEmpty(ss)) {
            return ss;
        }
        // there is no affixes
        switch (e.type()) {
            case NAMED_UPLET:
            case UPLET: {
                return ")";
            }
            case FULL_OBJECT:
            case PARAM_OBJECT:
            case NAMED_OBJECT:
            case OBJECT:
            case CHAR_STREAM: {
                return "}";
            }
            case FULL_ARRAY:
            case PARAM_ARRAY:
            case NAMED_ARRAY:
            case ARRAY:
            case BINARY_STREAM: {
                return "]";
            }
            case PAIR: {
                NElement value = ((NPairElement) e).value();
                return lastStringOfElement(value);
            }
            case FLAT_EXPR: {
                NFlatExprElement e1 = (NFlatExprElement) e;
                if (!e1.children().isEmpty()) {
                    return lastStringOfElement(e1.children().get(e1.children().size() - 1));
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case DOUBLE_QUOTED_STRING: {
                return "\"";
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                return "\"\"\"";
            }
            case CHAR:
            case SINGLE_QUOTED_STRING: {
                return "'";
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                return "'''";
            }
            case BACKTICK_STRING: {
                return "`";
            }
            case TRIPLE_BACKTICK_STRING: {
                return "```";
            }
            case BLOCK_STRING:
            case LINE_STRING: {
                return "\n";
            }
            case UNARY_OPERATOR: {
                NUnaryOperatorElement e1 = (NUnaryOperatorElement) e;
                switch (e1.position()) {
                    case PREFIX: {
                        String s = lastStringOfElement(e1.operand());
                        if (!NBlankable.isBlank(s)) {
                            return s;
                        }
                        return e1.operatorSymbol().lexeme();
                    }
                    case POSTFIX: {
                        return e1.operatorSymbol().lexeme();
                    }
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case BINARY_OPERATOR: {
                NBinaryOperatorElement e1 = (NBinaryOperatorElement) e;
                switch (e1.position()) {
                    case PREFIX: {
                        String s = lastStringOfElement(e1.secondOperand());
                        if (s != null) {
                            return s;
                        }
                        s = lastStringOfElement(e1.firstOperand());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol().lexeme();
                    }
                    case POSTFIX: {
                        return e1.operatorSymbol().lexeme();
                    }
                    case INFIX: {
                        String s = lastStringOfElement(e1.secondOperand());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol().lexeme();
                    }
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case TERNARY_OPERATOR:
            case NARY_OPERATOR: {
                NOperatorElement e1 = (NOperatorElement) e;
                switch (e1.position()) {
                    case POSTFIX: {
                        return e1.operatorSymbol(e1.operatorSymbols().size() - 1).get().lexeme();
                    }
                    case INFIX: {
                        String s = lastStringOfElement(e1.operand(e1.operands().size() - 1).get());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol(e1.operatorSymbols().size() - 1).get().lexeme();
                    }
                    case PREFIX: {
                        String s = lastStringOfElement(e1.operand(e1.operands().size() - 1).get());
                        if (s != null) {
                            return s;
                        }
                        return e1.operatorSymbol(e1.operatorSymbols().size() - 1).get().lexeme();
                    }
                }
                return DefaultTsonWriter.formatTson(e);
            }
            case OPERATOR_SYMBOL: {
                return ((NOperatorSymbolElement) e).symbol().lexeme();
            }
            case ORDERED_LIST:
            case UNORDERED_LIST: {
                NListElement e1 = (NListElement) e;
                NListItemElement i = e1.items().get(e1.size() - 1);
                if (i.subList().isPresent()) {
                    return lastStringOfElement(i.subList().get());
                }
                if (i.value().isPresent()) {
                    return lastStringOfElement(i.value().get());
                }
                //after list item symbol
                String s = lastStringOfBoundAffixes(i.affixes(), NAffixAnchor.POST_1);
                if (!NStringUtils.isEmpty(s)) {
                    return s;
                }
                return i.marker();
            }
            case BIG_COMPLEX:
            case BIG_INT:
            case BYTE:
            case NULL:
            case BOOLEAN:
            case INT:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME:
            case INSTANT:
            case LONG:
            case DOUBLE_COMPLEX:
            case FLOAT:
            case SHORT:
            case UBYTE:
            case DOUBLE:
            case USHORT:
            case FLOAT_COMPLEX:
            case ULONG:
            case EMPTY:
            case UINT:
            case BIG_DECIMAL:
            case CUSTOM:
            case NAME:
            case FRAGMENT:
            {
                return DefaultTsonWriter.formatTson(e);
            }
        }
        return DefaultTsonWriter.formatTson(e);
    }
}
