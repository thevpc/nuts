/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.json;

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
public class DefaultSearchFormatJson implements NutsIncrementalFormatHandler {

    private boolean compact;

    private NutsFetchDisplayOptions displayOptions;

    @Override
    public NutsOutputFormat getOutputFormat() {
        return NutsOutputFormat.JSON;
    }

    @Override
    public void init(NutsIncrementalFormatContext context) {
        displayOptions = new NutsFetchDisplayOptions(context.getWorkspace());
    }

    @Override
    public void start(NutsIncrementalFormatContext context) {
        context.getWriter().println("[");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        if (displayOptions.configureFirst(cmd)) {
            return true;
        }
        switch (a.getStringKey()) {
            case "--compact": {
                this.compact = cmd.nextBoolean().getBooleanValue();
                return true;
            }
        }
        return false;
    }

    @Override
    public void next(NutsIncrementalFormatContext context, Object object, long index) {
        if (index > 0) {
            context.getWriter().print(", ");
        }
        context.getWriter().printf("%N%n", context.getWorkspace().format().json().compact(isCompact()).toJsonString(object));
        context.getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

    @Override
    public void complete(NutsIncrementalFormatContext context, long count) {
        context.getWriter().println("]");
    }

}
