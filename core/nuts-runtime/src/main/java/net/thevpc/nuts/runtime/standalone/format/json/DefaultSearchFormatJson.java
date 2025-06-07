/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.json;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;

/**
 * @author thevpc
 */
public class DefaultSearchFormatJson extends DefaultSearchFormatBase {

    private boolean compact;

    NTexts txt;
    private NCodeHighlighter codeFormat;

    public DefaultSearchFormatJson(NPrintStream writer, NFetchDisplayOptions options) {
        super(writer, NContentType.JSON, options);
        txt = NTexts.of();
        codeFormat = NTexts.of().getCodeHighlighter("json");
    }

    @Override
    public void start() {
        getWriter().println(codeFormat.tokenToText("[", "separator", txt));
        getWriter().flush();
    }

    @Override
    public void complete(long count) {
        getWriter().println(codeFormat.tokenToText("]", "separator", txt));
        getWriter().flush();
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        switch (aa.key()) {
            case "--compact": {
                cmdLine.withNextFlag((v) -> this.compact = v.booleanValue());
                return true;
            }
        }
        return false;
    }

    @Override
    public void next(Object object, long index) {
        if (index > 0) {
            getWriter().print(", ");
        } else {
            getWriter().print("  ");
        }
        String json = NElementWriter.ofJson()
                .setCompact(isCompact())
                .toString(object);
        NText ee = codeFormat.stringToText(json, txt);
        getWriter().println(ee);
        getWriter().flush();
    }

    public boolean isCompact() {
        return compact;
    }

}
