/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.xml;

import java.io.PrintWriter;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.runtime.format.DefaultSearchFormatBase;
import net.vpc.app.nuts.runtime.format.NutsFetchDisplayOptions;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase {

    private boolean compact;
    private String rootName = "root";

    public DefaultSearchFormatXml(NutsSession session, PrintWriter writer,NutsFetchDisplayOptions options) {
        super(session, writer, NutsOutputFormat.XML,options);
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        getWriter().println("<" + rootName + ">");
    }

    @Override
    public void next(Object object, long index) {
        NutsXmlUtils.print(String.valueOf(index), object, getWriter(), compact, getWorkspace());
    }

    @Override
    public void complete(long count) {
        getWriter().println("</" + rootName + ">");
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
            case "--root-name": {
                this.rootName = cmd.nextString().getStringValue();
                return true;
            }
        }
        return false;
    }
}
