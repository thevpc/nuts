package net.thevpc.nuts.elem;

import java.util.Collections;
import java.util.List;

/**
 * NElementTransform interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementTransform {
    /**
     * Pre transform.
     *
     * @param context context
     * @return pre transform result
     */
    default List<NElement> preTransform(NElementTransformContext context){
        return Collections.singletonList(context.element());
    }

    /**
     * Prepare child context.
     *
     * @param parent parent
     * @param childContext child context
     * @return prepare child context result
     */
    default NElementTransformContext prepareChildContext(NElement parent,NElementTransformContext childContext){
        return childContext;
    }

    /**
     * Post transform.
     *
     * @param context context
     * @return post transform result
     */
    default List<NElement> postTransform(NElementTransformContext context){
        return Collections.singletonList(context.element());
    }
}
