/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NTextBuilder;
import org.w3c.dom.Document;

/**
 * @author thevpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase {

    private boolean compact;
    private String rootName = "root";
    private NCodeHighlighter codeFormat;

    public DefaultSearchFormatXml(NPrintStream writer, NFetchDisplayOptions options) {
        super(writer, NContentType.XML, options);
        codeFormat = NCodeHighlighter.of("xml");
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        NTextBuilder builder = NTextBuilder.of();

        builder.append(codeFormat.tokenToText("<?", "separator"));
        builder.append(codeFormat.tokenToText("xml", "name"));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("version", "attribute"));
        builder.append(codeFormat.tokenToText("=", "separator"));
        builder.append(codeFormat.tokenToText("\"1.0\"", "string"));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("encoding", "attribute"));
        builder.append(codeFormat.tokenToText("=", "separator"));
        builder.append(codeFormat.tokenToText("?>", "separator"));
        builder.append("\n");

        builder.append(codeFormat.tokenToText("<", "separator"));
        builder.append(codeFormat.tokenToText(rootName, "name"));
        builder.append(codeFormat.tokenToText(">", "separator"));

        getWriter().println(builder.toString());
    }

    @Override
    public void next(Object object, long index) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        XmlUtils.print(String.valueOf(index), object, getWriter(), compact, false, getWorkspace());
        PrintWriter pw = new PrintWriter(bos);
        org.w3c.dom.Element xmlElement = NElement.convertAny(object, org.w3c.dom.Element.class);
        Document doc = XmlUtils.createDocument();
        doc.adoptNode(xmlElement);
        doc.appendChild(xmlElement);
        XmlUtils.writeDocument(doc, new javax.xml.transform.stream.StreamResult(pw), compact, false);
        pw.flush();
        getWriter().print(codeFormat.stringToText(bos.toString()));
    }

    @Override
    public void complete(long count) {
        NTextBuilder builder = NTextBuilder.of();

        builder.append(codeFormat.tokenToText("</", "separator"));
        builder.append(codeFormat.tokenToText(rootName, "name"));
        builder.append(codeFormat.tokenToText(">", "separator"));

        getWriter().println(builder.toString());
        getWriter().flush();
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        switch (a.key()) {
            case "--compact": {
                return cmdLine.matcher().whenAny().asFlag((v) -> compact = v.booleanValue()).anyMatch();
            }
            case "--root-name": {
                return cmdLine.matcher().whenAny().asEntry((v) -> rootName = v.stringValue()).anyMatch();
            }
        }
        return false;
    }
}
