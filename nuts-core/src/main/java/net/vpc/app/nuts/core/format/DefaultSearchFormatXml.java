/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsIncrementalFormat;
import net.vpc.app.nuts.core.util.NutsXmlUtils;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase<NutsIncrementalFormat> {

    private boolean compact;

    public DefaultSearchFormatXml(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.XML);
    }

    @Override
    public void formatStart() {
        getValidOut().println("<root>");
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
    public void formatNext(Object object, long index) {
        NutsXmlUtils.print(String.valueOf(index), object, getValidOut(), compact, getCanonicalBuilder());
    }

    @Override
    public void formatComplete(long count) {
        getValidOut().println("</root>");
    }

}
