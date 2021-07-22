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

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdRow extends MdAbstractElement {

    private MdElement[] cells;
    private boolean header;

    public MdRow(MdElement[] cells, boolean header) {
        this.cells = cells;
        this.header = header;
    }

    public boolean isHeader() {
        return header;
    }

    public MdElement[] getCells() {
        return cells;
    }

    public MdElement get(int index) {
        return cells[index];
    }

    public int size(){
        return cells.length;
    }
    
    @Override
    public MdElementType type() {
        return MdElementType.ROW;
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
        MdRow mdRow = (MdRow) o;
        return header == mdRow.header && Arrays.equals(cells, mdRow.cells);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(header);
        result = 31 * result + Arrays.hashCode(cells);
        return result;
    }
}
