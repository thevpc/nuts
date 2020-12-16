/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.io.PrintStream;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase {

    private boolean compact;
    private String rootName = "root";

    public DefaultSearchFormatXml(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.XML,options);
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        getWriter().println("\\<?xml version=\"1.0\" encoding=\"UTF-8\"?\\>");
        getWriter().println("\\<" + rootName + "\\>");
    }

    @Override
    public void next(Object object, long index) {
//        NutsXmlUtils.print(String.valueOf(index), object, getWriter(), compact, false, getWorkspace());
        NutsXmlUtils.print("item", object, index, getWriter(), compact, false, getWorkspace());
    }

    @Override
    public void complete(long count) {
        getWriter().println("\\</" + rootName + "\\>");
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
                if(enabled) {
                    this.compact = val;
                }
                return true;
            }
            case "--root-name": {
                String val = cmd.nextString().getStringValue();
                if(enabled) {
                    this.rootName = val;
                }
                return true;
            }
        }
        return false;
    }
}
