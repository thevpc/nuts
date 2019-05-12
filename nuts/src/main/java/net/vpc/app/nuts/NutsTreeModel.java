package net.vpc.app.nuts;

import java.util.List;

public interface NutsTreeModel<T> {

    T getRoot();

    List<T> getChildren(T o);
}
