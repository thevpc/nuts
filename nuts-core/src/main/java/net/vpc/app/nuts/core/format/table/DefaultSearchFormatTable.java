/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.table;

import java.util.Arrays;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.core.format.FormattableNutsId;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;
import net.vpc.app.nuts.NutsIncrementalFormatHandler;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatTable implements NutsIncrementalFormatHandler {

    private NutsTableFormat table;

    private NutsFetchDisplayOptions displayOptions;

    @Override
    public NutsOutputFormat getOutputFormat() {
        return NutsOutputFormat.TABLE;
    }

    @Override
    public void init(NutsIncrementalFormatContext context) {
        displayOptions = new NutsFetchDisplayOptions(context.getWorkspace());
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
        if(displayOptions.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public void start(NutsIncrementalFormatContext context) {
        getTable(context.getWorkspace()).addHeaderCells(Arrays.stream(displayOptions.getDisplayProperties())
                .map(x -> CoreCommonUtils.getEnumString(x)).toArray());
    }

    @Override
    public void next(NutsIncrementalFormatContext context, Object object, long index) {
        FormattableNutsId fid = FormattableNutsId.of(object, context.getWorkspace(), context.getSession());
        if (fid != null) {
            formatElement(context, fid, index);
        } else {
            getTable(context.getWorkspace()).newRow().addCell(object);
        }
        context.getWriter().flush();
    }

    public void formatElement(NutsIncrementalFormatContext context, FormattableNutsId id, long index) {
        getTable(context.getWorkspace()).newRow().addCells((Object[]) id.getMultiColumnRow(displayOptions));
    }

    @Override
    public void complete(NutsIncrementalFormatContext context, long count) {
        getTable(context.getWorkspace()).println(context.getWriter());
    }

}
