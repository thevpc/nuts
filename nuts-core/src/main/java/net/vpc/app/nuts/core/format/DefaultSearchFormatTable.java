/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.util.Arrays;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatTable extends DefaultSearchFormatBase<NutsIncrementalOutputFormat> {

    private boolean compact;
    private NutsTableFormat table;

    public DefaultSearchFormatTable(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.TABLE);
        table = ws.formatter().createTableFormat();
    }

    @Override
    public boolean configureFirst(NutsCommand cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "--compact": {
                this.compact = cmd.nextBoolean().getValue().getBoolean();
                return true;
            }
        }
        return super.configureFirst(cmd);
    }

    @Override
    public void startImpl() {
        table.addHeaderCells(Arrays.stream(getDisplayOptions().getDisplays()).map(x -> CoreCommonUtils.getEnumString(x)).toArray());
    }

    @Override
    public void nextImpl(Object object, long index) {
        FormattableNutsId fid = FormattableNutsId.of(object, getWs(), getValidSession());
        if (fid != null) {
            formatElement(fid, index);
        } else {
            table.newRow().addCell(object);
        }
        getValidOut().flush();
    }

    public void formatElement(FormattableNutsId id, long index) {
        table.newRow().addCells((Object[]) id.getMultiColumnRow(getDisplayOptions()));
    }

    @Override
    public void completeImpl(long count) {
        table.println(getValidOut());
    }

}
