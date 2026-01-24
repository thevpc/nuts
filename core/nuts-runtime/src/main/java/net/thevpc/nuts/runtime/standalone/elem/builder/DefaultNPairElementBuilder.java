package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNPairElement;
import net.thevpc.nuts.util.NAssignmentPolicy;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.function.Consumer;

public class DefaultNPairElementBuilder extends AbstractNElementBuilder implements NPairElementBuilder {
    private NElement key;
    private NElement value;

    public DefaultNPairElementBuilder() {
        key = NElement.ofNull();
        value = NElement.ofNull();
    }

    @Override
    public NOptional<String> name() {
        if (key.isAnyString()) {
            return key.asStringValue();
        }
        return NOptional.ofNamedEmpty("name");
    }

    @Override
    public NPairElementBuilder doWith(Consumer<NPairElementBuilder> con) {
        if (con != null) {
            con.accept(this);
        }
        return this;
    }

    public DefaultNPairElementBuilder(NElement key, NElement value) {
        this.key = key == null ? NElement.ofNull() : key;
        this.value = value == null ? NElement.ofNull() : value;
    }


    public NPairElementBuilder value(NElement value) {
        this.value = value == null ? NElement.ofNull() : value;
        return this;
    }


    public NPairElementBuilder key(NElement key) {
        this.key = key == null ? NElement.ofNull() : key;
        return this;
    }

    @Override
    public NPairElementBuilder key(String key) {
        this.key = key == null ? NElement.ofNull() : NElement.ofNameOrString(key);
        return this;
    }

    @Override
    public NPairElement build() {
        return new DefaultNPairElement(key, value, affixes(), diagnostics());
    }

    @Override
    public NElementType type() {
        return NElementType.PAIR;
    }

    @Override
    public NElement value() {
        return value;
    }

    @Override
    public NElement key() {
        return key;
    }

    // ------------------------------------------

    @Override
    public NPairElementBuilder copyFrom(NElementBuilder other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPairElementBuilder) {
            NPairElementBuilder from = (NPairElementBuilder) other;
            this.key = from.key();
            this.value = from.value();
            return this;
        }
        if (other instanceof NUpletElementBuilder) {
            NUpletElementBuilder from = (NUpletElementBuilder) other;
            if (from.size() > 0) {
                this.key = from.get(0).get();
            }
            if (from.size() > 1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        if (other instanceof NObjectElementBuilder) {
            NObjectElementBuilder from = (NObjectElementBuilder) other;
            if (from.size() > 0) {
                this.key = from.getAt(0).get();
            }
            if (from.size() > 1) {
                this.value = from.getAt(1).get();
            }
            return this;
        }
        if (other instanceof NArrayElementBuilder) {
            NArrayElementBuilder from = (NArrayElementBuilder) other;
            if (from.size() > 0) {
                this.key = from.get(0).get();
            }
            if (from.size() > 1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        return this;
    }

    @Override
    public NPairElementBuilder copyFrom(NElement other, NAssignmentPolicy assignmentPolicy) {
        if (other == null) {
            return this;
        }
        super.copyFrom(other, assignmentPolicy);
        if (other instanceof NPairElement) {
            NPairElement from = (NPairElement) other;
            this.key = from.key();
            this.value = from.value();
            return this;
        }
        if (other instanceof NUpletElement) {
            NUpletElement from = (NUpletElement) other;
            if (from.size() > 0) {
                this.key = from.get(0).get();
            }
            if (from.size() > 1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        if (other instanceof NObjectElement) {
            NObjectElement from = (NObjectElement) other;
            if (from.size() > 0) {
                this.key = from.getAt(0).get();
            }
            if (from.size() > 1) {
                this.value = from.getAt(1).get();
            }
            return this;
        }
        if (other instanceof NArrayElement) {
            NArrayElement from = (NArrayElement) other;
            if (from.size() > 0) {
                this.key = from.get(0).get();
            }
            if (from.size() > 1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        return this;
    }

    // ------------------------------------------
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name, args);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NPairElementBuilder addAffix(int index, NBoundAffix affix) {
        super.addAffix(index, affix);
        return this;
    }

    @Override
    public NPairElementBuilder removeAffix(int index) {
        super.removeAffix(index);
        return this;
    }

    @Override
    public NPairElementBuilder clearAnnotations() {
        super.clearAnnotations();
        return this;
    }

    @Override
    public NPairElementBuilder clearComments() {
        super.clearComments();
        return this;
    }

    @Override
    public NPairElementBuilder setAffix(int index, NBoundAffix affix) {
        super.setAffix(index, affix);
        return this;
    }

    public NPairElementBuilder addAffix(NBoundAffix affix){
        super.addAffix(affix);
        return this;
    }


    @Override
    public NPairElementBuilder addAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.addAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NPairElementBuilder setAffix(int index, NAffix affix, NAffixAnchor anchor) {
        super.setAffix(index, affix, anchor);
        return this;
    }

    @Override
    public NPairElementBuilder removeDiagnostic(NElementDiagnostic error) {
        super.removeDiagnostic(error);
        return this;
    }

    @Override
    public NPairElementBuilder addAffixes(List<NBoundAffix> affixes) {
        super.addAffixes(affixes);
        return this;
    }

    @Override
    public NPairElementBuilder addDiagnostic(NElementDiagnostic error) {
        super.addDiagnostic(error);
        return this;
    }

    @Override
    public NPairElementBuilder addAffixes(List<? extends NAffix> affixes, NAffixAnchor anchor) {
        super.addAffixes(affixes, anchor);
        return this;
    }

    @Override
    public NPairElementBuilder addAffix(NAffix affix, NAffixAnchor anchor) {
        super.addAffix(affix, anchor);
        return this;
    }

    @Override
    public NPairElementBuilder removeAffixes(NAffixType type, NAffixAnchor anchor) {
        super.removeAffixes(type, anchor);
        return this;
    }

    @Override
    public NPairElementBuilder addLeadingComment(NElementComment comment) {
        super.addLeadingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder addLeadingComments(NElementComment... comments) {
        super.addLeadingComments(comments);
        return this;
    }

    @Override
    public NPairElementBuilder addTrailingComment(NElementComment comment) {
        super.addTrailingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder addTrailingComments(NElementComment... comments) {
        super.addTrailingComments(comments);
        return this;
    }

    @Override
    public NPairElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }

    @Override
    public NPairElementBuilder copyFrom(NElementBuilder other) {
        super.copyFrom(other);
        return this;
    }

    @Override
    public NPairElementBuilder copyFrom(NElement other) {
        super.copyFrom(other);
        return this;
    }

}
