/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.plain;

import java.io.PrintWriter;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.core.format.FormattableNutsId;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.format.DefaultSearchFormatBase;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatPlain extends DefaultSearchFormatBase {

    public DefaultSearchFormatPlain(NutsWorkspace ws, NutsSession session, PrintWriter writer) {
        super(ws, session, writer, NutsOutputFormat.PLAIN);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (getDisplayOptions().configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public void start() {
    }

    @Override
    public void complete(long count) {

    }

    @Override
    public void next(Object object, long index) {
        FormattableNutsId fid = FormattableNutsId.of(object, getWorkspace(), getSession());
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getWriter().print(object);
            getWriter().println();
            getWriter().flush();
        }
    }

    private void formatElement(FormattableNutsId id, long index) {
        getWriter().printf(id.getSingleColumnRow(getDisplayOptions()));
        getWriter().println();
        getWriter().flush();
    }

}
