/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.props;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.format.NutsFormatUtils;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.format.DefaultSearchFormatBase;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatProps extends DefaultSearchFormatBase {

    public DefaultSearchFormatProps(NutsSession session, PrintWriter writer) {
        super(session, writer, NutsOutputFormat.PROPS);
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
        Map<String, String> p = new LinkedHashMap<>();
        NutsFormatUtils.putAllInProps(String.valueOf(index + 1), p,
                getWorkspace().format().element().toElement(object)
        );
        CoreIOUtils.storeProperties(p, getWriter(), false);
        getWriter().flush();
    }

}
