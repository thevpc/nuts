package net.thevpc.nuts.elem;

import java.util.Collections;
import java.util.List;

public interface NElementTransform {
    default List<NElement> preTransform(NElementPath path, NElement element){
        return Collections.singletonList(element);
    }
    default List<NElement> postTransform(NElementPath path,NElement element){
        return Collections.singletonList(element);
    }
}
