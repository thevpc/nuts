package net.vpc.app.nuts.core.format.tree;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.core.format.DefaultFormatBase;
import net.vpc.app.nuts.core.util.common.IteratorUtils;

public class DefaultTreeFormat extends DefaultFormatBase<NutsTreeFormat> implements NutsTreeFormat {

    public static final NutsTreeLinkFormat LINK_ASCII_FORMATTER = new AsciiTreeLinkFormat();
    public static final NutsTreeLinkFormat LINK_SPACE_FORMATTER = new SpaceTreeLinkFormat();

    public static final NutsTreeNodeFormat TO_STRING_FORMATTER = new NutsTreeNodeFormat() {
        @Override
        public String format(Object o, int depth) {
            return String.valueOf(o);
        }
    };
    private NutsTreeNodeFormat formatter = TO_STRING_FORMATTER;
    private NutsTreeLinkFormat linkFormatter = LINK_ASCII_FORMATTER;
    private NutsTreeModel tree;
    private boolean omitRoot = false;
    private boolean infinite = false;
    private boolean omitEmptyRoot = true;

    public DefaultTreeFormat(NutsWorkspace ws) {
        super(ws, "tree-format");
        formatter = TO_STRING_FORMATTER;
        linkFormatter = LINK_ASCII_FORMATTER;
    }

    public DefaultTreeFormat(NutsWorkspace ws, NutsTreeModel tree) {
        this(ws, tree, null, null);
    }

    public DefaultTreeFormat(NutsWorkspace ws, NutsTreeModel tree, NutsTreeNodeFormat formatter, NutsTreeLinkFormat linkFormatter) {
        super(ws, "tree");
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        if (tree == null) {
            throw new NullPointerException("Null Tree");
        }
        this.formatter = formatter;
        this.linkFormatter = linkFormatter;
        this.tree = tree;
    }

    @Override
    public NutsTreeNodeFormat getNodeFormat() {
        return formatter;
    }

    @Override
    public DefaultTreeFormat setNodeFormat(NutsTreeNodeFormat formatter) {
        if (formatter == null) {
            formatter = TO_STRING_FORMATTER;
        }
        this.formatter = formatter;
        return this;
    }

    @Override
    public NutsTreeLinkFormat getLinkFormat() {
        return linkFormatter;
    }

    @Override
    public DefaultTreeFormat setLinkFormat(NutsTreeLinkFormat linkFormatter) {
        if (linkFormatter == null) {
            linkFormatter = LINK_ASCII_FORMATTER;
        }
        this.linkFormatter = linkFormatter;
        return this;
    }

    @Override
    public NutsTreeModel getModel() {
        return tree;
    }

    @Override
    public DefaultTreeFormat setModel(NutsTreeModel tree) {
        this.tree = tree;
        return this;
    }

    public boolean isEffectiveOmitRoot() {
        return isOmitRoot()
                || (omitEmptyRoot
                && (tree.getRoot() == null || tree.getRoot().toString().isEmpty()));
    }

    public boolean isOmitRoot() {
        return omitRoot;
    }

    public void setOmitRoot(boolean hideRoot) {
        this.omitRoot = hideRoot;
    }

    @Override
    public String toString() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(b);
        print("", NutsTreeLinkFormat.Type.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0);
        out.flush();
        return b.toString();
    }

    @Override
    public void print(PrintStream out) {
        print("", NutsTreeLinkFormat.Type.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0);
        out.flush();
    }

    @Override
    public void print(Writer w) {
        PrintWriter out = getValidPrintWriter(w);
        print("", NutsTreeLinkFormat.Type.FIRST, tree.getRoot(), out, isEffectiveOmitRoot(), 0);
        out.flush();
    }

    private void print(String prefix, NutsTreeLinkFormat.Type type, Object o, PrintStream out, boolean hideRoot, int depth) {
        if (!hideRoot) {
            out.print(prefix);
            out.print(linkFormatter.formatMain(type));
            out.print(formatter.format(o, depth));
            out.println();
            out.flush();
        }
        Iterator<Object> children = tree.getChildren(o).iterator();
        if (children == null) {
            children = IteratorUtils.emptyIterator();
        }
        Object last = null;
        if (children.hasNext()) {
            last = children.next();
        }
        while (children.hasNext()) {
            Object c = last;
            last = children.next();
            print(prefix + linkFormatter.formatChild(type), NutsTreeLinkFormat.Type.MIDDLE, c, out, false, depth + 1);
        }
        if (last != null) {
            print(prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NutsTreeLinkFormat.Type.MIDDLE : NutsTreeLinkFormat.Type.LAST, last, out, false, depth + 1);
        }
    }

    private void print(String prefix, NutsTreeLinkFormat.Type type, Object o, PrintWriter out, boolean hideRoot, int depth) {
        boolean skipNewLine = true;
        if (!hideRoot) {
            out.print(prefix);
            out.print(linkFormatter.formatMain(type));
            out.print(formatter.format(o, depth));
            skipNewLine = false;
            out.flush();
        }
        List children1 = tree.getChildren(o);
        if (children1 == null) {
            children1 = Collections.emptyList();
        }
        Iterator<Object> children = children1.iterator();
        Object last = null;
        if (children.hasNext()) {
            last = children.next();
        }
        while (children.hasNext()) {
            Object c = last;
            last = children.next();
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.println();
            }
            print(prefix + linkFormatter.formatChild(type), NutsTreeLinkFormat.Type.MIDDLE, c, out, false, depth + 1);
        }
        if (last != null) {
            if (skipNewLine) {
                skipNewLine = false;
            } else {
                out.println();
            }
            print(prefix + linkFormatter.formatChild(type), (infinite && "".equals(prefix)) ? NutsTreeLinkFormat.Type.MIDDLE : NutsTreeLinkFormat.Type.LAST, last, out, false, depth + 1);
        }
        out.flush();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--border": {
                a = cmdLine.nextString("--border");
                switch (a.getArgumentValue().getStringKey()) {
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
            case "--omit-root": {
                setOmitRoot(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--infinite": {
                this.infinite = (cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
        }
        return false;
    }

    private static class AsciiTreeLinkFormat implements NutsTreeLinkFormat {

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

    private static class SpaceTreeLinkFormat implements NutsTreeLinkFormat {

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
