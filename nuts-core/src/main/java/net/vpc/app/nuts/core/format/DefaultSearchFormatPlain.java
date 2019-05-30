/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsIncrementalFormat;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatPlain extends DefaultSearchFormatBase<NutsIncrementalFormat> {

    public DefaultSearchFormatPlain(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.PLAIN);
    }

    @Override
    public void formatNext(Object object, long index) {
        FormattableNutsId fid = FormattableNutsId.of(object, getWs(), getValidSession());
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getValidOut().print(object);
            getValidOut().println();
            getValidOut().flush();
        }
    }

    private void formatElement(FormattableNutsId id, long index) {
        getValidOut().printf(id.getSingleColumnRow(getDisplayOptions()));
        getValidOut().println();
        getValidOut().flush();
    }

}
