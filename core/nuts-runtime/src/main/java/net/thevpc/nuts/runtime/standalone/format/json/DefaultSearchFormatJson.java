/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.json;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTexts;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase {

    private boolean compact;

    NutsTexts txt;
    private NutsCodeHighlighter codeFormat;

    public DefaultSearchFormatJson(NutsSession session, NutsPrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.JSON, options);
        txt = NutsTexts.of(session);
        codeFormat = NutsTexts.of(session).setSession(session).getCodeHighlighter("json");
    }

    @Override
    public void start() {
        getWriter().println(codeFormat.tokenToText("[", "separator", txt, getSession()));
        getWriter().flush();
    }

    @Override
    public void complete(long count) {
        getWriter().println(codeFormat.tokenToText("]", "separator", txt, getSession()));
        getWriter().flush();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsSession session = getSession();
        NutsArgument a = cmd.peek().get(session);
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmd)) {
            return true;
        }
        boolean enabled = a.isActive();
        switch(a.getStringKey().orElse("")) {
            case "--compact": {
                boolean val = cmd.nextBooleanValueLiteral().get(session);
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
        NutsText ee = codeFormat.stringToText(json, txt, getSession());
        getWriter().printf("%s%n", ee);
        getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

}
