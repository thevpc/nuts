/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.props;

import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIncrementalFormatContext;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.format.NutsFormatUtils;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsIncrementalFormatHandler;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatProps implements NutsIncrementalFormatHandler {

    private NutsFetchDisplayOptions displayOptions;

    @Override
    public void init(NutsIncrementalFormatContext context) {
        displayOptions = new NutsFetchDisplayOptions(context.getWorkspace());
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        return NutsOutputFormat.PROPS;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (displayOptions.configureFirst(cmd)) {
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
        Map<String, String> p = new LinkedHashMap<>();
        NutsFormatUtils.putAllInProps(String.valueOf(index + 1), p,
                context.getWorkspace().format().element().toElement(object)
        );
        CoreIOUtils.storeProperties(p, context.getWriter(), false);
        context.getWriter().flush();
    }

}
