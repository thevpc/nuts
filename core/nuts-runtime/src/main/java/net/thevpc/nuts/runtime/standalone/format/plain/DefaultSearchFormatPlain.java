/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.plain;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.text.NText;

/**
 * @author thevpc
 */
public class DefaultSearchFormatPlain extends DefaultSearchFormatBase {

    public DefaultSearchFormatPlain(NWorkspace workspace, NPrintStream writer, NFetchDisplayOptions options) {
        super(workspace, writer, NContentType.PLAIN, options);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (getDisplayOptions().configureFirst(cmdLine)) {
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
        NIdFormatHelper fid = NIdFormatHelper.of(object);
        if (fid != null) {
            formatElement(fid, index);
        } else {
            getWriter()
                    .resetLine()
                    .println(object)
                    .flush();
        }
    }

    private void formatElement(NIdFormatHelper id, long index) {
        NText s = id.getSingleColumnRow(getDisplayOptions());
        getWriter()
                .resetLine()
                .println(s)
                .flush();
    }

}
