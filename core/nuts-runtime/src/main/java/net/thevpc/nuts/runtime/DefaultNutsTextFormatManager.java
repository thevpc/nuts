package net.thevpc.nuts.runtime;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsTextFormatManager;
import net.thevpc.nuts.NutsTextFormatStyle;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.io.NutsWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.fprint.TextNodeParser;
import net.thevpc.nuts.runtime.util.fprint.TextNodeWriterStringer;
import net.thevpc.nuts.runtime.util.fprint.parser.DefaultTextNodeParser;
import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;
import net.thevpc.nuts.runtime.util.fprint.parser.TextNodeAnchor;
import net.thevpc.nuts.runtime.util.fprint.parser.TextNodeList;
import net.thevpc.nuts.runtime.util.fprint.util.FormattedPrintStreamUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

public class DefaultNutsTextFormatManager implements NutsTextFormatManager {
    private NutsWorkspace ws;
    private NutsWorkspaceVarExpansionFunction pathExpansionConverter;

    public DefaultNutsTextFormatManager(NutsWorkspace ws) {
        this.ws = ws;
        pathExpansionConverter=new NutsWorkspaceVarExpansionFunction(ws);
    }

    @Override
    public String loadFormattedString(Reader is, ClassLoader classLoader) {
        return loadHelp(is, classLoader, true, 36, true,null);
    }

    @Override
    public String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue) {
        return loadHelp(resourcePath, classLoader, false, true, defaultValue);
    }


    private String loadHelp(String urlPath, ClassLoader clazz, boolean err, boolean vars, String defaultValue) {
        return loadHelp(urlPath, clazz, err, 36, vars, defaultValue);
    }

    private String loadHelp(String urlPath, ClassLoader classLoader, boolean err, int depth, boolean vars, String defaultValue) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Unable to load " + urlPath + ". Too many recursions");
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }
        int interr = urlPath.indexOf('?');
        String anchor = null;
        if (interr > 0) {
            anchor = urlPath.substring(interr + 1);
            urlPath = urlPath.substring(interr + 1);
        }
        URL resource = classLoader.getResource(urlPath);
        Reader in = null;
        if (resource == null) {
            if (err) {
                return "@@Not Found resource " + escapeText(urlPath) + "@@";
            }
            if (defaultValue == null) {
                return null;
            }
            in=new StringReader(defaultValue);
        } else {
            try {
                in = new InputStreamReader(resource.openStream());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        try (Reader is = in) {
            return loadHelp(is, classLoader, true, depth, vars,anchor);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String loadHelp(Reader is, ClassLoader classLoader, boolean err, int depth, boolean vars,String anchor) {
        return processHelp(CoreIOUtils.loadString(is, true), classLoader, err, depth, vars,anchor);
    }

    private String processHelp(String s, ClassLoader classLoader, boolean err, int depth, boolean vars,String anchor) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, "\n\r", true);
            while (st.hasMoreElements()) {
                String e = st.nextToken();
                if (e.length() > 0) {
                    if (e.charAt(0) == '\n' || e.charAt(0) == '\r') {
                        sb.append(e);
                    } else if (e.startsWith("#!include<") && e.trim().endsWith(">")) {
                        e = e.trim();
                        e = e.substring("#!include<".length(), e.length() - 1);
                        sb.append(loadHelp(e, classLoader, err, depth - 1, false, "@@NOT FOUND\\<" + escapeText(e) + "\\>@@"));
                    } else {
                        sb.append(e);
                    }
                }
            }
        }
        String help = sb.toString();
        if (vars) {
            help = CoreStringUtils.replaceDollarPlaceHolders(help, pathExpansionConverter);
        }
        TextNodeParser p=new DefaultTextNodeParser();
        TextNode node = p.parse(new StringReader(help));
        if(anchor!=null){
            List<TextNode> ok=new ArrayList<>();
            boolean start=false;
            if(node instanceof TextNodeList){
                for (TextNode o : ((TextNodeList)node)) {
                    if(start){
                        ok.add(o);
                    }else if(o instanceof TextNodeAnchor){
                        if(anchor.equals(((TextNodeAnchor) o).getValue())){
                            start=true;
                        }
                    }
                }
            }
            if(start){
                node=new TextNodeList(ok.toArray(new TextNode[0]));
            }
            help=TextNodeWriterStringer.toString(node);
        }
        return help;
    }




    @Override
    public int textLength(String value) {
        return filterText(value).length();
    }

    /**
     * extract plain text from formatted text
     *
     * @param value value
     * @return filtered text
     */
    @Override
    public String filterText(String value) {
        return FormattedPrintStreamUtils.filterText(value);
    }

    /**
     * transform plain text to formatted text so that the result is rendered as
     * is
     *
     * @param text text
     * @return escaped text
     */    @Override
    public String escapeText(String text) {
        if (text == null) {
            return "";
        }
        return FormattedPrintStreamUtils.escapeText(text);
    }

    /**
     * @param style style
     * @param locale locale
     * @param format format
     * @param args args
     * @return formatted text
     */
    @Override
    public String formatText(NutsTextFormatStyle style, Locale locale, String format, Object... args) {
        if (style == NutsTextFormatStyle.CSTYLE) {
            return FormattedPrintStreamUtils.formatCStyle(ws,locale, format, args);
        } else {
            return FormattedPrintStreamUtils.formatPositionalStyle(ws,locale, format, args);
        }
    }

    @Override
    public String formatText(NutsTextFormatStyle style, String format, Object... args) {
        if (style == NutsTextFormatStyle.CSTYLE) {
            return FormattedPrintStreamUtils.formatCStyle(ws,Locale.getDefault(), format, args);
        } else {
            return FormattedPrintStreamUtils.formatPositionalStyle(ws,Locale.getDefault(), format, args);
        }
    }
}
