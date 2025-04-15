package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders.TsonArrayBuilderImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.UnmodifiableArrayList;

import java.util.*;

public class TsonMatrixImpl extends AbstractNonPrimitiveTsonElement implements TsonMatrix {
    private final UnmodifiableArrayList<TsonArray> rows;
    private final String name;
    private TsonElementList params;

    public TsonMatrixImpl(String name, TsonElementList params, UnmodifiableArrayList<TsonArray> rows) {
        super(
                name == null && params == null ? TsonElementType.MATRIX
                        : name == null && params != null ? TsonElementType.PARAMETRIZED_MATRIX
                        : name != null && params == null ? TsonElementType.NAMED_MATRIX
                        : TsonElementType.NAMED_PARAMETRIZED_MATRIX
        );
        this.name = name;
        this.params = params;
        this.rows = rows;
    }

    @Override
    public TsonMatrix toMatrix() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    @Override
    public int rowSize() {
        return rows.size();
    }

    public int columnSize() {
        return rows.size() == 0 ? 0 : rows.get(0).size();
    }

    @Override
    public List<TsonArray> rows() {
        return this.rows();
    }

    @Override
    public TsonElement cell(int col, int row) {
        TsonArray a = rows.get(row);
        return a.get(col);
    }

    @Override
    public TsonArray row(int row) {
        return rows.get(row);
    }

    @Override
    public TsonArray column(int column) {
        List<TsonElement> c = new ArrayList<>(rowSize());
        for (int row = 0; row < rowSize(); row++) {
            c.add(TsonMatrixImpl.this.cell(column, row));
        }
        return TsonUtils.toArray(c);
    }

    @Override
    public List<TsonArray> columns() {
        return new AbstractList<TsonArray>() {
            @Override
            public TsonArray get(int column) {
                return column(column);
            }

            @Override
            public int size() {
                return columnSize();
            }
        };
    }

    @Override
    public Iterator<TsonArray> iterator() {
        return this.rows().iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TsonMatrixImpl that = (TsonMatrixImpl) o;
        return Objects.equals(rows, that.rows) &&
                Objects.equals(name, that.name()) &&
                Objects.equals(params, that.params())
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), name, params, rows);
        result = 31 * result + Objects.hashCode(rows);
        return result;
    }

    @Override
    public TsonArrayBuilder builder() {
        return new TsonArrayBuilderImpl().merge(this);
    }

    @Override
    public boolean visit(TsonDocumentVisitor visitor) {
        if (params != null) {
            for (TsonElement element : params) {
                if (!visitor.visit(element)) {
                    return false;
                }
            }
        }
        for (TsonElement element : rows) {
            if (!visitor.visit(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected int compareCore(TsonElement o) {
        TsonMatrix na = o.toMatrix();
        int i = this.name().compareTo(na.name());
        if (i != 0) {
            return i;
        }
        i = TsonUtils.compareElementsArray(this.params(), na.params());
        if (i != 0) {
            return i;
        }
        return TsonUtils.compareElementsArray((List) this.rows(), (List) na.rows());
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitElementStart();
        if (name != null) {
            visitor.visitNamedStart(this.name());
        }
        if (params != null) {
            visitor.visitParamsStart();
            for (TsonElement param : this.params()) {
                visitor.visitParamElementStart();
                param.visit(visitor);
                visitor.visitParamElementEnd();
            }
            visitor.visitParamsEnd();
        }
        visitor.visitNamedArrayStart();
        for (TsonArray element : this.rows()) {
            visitor.visitArrayElementStart();
            element.visit(visitor);
            visitor.visitArrayElementEnd();
        }
        visitor.visitArrayEnd();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isParametrized() {
        return params != null;
    }

    @Override
    public TsonElementList params() {
        return params;
    }

    @Override
    public boolean isNamed() {
        return name != null;
    }

    @Override
    public int paramsCount() {
        return params == null ? 0 : params.size();
    }
}
