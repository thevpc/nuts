/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.NBlankable;

import java.util.Arrays;

/**
 *
 * @author thevpc
 */
public class MdTable extends MdAbstractElement {
    private MdColumn[] columns;
    private MdRow[] rows;

    public MdTable(MdColumn[] columns, MdRow[] rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public MdColumn[] getColumns() {
        return columns;
    }

    public MdRow[] getRows() {
        return rows;
    }

    @Override
    public MdElementType type() {
        return MdElementType.TABLE;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public boolean isEndWithNewline() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdTable mdTable = (MdTable) o;
        return Arrays.equals(columns, mdTable.columns) && Arrays.equals(rows, mdTable.rows);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(columns);
        result = 31 * result + Arrays.hashCode(rows);
        return result;
    }

    @Override
    public boolean isBlank() {
        return
                (columns==null || Arrays.stream(columns).allMatch(x-> NBlankable.isBlank(x)))
                || (rows==null || Arrays.stream(rows).allMatch(x-> NBlankable.isBlank(x)))
                ;
    }
}
