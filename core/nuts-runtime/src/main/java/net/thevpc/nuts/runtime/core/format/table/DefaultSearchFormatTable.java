/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.table;

import java.io.PrintStream;
import java.util.Arrays;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.format.NutsIdFormatHelper;
import net.thevpc.nuts.runtime.core.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.core.util.CoreEnumUtils;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatTable extends DefaultSearchFormatBase {

    private NutsTableFormat table;
    private NutsMutableTableModel model;

    public DefaultSearchFormatTable(NutsSession session, NutsPrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.TABLE, options);
    }

    public NutsMutableTableModel getTableModel(NutsSession ws) {
        getTable(ws);
        return model;
    }

    public NutsTableFormat getTable(NutsSession ws) {
        if (table == null) {
            table = ws.formats().table();
            model = table.createModel();
            table.setValue(model);
            if (getSession() != null && getSession().getOutputFormatOptions() != null) {
                for (String outputFormatOption : getSession().getOutputFormatOptions()) {
                    if (outputFormatOption != null) {
                        table.configure(true, NutsCommandLine.parse(outputFormatOption,ws));
                    }
                }
            }
        }
        return table;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        getTableModel(getSession())
                .addHeaderCells(
                        Arrays.stream(getDisplayOptions().getDisplayProperties())
                                .map(x -> CoreEnumUtils.getEnumString(x)).toArray()
                );
    }

    @Override
    public void next(Object object, long index) {
        NutsIdFormatHelper fid = NutsIdFormatHelper.of(object, getSession());
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getTableModel(getSession()).newRow().addCell(object);
        }
        getWriter().flush();
    }

    public void formatElement(NutsIdFormatHelper id, long index) {
        getTableModel(getSession()).newRow().addCells((Object[]) id.getMultiColumnRow(getDisplayOptions()));
    }

    @Override
    public void complete(long count) {
        getTable(getSession()).println(getWriter());
    }

}
