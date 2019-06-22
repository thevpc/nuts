/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.table;

import java.io.PrintWriter;
import java.util.Arrays;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.core.format.FormattableNutsId;
import net.vpc.app.nuts.core.format.DefaultSearchFormatBase;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatTable extends DefaultSearchFormatBase {

    private NutsTableFormat table;

    public DefaultSearchFormatTable(NutsSession session, PrintWriter writer,NutsFetchDisplayOptions options) {
        super(session, writer, NutsOutputFormat.TABLE,options);
    }

    public NutsTableFormat getTable(NutsWorkspace ws) {
        if (table == null) {
            table = ws.format().table();
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
        getTable(getWorkspace()).addHeaderCells(Arrays.stream(getDisplayOptions().getDisplayProperties())
                .map(x -> CoreCommonUtils.getEnumString(x)).toArray());
    }

    @Override
    public void next(Object object, long index) {
        FormattableNutsId fid = FormattableNutsId.of(object, getSession());
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getTable(getWorkspace()).newRow().addCell(object);
        }
        getWriter().flush();
    }

    public void formatElement(FormattableNutsId id, long index) {
        getTable(getWorkspace()).newRow().addCells((Object[]) id.getMultiColumnRow(getDisplayOptions()));
    }

    @Override
    public void complete(long count) {
        getTable(getWorkspace()).println(getWriter());
    }

}
