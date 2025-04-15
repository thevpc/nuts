package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.List;

public interface TsonMatrix extends TsonElement, Iterable<TsonArray>, TsonParametrizedElement, TsonNamedElement {
    boolean isEmpty();

    int rowSize();

    int columnSize();

    List<TsonArray> rows();

    TsonElement cell(int col, int row);

    TsonArray row(int row);

    TsonArray column(int column);

    List<TsonArray> columns();

    TsonArrayBuilder builder();
}
