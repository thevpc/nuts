package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMsg;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNPrimitiveElementBuilder extends AbstractNElementBuilder implements NPrimitiveElementBuilder {
    private Object value;
    private NNumberLayout numberLayout;
    private String numberSuffix;

    private NElementType type;

    public DefaultNPrimitiveElementBuilder() {
        this.type = NElementType.NULL;
    }

    public NNumberLayout numberLayout() {
        return numberLayout;
    }

    public NPrimitiveElementBuilder numberLayout(NNumberLayout numberLayout) {
        this.numberLayout = numberLayout;
        return this;
    }

    public String numberSuffix() {
        return numberSuffix;
    }

    public NPrimitiveElementBuilder numberSuffix(String numberSuffix) {
        this.numberSuffix = numberSuffix;
        return this;
    }

    public NPrimitiveElementBuilder value(Object value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            switch (value.getClass().getName()) {
                case "java.lang.String": {
                    this.value = value;
                    this.type = NElementType.DOUBLE_QUOTED_STRING;
                    break;
                }
                case "java.lang.Boolean":
                case "boolean": {
                    this.value = value;
                    this.type = NElementType.BOOLEAN;
                    break;
                }
                case "java.lang.Byte":
                case "byte": {
                    this.value = value;
                    this.type = NElementType.BYTE;
                    break;
                }
                case "java.lang.Short":
                case "short": {
                    this.value = value;
                    this.type = NElementType.SHORT;
                    break;
                }
                case "java.lang.Character":
                case "char": {
                    this.value = value;
                    this.type = NElementType.CHAR;
                    break;
                }
                case "java.lang.Integer":
                case "int": {
                    this.value = value;
                    this.type = NElementType.INTEGER;
                    break;
                }
                case "java.lang.Long":
                case "long": {
                    this.value = value;
                    this.type = NElementType.LONG;
                    break;
                }
                case "java.lang.Float":
                case "float": {
                    this.value = value;
                    this.type = NElementType.FLOAT;
                    break;
                }
                case "java.lang.Double":
                case "double": {
                    this.value = value;
                    this.type = NElementType.DOUBLE;
                    break;
                }
                case "java.time.Instant": {
                    this.value = value;
                    this.type = NElementType.INSTANT;
                    break;
                }
                case "java.math.BigInteger": {
                    this.value = value;
                    this.type = NElementType.BIG_INTEGER;
                    break;
                }
                case "java.math.BigDecimal": {
                    this.value = value;
                    this.type = NElementType.BIG_DECIMAL;
                    break;
                }
                default: {
                    if (value instanceof NDoubleComplex) {
                        this.value = value;
                        this.type = NElementType.DOUBLE_COMPLEX;
                    } else if (value instanceof NFloatComplex) {
                        this.value = value;
                        this.type = NElementType.FLOAT_COMPLEX;
                    } else if (value instanceof NBigComplex) {
                        this.value = value;
                        this.type = NElementType.BIG_COMPLEX;
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofC("Unsupported type: %s", value.getClass().getName()));
                    }
                }
            }
        }
        return this;
    }

    public NPrimitiveElementBuilder setInt(int value) {
        this.type = NElementType.INTEGER;
        this.value = value;
        return this;
    }

    public NPrimitiveElementBuilder setByte(byte value) {
        this.type = NElementType.BYTE;
        this.value = value;
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setDoubleComplex(NDoubleComplex value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            this.value = value;
            this.type = NElementType.DOUBLE_COMPLEX;
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setFloatComplex(NFloatComplex value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            this.value = value;
            this.type = NElementType.FLOAT_COMPLEX;
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder setBigComplex(NBigComplex value) {
        if (value == null) {
            this.value = null;
            this.type = NElementType.NULL;
        } else {
            this.value = value;
            this.type = NElementType.BIG_COMPLEX;
        }
        return this;
    }

    @Override
    public NPrimitiveElement build() {
        if (type().isNumber()) {
            return new DefaultNNumberElement(type, (Number) value, numberLayout(), numberSuffix(), annotations().toArray(new NElementAnnotation[0]), comments());
        }
        if (type().isAnyString()) {
            return new DefaultNStringElement(type, (String) value, annotations().toArray(new NElementAnnotation[0]), comments());
        }
        return new DefaultNPrimitiveElement(type, value, annotations().toArray(new NElementAnnotation[0]), comments());
    }

    @Override
    public NElementType type() {
        return type;
    }

    @Override
    public Object value() {
        return value;
    }


    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NPrimitiveElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name,args);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
        return this;
    }

    @Override
    public NPrimitiveElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NPrimitiveElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    public NPrimitiveElementBuilder copyFrom(NPrimitiveElement element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
            if (element instanceof NNumberElement) {
                NNumberElement ne = (NNumberElement) element;
                numberLayout(ne.numberLayout());
                numberSuffix(ne.numberSuffix());
            }
        }
        return this;
    }

    public NPrimitiveElementBuilder copyFrom(NPrimitiveElementBuilder element) {
        if (element != null) {
            addAnnotations(element.annotations());
            addComments(element.comments());
            value(element.value());
            numberLayout(element.numberLayout());
            numberSuffix(element.numberSuffix());
        }
        return this;
    }

    @Override
    public NPrimitiveElementBuilder doWith(Consumer<NPrimitiveElementBuilder> con) {
        if(con!=null){
            con.accept(this);
        }
        return this;
    }
}
