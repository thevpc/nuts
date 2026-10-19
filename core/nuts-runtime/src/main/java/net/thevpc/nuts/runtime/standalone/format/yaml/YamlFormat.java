package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Base64;
import java.util.List;

public class YamlFormat {
    private NElement str(String any) {
        return NElement.ofString(any);
    }

    private NElement stringify(NElement value) {
        if (value.isAnyString()) {
            return str(value.asStringValue().get());
        }
        if (value.isPrimitive() && value.asPrimitive().get().value() != null) {
            return str(value.asPrimitive().get().value().toString());
        }
        return str(value.toString());
    }

    private NElement ensureYaml(NElement value) {
        switch (value.type()) {
            case BIG_COMPLEX:
            case FLOAT_COMPLEX:
            case DOUBLE_COMPLEX:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME:
            case LINE_STRING:
            case BLOCK_STRING:
            case BACKTICK_STRING:
            case TRIPLE_BACKTICK_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case INSTANT:
            case CUSTOM: {
                return stringify(value);
            }
            case OPERATOR_SYMBOL:{
                NOperatorSymbolElement ope = (NOperatorSymbolElement) value;
                NObjectElementBuilder value1 = NElement.ofObjectBuilder().copyFrom(value);
                value1.clearChildren();
                value1.set("op", ope.type().id());
                value1.set("symbol", ope.symbol().id());
                value1.name(null);
                return value1.build();
            }
            case BINARY_OPERATOR:
            case TERNARY_OPERATOR:
            case UNARY_OPERATOR:
            case NARY_OPERATOR:
            case FLAT_EXPR:{
                NOperatorElement ope = (NOperatorElement) value;
                NObjectElementBuilder value1 = NElement.ofObjectBuilder().copyFrom(value);
                value1.clearChildren();
                value1.set("op", ope.type().id());
                value1.set("symbols", NElement.ofEnumArray(ope.operatorSymbols().toArray(new Enum[0])));
                value1.set("operands", NElement.ofArray(ope.operands().toArray(new NElement[0])));
                value1.set("position", ope.fixity().id());
                value1.name(null);
                return value1.build();
            }
            case SINGLE_QUOTED_STRING:
            case DOUBLE_QUOTED_STRING: {
                return value;
            }
            case NAME:
                return value;
            case BINARY_STREAM: {
                NBinaryStreamElement value1 = (NBinaryStreamElement) value;
                String s = Base64.getEncoder().encodeToString(NInputSource.of(value1.value()).readBytes());
                return NElement.ofPrimitiveBuilder().copyFrom(value).setString(s).build();
            }
            case CHAR_STREAM: {
                NCharStreamElement value1 = (NCharStreamElement) value;
                String s = NInputSource.of(value1.value()).readString();
                return NElement.ofPrimitiveBuilder().copyFrom(value).setString(s).build();
            }
            case NAMED_OBJECT:
            case PARAM_OBJECT:
            case FULL_OBJECT: {
                NObjectElementBuilder value1 = ((NObjectElement) value).builder();
                if (value1.name().isPresent()) {
                    value1.set("$name", value1.name().get());
                    value1.name(null);
                }
                if (value1.params().isPresent()) {
                    value1.set("$params", NElement.ofArray(value1.params().get().toArray(new NElement[0])));
                    value1.setParametrized(false);
                }
                return value1.build();
            }
            case NAMED_ARRAY:
            case PARAM_ARRAY:
            case FULL_ARRAY: {
                NObjectElementBuilder value1 = NElement.ofObjectBuilder().copyFrom(value);
                if (value1.name().isPresent()) {
                    value1.set("$name", value1.name().get());
                    value1.name(null);
                }
                if (value1.params().isPresent()) {
                    value1.set("$params", NElement.ofArray(value1.params().get().toArray(new NElement[0])));
                    value1.setParametrized(false);
                }
                value1.set("$array", NElement.ofArray(value1.children().toArray(new NElement[0])));
                return value1.build();
            }
            case TUPLE:
            case NAMED_TUPLE: {
                NObjectElementBuilder value1 = NElement.ofObjectBuilder().copyFrom(value);
                List<NElement> children = value1.children();
                value1.clearChildren();
                if (value1.name().isPresent()) {
                    value1.set("$name", value1.name().get());
                    value1.name(null);
                }
                if (value1.params().isPresent()) {
                    value1.set("$params", NElement.ofArray(value1.params().get().toArray(new NElement[0])));
                    value1.setParametrized(false);
                }
                value1.set("$array", NElement.ofArray(children.toArray(new NElement[0])));
                return value1.build();
            }
            case PAIR: {
                NObjectElementBuilder value1 = NElement.ofObjectBuilder().copyFrom(value);
                return value1.build();
            }
            case OBJECT:
            case ARRAY:
            case FRAGMENT:
            case BOOLEAN:
            case INT:
            case FLOAT:
            case SHORT:
            case DOUBLE:
            case LONG:
            case CHAR:
            case BYTE:
            case BIG_INT:
            case BIG_DECIMAL:
            case NULL:
                return value;
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported %s for yaml", value));
    }


    void formatNode(NElement value, NPrintStream out, NElementFormatter formatter, NElementFactoryContext context, int indentLevel) {
        if (value.type() == NElementType.FRAGMENT) {
            boolean first = true;
            for (NElement document : value.asFragment().get()) {
                if (!first) {
                    out.print("\n");
                }
                first = false;
                out.print("---\n");
                formatNode(document, out, formatter, context, 0);
                out.print("\n");
            }
            return;
        }
        String alias = annotationValue(value, "yamlAlias");
        if (alias != null) {
            out.print("*");
            out.print(alias);
            return;
        }
        String anchor = annotationValue(value, "yamlAnchor");
        String tag = annotationValue(value, "yamlTag");
        if (anchor != null && isComplexType(value)) {
            out.print(NStringUtils.repeat("   ", indentLevel));
            out.print("&");
            out.print(anchor);
            out.print(" ");
            if (tag != null) {
                out.print("!<");
                out.print(tag);
                out.print("> ");
            }
            formatFlowNode(value, out, formatter, context);
            return;
        }
        if (anchor != null) {
            out.print("&");
            out.print(anchor);
            out.print(isComplexType(value) ? "\n" : " ");
        }
        if (tag != null) {
            out.print("!<");
            out.print(tag);
            out.print("> ");
        }
        value = ensureYaml(value);
        String indent = NStringUtils.repeat("   ", indentLevel);
        switch (value.type()) {
            case NULL: {
                out.print("null");
                break;
            }
            case BOOLEAN:
            case INT:
            case BYTE:
            case LONG:
            case BIG_INT:
            case BIG_DECIMAL: {
                out.print(value.toString());
                break;
            }
            case FLOAT:
            case DOUBLE: {
                out.print(formatFloating(value));
                break;
            }
            case DOUBLE_QUOTED_STRING: {
                out.print(quoteYaml(value.asStringValue().get()));
                break;
            }
            case SINGLE_QUOTED_STRING: {
                String text = value.asStringValue().get();
                out.print("'" + text.replace("'", "''") + "'");
                break;
            }
            case NAME: {
                formatName(value.asStringValue().get(), out);
                break;
            }
            case OBJECT: {
                if (value.asObject().get().pairs().isEmpty()) {
                    out.print("{}");
                    break;
                }
                boolean first = true;
                for (NPairElement entry : value.asObject().get().pairs().toArray(new NPairElement[0])) {
                    if (!first) {
                        out.print("\n");
                    }
                    first = false;

                    NElement key = entry.key();
                    NElement val = entry.value();
                    if (isComplexType(key)) {
                        out.print(indent);
                        out.print("?\n");
                        formatNode(key, out, formatter, context, indentLevel + 1);
                        out.print("\n");
                        out.print(indent);
                        out.print(":");
                        if (isComplexBlockValue(val)) {
                            out.print("\n");
                            formatNode(val, out, formatter, context, indentLevel + 1);
                        } else {
                            out.print(" ");
                            formatNode(val, out, formatter, context, indentLevel + 1);
                        }
                    } else {
                        out.print(indent);
                        formatKey(key, out, formatter, context, indentLevel);
                        out.print(":");
                        if (isComplexBlockValue(val)) {
                            out.print("\n");
                            formatNode(val, out, formatter, context, indentLevel + 1);
                        } else {
                            out.print(" ");
                            formatNode(val, out, formatter, context, indentLevel);
                        }
                    }
                }
                break;
            }
            case ARRAY: {
                if (value.asArray().get().isEmpty()) {
                    out.print("[]");
                    break;
                }
                boolean first = true;
                for (NElement a : value.asArray().get()) {
                    if (!first) {
                        out.print("\n");
                    }
                    first = false;

                    out.print(indent);
                    out.print("- ");
                    if (isComplexBlockValue(a)) {
                        out.print("\n");
                        formatNode(a, out, formatter, context, indentLevel + 1);
                    } else {
                        formatNode(a, out, formatter, context, indentLevel);
                    }
                }
                break;
            }
            default: {
                if (value.isAnyString()) {
                    out.print(quoteYaml(value.asStringValue().get()));
                } else {
                    out.print(value.toString());
                }
                break;
            }
        }
    }

    private void formatKey(NElement key, NPrintStream out, NElementFormatter formatter,
                           NElementFactoryContext context, int indentLevel) {
        if (key.type() == NElementType.NAME) {
            formatName(key.asStringValue().get(), out);
        } else if (key.isAnyString()) {
            out.print(quoteYaml(key.asStringValue().get()));
        } else {
            formatNode(key, out, formatter, context, indentLevel);
        }
    }

    private void formatName(String name, NPrintStream out) {
        if (name.matches("[a-zA-Z_]([a-zA-Z0-9_-])*")
                && !name.equalsIgnoreCase("null")
                && !name.equalsIgnoreCase("true")
                && !name.equalsIgnoreCase("false")) {
            out.print(name);
        } else {
            out.print("!<tag:thevpc.net,nuts/name> ");
            out.print(quoteYaml(name));
        }
    }

    private String annotationValue(NElement element, String name) {
        for (NElementAnnotation annotation : element.annotations()) {
            if (name.equals(annotation.name()) && annotation.param(0).isPresent()) {
                return annotation.param(0).get().asStringValue().get();
            }
        }
        return null;
    }

    private String quoteYaml(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\': result.append("\\\\"); break;
                case '"': result.append("\\\""); break;
                case '\0': result.append("\\0"); break;
                case '\u0007': result.append("\\a"); break;
                case '\b': result.append("\\b"); break;
                case '\t': result.append("\\t"); break;
                case '\n': result.append("\\n"); break;
                case '\u000b': result.append("\\v"); break;
                case '\f': result.append("\\f"); break;
                case '\r': result.append("\\r"); break;
                case '\u001b': result.append("\\e"); break;
                case '\u0085': result.append("\\N"); break;
                case '\u00a0': result.append("\\_"); break;
                case '\u2028': result.append("\\L"); break;
                case '\u2029': result.append("\\P"); break;
                default:
                    if (c < 0x20 || c == 0x7f) {
                        result.append(String.format("\\u%04X", (int) c));
                    } else {
                        result.append(c);
                    }
            }
        }
        return result.append('"').toString();
    }

    private String formatFloating(NElement value) {
        double number = ((Number) value.asPrimitive().get().value()).doubleValue();
        if (Double.isNaN(number)) return ".nan";
        if (number == Double.POSITIVE_INFINITY) return ".inf";
        if (number == Double.NEGATIVE_INFINITY) return "-.inf";
        return Double.toString(number);
    }

    private boolean isComplexType(NElement value) {
        return value.isArray() || value.isObject();
    }

    private boolean isComplexBlockValue(NElement value) {
        return isComplexType(value) && annotationValue(value, "yamlAlias") == null;
    }

    private void formatFlowNode(NElement value, NPrintStream out, NElementFormatter formatter,
                                NElementFactoryContext context) {
        value = ensureYaml(value);
        if (value.type() == NElementType.ARRAY) {
            out.print("[");
            boolean first = true;
            for (NElement child : value.asArray().get()) {
                if (!first) out.print(", ");
                first = false;
                formatFlowNode(child, out, formatter, context);
            }
            out.print("]");
        } else if (value.type() == NElementType.OBJECT) {
            out.print("{");
            boolean first = true;
            for (NPairElement pair : value.asObject().get().pairs()) {
                if (!first) out.print(", ");
                first = false;
                formatFlowNode(pair.key(), out, formatter, context);
                out.print(": ");
                formatFlowNode(pair.value(), out, formatter, context);
            }
            out.print("}");
        } else {
            formatNode(value, out, formatter, context, 0);
        }
    }


}
