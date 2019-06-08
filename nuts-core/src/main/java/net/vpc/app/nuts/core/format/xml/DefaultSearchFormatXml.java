/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.xml;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIncrementalFormatContext;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsIncrementalFormatHandler;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatXml implements NutsIncrementalFormatHandler {

    private boolean compact;
    private String rootName = "root";

    private NutsFetchDisplayOptions displayOptions;

    @Override
    public NutsOutputFormat getOutputFormat() {
        return NutsOutputFormat.XML;
    }

    public String getRootName() {
        return rootName;
    }


    @Override
    public void init(NutsIncrementalFormatContext context) {
        displayOptions = new NutsFetchDisplayOptions(context.getWorkspace());
    }

    @Override
    public void start(NutsIncrementalFormatContext context) {
        context.getWriter().println("<" + rootName + ">");
    }

    @Override
    public void next(NutsIncrementalFormatContext context, Object object, long index) {
        NutsXmlUtils.print(String.valueOf(index), object, context.getWriter(), compact, context.getWorkspace());
    }

    @Override
    public void complete(NutsIncrementalFormatContext context, long count) {
        context.getWriter().println("</" + rootName + ">");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        if(displayOptions.configureFirst(cmd)) {
            return true;
        }
        switch (a.getStringKey()) {
            case "--compact": {
                this.compact = cmd.nextBoolean().getBooleanValue();
                return true;
            }
            case "--root-name": {
                this.rootName = cmd.nextString().getStringValue();
                return true;
            }
        }
        return false;
    }
}
