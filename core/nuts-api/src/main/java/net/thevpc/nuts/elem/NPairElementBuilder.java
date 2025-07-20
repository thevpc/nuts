package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NMapStrategy;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.function.Consumer;

public interface NPairElementBuilder extends NElementBuilder {
    NPairElementBuilder doWith(Consumer<NPairElementBuilder> con);

    NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NPairElementBuilder addAnnotation(String name, NElement... args);

    NPairElementBuilder addAnnotation(NElementAnnotation annotation);

    NPairElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NPairElementBuilder removeAnnotationAt(int index);

    NPairElementBuilder removeAnnotation(NElementAnnotation annotation);

    NPairElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NPairElement build();

    NElement key();

    NElement value();

    NOptional<String> name();

    NPairElementBuilder key(String value);

    NPairElementBuilder key(NElement value);

    NPairElementBuilder value(NElement value);

    NPairElementBuilder addLeadingComment(NElementCommentType type, String text);

    NPairElementBuilder addTrailingComment(NElementCommentType type, String text);

    NPairElementBuilder addLeadingComment(NElementComment comment);

    NPairElementBuilder addLeadingComments(NElementComment... comments);

    NPairElementBuilder addTrailingComment(NElementComment comment);

    NPairElementBuilder addTrailingComments(NElementComment... comments);

    NPairElementBuilder removeLeadingComment(NElementComment comment);

    NPairElementBuilder removeTrailingComment(NElementComment comment);

    NPairElementBuilder removeLeadingCommentAt(int index);

    NPairElementBuilder removeTrailingCommentAt(int index);

    NPairElementBuilder addComments(NElementComments comments);

    NPairElementBuilder copyFrom(NElementBuilder other);

    NPairElementBuilder copyFrom(NElement other);

    NPairElementBuilder copyFrom(NElementBuilder other, NMapStrategy strategy);

    NPairElementBuilder copyFrom(NElement other, NMapStrategy strategy);
}
