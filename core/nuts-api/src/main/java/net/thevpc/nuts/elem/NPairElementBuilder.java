package net.thevpc.nuts.elem;

import java.util.List;

public interface NPairElementBuilder extends NElementBuilder {
    NPairElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NPairElementBuilder addAnnotation(NElementAnnotation annotation);

    NPairElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NPairElementBuilder removeAnnotationAt(int index);

    NPairElementBuilder clearAnnotations();

    List<NElementAnnotation> annotations();

    NPairElement build();

    NElement key();

    NElement value();

    NPairElementBuilder key(NElement value);

    NPairElementBuilder value(NElement value);

    NPairElementBuilder copyFrom(NPairElement other);

    NPairElementBuilder copyFrom(NPairElementBuilder element);

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

}
