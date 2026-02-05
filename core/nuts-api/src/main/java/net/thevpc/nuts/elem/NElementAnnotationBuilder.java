package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NBlankable;

import java.util.List;

public interface NElementAnnotationBuilder extends NBlankable {
    String name();

    boolean isParametrized();

    NElementAnnotationBuilder setParameterized(boolean p);

    NElement param(int index);

    NElementAnnotationBuilder clear();

    NElementAnnotationBuilder setName(String name);

    NElementAnnotation build();

    List<NElement> params();

    NElementAnnotationBuilder add(NElement element);

    NElementAnnotationBuilder removeAt(int index);

    int size();

    NElementAnnotationBuilder addAll(List<NElement> all);
}
