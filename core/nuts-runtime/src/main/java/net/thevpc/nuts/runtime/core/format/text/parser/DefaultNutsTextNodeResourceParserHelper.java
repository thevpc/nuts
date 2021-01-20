package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.format.text.NutsTextNodeWriterStringer;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.NutsWorkspaceVarExpansionFunction;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DefaultNutsTextNodeResourceParserHelper {
    private NutsTextNodeParser parser;
    private NutsWorkspace ws;
    private NutsWorkspaceVarExpansionFunction pathExpansionConverter;

    public DefaultNutsTextNodeResourceParserHelper(NutsTextNodeParser parser, NutsWorkspace ws) {
        this.parser = parser;
        this.ws = ws;
        pathExpansionConverter = new NutsWorkspaceVarExpansionFunction(ws);
    }

    public NutsTextNode parseResource(String resourceName, NutsTextFormatLoader loader) {
        if(loader==null){
            throw new NutsIllegalArgumentException(ws,"missing loader");
        }
        Reader reader = loader.forPath(resourceName);
        if(reader==null){
            return null;
        }
        return parseResource(resourceName,reader,loader);
    }


    public NutsTextFormatLoader createClassPathLoader(ClassLoader loader) {
        return new NutsTextFormatLoaderClassPath(loader);
    }

    public NutsTextFormatLoader createFileLoader(File root) {
        return new NutsTextFormatLoaderFile(root);
    }

    public NutsTextNode parseResource(String resourceName, Reader reader, NutsTextFormatLoader loader) {
        if(loader==null){
            throw new NutsIllegalArgumentException(ws,"missing loader");
        }
        if(reader==null) {
            reader = loader.forPath(resourceName);
        }
        if(reader==null){
            return null;
        }
        return processHelp(CoreIOUtils.loadString(reader, true),loader,true,null);
    }


//    private String loadHelp(String urlPath, ClassLoader clazz, boolean err, boolean vars, String defaultValue) {
//        return loadHelp(urlPath, clazz, err, 36, vars, defaultValue);
//    }
//
//    private String loadHelp(String urlPath, ClassLoader classLoader, boolean err, int depth, boolean vars, String defaultValue) {
//        if (depth <= 0) {
//            throw new IllegalArgumentException("Unable to load " + urlPath + ". Too many recursions");
//        }
//        if (classLoader == null) {
//            classLoader = Thread.currentThread().getContextClassLoader();
//        }
//        if (urlPath.startsWith("/")) {
//            urlPath = urlPath.substring(1);
//        }
//        int interr = urlPath.indexOf('?');
//        String anchor = null;
//        if (interr > 0) {
//            anchor = urlPath.substring(interr + 1);
//            urlPath = urlPath.substring(interr + 1);
//        }
//        URL resource = classLoader.getResource(urlPath);
//        Reader in = null;
//        if (resource == null) {
//            if (err) {
//                return builder().append(
//                        "not found resource " + urlPath, NutsTextNodeStyle.error()
//                ).toString();
//            }
//            if (defaultValue == null) {
//                return null;
//            }
//            in = new StringReader(defaultValue);
//        } else {
//            in = new InputStreamReader(NutsWorkspaceUtils.of(ws).openURL(resource));
//        }
//        try (Reader is = in) {
//            return loadHelp(is, classLoader, true, depth, vars, anchor);
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//    }
//
//    private String loadHelp(Reader is, ClassLoader classLoader, boolean err, int depth, boolean vars, String anchor) {
//        return processHelp(CoreIOUtils.loadString(is, true), classLoader, err, depth, vars, anchor);
//    }

    private NutsTextNode processHelp(String s, NutsTextFormatLoader classLoader, boolean vars, String anchor) {

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
                        NutsTextNode other=null;
                        try {
                            other = parseResource(e, classLoader);
                        }catch (Throwable t){
                            other=ws.formats().text().
                                    builder().append("NOT FOUND", NutsTextNodeStyle.error())
                                    .append(" <" + e + ">").toNode();
                        }
                        sb.append(other);
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
                node = ws.formats().text().factory().list(ok);
            }
            return node;
        }
        return parser.parse(new StringReader(help));
    }

    private static class NutsTextFormatLoaderClassPath implements NutsTextFormatLoader {
        private final ClassLoader loader;

        public NutsTextFormatLoaderClassPath(ClassLoader loader) {
            this.loader = loader;
        }

        @Override
        public Reader forPath(String path) {
            ClassLoader classLoader= loader;
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }
            URL r = classLoader.getResource(path);
            if(r==null){
                if(path.length()>0 && path.startsWith("/")){
                    r=classLoader.getResource(path.substring(1));
                }
            }
            if(r==null){
                return null;
            }
            try {
                return new InputStreamReader(r.openStream());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class NutsTextFormatLoaderFile implements NutsTextFormatLoader {
        private final File root;

        public NutsTextFormatLoaderFile(File root) {
            this.root = root;
        }

        @Override
        public Reader forPath(String path) {
            File r = root==null?new File(path):new File(root,path);
            if(!r.isFile()){
                return null;
            }
            try {
                return new FileReader(r);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }


//    public String loadFormattedString(Reader is, ClassLoader classLoader) {
//        return loadHelp(is, classLoader, true, 36, true, null);
//    }
//
//    public String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue) {
//        return loadHelp(resourcePath, classLoader, false, true, defaultValue);
//    }

}
