package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.text.NTableCell;
import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegion;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NStringUtils;

import java.io.PrintStream;

public class DefaultCell implements NTableCell {

    int colspan = 1;
    int rowspan = 1;
    int x;
    int y;
    NPositionType vAlign;
    NPositionType hAlign;

    NText content;
    NTextRegion renderedContent;
    boolean header;

    public DefaultCell(boolean header) {
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

    @Override
    public int getColspan() {
        return colspan;
    }

    @Override
    public DefaultCell setColspan(int colspan) {
        this.colspan = colspan <= 0 ? 1 : colspan;
        return this;
    }

    @Override
    public int getRowspan() {
        return rowspan;
    }

    @Override
    public DefaultCell setRowspan(int rowspan) {
        this.rowspan = rowspan <= 0 ? 1 : rowspan;
        return this;
    }

    @Override
    public int getX() {
        return x;
    }

    //        public void setX(int x) {
//            this.x = x;
//        }
    @Override
    public int getY() {
        return y;
    }

    //        public void setY(int y) {
//            this.y = y;
//        }
    @Override
    public NText getContent() {
        return content;
    }

    @Override
    public DefaultCell setContent(NText content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        return "Cell{"
                + "" + x + "->" + (x + colspan)
                + ", " + y + "->" + (y + rowspan)
                + ", " + content
                + '}';
    }

    public void dump(PrintStream out, String prefix) {
        out.println(prefix + "x,y  = " + x+","+y);
        out.println(prefix + "  colspan,rowspan  = " + colspan+","+rowspan);
        out.println(prefix + "  charColumns x charLines = " + renderedContent.columns()+" x "+ renderedContent.rows());
        out.println(prefix + "  svalue  = " + NStringUtils.formatStringLiteral(content.filteredText()));
    }

}
