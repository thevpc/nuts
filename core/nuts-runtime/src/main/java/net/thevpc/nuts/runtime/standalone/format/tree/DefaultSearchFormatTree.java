/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.tree;

import java.util.AbstractMap;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NTreeFormat;
import net.thevpc.nuts.text.NTreeNode;
import net.thevpc.nuts.text.NTreeNodeFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextArt;
import net.thevpc.nuts.text.NTextArtTreeRenderer;

/**
 * @author thevpc
 */
public class DefaultSearchFormatTree extends DefaultSearchFormatBase {

    private Object lastObject;
    NTreeNodeFormat nTreeNodeFormat = new NTreeNodeFormat() {
        @Override
        public NText format(NTreeNode o, int depth) {
            NIdFormatHelper fid = NIdFormatHelper.of(o);
            if (fid != null) {
                return fid.getSingleColumnRow(getDisplayOptions());
            } else {
                return o.value();
            }
        }
    };

    public DefaultSearchFormatTree(NPrintStream writer, NFetchDisplayOptions options) {
        super(writer, NContentType.TREE, options);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void next(Object object, long index) {
        if (index > 0) {
            formatElement(lastObject, index - 1, false);
        }
        lastObject = object;
    }

    @Override
    public void complete(long count) {
        if (count > 0) {
            formatElement(lastObject, count - 1, true);
        }
    }

    public void formatElement(Object object, long index, boolean last) {
        NTextArtTreeRenderer treeRenderer = NTextArt.of().getTreeRenderer().get()
                .setNodeFormat(nTreeNodeFormat)
                .setOmitRoot(true)
                .setInfinite(!last)
                ;
        getWriter().println(treeRenderer.render(NTreeFormat.of(new AbstractMap.SimpleEntry<Object, Object>("ROOT",object)).getModel()));
        getWriter().flush();
    }

//    private static class SearchResultTreeModel implements NutsTreeModel {
//
//        private final Object object;
//
//        public SearchResultTreeModel(Object object) {
//            this.object = object;
//        }
//
//        @Override
//        public Object getRoot() {
//            return null;
//        }
//
//        @Override
//        public List getChildren(Object o) {
//            if (o == null) {
//                return Arrays.asList(object);
//            } else if (o instanceof NutsDefinition) {
//                NutsDefinition d = (NutsDefinition) o;
//                NutsDependencyTreeNode[] z = null;
//                try {
//                    z = d.getDependencies().nodes().toArray(new NutsDependencyTreeNode[0]);
//                } catch (NutsElementNotFoundException ex) {
//                    //this exception will be raised if dependencyNodes(...) was not called.
//                    //so we will ignore dependencies.
//                }
//                if (z != null) {
//                    return Arrays.asList(z);
//                }
//                NutsDependencies dz = null;
//                try {
//                    dz = d.getDependencies();
//                } catch (NutsElementNotFoundException ex) {
//                    //this exception will be raised if dependencies(...) was not called.
//                    //so we will ignore dependencies.
//                }
//                if (dz != null) {
//                    return dz.immediate();
//                }
//                return null;
//            } else if (o instanceof NutsDependencyTreeNode) {
//                NutsDependencyTreeNode[] z = ((NutsDependencyTreeNode) o).getChildren();
//                if (z == null) {
//                    return null;
//                }
//                return Arrays.asList(z);
//            }
//            return null;
//        }
//    }
}
