package net.thevpc.nuts.util;

import java.util.Objects;

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

    static <T> NEqualizer<T> ofRef(){return (NEqualizer) REF;}
    static <T> NEqualizer<T> ofDefault(){return (NEqualizer) DEFAULT;}

    boolean equals(T a, T b);
}
