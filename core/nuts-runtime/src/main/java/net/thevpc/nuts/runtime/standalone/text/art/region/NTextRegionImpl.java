package net.thevpc.nuts.runtime.standalone.text.art.region;

import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextBuilder;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NTextRegionImpl implements NTextRegion {

    private NPrimitiveText[][] rendered;
    private int charLines;
    private int charColumns;

//    private NTextRegion() {
//    }

    //    public NTextRegion(int c, int r) {
//        rendered=new NPrimitiveText[r][c];
//    }
    private NTextRegionImpl(NPrimitiveText[][] rendered, int charColumns, int charLines) {
        this.rendered = rendered;
        this.charColumns = charColumns;
        this.charLines = charLines;
        for (NPrimitiveText[] r : rendered) {
            NAssert.requireTrue(charColumns >= r.length, "charColumns>=" + r.length);
        }
        NAssert.requireTrue(charLines == rendered.length, "charLines==" + rendered.length);
    }

    NTextRegionImpl(NPrimitiveText a, int charColumns, int charLines) {
        NAssert.requireTrue(a.length() == 1, "length==" + 1);
        this.rendered = new NPrimitiveText[charLines][charColumns];
        this.charColumns = charColumns;
        this.charLines = charLines;
        for (int r = 0; r < charLines; r++) {
            for (int c = 0; c < charColumns; c++) {
                rendered[r][c] = a;
            }
        }
    }

    public NPrimitiveText[] charsAt(int cellLineIndex) {
        return rendered[cellLineIndex];
    }

    public NTextRegionImpl(NText str) {
        if (str == null) {
            str = NText.ofBlank();
        } else {
            str = str.normalize();
        }
        List<NTextBuilder> strings = new ArrayList<>();
        List<NText> st = str.split("\n", true);
        charColumns = 0;
        for (NText e : st) {
            if (e.type() == NTextType.PLAIN && e.isNewLine()) {
                strings.add(NTextBuilder.of());
            } else {
                if (strings.isEmpty()) {
                    strings.add(NTextBuilder.of());
                }
                NTextBuilder last = strings.get(strings.size() - 1);
                last.append(e);
                charColumns = Math.max(charColumns, last.length());
            }
        }
        charLines = strings.size();
        if (charLines == 0) {
            rendered = new NPrimitiveText[][]{{(NPrimitiveText) NText.ofSpace()}};
            charLines = 1;
            charColumns = 1;
        } else {
            rendered = new NPrimitiveText[charLines][];
            for (int i = 0, stringsSize = strings.size(); i < stringsSize; i++) {
                NTextBuilder s = strings.get(i);
                rendered[i] = s.toCharArray();
            }
        }
    }


    @Override
    public NTextRegion trimLeft() {
        int left = -1;
        if (rendered.length == 0) {
            return this;
        }
        for (int i = 0; i < rendered.length; i++) {
            int count = 0;
            for (NPrimitiveText e : rendered[i]) {
                if (e.isNewLine()) {
                    count++;
                } else {
                    break;
                }
            }
            if (left < 0 || left > count) {
                left = count;
            }
            if (left > 0) {
                NPrimitiveText[][] rendered0 = new NPrimitiveText[rendered.length][];
                for (int j = 0; j < rendered0.length; j++) {
                    rendered0[i] = Arrays.copyOfRange(rendered[j], left, rendered[i].length);
                }
                return new NTextRegionImpl(rendered0, charColumns - left, charLines);
            }
        }
        return this;
    }

    @Override
    public NPrimitiveText charAt(int column, int row) {
        NPrimitiveText[] u = rendered[row];
        if (column < u.length) {
            return u[column];
        }
        if (column < charColumns) {
            return (NPrimitiveText) NText.ofSpace();
        }
        //wil throw exception!
        return u[column];
    }

    @Override
    public NTextRegion ensureColumns() {
        NPrimitiveText[][] rendered0 = new NPrimitiveText[charLines][];
        for (int i = 0; i < rendered0.length; i++) {
            NTextBuilder sb = new DefaultNTextBuilder();
            int count = 0;
            for (NPrimitiveText e : rendered[i]) {
                sb.append(e);
                count++;
            }
            int remaining = columns() - count;
            if (remaining > 0) {
                for (int j = 0; j < remaining; j++) {
                    sb.append(NText.ofSpace());
                }
            }
            rendered0[i] = sb.toCharArray();
        }
        return new NTextRegionImpl(rendered0, charColumns, rendered0.length);
    }

    @Override
    public NTextRegion concatHorizontally(NText other) {
        return concatHorizontally(new NTextRegionImpl(other));
    }

    @Override
    public NTextRegion concatHorizontally(NTextRegion other) {
        NPrimitiveText[][] rendered0 = new NPrimitiveText[Math.max(charLines, other.rows())][];
        for (int i = 0; i < rendered0.length; i++) {
            NTextBuilder sb = new DefaultNTextBuilder();
            if (i < rendered.length) {
                for (NPrimitiveText e : rendered[i]) {
                    sb.append(e);
                }
            } else {
                for (int j = 0; j < charColumns; j++) {
                    sb.append(NText.ofSpace());
                }
            }
            if (i < other.rows()) {
                sb.append(other.lineAt(i));
            } else {
                for (int j = 0; j < other.columns(); j++) {
                    sb.append(NText.ofSpace());
                }
            }
            rendered0[i] = sb.toCharArray();
        }
        return new NTextRegionImpl(rendered0, charColumns + other.columns(), rendered0.length);
    }

    @Override
    public NTextRegion concatVertically(NText other) {
        return concatVertically(new NTextRegionImpl(other));
    }

    @Override
    public NTextRegion concatVertically(NTextRegion other) {
        NPrimitiveText[][] rendered0 = new NPrimitiveText[charLines + other.rows()][];
        int cols = Math.max(charColumns, other.columns());
        for (int i = 0; i < rendered.length; i++) {
            NTextBuilder sb = new DefaultNTextBuilder();
            sb.append(lineAt(i));
            int remaining = cols - charColumns;
            if (remaining > 0) {
                for (int j = 0; j < remaining; j++) {
                    sb.append(NText.ofSpace());
                }
            }
            rendered0[i] = sb.toCharArray();
        }
        for (int i = 0; i < other.rows(); i++) {
            NTextBuilder sb = new DefaultNTextBuilder();
            sb.append(other.lineAt(i));
            int remaining = cols - other.columns();
            if (remaining > 0) {
                for (int j = 0; j < remaining; j++) {
                    sb.append(NText.ofSpace());
                }
            }
            rendered0[i + rendered.length] = sb.toCharArray();
        }
        return new NTextRegionImpl(rendered0, Math.max(charColumns,other.columns()), rendered0.length);
    }

    @Override
    public NTextRegion replaceSubRegion(NTextRegion other, int col, int row, NPositionType halign, NPositionType valign) {
        NPrimitiveText[][] rendered0 = resize(Math.max(charColumns, col + other.columns()), Math.max(charLines, row + other.rows()), halign, valign).rendered;
        for (int r = 0; r < other.rows(); r++) {
            for (int c = 0; c < other.columns(); c++) {
                rendered0[row + r][col + c] = other.charAt(c, r);
            }
        }
        return new NTextRegionImpl(rendered0, charColumns, charLines);
    }

    @Override
    public NTextRegion subRegion(int col, int row, int toCol, int toRow) {
        NPrimitiveText[][] rendered0 = new NPrimitiveText[toRow - row][toCol - col];
        for (int i = 0; i < rendered0.length; i++) {
            System.arraycopy(rendered[i + row], col, rendered0[i], 0, toCol - col);
        }
        return new NTextRegionImpl(rendered0, toCol - col, toRow - row);
    }

    @Override
    public NTextRegionImpl resize(int columns, int rows, NPositionType horizontalAlign, NPositionType verticalAlign) {
        NPrimitiveText[][] rendered0 = new NPrimitiveText[rows][];
        switch (verticalAlign) {
            case FIRST: {
                for (int i = 0; i < rows; i++) {
                    if (i < rendered.length) {
                        NPrimitiveText[] chars = rendered[i];
                        int ll = NTextBuilder.of().appendAll(chars).length();
                        int min = Math.min(columns, ll);
                        if (min < columns) {
                            int x = columns - min;
                            NTextBuilder s = NTextBuilder.of();
                            s.appendAll(chars);
                            CoreNUtils.formatAndHorizontalAlign(s, horizontalAlign, columns);
                            rendered0[i] = s.toCharArray();
                        } else {
                            rendered0[i] = rendered[i];
                        }
                    } else {
                        rendered0[i] = NText.ofSpaces(columns).toCharArray();
                    }
                }
                break;
            }
            case LAST: {
                //FIX ME
                for (int i = 0; i < rows; i++) {
                    if (i < rows - rendered.length) {
                        rendered0[i] = NText.ofSpaces(columns).toCharArray();
                    } else {
                        NPrimitiveText[] chars = rendered[i];
                        int ll = NTextBuilder.of().appendAll(chars).length();
                        int min = Math.min(columns, ll);
                        if (min < columns) {
                            int x = columns - min;
                            NTextBuilder s = NTextBuilder.of();
                            s.appendAll(chars);
                            CoreNUtils.formatAndHorizontalAlign(s, horizontalAlign, columns);
                            rendered0[i] = s.toCharArray();
                        } else {
                            rendered0[i] = rendered[i];
                        }
                    }
                }
                break;
            }
            case CENTER: {
                //FIX ME
                for (int i = 0; i < rows; i++) {
                    if (i < rendered.length) {
                        NPrimitiveText[] chars = rendered[i];
                        int ll = NTextBuilder.of().appendAll(chars).length();
                        int min = Math.min(columns, ll);
                        if (min < columns) {
                            int x = columns - min;
                            NTextBuilder s = NTextBuilder.of();
                            s.appendAll(chars);
                            CoreNUtils.formatAndHorizontalAlign(s, horizontalAlign, columns);
                            rendered0[i] = s.toCharArray();
                        } else {
                            rendered0[i] = rendered[i];
                        }
                    } else {
                        rendered0[i] = NText.ofSpaces(columns).toCharArray();
                    }
                }
                break;
            }
        }
//            for (int i = 0; i < rendered0.length; i++) {
//                if(i<rendered.length) {
//                    rendered0[i] = new char[rendered[i].length];
//                    System.arraycopy(rendered[i], 0, rendered0[i], 0, rendered[i].length);
//                }else{
//                    rendered0[i] = new char[columns];
//                    Arrays.fill(rendered0[i],0,columns,' ');
//                }
//            }
        return new NTextRegionImpl(rendered0, columns, rows);
    }

    public NTextRegion copy() {
        NPrimitiveText[][] rendered0 = new NPrimitiveText[rendered.length][];
        for (int i = 0; i < rendered0.length; i++) {
            rendered0[i] = new NPrimitiveText[rendered[i].length];
            System.arraycopy(rendered[i], 0, rendered0[i], 0, rendered[i].length);
        }
        return new NTextRegionImpl(rendered0, charColumns, charLines);
    }

    @Override
    public int rows() {
        return charLines;
    }

    @Override
    public int columns() {
        return charColumns;
    }

    @Override
    public NText toText() {
        NTextBuilder sb = NTextBuilder.of();
        for (int i = 0, renderedLength = rendered.length; i < renderedLength; i++) {
            NPrimitiveText[] chars = rendered[i];
            if (i > 0) {
                sb.newLine();
            }
            sb.appendAll(Arrays.asList(chars));
        }
        return sb.build();
    }

    @Override
    public String toString() {
        return toText().filteredText();
    }

    @Override
    public NText lineAt(int cellLineIndex) {
        return NText.ofList(rendered[cellLineIndex]).simplify();
    }

    @Override
    public NText columnAt(int cellColumnIndex) {
        NTextBuilder sb = NTextBuilder.of();
        for (int i = 0; i < rows(); i++) {
            sb.append(charAt(cellColumnIndex, i));
            if (i < rows() - 1) {
                sb.append(NText.ofNewLine());
            }
        }
        return sb.build();
    }
}
