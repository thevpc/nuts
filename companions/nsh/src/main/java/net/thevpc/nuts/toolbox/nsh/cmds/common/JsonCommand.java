/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.common;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltinBase;
import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class JsonCommand extends JShellBuiltinDefault {

    public JsonCommand() {
        super("json", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean configureFirst(NCmdLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a;
        if ((a = commandLine.nextEntry("-f", "--file").orNull()) != null) {
            options.input = a.getStringValue().get(session);
            return true;
        } else if ((a = commandLine.nextEntry("-q").orNull()) != null) {
            options.queryType = "jpath";
            options.queries.add(a.getStringValue().get(session));
            return true;
        } else if ((a = commandLine.nextEntry("--xpath").orNull()) != null) {
            options.queryType = "xpath";
            options.queries.add(a.getStringValue().get(session));
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NCmdLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
//        if (options.xpaths.isEmpty()) {
//            commandLine.required();
//        }

        NSession session = context.getSession();
        if (options.queries.isEmpty()) {
            NElement inputDocument = readJsonConvertElement(options.input, context.getShellContext());
            if (session.getOutputFormat() == NContentType.PLAIN) {
                session.out().println(NElements.of(session).json().setValue(inputDocument).format());
            } else {
                session.out().println(inputDocument);
            }
        } else {
            switch (options.queryType) {
                case "xpath": {
                    Document inputDocument = readJsonConvertXml(options.input, context.getShellContext());
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                    Document resultDocument;
                    try {
                        resultDocument = documentFactory.newDocumentBuilder().newDocument();
                    } catch (ParserConfigurationException ex) {
                        throw new NExecutionException(session, NMsg.ofPlain("failed to create xml document"), ex, 1);
                    }
                    Element resultElement = resultDocument.createElement("result");
                    resultDocument.appendChild(resultElement);
                    for (String query : options.queries) {
                        try {
                            NodeList evaluated = (NodeList) xPath.compile(query).evaluate(inputDocument, XPathConstants.NODESET);
                            for (int i = 0; i < evaluated.getLength(); i++) {
                                Node item = evaluated.item(i);
                                Node o = resultDocument.importNode(item, true);
                                resultElement.appendChild(o);
                            }
                        } catch (XPathExpressionException ex) {
                            throw new NExecutionException(session, NMsg.ofC("%s", ex), ex, 103);
                        }
                    }
                    NElement json = NElements.of(session).toElement(resultDocument);
                    session.out().println(json);
                    break;
                }
                case "jpath": {
                    NElement inputDocument = readJsonConvertElement(options.input, context.getShellContext());
                    List<NElement> all = new ArrayList<>();
                    for (String query : options.queries) {
                        all.addAll(NElements.of(session)
                                .compilePath(query)
                                .filter(inputDocument)
                        );
                    }
                    Object result = all.size() == 1 ? all.get(0) : all;
                    NElement json = NElements.of(session).toElement(result);
                    session.out().println(json);
                    break;
                }
            }

        }
    }

    private Document readJsonConvertXml(String path, JShellContext context) {
        return readJsonConvertAny(path, Document.class, context);
    }

    private NElement readJsonConvertElement(String path, JShellContext context) {
        return readJsonConvertAny(path, NElement.class, context);
    }

    private <T> T readJsonConvertAny(String path, Class<T> cls, JShellContext context) {
        NSession session = context.getSession();
        NElements njson = NElements.of(session).json();
        T inputDocument = null;
        if (path != null) {
            NPath file = NPath.of(path, session).toAbsolute(context.getCwd());
            if (file.exists()) {
                inputDocument = njson.parse(file, cls);
            } else {
                throw new NExecutionException(session, NMsg.ofC("invalid path %s", path), 1);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = null;
                try {
                    line = reader.readLine();
                } catch (IOException ex) {
                    throw new NExecutionException(session, NMsg.ofPlain("broken Input"), 2);
                }
                if (line == null) {
                    inputDocument = njson.parse(new StringReader(sb.toString()), cls);
                    break;
                } else {
                    sb.append(line);
                    try {
                        inputDocument = njson.parse(new StringReader(sb.toString()), cls);
                        break;
                    } catch (Exception ex) {
                        //
                    }
                }
            }
        }
        return inputDocument;
    }

    private static class Options {

        String input;
        String queryType = "jpath";
        List<String> queries = new ArrayList<>();
    }

}
