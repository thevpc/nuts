package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.text.*;
import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegion;
import net.thevpc.nuts.util.NStringUtils;

import java.io.PrintStream;

public class DefaultCellDef implements NTableCellDef {

    int colspan = 1;
    int rowspan = 1;
    int x;
    int y;
    NPositionType verticalAlign = NPositionType.FIRST;
    NPositionType horizontalAlign = NPositionType.CENTER;

    NText content;
    NTextRegion renderedContent;
    boolean header;

    public DefaultCellDef(boolean header) {
        this.header = header;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public NTextRegion getRenderedContent() {
        return renderedContent;
    }

    public void setRenderedContent(NTextRegion renderedContent) {
        this.renderedContent = renderedContent;
    }

    public void copyFrom(NTableCellDef other) {
        if (other != null) {
            this.colspan = other.colspan() <= 1 ? 1 : other.colspan();
            this.rowspan = other.rowspan() <= 1 ? 1 : other.rowspan();
            this.content = other.content();
            this.horizontalAlign = other.horizontalAlign() == null ? NPositionType.FIRST : other.horizontalAlign();
            this.verticalAlign = other.verticalAlign() == null ? NPositionType.FIRST : other.verticalAlign();
        }
    }

    public void copyFrom(NTableCell other) {
        if (other != null) {
            this.colspan = other.colspan() <= 1 ? 1 : other.colspan();
            this.rowspan = other.rowspan() <= 1 ? 1 : other.rowspan();
            this.content = other.content();
            this.horizontalAlign = other.horizontalAlign() == null ? NPositionType.FIRST : other.horizontalAlign();
            this.verticalAlign = other.verticalAlign() == null ? NPositionType.FIRST : other.verticalAlign();
        }
    }

    public void copyNonDefaultFrom(NTableCell other) {
        if (other != null) {
            if (other.colspan() >= 1) {
                this.colspan = Math.max(other.colspan(), 1);
            }
            if (other.rowspan() >= 1) {
                this.rowspan = Math.max(other.rowspan(), 1);
            }
            if (other.content() != null) {
                this.content = other.content();
            }
            if (other.horizontalAlign() != null) {
                this.horizontalAlign = other.horizontalAlign();
            }
            if (other.verticalAlign() != null) {
                this.verticalAlign = other.verticalAlign();
            }
        }
    }

    @Override
    public NPositionType horizontalAlign() {
        return horizontalAlign;
    }

    @Override
    public NPositionType verticalAlign() {
        return verticalAlign;
    }

    @Override
    public NTableCellBuilder builder() {
        return new DefaultNTableCellBuilder(this);
    }

    @Override
    public int colspan() {
        return colspan;
    }

    @Override
    public DefaultCellDef colspan(int colspan) {
        this.colspan = colspan <= 0 ? 1 : colspan;
        return this;
    }

    @Override
    public int rowspan() {
        return rowspan;
    }

    @Override
    public DefaultCellDef rowspan(int rowspan) {
        this.rowspan = rowspan <= 0 ? 1 : rowspan;
        return this;
    }

    @Override
    public int x() {
        return x;
    }

    //        public void setX(int x) {
//            this.x = x;
//        }
    @Override
    public int y() {
        return y;
    }

    //        public void setY(int y) {
//            this.y = y;
//        }
    @Override
    public NText content() {
        return content;
    }

    @Override
    public DefaultCellDef content(NText content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        return "Cell{"
                + x + "->" + (x + colspan)
                + ", " + y + "->" + (y + rowspan)
                + ", " + content
                + '}';
    }

    public void dump(PrintStream out, String prefix) {
        out.println(prefix + "x,y  = " + x + "," + y);
        out.println(prefix + "  colspan,rowspan  = " + colspan + "," + rowspan);
        out.println(prefix + "  charColumns x charLines = " + renderedContent.columns() + " x " + renderedContent.rows());
        out.println(prefix + "  svalue  = " + NStringUtils.formatStringLiteral(content.filteredText()));
    }

}
