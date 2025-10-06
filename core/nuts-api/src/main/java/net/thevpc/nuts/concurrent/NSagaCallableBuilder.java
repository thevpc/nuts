package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaCallableBuilder {
    Suite<NSagaCallableBuilder> start();
    <T> NSagaCallable<T> build();

    interface Suite<P>  {
        Suite<P> then(String name, NSagaStep step);
        If<Suite<P>> thenIf(String name, NSagaCondition condition);
        While<Suite<P>> thenWhile(String name, NSagaCondition condition);

        P end();
    }
    interface While<P>  {
        While<P> then(String name, NSagaStep step);
        If<While<P>> thenIf(String name, NSagaCondition condition);
        While<While<P>> thenWhile(String name, NSagaCondition condition);
        P end();
    }

    interface If<P>  {
        If<P> then(String name, NSagaStep step);
        If<If<P>> thenIf(String name, NSagaCondition condition);
        While<If<P>> thenWhile(String name, NSagaCondition condition);
        If<P> elseIf(String name, NSagaCondition condition);
        If<P> otherwise();
        P end();
    }
}
