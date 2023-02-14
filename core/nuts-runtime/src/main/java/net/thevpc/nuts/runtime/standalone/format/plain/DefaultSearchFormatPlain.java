/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.plain;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatPlain extends DefaultSearchFormatBase {

    public DefaultSearchFormatPlain(NSession session, NPrintStream writer, NFetchDisplayOptions options) {
        super(session, writer, NContentType.PLAIN,options);
    }

    @Override
    public boolean configureFirst(NCmdLine cmd) {
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
        NIdFormatHelper fid = NIdFormatHelper.of(object, getSession());
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getWriter().resetLine();
            getWriter().print(object);
            getWriter().println();
            getWriter().flush();
        }
    }

    private void formatElement(NIdFormatHelper id, long index) {
        NString s = id.getSingleColumnRow(getDisplayOptions());
        getWriter().resetLine();
        getWriter().print(s);
        getWriter().println();
        getWriter().flush();
    }

}
