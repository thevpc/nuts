package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.text.NTreeVisitResult;

public class TsonFormatPrettyAction implements NElementFormatterAction {
    @Override
    public NElementFormatContext prepareChildContext(NElement parent, NElementFormatContext childContext) {
        NElementFormatOptions options = childContext.options();
        switch (parent.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY: {
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
        NElement element = builder.build();
        Stats score = calculateStats(element); // Peek at the result
        switch (builder.type()) {
            case OBJECT:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY: {
                applyObjectOrArray(builder, score, context);
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
        builder.addAffixSpace(" ", NAffixAnchor.PRE_1);
        builder.addAffixSpace(" ", NAffixAnchor.POST_1);
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
//        if (score.isComplex(options)) {
////            builder.addAffixSeparator(" , ", NAffixAnchor.SEP_1);
//            builder.addAffixSpace(" ", NAffixAnchor.SEP_1);
//            builder.addAffixSeparator(",", NAffixAnchor.SEP_1);
//            builder.addAffixSpace(" ", NAffixAnchor.SEP_1);
//
//            // after )
//            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.POST_3);
//            builder.addAffixSpace(indent, NAffixAnchor.POST_3);
//
//            // after { or [
//            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.POST_4);
//            builder.addAffixSpace(indent + unit, NAffixAnchor.POST_4);
//
//
//            // between {} or [] items
//            builder.addAffixSpace(" ", NAffixAnchor.SEP_2);
//            builder.addAffixSeparator(",", NAffixAnchor.SEP_2);
//            builder.addAffixSpace(" ", NAffixAnchor.SEP_2);
//
//            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.SEP_2);
//            builder.addAffixSpace(indent + unit, NAffixAnchor.SEP_2);
//
//            // before } or ]
//            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.PRE_5);
//            builder.addAffixSpace(indent, NAffixAnchor.PRE_5);
//        } else {
////            builder.addAffixSpace(indent, NAffixAnchor.START);
//            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.POST_4);
//            builder.addAffixSpace(indent + unit, NAffixAnchor.POST_4);
//            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.PRE_5);
//            builder.addAffixSpace(indent, NAffixAnchor.PRE_5);
//            applyDefault(builder, score, context);
//        }

        if (score.isComplex(options)) {
            // Clear old affixes if necessary, or just set them cleanly:

            // 1. After { or [
            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.POST_4);
            builder.addAffixSpace(indent + unit, NAffixAnchor.POST_4);

            // 2. Separators: The comma goes in SEP_1, the NewLine in SEP_2
            builder.addAffixSeparator(",", NAffixAnchor.SEP_1);
            builder.addAffixSpace(" ", NAffixAnchor.SEP_1); // Optional trailing space after comma

            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.SEP_2);
            builder.addAffixSpace(indent + unit, NAffixAnchor.SEP_2);

            // 3. Before } or ]
            builder.addAffixNewLine(options.getNewLineMode(), NAffixAnchor.PRE_5);
            builder.addAffixSpace(indent, NAffixAnchor.PRE_5);
        } else {
            // Simple mode: One-liner with single spaces
            builder.addAffixSpace(" ", NAffixAnchor.POST_4);
            builder.addAffixSeparator(",", NAffixAnchor.SEP_1);
            builder.addAffixSpace(" ", NAffixAnchor.SEP_2);
            builder.addAffixSpace(" ", NAffixAnchor.PRE_5);
        }
    }

    private void applyDefault(NElementBuilder builder, Stats score, NElementFormatContext context) {
        // Apply HORIZONTAL formatting (just a space)
//        builder.addAffixSpace(" ", NAffixAnchor.START);
    }

    private Stats calculateStats(NElement element) {
        Stats s = new Stats();
        s.charSize = element.toString().length();
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
