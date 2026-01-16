package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NStream;

import java.util.List;

public interface NListOrParametrizedContainerElement extends NElement {
    List<NParamOrChild> paramsOrChildren();

    NStream<NParamOrChild> streamParamsOrChildren();
}
