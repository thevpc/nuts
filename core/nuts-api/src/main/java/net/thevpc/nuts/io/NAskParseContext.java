package net.thevpc.nuts.io;

/**
 * NAskParseContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NAskParseContext<T> {
    /**
     * Response.
     *
     * @return response result
     */
    Object response();

    /**
     * Question.
     *
     * @return question result
     */
    NAsk<T> question();
}
