/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.json;

import java.io.PrintWriter;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.format.DefaultSearchFormatBase;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase {

    private boolean compact;

    public DefaultSearchFormatJson(NutsSession session, PrintWriter writer,NutsFetchDisplayOptions options) {
        super(session, writer, NutsOutputFormat.JSON,options);
    }

    @Override
    public void start() {
        getWriter().println("[");
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
        switch (a.getStringKey()) {
            case "--compact": {
                this.compact = cmd.nextBoolean().getBooleanValue();
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
        getWriter().printf("%s%n", new NutsString(getWorkspace().json().compact(isCompact()).value(object).format()));
        getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

    @Override
    public void complete(long count) {
        getWriter().println("]");
    }

}
