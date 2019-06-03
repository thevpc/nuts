/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.NutsIncrementalOutputFormat;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatProps extends DefaultSearchFormatBase<NutsIncrementalOutputFormat> {

    public DefaultSearchFormatProps(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.PROPS);
    }

    @Override
    public void startImpl() {
    }
    
    @Override
    public void completeImpl(long count) {
        
    }

    @Override
    public void nextImpl(Object object, long index) {
        Map<String, String> p = new LinkedHashMap<>();
        CoreCommonUtils.putAllInProps(String.valueOf(index + 1), p, getValidCanonicalBuilder().toCanonical(object));
        CoreIOUtils.storeProperties(p, getValidOut(),false);
        getValidOut().flush();
    }

}
