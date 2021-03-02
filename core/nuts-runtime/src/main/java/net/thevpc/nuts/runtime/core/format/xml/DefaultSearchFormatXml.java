/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextNodeBuilder;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.core.format.DefaultSearchFormatBase;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase {

    private boolean compact;
    private String rootName = "root";
    private NutsCodeFormat codeFormat;

    public DefaultSearchFormatXml(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.XML, options);
        codeFormat = session.getWorkspace().formats().getCodeFormat("xml");

    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        NutsTextNodeBuilder builder = getWorkspace().formats().text().builder();

        builder.append(codeFormat.tokenToNode("<?", "separator"));
        builder.append(codeFormat.tokenToNode("xml", "name"));

        builder.append(" ");
        builder.append(codeFormat.tokenToNode("version", "attribute"));
        builder.append(codeFormat.tokenToNode("=", "separator"));
        builder.append(codeFormat.tokenToNode("\"1.0\"", "string"));

        builder.append(" ");
        builder.append(codeFormat.tokenToNode("encoding", "attribute"));
        builder.append(codeFormat.tokenToNode("=", "separator"));
        builder.append(codeFormat.tokenToNode("?>", "separator"));
        builder.append("\n");

        builder.append(codeFormat.tokenToNode("<", "separator"));
        builder.append(codeFormat.tokenToNode(rootName, "name"));
        builder.append(codeFormat.tokenToNode(">", "separator"));

        getWriter().println(builder.toString());
    }

    @Override
    public void next(Object object, long index) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        NutsXmlUtils.print(String.valueOf(index), object, getWriter(), compact, false, getWorkspace());
        PrintWriter pw = new PrintWriter(bos);
        NutsXmlUtils.print("item", object, index, pw, compact, false, getSession());
        pw.flush();

        getWriter().print(codeFormat.textToNode(bos.toString()));
    }

    @Override
    public void complete(long count) {
        NutsTextNodeBuilder builder = getWorkspace().formats().text().builder();

        builder.append(codeFormat.tokenToNode("</", "separator"));
        builder.append(codeFormat.tokenToNode(rootName, "name"));
        builder.append(codeFormat.tokenToNode(">", "separator"));

        getWriter().println(builder.toString());
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
            case "--root-name": {
                String val = cmd.nextString().getStringValue();
                if (enabled) {
                    this.rootName = val;
                }
                return true;
            }
        }
        return false;
    }
}
