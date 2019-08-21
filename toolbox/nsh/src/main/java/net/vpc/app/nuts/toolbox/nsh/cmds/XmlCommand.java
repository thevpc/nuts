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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class XmlCommand extends SimpleNshBuiltin {

    public XmlCommand() {
        super("xml", DEFAULT_SUPPORT);
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
        if (options.xpaths.isEmpty()) {
            commandLine.required();
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception ex) {
            throw new NutsExecutionException(context.getWorkspace(), "Invalid to initialize xml system", ex, 3);
        }

        Document doc = null;
        if (options.input != null) {
            File file = new File(context.getRootContext().getAbsolutePath(options.input));
            if (file.isFile()) {
                try {
                    doc = dBuilder.parse(file);
                } catch (Exception ex) {
                    throw new NutsExecutionException(context.getWorkspace(), "Invalid xml " + options.input, ex, 2);
                }
            } else {
                throw new NutsExecutionException(context.getWorkspace(), "Invalid path " + options.input, 1);
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
                    try {
                        doc = dBuilder.parse(new InputSource(new StringReader(sb.toString())));
                    } catch (Exception ex) {
                        throw new NutsExecutionException(context.getWorkspace(), "Invalid xml : " + sb, ex, 2);
                    }
                    break;
                } else {
                    sb.append(line);
                    try {
                        doc = dBuilder.parse(new InputSource(new StringReader(sb.toString())));
                        break;
                    } catch (Exception ex) {
                        //
                    }
                }
            }
        }

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        List<Object> all = new ArrayList<>();
        all.add(doc);
        XPath xPath = XPathFactory.newInstance().newXPath();
        List<NodeList> result = new ArrayList<>();
        for (String query : options.xpaths) {
            try {
                result.add((NodeList) xPath.compile(query).evaluate(doc, XPathConstants.NODESET));
            } catch (XPathExpressionException ex) {
                throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 103);
            }
        }
        if (all.size() == 1) {
            context.setPrintOutObject(all.get(0));
        } else {
            context.setPrintOutObject(all);
        }
    }

}
