package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeAnchor;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.NutsWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DefaultNutsTextFormatManager implements NutsTextFormatManager {
    private NutsWorkspace ws;
    private NutsWorkspaceVarExpansionFunction pathExpansionConverter;
    private NutsTextNodeFactory nodesFactory;

    public DefaultNutsTextFormatManager(NutsWorkspace ws) {
        this.ws = ws;
        pathExpansionConverter = new NutsWorkspaceVarExpansionFunction(ws);
        nodesFactory = new DefaultNutsTextNodeFactory(ws);
    }

    @Override
    public String loadFormattedString(Reader is, ClassLoader classLoader) {
        return loadHelp(is, classLoader, true, 36, true, null);
    }

    @Override
    public String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue) {
        return loadHelp(resourcePath, classLoader, false, true, defaultValue);
    }

    @Override
    public NutsTextNodeFactory factory() {
        return nodesFactory;
    }

    @Override
    public NutsTextNodeBuilder builder() {
        return new DefaultNutsTextNodeBuilder(ws);
    }

    @Override
    public NutsTextNode parse(String t) {
        return t==null?factory().blank():parser().parse(new StringReader(t));
    }

    public NutsString of(Object instance, NutsSession session) {
        if (instance instanceof NutsString) {
            return (NutsString) instance;
        } else {
            return factory().formatted(instance);
        }
    }

    public NutsString of(Object instance) {
        return of(instance, null);
    }

    @Override
    public NutsTextNodeParser parser() {
        return new DefaultNutsTextNodeParser(ws);
    }

    @Override
    public NutsTitleNumberSequence createTitleNumberSequence() {
        return new DefaultNutsTitleNumberSequence("");
    }

    @Override
    public NutsTitleNumberSequence createTitleNumberSequence(String pattern) {
        return new DefaultNutsTitleNumberSequence((pattern == null || pattern.isEmpty()) ? "1.1.1.a.1" : pattern);
    }

//    /**
//     * transform plain text to formatted text so that the result is rendered as
//     * is
//     *
//     * @param text text
//     * @return escaped text
//     */    @Override
//    public String escapeText(String text) {
//        if (text == null) {
//            return "";
//        }
//        NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(text));
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        NutsTextNodeWriter w = new NutsTextNodeWriterStringer(out, ws)
//                .setWriteConfiguration(
//                        new NutsTextNodeWriteConfiguration()
//                        .setFiltered(true)
//                );
//        w.writeNode(node);
//        return out.toString();
//    }

//    @Override
//    public String escapeCodeText(String text) {
//        if (text == null) {
//            return "";
//        }
//        //TODO...
//        return text;
//    }

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
                return builder().append(
                        "not found resource " + urlPath, NutsTextNodeStyle.error()
                ).toString();
            }
            if (defaultValue == null) {
                return null;
            }
            in = new StringReader(defaultValue);
        } else {
            in = new InputStreamReader(NutsWorkspaceUtils.of(ws).openURL(resource));
        }
        try (Reader is = in) {
            return loadHelp(is, classLoader, true, depth, vars, anchor);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String loadHelp(Reader is, ClassLoader classLoader, boolean err, int depth, boolean vars, String anchor) {
        return processHelp(CoreIOUtils.loadString(is, true), classLoader, err, depth, vars, anchor);
    }

    private String processHelp(String s, ClassLoader classLoader, boolean err, int depth, boolean vars, String anchor) {
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
                        sb.append(loadHelp(e, classLoader, err, depth - 1, false,
                                builder().append("NOT FOUND", NutsTextNodeStyle.error())
                                        .append(" <" + e + ">").toString()
                        ));
                    } else {
                        sb.append(e);
                    }
                }
            }
        }
        String help = sb.toString();
        if (vars) {
            help = StringPlaceHolderParser.replaceDollarPlaceHolders(help, pathExpansionConverter);
        }
        NutsTextNodeParser p = new DefaultNutsTextNodeParser(ws);
        NutsTextNode node = p.parse(new StringReader(help));
        if (anchor != null) {
            List<NutsTextNode> ok = new ArrayList<>();
            boolean start = false;
            if (node.getType() == NutsTextNodeType.LIST) {
                for (NutsTextNode o : ((NutsTextNodeList) node)) {
                    if (start) {
                        ok.add(o);
                    } else if (o.getType() == NutsTextNodeType.ANCHOR) {
                        if (anchor.equals(((DefaultNutsTextNodeAnchor) o).getValue())) {
                            start = true;
                        }
                    }
                }
            }
            if (start) {
                node = factory().list(ok);
            }
            help = NutsTextNodeWriterStringer.toString(node, ws);
        }
        return help;
    }

}
