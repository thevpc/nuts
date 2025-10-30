package net.thevpc.nuts.runtime.standalone.format.tree;

import java.util.*;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.text.art.tree.DefaultNTextArtTreeRenderer;
import net.thevpc.nuts.runtime.standalone.text.art.tree.XNode;
import net.thevpc.nuts.runtime.standalone.text.art.tree.XNodeFormatter;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultNPropertiesFormat;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

public class DefaultNTreeFormat extends DefaultFormatBase<NTreeFormat> implements NTreeFormat {

    private DefaultNTextArtTreeRenderer renderer = new DefaultNTextArtTreeRenderer("default");
    private NText rootName;


    private Object tree;

    public DefaultNTreeFormat() {
        super("tree-format");
    }

    public DefaultNTreeFormat(NTreeNode tree) {
        this(tree, null, null);
    }

    public DefaultNTreeFormat(NTreeNode tree, NTreeNodeFormat formatter, NTreeLinkFormat linkFormatter) {
        super("tree");
        renderer.setFormatter(formatter);
        renderer.setLinkFormat(linkFormatter);
        NAssert.requireNonNull(tree, "tree");
        this.tree = tree;
    }

    private XNodeFormatter xNodeFormatter = new XNodeFormatter() {
        @Override
        public NText[] getMultilineArray(NText key, Object value) {
            return DefaultNTreeFormat.this.getMultilineArray(key, value);
        }

        @Override
        public NText stringValue(Object o) {
            return renderer.getNodeFormat().format((NTreeNode)o, -1);
        }

    };

    private NText[] getMultilineArray(NText key, Object value) {
        String sep = renderer.getMultilineSeparator(key);
        if (sep == null) {
            return null;
        }
        String[] vv = CoreStringUtils.stringValue(value).split(sep);
        if (vv.length == 0 || vv.length == 1) {
            return null;
        }
        return Arrays.stream(vv).map(x -> NText.of(x)).toArray(NText[]::new);
    }


    @Override
    public NTreeNode getModel() {
        if (tree instanceof NTreeNode) {
//        if(tree instanceof NutsTreeModel){
            return (NTreeNode) tree;
        }
        NElements ee = NElements.of();
        ee.mapperStore().
        Object destructredObject = ee
                .destruct(tree);
        return XNode.root(destructredObject, rootName, xNodeFormatter);
    }

    @Override
    public DefaultNTreeFormat setValue(Object value) {
        this.tree = value;
        return this;
    }


    @Override
    public String toString() {
        return renderer.render(getModel()).filteredText();
    }

    @Override
    public void print(NPrintStream out) {
        out.print(renderer.render(getModel()));
        out.flush();
    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().orNull();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isUncommented();
        switch (aa.key()) {
            case "--border": {
                return cmdLine.matcher().matchEntry((v) -> {
                    switch (NStringUtils.trim(v.stringValue())) {
                        case "simple": {
                            renderer.setLinkFormat(DefaultNTextArtTreeRenderer.LINK_ASCII_FORMATTER);
                            break;
                        }
                        case "none": {
                            renderer.setLinkFormat(DefaultNTextArtTreeRenderer.LINK_SPACE_FORMATTER);
                            break;
                        }
                    }
                }).anyMatch();
            }
            case "--omit-root": {
                return cmdLine.matcher().matchFlag((v) -> renderer.setOmitRoot(v.booleanValue())).anyMatch();
            }
            case "--infinite": {
                return cmdLine.matcher().matchFlag((v) -> renderer.setInfinite((v.booleanValue()))).anyMatch();
            }
            case DefaultNPropertiesFormat.OPTION_MULTILINE_PROPERTY: {
                NArg i = cmdLine.nextEntry().get();
                if (enabled) {
                    renderer.addMultilineProperty(i.key(), i.getStringValue().get());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getValue() {
        return tree;
    }


    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}
