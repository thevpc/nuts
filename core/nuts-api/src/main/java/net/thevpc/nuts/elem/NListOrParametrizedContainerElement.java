package net.thevpc.nuts.elem;

import net.thevpc.nuts.pipeline.NStream;

import java.util.List;

/**
 * NListOrParametrizedContainerElement interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NListOrParametrizedContainerElement extends NElement {
    /**
     * Params or children.
     *
     * @return params or children result
     */
    List<NParamOrChild> paramsOrChildren();

    /**
     * Stream params or children.
     *
     * @return stream params or children result
     */
    NStream<NParamOrChild> streamParamsOrChildren();
}
