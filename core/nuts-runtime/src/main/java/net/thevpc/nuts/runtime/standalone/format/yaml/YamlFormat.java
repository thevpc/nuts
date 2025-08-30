package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Base64;
import java.util.List;

public class YamlFormat {
    private NElement ensureYaml(NElement value) {
        switch (value.type().typeGroup()) {
            case OPERATOR: {
                NOperatorElement ope = (NOperatorElement) value;
                NObjectElementBuilder value1 = NElement.ofObjectBuilder().copyFrom(value);
                value1.clearChildren();
                value1.set("op", ope.type().opSymbol());
                value1.set("mode", ope.operatorType().id());
                value1.name(null);
                if (ope.first().isPresent()) {
                    value1.set("$first", ope.first().get());
                }
                if (ope.second().isPresent()) {
                    value1.set("$second", ope.second().get());
                }
                return value1.build();
            }
        }
        switch (value.type()) {
            case BIG_COMPLEX:
            case FLOAT_COMPLEX:
            case DOUBLE_COMPLEX:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME:
            case REGEX:
            case NAME:
            case SINGLE_QUOTED_STRING:
            case DOUBLE_QUOTED_STRING:
            case LINE_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case INSTANT:
            case CUSTOM: {
                return NElement.ofPrimitiveBuilder().copyFrom(value).setString(value.toString()).build();
            }
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
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT: {
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
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY: {
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
            case UPLET:
            case NAMED_UPLET: {
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
            case MATRIX: {
                return NElement.ofArrayBuilder().copyFrom(value).build();
            }
            case NAMED_PARAMETRIZED_MATRIX:
            case PARAMETRIZED_MATRIX:
            case NAMED_MATRIX: {
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
            case ALIAS:
            case NULL:
                return value;
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported %s for yaml", value));
    }


    void formatNode(NElement value, NPrintStream out, boolean compact, NElementFactoryContext context, int indentLevel) {
        value = ensureYaml(value);
        String indent = NStringUtils.repeat("   ", indentLevel);
        switch (value.type()) {
            case NULL: {
                out.print("null");
                break;
            }
            case BOOLEAN:
            case FLOAT:
            case DOUBLE:
            case INT:
            case BYTE:
            case LONG:
            case BIG_INT:
            case BIG_DECIMAL: {
                out.print(value.toString());
                break;
            }
            case DOUBLE_QUOTED_STRING: {
                out.print(value.asLiteral().toStringLiteral());
                break;
            }
            case ALIAS: {
                out.print(value.toString());
                break;
            }
            case OBJECT: {
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
                        formatNode(key, out, compact, context, indentLevel + 1);
                        out.print(indent);
                        out.print(":");
                        if (isComplexType(val)) {
                            out.print("\n");
                            formatNode(val, out, compact, context, indentLevel + 1);
                        } else {
                            out.print(" ");
                            formatNode(val, out, compact, context, indentLevel + 1);
                        }
                    } else {
                        out.print(indent);
                        formatNode(key, out, compact, context, indentLevel);
                        out.print(":");
                        if (isComplexType(val)) {
                            out.print("\n");
                            formatNode(val, out, compact, context, indentLevel + 1);
                        } else {
                            out.print(" ");
                            formatNode(val, out, compact, context, indentLevel);
                        }
                    }
                }
                break;
            }
            case ARRAY: {
                boolean first = true;
                for (NElement a : value.asArray().get()) {
                    if (!first) {
                        out.print("\n");
                    }
                    first = false;

                    out.print(indent);
                    out.print("- ");
                    if (a.type() == NElementType.OBJECT || a.type() == NElementType.ARRAY) {
                        out.print("\n");
                        formatNode(a, out, compact, context, indentLevel + 1);
                    } else {
                        formatNode(a, out, compact, context, indentLevel);
                    }
                }
                break;
            }
            default: {
                out.print(value.toString());
                break;
            }
        }
    }

    private boolean isComplexType(NElement value) {
        return value.type() == NElementType.ARRAY || value.type() == NElementType.OBJECT;
    }


}
