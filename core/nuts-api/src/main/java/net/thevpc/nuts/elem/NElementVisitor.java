package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NTreeVisitResult;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * NElementVisitor interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
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

    /**
     * Creates a visitor that runs an action on enter (pre-order) and continues traversal.
     */
    static NElementVisitor ofEnter(Consumer<NElement> enterAction) {
        return element -> {
            enterAction.accept(element);
            return NTreeVisitResult.CONTINUE;
        };
    }

    /**
     * Creates a visitor with explicit traversal control on enter.
     */
    static NElementVisitor ofEnter(Function<NElement, NTreeVisitResult> enterFunction) {
        return enterFunction::apply;
    }

    /**
     * Creates a visitor that runs an action on exit (post-order).
     */
    static NElementVisitor ofExit(Consumer<NElement> exitAction) {
        return new NElementVisitor() {
            @Override
            public NTreeVisitResult enter(NElement element) {
                return NTreeVisitResult.CONTINUE;
            }

            @Override
            public void exit(NElement element) {
                exitAction.accept(element);
            }
        };
    }
}
