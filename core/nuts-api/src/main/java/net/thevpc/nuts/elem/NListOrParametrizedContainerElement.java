package net.thevpc.nuts.elem;

import net.thevpc.nuts.pipeline.NStream;

import java.util.List;

public interface NListOrParametrizedContainerElement extends NElement {
    List<NParamOrChild> paramsOrChildren();

    NStream<NParamOrChild> streamParamsOrChildren();
}
