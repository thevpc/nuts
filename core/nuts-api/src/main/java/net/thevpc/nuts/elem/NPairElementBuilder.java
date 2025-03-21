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

    NElement getKey();

    NElement getValue();

    NPairElementBuilder setKey(NElement value);

    NPairElementBuilder setValue(NElement value);

    NPairElementBuilder copyFrom(NPairElement other);

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
