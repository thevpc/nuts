/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.json;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase {

    private boolean compact;

    NutsTexts factory;
    private NutsCodeHighlighter codeFormat;

    public DefaultSearchFormatJson(NutsSession session, NutsPrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.JSON, options);
        factory = NutsTexts.of(session);
        codeFormat = NutsTexts.of(session).setSession(session).getCodeHighlighter("json");
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
        boolean enabled = a.isActive();
        switch (a.getKey().getString()) {
            case "--compact": {
                boolean val = cmd.nextBoolean().getValue().getBoolean();
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
        String json = NutsElements.of(getSession())
                .json().setNtf(false).setValue(object).setCompact(isCompact())
                .format()
                .filteredText()
                ;
        NutsText ee = codeFormat.stringToText(json, getSession());
        getWriter().printf("%s%n", ee);
        getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

}
