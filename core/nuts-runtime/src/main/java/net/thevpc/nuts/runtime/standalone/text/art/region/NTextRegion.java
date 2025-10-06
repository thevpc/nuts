package net.thevpc.nuts.runtime.standalone.text.art.region;

import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.text.NTextFormattable;
import net.thevpc.nuts.text.NPrimitiveText;
import net.thevpc.nuts.text.NText;

/**
 * immutable 2D text representation of a string
 * by default it is left aligned, so that, lines could be not of same length.
 * you could call ensureColumns to ensure all rows hase the same width.
 */
public interface NTextRegion extends NTextFormattable {
    static NTextRegion of(NText text) {
        return new NTextRegionImpl(text);
    }

    static NTextRegion ofWhitespace(int charWidth, int charHeight) {
        return new NTextRegionImpl((NPrimitiveText)NText.of(" "),charWidth,charHeight);
    }

    NTextRegion trimLeft();

    NTextRegion ensureColumns();

    NPrimitiveText charAt(int column, int row);

    NTextRegion concatHorizontally(NText other);

    NTextRegion concatHorizontally(NTextRegion other);

    NTextRegion concatVertically(NText other);

    NTextRegion concatVertically(NTextRegion other);

    NTextRegion replaceSubRegion(NTextRegion other, int col, int row, NPositionType horizontalAlign, NPositionType valign);

    NTextRegion subRegion(int col, int row, int toCol, int toRow);

    NTextRegion resize(int columns, int rows, NPositionType horizontalAlign, NPositionType verticalAlign);

    int rows();

    int columns();

    NText lineAt(int cellLineIndex);

    /**
     * return a text of all chars in the columns separated with newline
     *
     * @param cellColumnIndex column index
     */
    NText columnAt(int cellColumnIndex);
}
