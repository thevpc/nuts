/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.json;

import java.io.PrintStream;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.format.DefaultSearchFormatBase;
import net.vpc.app.nuts.runtime.format.NutsFetchDisplayOptions;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase {

    private boolean compact;

    public DefaultSearchFormatJson(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsOutputFormat.JSON,options);
    }

    @Override
    public void start() {
        getWriter().println("\\[");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmd)) {
            return true;
        }
        boolean enabled=a.isEnabled();
        switch (a.getStringKey()) {
            case "--compact": {
                boolean val = cmd.nextBoolean().getBooleanValue();
                if(enabled) {
                    this.compact = val;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void next(Object object, long index) {
        if (index > 0) {
            getWriter().print(", ");
        }
        getWriter().printf("%s%n", new NutsString(getWorkspace().formats().json().compact(isCompact()).value(object).format()));
        getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

    @Override
    public void complete(long count) {
        getWriter().println("\\]");
        getWriter().flush();
    }

}
