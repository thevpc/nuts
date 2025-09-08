package net.thevpc.nuts.runtime.standalone.text.art.table;

import java.util.Objects;

class Pos {

    int column;
    int row;

    public Pos(int column, int row) {
        this.column = column;
        this.row = row;
    }

    @Override
    public int hashCode() {

        return Objects.hash(column, row);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pos pos = (Pos) o;
        return column == pos.column
                && row == pos.row;
    }

    @Override
    public String toString() {
        return "("
                + "" + column
                + "," + row
                + ')';
    }

}
