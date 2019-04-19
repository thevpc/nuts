/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTraceFormat;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class FailsafeNutsTraceFormat implements NutsTraceFormat {

    private NutsTraceFormat other;
    private NutsTraceFormat fallback;

    public FailsafeNutsTraceFormat(NutsTraceFormat other, NutsTraceFormat fallback) {
        this.other = other;
        this.fallback = fallback;
    }

    public NutsTraceFormat getOther() {
        return other;
    }

    public void setOther(NutsTraceFormat other) {
        this.other = other;
    }

    @Override
    public Object format(NutsId id, NutsOutputFormat type, NutsWorkspace ws) {
        if (other != null) {
            try {
                Object p = other.format(id, type, ws);
                if (p != null) {
                    return p;
                }
            } catch (NutsUnsupportedArgumentException a) {
                //ignore;
            }
        }
        if (fallback != null) {
            try {
                Object p = fallback.format(id, type, ws);
                if (p != null) {
                    return p;
                }
            } catch (NutsUnsupportedArgumentException a) {
                //ignore;
            }
        }
        return DefaultNutsFindTraceFormat.INSTANCE.format(id, type, ws);
    }

    @Override
    public Object format(NutsDefinition def, NutsOutputFormat type, NutsWorkspace ws) {
        if (other != null) {
            try {
                Object p = other.format(def, type, ws);
                if (p != null) {
                    return p;
                }
            } catch (NutsUnsupportedArgumentException a) {
                //ignore;
            }
        }
        if (fallback != null) {
            try {
                Object p = fallback.format(def, type, ws);
                if (p != null) {
                    return p;
                }
            } catch (NutsUnsupportedArgumentException a) {
                //ignore;
            }
        }
        return DefaultNutsFindTraceFormat.INSTANCE.format(def, type, ws);

    }

}
