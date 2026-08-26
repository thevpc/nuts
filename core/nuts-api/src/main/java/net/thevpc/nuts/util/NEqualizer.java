package net.thevpc.nuts.util;

import java.util.Objects;

/**
 * NEqualizer interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NEqualizer<T> {
    NEqualizer<?> REF=new NEqualizer<Object>() {
        @Override
        public boolean equals(Object a, Object b) {
            return a==b;
        }
    };
    NEqualizer<?> DEFAULT =new NEqualizer<Object>() {
        @Override
        public boolean equals(Object a, Object b) {
            return Objects.equals(a, b);
        }
    };

    /**
     * Creates a new instance of of ref.
     *
     * @return of ref result
     */
    static <T> NEqualizer<T> ofRef(){return (NEqualizer) REF;}
    /**
     * Creates a new instance of of default.
     *
     * @return of default result
     */
    static <T> NEqualizer<T> ofDefault(){return (NEqualizer) DEFAULT;}

    boolean equals(T a, T b);
}
