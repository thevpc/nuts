package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonElementListImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonMatrixImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TsonMatrixBuilderImpl extends AbstractTsonElementBuilder<TsonMatrixBuilder> implements TsonMatrixBuilder {
    private ArrayList<ArrayList<TsonElement>> rows = new ArrayList<>();
    private int rowsCount;
    private int columnsCount;
    private String name;
    private List<TsonElement> params;

    public TsonMatrixBuilder ensureCapacity(int columns0, int rows0) {
        int oldColumnsCount = columnsCount;
        int oldRowsCount = rowsCount;

        columnsCount = columns0 = Math.max(columns0, columnsCount);
        rowsCount = rows0 = Math.max(rows0, rowsCount);

        TsonElement nullElem = Tson.ofNull();

        if (columnsCount != oldColumnsCount) {
            for (int i = 0; i < rowsCount; i++) {
                ArrayList<TsonElement> row = rows.get(i);
                row.ensureCapacity(columns0);
                while (row.size() < columns0) {
                    row.add(nullElem);
                }
            }
        }
        if (rowsCount != oldRowsCount) {
            while (rowsCount < rows0) {
                List<TsonElement> row = new ArrayList<>(columns0);
                for (int i = 0; i < columns0; i++) {
                    row.add(nullElem);
                }
            }
        }
        return this;
    }

    @Override
    public TsonElementType type() {
        return                 name == null && params == null ? TsonElementType.MATRIX
                : name == null && params != null ? TsonElementType.PARAMETRIZED_MATRIX
                : name != null && params == null ? TsonElementType.NAMED_MATRIX
                : TsonElementType.NAMED_PARAMETRIZED_MATRIX
                ;
    }

    @Override
    public TsonElement get(int column, int row) {
        return rows.get(row).get(column);
    }

    @Override
    public TsonArray getRow(int row) {
        return TsonUtils.toArray(rows.get(row));
    }

    @Override
    public TsonArray getColumn(int column) {
        List<TsonElement> col = new ArrayList<>(rowsCount());
        for (int i = 0; i < rowsCount(); i++) {
            col.add(rows.get(i).get(column));
        }
        return TsonUtils.toArray(col);
    }

    @Override
    public TsonMatrixBuilder addRow(TsonArray element) {
        rows.add(new ArrayList<>(element.body().toList()));
        ensureCapacity(columnsCount, rowsCount + 1);
        return this;
    }

    @Override
    public TsonMatrixBuilder addRows(TsonArray... elements) {
        for (TsonArray element : elements) {
            rows.add(new ArrayList<>(element.body().toList()));
        }
        ensureCapacity(columnsCount, rowsCount + elements.length);
        return this;
    }

    @Override
    public TsonMatrixBuilder addRows(Iterable<? extends TsonArray> elements) {
        int count = 0;
        for (TsonArray element : elements) {
            rows.add(new ArrayList<>(element.body().toList()));
            count++;
        }
        ensureCapacity(columnsCount, rowsCount + count);
        return null;
    }

    @Override
    public TsonMatrixBuilder addColumn(TsonArray element) {
        ensureCapacity(columnsCount(), element.size());
        int i = 0;
        TsonElement nullElem = Tson.ofNull();
        for (ArrayList<TsonElement> row : rows) {
            if (i < element.size()) {
                row.add(element.get(i));
            } else {
                row.add(nullElem);
            }
            i++;
        }
        ensureCapacity(columnsCount() + 1, rowsCount());
        return this;
    }

    @Override
    public TsonMatrixBuilder addColumns(TsonArray... elements) {
        for (TsonArray element : elements) {
            addColumn(element);
        }
        ensureCapacity(columnsCount() + elements.length, rowsCount());
        return this;
    }


    @Override
    public TsonMatrixBuilder addColumns(Iterable<? extends TsonArray> elements) {
        int count = 0;
        for (TsonArray element : elements) {
            addColumn(element);
            count++;
        }
        ensureCapacity(columnsCount() + count, rowsCount());
        return this;
    }


    @Override
    public List<TsonArray> rows() {
        return getRows();
    }

    @Override
    public List<TsonArray> getRows() {
        return new AbstractList<TsonArray>() {
            @Override
            public TsonArray get(int index) {
                return getRow(index);
            }

            @Override
            public int size() {
                return columnsCount();
            }
        };
    }

    @Override
    public TsonMatrixBuilder removeRow(int row) {
        if (row >= 0 && row < rowsCount) {
            rows.remove(row);
            rowsCount--;
        } else {
            throw new ArrayIndexOutOfBoundsException(row);
        }
        return this;
    }

    @Override
    public TsonMatrixBuilder removeColumn(int column) {
        if (column >= 0 && column < columnsCount) {
            for (ArrayList<TsonElement> row : rows) {
                row.remove(column);
            }
            columnsCount--;
        } else {
            throw new ArrayIndexOutOfBoundsException(column);
        }
        return this;
    }

    @Override
    public int columnsCount() {
        return columnsCount;
    }

    @Override
    public int rowsCount() {
        return columnsCount;
    }

    @Override
    public TsonMatrixBuilder set(TsonElementBase element, int column, int row) {
        ensureCapacity(column, row);
        rows.get(row).set(column, Tson.of(element));
        return this;
    }

    @Override
    public Iterator<TsonArray> iterator() {
        Iterator<ArrayList<TsonElement>> t = rows.iterator();
        return new Iterator<TsonArray>() {
            @Override
            public boolean hasNext() {
                return t.hasNext();
            }

            @Override
            public TsonArray next() {
                return TsonUtils.toArray(t.next());
            }
        };
    }

    @Override
    public TsonMatrixBuilder reset() {
        rows.clear();
        name = null;
        params = null;
        rowsCount = 0;
        columnsCount = 0;
        return this;
    }


    @Override
    public TsonMatrix build() {
        List<TsonArray> arrays = new ArrayList<>();
        for (ArrayList<TsonElement> row : rows) {
            arrays.add(TsonUtils.toArray(row));
        }
        TsonMatrixImpl built = new TsonMatrixImpl(name,
                params == null ? null : new TsonElementListImpl((List) params),
                TsonUtils.unmodifiableArrays(arrays)
        );
        return (TsonMatrix) TsonUtils.decorate(
                built
                , comments(), annotations())
                ;
    }


    @Override
    public TsonMatrixBuilder merge(TsonElementBase element) {
        TsonElement e = Tson.of(element);
        switch (e.type()) {
            case UPLET:
            case NAMED_UPLET:
            {
                TsonUplet uplet = e.toUplet();
                if (uplet.isNamed()) {
                    name(uplet.name());
                }
                addParams(uplet);
                break;
            }
            case NAME: {
                name(e.toName().value());
                break;
            }
            case OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            {
                TsonObject h = e.toObject();
                name(h.name());
                addParams(h.params());
                addRow(TsonUtils.toArray(e.toObject().body()));
                break;
            }
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_ARRAY:
            {
                TsonArray h = e.toArray();
                name(h.name());
                addParams(h.params());
                addRow(TsonUtils.toArray(e.toArray().body()));
                break;
            }
            case MATRIX:
            case NAMED_MATRIX:
            case PARAMETRIZED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
            {
                TsonMatrix h = e.toMatrix();
                name(h.name());
                addParams(h.params());
                for (TsonArray m : e.toMatrix()) {
                    addRow(m);
                }
                break;
            }
        }
        return this;
    }


    /// ////////////////
    /// args

    @Override
    public boolean isParametrized() {
        return params != null;
    }

    @Override
    public TsonMatrixBuilder setParametrized(boolean parametrized) {
        if (parametrized) {
            if (params == null) {
                params = new ArrayList<>();
            }
        } else {
            params = null;
        }
        return this;
    }

    @Override
    public List<TsonElement> params() {
        return params;
    }

    @Override
    public int paramsCount() {
        return params == null ? 0 : params.size();
    }

    @Override
    public TsonMatrixBuilder clearParams() {
        if(params!=null) {
            params.clear();
        }
        return this;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public TsonMatrixBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TsonMatrixBuilder addParam(TsonElementBase element) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonMatrixBuilder removeParam(TsonElementBase element) {
        if (element != null && params != null) {
            params.remove(Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonMatrixBuilder addParam(TsonElementBase element, int index) {
        if (element != null) {
            if (params == null) {
                params = new ArrayList<>();
            }
            params.add(index, Tson.of(element).build());
        }
        return this;
    }

    @Override
    public TsonMatrixBuilder removeParamAt(int index) {
        if (params != null) {
            params.remove(index);
        }
        return this;
    }

    @Override
    public TsonMatrixBuilder addParams(TsonElement[] element) {
        if (element != null) {
            for (TsonElement tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonMatrixBuilder addParams(TsonElementBase[] element) {
        if (element != null) {
            for (TsonElementBase tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    @Override
    public TsonMatrixBuilder addParams(Iterable<? extends TsonElementBase> element) {
        if (element != null) {
            for (TsonElementBase tsonElement : element) {
                addParam(tsonElement);
            }
        }
        return this;
    }

    /// ////////////////


}
