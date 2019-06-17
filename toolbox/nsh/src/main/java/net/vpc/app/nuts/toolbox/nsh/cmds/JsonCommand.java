/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

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
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsElement;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsJsonFormat;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.toolbox.nsh.NutsShellContext;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by vpc on 1/7/17.
 */
public class JsonCommand extends SimpleNshBuiltin {

    public JsonCommand() {
        super("json", DEFAULT_SUPPORT);
    }

    private static class Options {

        String input;
        List<String> xpaths = new ArrayList<>();
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
        } else if ((a = commandLine.nextString("-q", "--xpath")) != null) {
            options.xpaths.add(a.getStringValue());
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

        if (options.xpaths.isEmpty()) {
            NutsElement inputDocument = readJsonConvertElement(options.input, context.getGlobalContext());
            if (context.getSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                context.setPrintOutObject(context.getWorkspace().format().json().set(inputDocument).format());
            } else {
                context.setPrintOutObject(inputDocument);
            }
        } else {
            Document inputDocument = readJsonConvertXml(options.input, context.getGlobalContext());
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
            for (String query : options.xpaths) {
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
            if (context.getSession().getOutputFormat() == NutsOutputFormat.PLAIN || context.getSession().getOutputFormat() == NutsOutputFormat.JSON) {
                context.setPrintOutObject(context.getWorkspace().format().json().set(resultDocument).format());
            } else {
                context.setPrintOutObject(resultDocument);
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
        NutsJsonFormat njson = context.getWorkspace().format().json();
        T inputDocument = null;
        if (path != null) {
            File file = new File(context.getAbsolutePath(path));
            if (file.isFile()) {
                inputDocument = njson.read(file, cls);
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
                    inputDocument = njson.read(new StringReader(sb.toString()), cls);
                    break;
                } else {
                    sb.append(line);
                    try {
                        inputDocument = njson.read(new StringReader(sb.toString()), cls);
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
