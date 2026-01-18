package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NTreeVisitResult;

public interface NElementVisitor {
    /**
     * Called before visiting children and annotations.
     * @return traversal directive
     */
    NTreeVisitResult enter(NElement element);

    /**
     * Called after visiting children and annotations.
     */
    default void exit(NElement element) {
        // no-op
    }

    /**
     * Called for each annotation (since annotations are not NElement).
     * @return traversal directive
     */
    default NTreeVisitResult visitAnnotation(NElementAnnotation annotation) {
        return NTreeVisitResult.CONTINUE;
    }
}
