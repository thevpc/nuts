package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.format.elem.builder.NElementCommentsBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.elem.item.NElementAnnotationImpl;
import net.thevpc.nuts.runtime.standalone.format.elem.item.NElementCommentImpl;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMapStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractNElementBuilder implements NElementBuilder {
    private NElementCommentsBuilderImpl comments = new NElementCommentsBuilderImpl();
    private final List<NElementAnnotation> annotations = new ArrayList<>();

    @Override
    public boolean isCustomTree() {
        if(annotations!=null){
            for (NElementAnnotation annotation : annotations) {
                if(annotation.isCustomTree()){
                    return true;
                }
            }
        }
        return false;
    }

    public NElementBuilder addLeadingComment(NElementCommentType type, String text) {
        NAssert.requireNonNull(type, "comment type");
        return addLeadingComment(new NElementCommentImpl(type, text));
    }

    public NElementBuilder addTrailingComment(NElementCommentType type, String text) {
        NAssert.requireNonNull(type, "comment type");
        return addTrailingComment(new NElementCommentImpl(type, text));
    }

    public NElementBuilder addLeadingComment(NElementComment comment) {
        this.comments.addLeading(comment);
        return this;
    }

    public NElementBuilder addLeadingComments(NElementComment... comments) {
        this.comments.addLeading(comments);
        return this;
    }

    public NElementBuilder addTrailingComment(NElementComment comment) {
        this.comments.addTrailing(comment);
        return this;
    }

    public NElementBuilder addTrailingComments(NElementComment... comments) {
        this.comments.addTrailing(comments);
        return this;
    }

    public NElementComments comments() {
        return comments.build();
    }

    @Override
    public NElementBuilder clearComments() {
        comments.clear();
        return this;
    }

    @Override
    public NElementBuilder removeTrailingCommentAt(int index) {
        comments.removeTrailingCommentAt(index);
        return this;
    }

    @Override
    public NElementBuilder removeLeadingCommentAt(int index) {
        comments.removeLeadingCommentAt(index);
        return this;
    }

    @Override
    public NElementBuilder removeTrailingComment(NElementComment comment) {
        comments.removeTrailingComment(comment);
        return this;
    }

    @Override
    public NElementBuilder removeLeadingComment(NElementComment comment) {
        comments.removeLeading(comment);
        return this;
    }

    @Override
    public List<NElementComment> trailingComments() {
        return comments.trailingComments();
    }

    @Override
    public List<NElementComment> leadingComments() {
        return comments.leadingComments();
    }

    @Override
    public NElementBuilder addComments(NElementComments comments) {
        this.comments.addComments(comments);
        return this;
    }

    @Override
    public NElementBuilder addAnnotations(List<NElementAnnotation> annotations) {
        if (annotations != null) {
            for (NElementAnnotation a : annotations) {
                if (a != null) {
                    this.annotations.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public NElementBuilder addAnnotation(String name, NElement... args) {
        return addAnnotation(new NElementAnnotationImpl(name, args));
    }

    @Override
    public NElementBuilder addAnnotation(NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(annotation);
        }
        return this;
    }

    @Override
    public NElementBuilder addAnnotationAt(int index, NElementAnnotation annotation) {
        if (annotation != null) {
            annotations.add(index, annotation);
        }
        return this;
    }

    @Override
    public NElementBuilder removeAnnotationAt(int index) {
        annotations.remove(index);
        return this;
    }

    @Override
    public NElementBuilder clearAnnotations() {
        annotations.clear();
        return this;
    }

    @Override
    public List<NElementAnnotation> annotations() {
        return Collections.unmodifiableList(annotations);
    }


    @Override
    public NElementBuilder copyFrom(NElementBuilder other) {
        copyFrom(other,NMapStrategy.ANY);
        return this;
    }

    @Override
    public NElementBuilder copyFrom(NElement other) {
        if(other!=null){
            copyFrom(other.builder());
        }
        return this;
    }

    @Override
    public NElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy) {
        if(other==null){
            return this;
        }
        this.comments.addLeading(other.leadingComments().toArray(new NElementComment[0]));
        this.comments.addTrailing(other.trailingComments().toArray(new NElementComment[0]));
        this.annotations.addAll(other.annotations());
        return this;
    }

    @Override
    public NElementBuilder copyFrom(NElement other, NMapStrategy strategy) {
        if(other==null){
            return this;
        }
        NElementComments cmt = other.comments();
        this.comments.addLeading(cmt.leadingComments().toArray(new NElementComment[0]));
        this.comments.addTrailing(cmt.trailingComments().toArray(new NElementComment[0]));
        this.annotations.addAll(other.annotations());
        return this;
    }

}
