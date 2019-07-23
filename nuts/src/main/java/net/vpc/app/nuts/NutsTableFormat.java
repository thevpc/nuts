/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 * @since 0.5.5
 */
public interface NutsTableFormat extends NutsFormat {

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsTableFormat session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsTableFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsTableFormat configure(boolean skipUnsupported, String... args);

    boolean isVisibleHeader();

    NutsTableFormat setVisibleHeader(boolean visibleHeader);

    NutsTableBordersFormat getBorder();

    NutsTableFormat setBorder(NutsTableBordersFormat border);

    NutsTableFormat setVisibleColumn(int col, boolean visible);

    NutsTableFormat unsetVisibleColumn(int col);

    Boolean getVisibleColumn(int col);

    NutsTableFormat setCellFormat(NutsTableCellFormat formatter);

    NutsTableModel getModel();
    
    NutsMutableTableModel createModel();

    NutsTableFormat setModel(NutsTableModel model);

    enum Separator {
        FIRST_ROW_START,
        FIRST_ROW_LINE,
        FIRST_ROW_SEP,
        FIRST_ROW_END,
        ROW_START,
        ROW_SEP,
        ROW_END,
        MIDDLE_ROW_START,
        MIDDLE_ROW_LINE,
        MIDDLE_ROW_SEP,
        MIDDLE_ROW_END,
        LAST_ROW_START,
        LAST_ROW_LINE,
        LAST_ROW_SEP,
        LAST_ROW_END;

        /**
         * lower-cased identifier for the enum entry
         */
        private final String id;

        Separator() {
            this.id = name().toLowerCase().replace('_', '-');
        }

        /**
         * lower cased identifier.
         * @return lower cased identifier
         */
        public String id() {
            return id;
        }
    }

}
