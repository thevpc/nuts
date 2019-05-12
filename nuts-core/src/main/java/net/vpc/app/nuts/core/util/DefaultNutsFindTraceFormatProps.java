/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.NutsOutputListFormat;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindTraceFormatProps extends DefaultNutsFindTraceFormatBase<NutsOutputListFormat> {

    public DefaultNutsFindTraceFormatProps(NutsWorkspace ws) {
        super(ws,NutsOutputFormat.PROPS);
    }
    
    @Override
    public void formatStart() {
    }

    @Override
    public void formatElement(Object object, long index) {
        Map<String,String> p = new LinkedHashMap<>();
        CoreCommonUtils.putAllInProps(String.valueOf(index+1), p, getValidCanonicalBuilder().toCanonical(object));
        CoreIOUtils.storeProperties(p, getValidOut());
        getValidOut().flush();
    }

    @Override
    public void formatEnd(long count) {

    }

}
