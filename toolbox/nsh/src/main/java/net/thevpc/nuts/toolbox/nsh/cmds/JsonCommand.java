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
 *
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
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.NutsShellContext;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JsonCommand extends SimpleNshBuiltin {

    public JsonCommand() {
        super("json", DEFAULT_SUPPORT);
    }

    private static class Options {

        String input;
        String queryType = "jpath";
        List<String> queries = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if ((a = commandLine.nextString("-f", "--file")) != null) {
            options.input = a.getStringValue();
            return true;
        } else if ((a = commandLine.nextString("-q")) != null) {
            options.queryType = "jpath";
            options.queries.add(a.getStringValue());
            return true;
        } else if ((a = commandLine.nextString("--xpath")) != null) {
            options.queryType = "xpath";
            options.queries.add(a.getStringValue());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
//        if (options.xpaths.isEmpty()) {
//            commandLine.required();
//        }

        if (options.queries.isEmpty()) {
            NutsElement inputDocument = readJsonConvertElement(options.input, context.getRootContext());
            if (context.getSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                context.setPrintlnOutObject(context.getWorkspace().formats().json().value(inputDocument).format());
            } else {
                context.setPrintlnOutObject(inputDocument);
            }
        } else {
            switch (options.queryType) {
                case "xpath": {
                    Document inputDocument = readJsonConvertXml(options.input, context.getRootContext());
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                    Document resultDocument;
                    try {
                        resultDocument = documentFactory.newDocumentBuilder().newDocument();
                    } catch (ParserConfigurationException ex) {
                        throw new NutsExecutionException(context.getWorkspace(), ex, 1);
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
                            throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 103);
                        }
                    }
                    if (context.getSession().getOutputFormat(null) == null) {
                        context.setPrintlnOutObject(context.getWorkspace().formats().json().value(resultDocument).format());
                    } else {
                        context.setPrintlnOutObject(resultDocument);
                    }
                    break;
                }
                case "jpath": {
                    NutsElement inputDocument = readJsonConvertElement(options.input, context.getRootContext());
                    List<NutsElement> all = new ArrayList<>();
                    for (String query : options.queries) {
                        all.addAll(context.getWorkspace().formats().element()
                                .setSession(context.getSession())
                                .compilePath(query)
                                .filter(inputDocument)
                        );
                    }
                    Object result = all.size() == 1 ? all.get(0) : all;
                    if (context.getSession().getOutputFormat(null) == null) {
                        context.setPrintlnOutObject(context.getWorkspace().formats().json().value(result).format());
                    } else {
                        context.setPrintlnOutObject(result);
                    }
                    break;
                }
            }

        }
    }

    private Document readJsonConvertXml(String path, NutsShellContext context) {
        return readJsonConvertAny(path, Document.class, context);
    }

    private NutsElement readJsonConvertElement(String path, NutsShellContext context) {
        return readJsonConvertAny(path, NutsElement.class, context);
    }

    private <T> T readJsonConvertAny(String path, Class<T> cls, NutsShellContext context) {
        NutsJsonFormat njson = context.getWorkspace().formats().json();
        T inputDocument = null;
        if (path != null) {
            File file = new File(context.getAbsolutePath(path));
            if (file.isFile()) {
                inputDocument = njson.parse(file, cls);
            } else {
                throw new NutsExecutionException(context.getWorkspace(), "Invalid path " + path, 1);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = null;
                try {
                    line = reader.readLine();
                } catch (IOException ex) {
                    throw new NutsExecutionException(context.getWorkspace(), "Broken Input", 2);
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

}
