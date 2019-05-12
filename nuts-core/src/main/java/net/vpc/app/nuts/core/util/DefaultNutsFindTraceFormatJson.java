/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsOutputListFormat;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindTraceFormatJson extends DefaultNutsFindTraceFormatBase<NutsOutputListFormat> {

    private boolean pretty = true;

    public DefaultNutsFindTraceFormatJson(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.JSON);
    }

    @Override
    public void formatStart() {
        getValidOut().println("[");
    }

    @Override
    public NutsOutputListFormat setOption(String name, String value) {
        if (name != null) {
            switch (name) {
                case "pretty": {
                    pretty = CoreCommonUtils.parseBoolean(value, true);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public void formatElement(Object object, long index) {
        if (index > 0) {
            getValidOut().print(", ");
        }
        getValidOut().printf("%N%n", getWs().io().json().pretty(pretty).toJsonString(object));
        getValidOut().flush();
    }

    @Override
    public void formatEnd(long count) {
        getValidOut().println("]");
    }

}
