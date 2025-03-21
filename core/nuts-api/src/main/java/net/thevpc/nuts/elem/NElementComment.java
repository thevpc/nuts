package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NBlankable;

public interface NElementComment extends Comparable<NElementComment>, NBlankable {
    NElementCommentType type();

    String text() ;
}
