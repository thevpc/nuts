package net.thevpc.nuts.concurrent;

/**
 * @since 0.8.7
 */
public interface NSagaBuilder {
    Suite<NSagaBuilder> start();
    <T> NSagaCall<T> build();

    interface Suite<P>  {
        Suite<P> then(String name, NSagaCallStep step);
        If<Suite<P>> thenIf(String name, NSagaCallCondition condition);
        While<Suite<P>> thenWhile(String name, NSagaCallCondition condition);

        P end();
    }
    interface While<P>  {
        While<P> then(String name, NSagaCallStep step);
        If<While<P>> thenIf(String name, NSagaCallCondition condition);
        While<While<P>> thenWhile(String name, NSagaCallCondition condition);
        P end();
    }

    interface If<P>  {
        If<P> then(String name, NSagaCallStep step);
        If<If<P>> thenIf(String name, NSagaCallCondition condition);
        While<If<P>> thenWhile(String name, NSagaCallCondition condition);
        If<P> elseIf(String name, NSagaCallCondition condition);
        If<P> otherwise();
        P end();
    }
}
