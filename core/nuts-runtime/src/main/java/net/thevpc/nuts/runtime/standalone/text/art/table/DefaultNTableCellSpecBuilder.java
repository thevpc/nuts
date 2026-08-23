package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.text.*;

import java.util.Objects;

public class DefaultNTableCellSpecBuilder implements NTableCellSpecBuilder {
    private int colspan;
    private int rowspan;
    private NText content;
    private NPositionType verticalAlign;
    private NPositionType horizontalAlign;

    public DefaultNTableCellSpecBuilder() {
        this.colspan = 1;
        this.rowspan = 1;
        this.content = null;
        this.horizontalAlign = NPositionType.FIRST;
        this.verticalAlign = NPositionType.CENTER;
    }

    public DefaultNTableCellSpecBuilder(NTableCellSpecBuilder other) {
        if (other != null) {
            this.colspan = other.colspan() < 1 ? 0 : other.colspan();
            this.rowspan = other.rowspan() < 1 ? 0 : other.rowspan();
            this.content = other.content();
            this.horizontalAlign = other.horizontalAlign();
            this.verticalAlign = other.verticalAlign();
        } else {
            this.colspan = 0;
            this.rowspan = 0;
            this.content = null;
            this.horizontalAlign = null;
            this.verticalAlign = null;
        }
    }

    public DefaultNTableCellSpecBuilder(NTableCell other) {
        if (other != null) {
            this.colspan = other.colspan()<=1?0:other.colspan();
            this.rowspan = other.rowspan()<=1?0:other.colspan();
            this.content = other.content();
            this.horizontalAlign = other.horizontalAlign();
            this.verticalAlign = other.verticalAlign();
        } else {
            this.colspan = 0;
            this.rowspan = 0;
            this.content = null;
            this.horizontalAlign = null;
            this.verticalAlign = null;
        }
    }
    public DefaultNTableCellSpecBuilder(NTableCellDef other) {
        if (other != null) {
            this.colspan = other.colspan() < 1 ? 0 : other.colspan();
            this.rowspan = other.rowspan() < 1 ? 0 : other.rowspan();
            this.content = other.content();
            this.horizontalAlign = other.horizontalAlign();
            this.verticalAlign = other.verticalAlign();
        } else {
            this.colspan = 0;
            this.rowspan = 0;
            this.content = null;
            this.horizontalAlign = null;
            this.verticalAlign = null;
        }
    }

    public DefaultNTableCellSpecBuilder(NText content, int colspan, int rowspan, NPositionType horizontalAlign, NPositionType verticalAlign) {
        this.colspan = colspan < 1 ? 0 : colspan;
        this.rowspan = rowspan < 1 ? 0 : rowspan;
        this.content = content;
        this.horizontalAlign = horizontalAlign;
        this.verticalAlign = verticalAlign;
    }

    @Override
    public NPositionType verticalAlign() {
        return verticalAlign;
    }

    @Override
    public NPositionType horizontalAlign() {
        return horizontalAlign;
    }

    public NTableCellSpecBuilder verticalAlign(NPositionType verticalAlign) {
        this.verticalAlign = verticalAlign;
        return this;
    }

    public NTableCellSpecBuilder horizontalAlign(NPositionType horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
        return this;
    }

    @Override
    public int colspan() {
        return colspan;
    }

    public NTableCellSpecBuilder colspan(int colspan) {
        this.colspan = Math.max(colspan, 1);
        return this;
    }

    @Override
    public int rowspan() {
        return rowspan;
    }

    public NTableCellSpecBuilder rowspan(int rowspan) {
        this.rowspan = Math.max(rowspan, 1);
        return this;
    }

    @Override
    public NText content() {
        return content;
    }

    public NTableCellSpecBuilder content(NText content) {
        this.content = content;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNTableCellSpecBuilder that = (DefaultNTableCellSpecBuilder) o;
        return colspan == that.colspan && rowspan == that.rowspan && Objects.equals(content, that.content) && verticalAlign == that.verticalAlign && horizontalAlign == that.horizontalAlign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(colspan, rowspan, content, verticalAlign, horizontalAlign);
    }

    @Override
    public NTableCell build() {
        return new DefaultNTableCell(this);
    }
}
