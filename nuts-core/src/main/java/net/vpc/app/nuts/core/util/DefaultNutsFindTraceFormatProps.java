/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTraceFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindTraceFormatProps implements NutsTraceFormat {

    CanonicalBuilder canonicalBuilder;

    public DefaultNutsFindTraceFormatProps() {
    }
    @Override
    public NutsOutputFormat getSupportedFormat() {
        return NutsOutputFormat.PROPS;
    }
    @Override
    public void formatStart(PrintStream out, NutsWorkspace ws) {
    }

    @Override
    public void formatElement(Object object, long index, PrintStream out, NutsWorkspace ws) {
        if (canonicalBuilder == null) {
            canonicalBuilder = new CanonicalBuilder(ws).setConvertDesc(true).setConvertId(false);
        }
        Map<String,String> p = new LinkedHashMap<>();
        CoreCommonUtils.putAllInProps(String.valueOf(index+1), p, canonicalBuilder.toCanonical(object));
        CoreIOUtils.storeProperties(p, out);
        out.flush();
        out.flush();
    }

    @Override
    public void formatEnd(long count, PrintStream out, NutsWorkspace ws) {

    }

}
