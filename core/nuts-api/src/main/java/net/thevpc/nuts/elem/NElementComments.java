package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NBlankable;

import java.util.List;

public interface NElementComments extends Comparable<NElementComments>, NBlankable {
    List<NElementComment> trailingComments();

    List<NElementComment> leadingComments();

    boolean isEmpty();
}
