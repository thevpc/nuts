package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.text.NTreeVisitResult;

import java.util.IdentityHashMap;
import java.util.Map;

public class TsonFormatPrettyAction implements NElementFormatterAction {
    @Override
    public NElementFormatContext prepareChildContext(NElement parent, NElementFormatContext childContext) {
        NElementFormatOptions options = childContext.options();
        switch (parent.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case FULL_OBJECT:
            case PARAM_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case FULL_ARRAY:
            case PARAM_ARRAY: {
                Stats score = calculateStats(parent, childContext); // Peek at the result
                if (score.isComplex(options)) {
                    return childContext.withIndent(childContext.indent() + getIndentUnit());
                }
                break;
            }
        }
        return childContext;
    }

    public String getIndentUnit() {
        return "  ";
    }

    public void apply(NElementFormatContext context) {
        NElementBuilder builder = context.builder();
        builder.removeAffixIf(x ->
                x.affix().type() == NAffixType.SPACE
                        || x.affix().type() == NAffixType.NEWLINE
        );
        NElement element = builder.build();
        Stats score = calculateStats(element, context); // Peek at the result
        switch (builder.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case FULL_OBJECT:
            case PARAM_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case FULL_ARRAY:
            case PARAM_ARRAY: {
                applyObjectOrArray(builder, score, context);
                return;
            }
            case NAMED_UPLET:
            case UPLET: {
                applyUplet(builder, score, context);
                return;
            }
            case PAIR: {
                applyPair((NPairElementBuilder) builder, score, context);
                return;
            }
        }
        applyDefault(builder, score, context);
    }

    private static class Stats {
        int score;
        int charSize;

        public boolean isComplex(NElementFormatOptions options) {
            return score >= options.getComplexityThreshold()
                    || charSize >= options.getColumnLimit();
        }
    }

    private int calculateLineWidth(NElement element) {
        return element.toString().length();
    }

    private void applyPair(NPairElementBuilder builder, Stats score, NElementFormatContext context) {
        NElementFormatOptions options = context.options();
        builder.addSpaceAffix(" ", NAffixAnchor.PRE_1);
        builder.addSpaceAffix(" ", NAffixAnchor.POST_1);
        String indent = context.indent();
//        if (score.isComplex(options)) {
//            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.POST_1);
//            builder.addAffixSpace(indent + getIndentUnit(), NAffixAnchor.POST_1);
//        }
        applyDefault(builder, score, context);

    }

    private void applyObjectOrArray(NElementBuilder builder, Stats score, NElementFormatContext context) {
        String indent = context.indent();
        NElementFormatOptions options = context.options();
        String unit = getIndentUnit();
        if (score.isComplex(options)) {
            // Clear old affixes if necessary, or just set them cleanly:

            // 1. After { or [
            builder.addNewLineAffix(options.getNewLineMode(), NAffixAnchor.POST_4);
            builder.addSpaceAffix(indent + unit, NAffixAnchor.POST_4);

            // 2. Separators: The comma goes in SEP_1, the NewLine in SEP_2
            builder.addSeparatorAffix(",", NAffixAnchor.SEP_1);
            builder.addSpaceAffix(" ", NAffixAnchor.SEP_1); // Optional trailing space after comma

            builder.addSpaceAffix(" ", NAffixAnchor.SEP_2);
            builder.addSeparatorAffix(",", NAffixAnchor.SEP_2);
            builder.addNewLineAffix(options.getNewLineMode(), NAffixAnchor.SEP_2);
            builder.addSpaceAffix(indent + unit, NAffixAnchor.SEP_2);

            // 3. Before } or ]
            builder.addNewLineAffix(options.getNewLineMode(), NAffixAnchor.PRE_5);
            builder.addSpaceAffix(indent, NAffixAnchor.PRE_5);
        } else {
            // Simple mode: One-liner with single spaces
//            builder.addAffixSpace(" ", NAffixAnchor.POST_4);

            builder.addSeparatorAffix(",", NAffixAnchor.SEP_1);
            builder.addSpaceAffix(" ", NAffixAnchor.SEP_1);

            builder.addSeparatorAffix(",", NAffixAnchor.SEP_2);
            builder.addSpaceAffix(" ", NAffixAnchor.SEP_2);

//            builder.addAffixSpace(" ", NAffixAnchor.PRE_5);
        }
    }

    private void applyUplet(NElementBuilder builder, Stats score, NElementFormatContext context) {
        String indent = context.indent();
        NElementFormatOptions options = context.options();
        String unit = getIndentUnit();
        if (score.isComplex(options)) {
            // Clear old affixes if necessary, or just set them cleanly:

            // 1. After { or [
            builder.addNewLineAffix(options.getNewLineMode(), NAffixAnchor.POST_2);
            builder.addSpaceAffix(indent + unit, NAffixAnchor.POST_2);

            // 2. Separators: The comma goes in SEP_1, the NewLine in SEP_2
            builder.addSeparatorAffix(",", NAffixAnchor.SEP_1);
            builder.addSpaceAffix(" ", NAffixAnchor.SEP_1); // Optional trailing space after comma

            // 3. Before } or ]
            builder.addNewLineAffix(options.getNewLineMode(), NAffixAnchor.PRE_3);
            builder.addSpaceAffix(indent, NAffixAnchor.PRE_3);
        } else {
            // Simple mode: One-liner with single spaces
            builder.addSeparatorAffix(",", NAffixAnchor.SEP_1);
            builder.addSpaceAffix(" ", NAffixAnchor.SEP_1);
        }
    }

    private void applyDefault(NElementBuilder builder, Stats score, NElementFormatContext context) {
        // Apply HORIZONTAL formatting (just a space)
//        builder.addAffixSpace(" ", NAffixAnchor.START);
    }

    private String formatCompact(NElement element) {
        return DefaultTsonWriter.formatTsonCompact(element);
    }

    private Stats calculateStats(NElement element, NElementFormatContext context) {
        Map<NElement, Stats> statsCache = (Map<NElement, Stats>) context.sharedConfig().computeIfAbsent(TsonFormatPrettyAction.class.getName() + "::statsCache", c -> new IdentityHashMap<NElement, Stats>());
        Stats o = statsCache.get(element);
        if (o != null) {
            return o;
        }
        Stats s = new Stats();
        element.traverse(
                element1 -> {
                    switch (element1.type()) {
                        case BYTE:
                        case UBYTE:
                        case INT:
                        case UINT:
                        case SHORT:
                        case USHORT:
                        case LONG:
                        case ULONG:
                        case DOUBLE:
                        case FLOAT:
                        case BIG_COMPLEX:
                        case BIG_INT:
                        case BIG_DECIMAL:
                        case FLOAT_COMPLEX:
                        case DOUBLE_COMPLEX: {
                            NNumberElement n = element1.asNumber().get();
                            s.score += 1;
                            s.charSize += n.numberValue().toString().length() + (n.numberSuffix()==null?0:n.numberSuffix().length());
                            break;
                        }
                        case CHAR: {
                            s.score += 1;
                            s.charSize += 3;
                            break;
                        }
                        case NULL: {
                            s.score += 1;
                            s.charSize += 4;
                            break;
                        }
                        case BOOLEAN: {
                            s.score += 1;
                            s.charSize += 5; // i know! false is longer then true, not important here
                            break;
                        }
                        case LOCAL_DATE: {
                            s.score += 1;
                            s.charSize += 10;
                            break;
                        }
                        case LOCAL_TIME: {
                            s.score += 1;
                            s.charSize += 16;
                            break;
                        }
                        case LOCAL_DATETIME: {
                            s.score += 1;
                            s.charSize += 27;
                            break;
                        }
                        case INSTANT: {
                            s.score += 1;
                            s.charSize += 31;
                            break;
                        }
                        case DOUBLE_QUOTED_STRING:
                        case SINGLE_QUOTED_STRING:
                        case BACKTICK_STRING: {
                            s.score += 1;
                            s.charSize += 2 + element1.asStringValue().get().length(); // ignoreescapes
                            break;
                        }
                        case TRIPLE_DOUBLE_QUOTED_STRING:
                        case TRIPLE_SINGLE_QUOTED_STRING:
                        case TRIPLE_BACKTICK_STRING: {
                            s.score += 1;
                            s.charSize += 6 + element1.asStringValue().get().length(); // ignoreescapes
                            break;
                        }
                        case OPERATOR_SYMBOL: {
                            s.score += 1;
                            s.charSize += element1.asOperatorSymbol().get().symbol().lexeme().length();
                            break;
                        }
                        case EMPTY: {
                            s.score += 1;
                            s.charSize += 0;
                            break;
                        }
                        case BLOCK_STRING: {
                            s.score += 1;
                            int lines = 1;// ignoring number of lines for now
                            s.charSize += (2 * lines) + element1.asStringValue().get().length();
                            break;
                        }
                        case LINE_STRING: {
                            s.score += 1;
                            int lines = 1; // ignoring number of lines for now
                            s.charSize += (1 * lines) + element1.asStringValue().get().length();
                            break;
                        }
                        case NAME:{
                            s.score += 1;
                            s.charSize += element1.asStringValue().get().length();
                            break;
                        }
                        case OBJECT:
                        case FULL_OBJECT:
                        case NAMED_OBJECT:
                        case PARAM_OBJECT:
                        {
                            s.score += 2;
                            NObjectElement o1 = element1.asObject().get();
                            s.charSize+= 2+ o1.name().orElse("").length();
                            if(o1.params().isPresent()){
                                s.charSize+=2; // pars
                            }
                            break;
                        }
                        case ARRAY:
                        case FULL_ARRAY:
                        case NAMED_ARRAY:
                        case PARAM_ARRAY:
                        {
                            s.score += 2;
                            NArrayElement o1 = element1.asArray().get();
                            s.charSize+= 2+ o1.name().orElse("").length();
                            if(o1.params().isPresent()){
                                s.charSize+=2; // pars
                            }
                            break;
                        }
                        case UPLET:
                        case NAMED_UPLET:
                        {
                            s.score += 2;
                            s.charSize+=2+ element1.asUplet().get().name().orElse("").length();
                            break;
                        }
                        case PAIR:{
                            s.score += 1;
                            break;
                        }
                        case BINARY_OPERATOR:
                        case UNARY_OPERATOR:
                        case FLAT_EXPR:
                        case TERNARY_OPERATOR:
                        case NARY_OPERATOR:{
                            s.score += 2;
                            break;
                        }
                        case FRAGMENT:{
                            // do nothing
                            break;
                        }
                        case UNORDERED_LIST:
                        case ORDERED_LIST:
                        {
                            for (NListItemElement item : element1.asList().get().items()) {
                                s.score += 2;
                                s.charSize += item.depth();
                            }
                            break;
                        }
                        case BINARY_STREAM:{
                            NBinaryStreamElement b = element1.asBinaryStream().get();
                            s.score += 5;
                            s.charSize += 1024;//just a big number
                            break;
                        }
                        case CHAR_STREAM:{
                            NCharStreamElement b = element1.asCharStream().get();
                            s.score += 5;
                            s.charSize += 1024;//just a big number
                            break;
                        }
                        case CUSTOM:{
                            s.score += 5;
                            s.charSize += 1024;//just a big number
                            break;
                        }
                    }
                    return NTreeVisitResult.CONTINUE;
                }
        );
        statsCache.put(element, s);
        return s;
    }


}
