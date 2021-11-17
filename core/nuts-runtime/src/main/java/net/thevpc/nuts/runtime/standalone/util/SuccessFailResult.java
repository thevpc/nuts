package net.thevpc.nuts.runtime.standalone.util;

public class SuccessFailResult<T, E extends Exception> {
    private T success = null;
    private E fail = null;

    public static <T, E extends Exception> SuccessFailResult<T, E> success(T success) {
        return new SuccessFailResult<>(success, null);
    }

    public static <T, E extends Exception> SuccessFailResult<T, E> fail(E fail) {
        return new SuccessFailResult<>(null, fail);
    }

    public SuccessFailResult(T success, E fail) {
        this.success = success;
        this.fail = fail;
    }

    public T getSuccess() {
        return success;
    }

    public E getFail() {
        return fail;
    }
}
