package net.thevpc.nuts.elem;

import java.util.Collections;
import java.util.List;

public interface NElementTransform {
    default List<NElement> preTransform(NElementTransformContext context){
        return Collections.singletonList(context.element());
    }

    default NElementTransformContext prepareChildContext(NElement parent,NElementTransformContext childContext){
        return childContext;
    }

    default List<NElement> postTransform(NElementTransformContext context){
        return Collections.singletonList(context.element());
    }
}
