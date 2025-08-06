package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.AbstractNElementBuilder;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNPairElement;
import net.thevpc.nuts.util.NMapStrategy;
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
    public NPairElementBuilder removeAnnotation(NElementAnnotation annotation) {
        super.removeAnnotation(annotation);
        return this;
    }
    @Override
    public NPairElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NOptional<String> name() {
        if (key.isAnyString()) {
            return key.asStringValue();
        }
        return NOptional.ofNamedEmpty("name");
    }

    @Override
    public NPairElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        if(other==null){
            return this;
        }
        super.copyFrom(other, strategy);
        if(other instanceof NPairElementBuilder){
            NPairElementBuilder from = (NPairElementBuilder) other;
            this.key = from.key();
            this.value = from.value();
            return this;
        }
        if(other instanceof NUpletElementBuilder){
            NUpletElementBuilder from = (NUpletElementBuilder) other;
            if(from.size()>0) {
                this.key = from.get(0).get();
            }
            if(from.size()>1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        if(other instanceof NObjectElementBuilder){
            NObjectElementBuilder from = (NObjectElementBuilder) other;
            if(from.size()>0) {
                this.key = from.getAt(0).get();
            }
            if(from.size()>1) {
                this.value = from.getAt(1).get();
            }
            return this;
        }
        if(other instanceof NArrayElementBuilder){
            NArrayElementBuilder from = (NArrayElementBuilder) other;
            if(from.size()>0) {
                this.key = from.get(0).get();
            }
            if(from.size()>1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        return this;
    }

    @Override
    public NPairElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        if(other==null){
            return this;
        }
        super.copyFrom(other, strategy);
        if(other instanceof NPairElement){
            NPairElement from = (NPairElement) other;
            this.key = from.key();
            this.value = from.value();
            return this;
        }
        if(other instanceof NUpletElement){
            NUpletElement from = (NUpletElement) other;
            if(from.size()>0) {
                this.key = from.get(0).get();
            }
            if(from.size()>1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        if(other instanceof NObjectElement){
            NObjectElement from = (NObjectElement) other;
            if(from.size()>0) {
                this.key = from.getAt(0).get();
            }
            if(from.size()>1) {
                this.value = from.getAt(1).get();
            }
            return this;
        }
        if(other instanceof NArrayElement){
            NArrayElement from = (NArrayElement) other;
            if(from.size()>0) {
                this.key = from.get(0).get();
            }
            if(from.size()>1) {
                this.value = from.get(1).get();
            }
            return this;
        }
        return this;
    }



    @Override
    public boolean isCustomTree() {
        if(super.isCustomTree()){
            return true;
        }
        if(key!=null && key.isCustomTree()){
            return true;
        }
        if(value!=null && value.isCustomTree()){
            return true;
        }
        return false;
    }

    @Override
    public NPairElementBuilder doWith(Consumer<NPairElementBuilder> con) {
        if(con!=null){
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
        return new DefaultNPairElement(key, value, annotations().toArray(new NElementAnnotation[0]), comments());
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
    // RETURN SIG
    // ------------------------------------------

    @Override
    public NPairElementBuilder addLeadingComment(NElementCommentType type, String text) {
        super.addLeadingComment(type, text);
        return this;
    }

    @Override
    public NPairElementBuilder addTrailingComment(NElementCommentType type, String text) {
        super.addTrailingComment(type, text);
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
    public NPairElementBuilder removeTrailingCommentAt(int index) {
        super.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NPairElementBuilder removeLeadingCommentAt(int index) {
        super.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NPairElementBuilder removeTrailingComment(NElementComment comment) {
        super.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder removeLeadingComment(NElementComment comment) {
        super.removeLeadingComment(comment);
        return this;
    }

    @Override
    public NPairElementBuilder addComments(NElementComments comments) {
        super.addComments(comments);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        super.addAnnotations(annotations);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotation(String name, NElement... args) {
        super.addAnnotation(name,args);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotation(NElementAnnotation annotation) {
        super.addAnnotation(annotation);
        return this;
    }

    @Override
    public NPairElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        super.addAnnotationAt(index, annotation);
        return this;
    }

    @Override
    public NPairElementBuilder removeAnnotationAt(int index) {
        super.removeAnnotationAt(index);
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
    public NPairElementBuilder copyFrom(NElement other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

}
