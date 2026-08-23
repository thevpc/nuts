/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.text;

import net.thevpc.nuts.internal.rpi.NTextRPI;

/**
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NTableCellSpecBuilder {
    static NTableCellSpecBuilder of() {
        return NTextRPI.of().createCellSpecBuilder();
    }

    static NTableCellSpecBuilder of(NText content) {
        return of().content(content);
    }

    static NTableCellSpecBuilder of(NText content, int colspan, int rowspan) {
        return of().content(content).colspan(colspan).rowspan(rowspan);
    }

    NPositionType verticalAlign();

    NPositionType horizontalAlign();

    NTableCellSpecBuilder verticalAlign(NPositionType align);

    NTableCellSpecBuilder horizontalAlign(NPositionType align);

    int colspan();

    NTableCellSpecBuilder colspan(int colspan);

    int rowspan();

    NTableCellSpecBuilder rowspan(int rowspan);

    NText content();

    NTableCellSpecBuilder content(NText content);

    NTableCell build();
}
