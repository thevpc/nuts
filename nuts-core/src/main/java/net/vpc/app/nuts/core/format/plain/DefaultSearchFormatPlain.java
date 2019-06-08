/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.plain;

import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIncrementalFormatContext;
import net.vpc.app.nuts.core.format.FormattableNutsId;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsIncrementalFormatHandler;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatPlain implements NutsIncrementalFormatHandler {

private NutsFetchDisplayOptions displayOptions;

    @Override
    public void init(NutsIncrementalFormatContext context) {
        displayOptions = new NutsFetchDisplayOptions(context.getWorkspace());
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        return NutsOutputFormat.PLAIN;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if(displayOptions.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    
    @Override
    public void start(NutsIncrementalFormatContext context) {
    }
    
    @Override
    public void complete(NutsIncrementalFormatContext context, long count) {
        
    }

    @Override
    public void next(NutsIncrementalFormatContext context, Object object, long index) {
        FormattableNutsId fid = FormattableNutsId.of(object, context.getWorkspace(), context.getSession());
        if (fid != null) {
            formatElement(context,fid, index);
        } else {
            context.getWriter().print(object);
            context.getWriter().println();
            context.getWriter().flush();
        }
    }

    private void formatElement(NutsIncrementalFormatContext context,FormattableNutsId id, long index) {
        context.getWriter().printf(id.getSingleColumnRow(displayOptions));
        context.getWriter().println();
        context.getWriter().flush();
    }

}
