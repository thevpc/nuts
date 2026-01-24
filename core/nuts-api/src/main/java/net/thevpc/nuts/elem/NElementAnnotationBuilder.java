package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NBlankable;

import java.util.List;

public interface NElementAnnotationBuilder extends NBlankable {
    String name();

    boolean isParametrized();
    NElementAnnotationBuilder setUnparameterized();

    NElement param(int index);

    NElementAnnotationBuilder setName(String name);

    NElementAnnotation build();

    List<NElement> params();

    NElementAnnotationBuilder removeAt(int index);

    int size();
}
