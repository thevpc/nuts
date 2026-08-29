package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.text.NTableCell;
import net.thevpc.nuts.text.NTableCellBuilder;
import net.thevpc.nuts.text.NText;

import java.util.Objects;

public class DefaultNTableCell implements NTableCell {
    private final int colspan;
    private final int rowspan;
    private final NText content;
    private final NPositionType verticalAlign;
    private final NPositionType horizontalAlign;

    public DefaultNTableCell(NTableCellBuilder other) {
        if (other != null) {
            this.colspan = other.colspan() < 1 ? 0 : other.colspan();
            this.rowspan = other.rowspan() < 1 ? 0 : other.rowspan();
            this.content = other.content();
            this.horizontalAlign = other.horizontalAlign();
            this.verticalAlign = other.verticalAlign();
        } else {
            this.colspan = 1;
            this.rowspan = 1;
            this.content = null;
            this.horizontalAlign = NPositionType.FIRST;
            this.verticalAlign = NPositionType.CENTER;
        }
    }

    public DefaultNTableCell(NText content, int colspan, int rowspan, NPositionType horizontalAlign, NPositionType verticalAlign) {
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

    @Override
    public int colspan() {
        return colspan;
    }

    @Override
    public int rowspan() {
        return rowspan;
    }

    @Override
    public NText content() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNTableCell that = (DefaultNTableCell) o;
        return colspan == that.colspan && rowspan == that.rowspan && Objects.equals(content, that.content) && verticalAlign == that.verticalAlign && horizontalAlign == that.horizontalAlign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(colspan, rowspan, content, verticalAlign, horizontalAlign);
    }

    @Override
    public NTableCellBuilder builder() {
        return new DefaultNTableCellBuilder(this);
    }
}
