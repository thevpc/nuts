package net.vpc.app.nuts.core.util.cmdline.format;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsTreeNodeFormatter;
import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsTreeLinkFormatter;
import net.vpc.app.nuts.NutsTreeFormat;

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
    public static final NutsTreeNodeFormatter TO_STRING_FORMATTER = new NutsTreeNodeFormatter() {
        @Override
        public String format(Object o) {
            return String.valueOf(o);
        }
    };
    private NutsTreeNodeFormatter formatter = TO_STRING_FORMATTER;
    private NutsTreeLinkFormatter linkFormatter = LINK_ASCII_FORMATTER;
    private NutsTreeModel tree;

    public DefaultTreeFormat() {
        formatter = TO_STRING_FORMATTER;
        linkFormatter = LINK_ASCII_FORMATTER;
    }

    public DefaultTreeFormat(NutsTreeModel<T> tree) {
        this(tree, null, null);
    }

    public DefaultTreeFormat(NutsTreeModel<T> tree, NutsTreeNodeFormatter formatter, NutsTreeLinkFormatter linkFormatter) {
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

    public NutsTreeNodeFormatter getFormatter() {
        return formatter;
    }

    public DefaultTreeFormat setFormatter(NutsTreeNodeFormatter formatter) {
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        this.formatter = formatter;
        return this;
    }

    public NutsTreeLinkFormatter getLinkFormatter() {
        return linkFormatter;
    }

    public DefaultTreeFormat setLinkFormatter(NutsTreeLinkFormatter linkFormatter) {
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

    public boolean configure(NutsCommandLine cmdLine) {
        NutsArgument a;
        if ((a = cmdLine.readStringOption("--border")) != null) {
            switch (a.getValue().strKey()) {
                case "simple": {
                    setLinkFormatter(LINK_ASCII_FORMATTER);
                    break;
                }
                case "none": {
                    setLinkFormatter(LINK_SPACE_FORMATTER);
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
