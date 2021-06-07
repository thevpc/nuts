/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.json;

import java.io.PrintStream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.core.format.DefaultSearchFormatBase;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase {

    private boolean compact;

    NutsTextManager factory;
    private NutsCodeFormat codeFormat;

    public DefaultSearchFormatJson(NutsSession session, NutsPrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.JSON, options);
        factory = getWorkspace().text();
        codeFormat = session.getWorkspace().text().setSession(session).getCodeFormat("json");
    }

    @Override
    public void start() {
        getWriter().println(codeFormat.tokenToText("[", "separator", getSession()));
        getWriter().flush();
    }

    @Override
    public void complete(long count) {
        getWriter().println(codeFormat.tokenToText("]", "separator", getSession()));
        getWriter().flush();
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
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "--compact": {
                boolean val = cmd.nextBoolean().getBooleanValue();
                if (enabled) {
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
        }else{
            getWriter().print("  ");
        }
        String json = getWorkspace().elem()
                .setSession(getSession())
                .setContentType(NutsContentType.JSON).setValue(object).setCompact(isCompact())
                .format()
                .filteredText()
                ;
        getWriter().printf("%s%n", codeFormat.stringToText(json, getSession()));
        getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

}
