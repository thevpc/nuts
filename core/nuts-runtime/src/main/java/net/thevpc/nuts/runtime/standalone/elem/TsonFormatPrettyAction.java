package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.writer.DefaultTsonWriter;
import net.thevpc.nuts.text.NTreeVisitResult;

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
                Stats score = calculateStats(parent); // Peek at the result
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
        Stats score = calculateStats(element); // Peek at the result
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
            case UPLET:
            {
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

    private Stats calculateStats(NElement element) {
        Stats s = new Stats();
        s.charSize = formatCompact(element).length();
        element.traverse(
                new NElementVisitor() {
                    @Override
                    public NTreeVisitResult enter(NElement element) {
                        switch (element.type().group()) {
                            case CONTAINER: {
                                if (element.type() == NElementType.PAIR) {
                                    //pairs are simple
                                    s.score += 1;
                                } else {
                                    s.score += 2;
                                }
                                break;
                            }
                            case CUSTOM: {
                                s.score += 3;
                                break;
                            }
                            case BOOLEAN:
                            case NULL:
                            case STRING:
                            case TEMPORAL:
                            case NUMBER: {
                                s.score += 1;
                                break;
                            }
                            case OPERATOR: {
                                //operators are simple as well
                                s.score += 1;
                                break;
                            }
                            case STREAM: {
                                s.score += 5;
                                break;
                            }
                            case OTHER: {
                                s.score += 1;
                                break;
                            }
                        }
                        return NTreeVisitResult.CONTINUE;
                    }
                }
        );
        return s;
    }


}
