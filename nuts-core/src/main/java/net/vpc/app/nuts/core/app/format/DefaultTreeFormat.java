package net.vpc.app.nuts.core.app.format;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsTreeLinkFormatter;
import net.vpc.app.nuts.NutsTreeFormat;
import net.vpc.app.nuts.NutsTreeNodeFormat;

public class DefaultTreeFormat<T> implements NutsTreeFormat {

    public static final NutsTreeLinkFormatter LINK_ASCII_FORMATTER = new AsciiTreeLinkFormatter();
    public static final NutsTreeLinkFormatter LINK_SPACE_FORMATTER = new SpaceTreeLinkFormatter();

//    public static void main(String[] args) {
//        String s = new TreeFormatter(new TreeModel<File>() {
//            @Override
//            public File getRoot() {
////                return new File("/home/vpc/.nuts/default-workspace/");
//                return new File("/home/vpc/.nuts/default-workspace/programs/net/vpc/app/nuts/");
//            }
//
//            @Override
//            public List<File> getChildren(File o) {
//                File f = (File) o;
//                File[] b = f.listFiles();
//                if (b == null) {
//                    return Collections.emptyList();
//                }
//                return Arrays.asList(b);
//            }
//        }).toString();
//        System.out.println(s);
//    }
    public static final NutsTreeNodeFormat TO_STRING_FORMATTER = new NutsTreeNodeFormat() {
        @Override
        public String format(Object o) {
            return String.valueOf(o);
        }
    };
    private NutsTreeNodeFormat formatter = TO_STRING_FORMATTER;
    private NutsTreeLinkFormatter linkFormatter = LINK_ASCII_FORMATTER;
    private NutsTreeModel tree;

    public DefaultTreeFormat() {
        formatter = TO_STRING_FORMATTER;
        linkFormatter = LINK_ASCII_FORMATTER;
    }

    public DefaultTreeFormat(NutsTreeModel<T> tree) {
        this(tree, null, null);
    }

    public DefaultTreeFormat(NutsTreeModel<T> tree, NutsTreeNodeFormat formatter, NutsTreeLinkFormatter linkFormatter) {
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        if (tree == null) {
            throw new NullPointerException("N?ull Tree");
        }
        this.formatter = formatter;
        this.linkFormatter = linkFormatter;
        this.tree = tree;
    }

    public NutsTreeNodeFormat getNodeFormat() {
        return formatter;
    }

    public DefaultTreeFormat setNodeFormat(NutsTreeNodeFormat formatter) {
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        this.formatter = formatter;
        return this;
    }

    public NutsTreeLinkFormatter getLinkFormat() {
        return linkFormatter;
    }

    public DefaultTreeFormat setLinkFormat(NutsTreeLinkFormatter linkFormatter) {
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        this.linkFormatter = linkFormatter;
        return this;
    }

    public NutsTreeModel getTree() {
        return tree;
    }

    public DefaultTreeFormat setTree(NutsTreeModel tree) {
        this.tree = tree;
        return this;
    }

    public String toString() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(b);
        print("", NutsTreeLinkFormatter.Type.FIRST, tree.getRoot(), out);
        out.flush();
        return b.toString();
    }

    public void print(PrintStream out) {
        print("", NutsTreeLinkFormatter.Type.FIRST, tree.getRoot(), out);
        out.flush();
    }

    public void print(Writer w) {
        PrintWriter out = (w instanceof PrintWriter) ? ((PrintWriter) w) : new PrintWriter(w);
        print("", NutsTreeLinkFormatter.Type.FIRST, tree.getRoot(), out);
        out.flush();
    }

    private void print(String prefix, NutsTreeLinkFormatter.Type type, Object o, PrintStream out) {
        out.print(prefix);
        out.print(linkFormatter.formatMain(type));
        out.print(formatter.format(o));
        out.print("\n");
        Iterator<Object> children = tree.getChildren(o).iterator();
        Object last = null;
        if (children.hasNext()) {
            last = children.next();
        }
        while (children.hasNext()) {
            Object c = last;
            last = children.next();
            print(prefix + linkFormatter.formatChild(type), NutsTreeLinkFormatter.Type.MIDDLE, c, out);
        }
        if (last != null) {
            print(prefix + linkFormatter.formatChild(type), NutsTreeLinkFormatter.Type.LAST, last, out);
        }
    }

    private void print(String prefix, NutsTreeLinkFormatter.Type type, Object o, PrintWriter out) {
        out.print(prefix);
        out.print(linkFormatter.formatMain(type));
        out.print(formatter.format(o));
        out.print("\n");
        List children1 = tree.getChildren(o);
        Iterator<Object> children = children1 == null ? Collections.emptyIterator() : children1.iterator();
        Object last = null;
        if (children.hasNext()) {
            last = children.next();
        }
        while (children.hasNext()) {
            Object c = last;
            last = children.next();
            print(prefix + linkFormatter.formatChild(type), NutsTreeLinkFormatter.Type.MIDDLE, c, out);
        }
        if (last != null) {
            print(prefix + linkFormatter.formatChild(type), NutsTreeLinkFormatter.Type.LAST, last, out);
        }
    }

    @Override
    public final boolean configure(NutsCommandLine commandLine, boolean skipIgnored) {
        boolean conf = false;
        while (commandLine.hasNext()) {
            if (!configure(commandLine, false)) {
                if (skipIgnored) {
                    commandLine.skip();
                } else {
                    commandLine.unexpectedArgument();
                }
            } else {
                conf = true;
            }
        }
        return conf;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a;
        if ((a = cmdLine.readStringOption("--border")) != null) {
            switch (a.getValue().strKey()) {
                case "simple": {
                    setLinkFormat(LINK_ASCII_FORMATTER);
                    break;
                }
                case "none": {
                    setLinkFormat(LINK_SPACE_FORMATTER);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private static class AsciiTreeLinkFormatter implements NutsTreeLinkFormatter {

        @Override
        public String formatMain(Type type) {
            switch (type) {
                case FIRST: {
                    return ("");
                }
                case MIDDLE: {
                    return ("├── ");
                }
                case LAST: {
                    return ("└── ");
                }
            }
            return "";
        }

        @Override
        public String formatChild(Type type) {
            String p = "";
            switch (type) {
                case FIRST: {
                    p = "";
                    break;
                }
                case MIDDLE: {
                    p = "│   ";
                    break;
                }
                case LAST: {
                    p = "    ";
                    break;
                }
            }
            return p;
        }
    }

    private static class SpaceTreeLinkFormatter implements NutsTreeLinkFormatter {

        @Override
        public String formatMain(Type type) {
            switch (type) {
                case FIRST: {
                    return ("");
                }
                case MIDDLE: {
                    return ("   ");
                }
                case LAST: {
                    return ("   ");
                }
            }
            return "";
        }

        @Override
        public String formatChild(Type type) {
            String p = "";
            switch (type) {
                case FIRST: {
                    p = "";
                    break;
                }
                case MIDDLE: {
                    p = "   ";
                    break;
                }
                case LAST: {
                    p = "   ";
                    break;
                }
            }
            return p;
        }
    }
}
