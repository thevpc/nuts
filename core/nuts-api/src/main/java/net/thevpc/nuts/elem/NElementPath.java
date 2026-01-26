package net.thevpc.nuts.elem;

public interface NElementPath {
    static NElementPath ofRoot() {
        return NElements.of().createRootPath();
    }

    int size();

    NElementPath param(String name);

    NElementPath ann(String name);

    NElementPath group(String group, int index);

    NElementPath group(String group, String name);

    NElementPath parent();

    boolean isRoot();

    NElementPath child(String name);

    NElementPath child(int i);

    NElementPath param(int i);

    NElementPath ann(int i);
}
