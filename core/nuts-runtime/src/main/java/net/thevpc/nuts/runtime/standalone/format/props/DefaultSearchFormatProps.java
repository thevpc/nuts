/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.props;

import java.util.LinkedHashMap;
import java.util.Map;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.NFormatUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatProps extends DefaultSearchFormatBase {

    public DefaultSearchFormatProps(NSession session, NOutputStream writer, NFetchDisplayOptions options) {
        super(session, writer, NContentType.PROPS,options);
    }

    @Override
    public boolean configureFirst(NCommandLine cmd) {
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
        Map<String, String> p = new LinkedHashMap<>();
        NSession session = getSession();
        NFormatUtils.putAllInProps(String.valueOf(index + 1), p,
                NElements.of(session)
                        .toElement(object),
                session);
        CoreIOUtils.storeProperties(p, getWriter().asPrintStream(), false, session);
        getWriter().flush();
    }

}
