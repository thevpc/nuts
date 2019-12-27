/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.plain;

import java.io.PrintStream;
import java.io.PrintWriter;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.runtime.format.NutsIdFormatHelper;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.runtime.format.DefaultSearchFormatBase;
import net.vpc.app.nuts.runtime.format.NutsFetchDisplayOptions;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatPlain extends DefaultSearchFormatBase {

    public DefaultSearchFormatPlain(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsOutputFormat.PLAIN,options);
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
        NutsIdFormatHelper fid = NutsIdFormatHelper.of(object, getSession());
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getWriter().print(object);
            getWriter().println();
            getWriter().flush();
        }
    }

    private void formatElement(NutsIdFormatHelper id, long index) {
        getWriter().printf(id.getSingleColumnRow(getDisplayOptions()));
        getWriter().println();
        getWriter().flush();
    }

}
