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
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.*;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNTreeFormat extends DefaultFormatBase<NTreeFormat> implements NTreeFormat {

    private DefaultNTextArtTreeRenderer renderer = new DefaultNTextArtTreeRenderer("default");
    private NText rootName;


    public DefaultNTreeFormat() {
        this( null, null);
    }

    public DefaultNTreeFormat(NTreeNodeFormat formatter, NTreeLinkFormat linkFormatter) {
        super("tree");
        renderer.setFormatter(formatter);
        renderer.setLinkFormat(linkFormatter);
    }

    private XNodeFormatter xNodeFormatter = new XNodeFormatter() {
        @Override
        public NText[] getMultilineArray(NText key, Object value) {
            return DefaultNTreeFormat.this.getMultilineArray(key, value);
        }

//        @Override
//        public NText stringValue(Object o) {
//            return renderer.getNodeFormat().format((NTreeNode)o, -1);
//        }

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
    public NTreeNode getModel(Object tree) {
        if (tree instanceof NTreeNode) {
//        if(tree instanceof NutsTreeModel){
            return (NTreeNode) tree;
        }
        NElements ee = NElements.of();
        //ee.mapperStore().
        Object destructredObject = ee
                .destruct(tree);
        return XNode.root(destructredObject, rootName, xNodeFormatter);
    }

    @Override
    public String formatPlain(Object value) {
        return renderer.render(getModel(value)).filteredText();
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        out.print(renderer.render(getModel(aValue)));
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



}
