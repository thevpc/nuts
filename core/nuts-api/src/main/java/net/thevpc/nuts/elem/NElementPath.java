package net.thevpc.nuts.elem;

public interface NElementPath {
    static NElementPath ofRoot() {
        return NElements.of().createRootPath();
    }

    int size();

    NElementPath parent();

    boolean isRoot();

    NElementPath child(int i);

    NElementPath param(int i);

    NElementPath ann(int i);
}
