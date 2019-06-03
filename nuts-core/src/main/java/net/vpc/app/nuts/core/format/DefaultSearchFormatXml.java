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
import net.vpc.app.nuts.core.util.NutsXmlUtils;
import net.vpc.app.nuts.NutsIncrementalOutputFormat;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase<NutsIncrementalOutputFormat> {

    private boolean compact;
    private String rootName = "root";

    public DefaultSearchFormatXml(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.XML);
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        getValidOut().println("<" + rootName + ">");
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
            case "--root-name": {
                this.rootName = cmd.nextString().getValue().getString();
                return true;
            }
        }
        return super.configureFirst(cmd);
    }

    @Override
    public void nextImpl(Object object, long index) {
        NutsXmlUtils.print(String.valueOf(index), object, getValidOut(), compact, getCanonicalBuilder());
    }

    @Override
    public void completeImpl(long count) {
        getValidOut().println("</" + rootName + ">");
    }

}
