/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.tree;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeFormat;
import net.thevpc.nuts.format.NTreeNodeFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NIdFormatHelper;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.text.NTexts;

/**
 * @author thevpc
 */
public class DefaultSearchFormatTree extends DefaultSearchFormatBase {

    private Object lastObject;
    NTreeNodeFormat nTreeNodeFormat = new NTreeNodeFormat() {
        @Override
        public NString format(Object o, int depth, NSession session) {
            NIdFormatHelper fid = NIdFormatHelper.of(o, getSession());
            if (fid != null) {
                return fid.getSingleColumnRow(getDisplayOptions());
            } else {
                if (o instanceof XNode) {
                    return ((XNode) o).toNutsString();
                }
                return NTexts.of(getSession()).ofBuilder().append(o).immutable();
            }
        }
    };

    public DefaultSearchFormatTree(NSession session, NPrintStream writer, NFetchDisplayOptions options) {
        super(session, writer, NContentType.TREE, options);
    }

    @Override
    public boolean configureFirst(NCmdLine commandLine) {
        if (getDisplayOptions().configureFirst(commandLine)) {
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
        NTreeFormat tree = NTreeFormat.of(getSession());
        List<String> options = new ArrayList<>();
        options.add("--omit-root");
        if (!last) {
            options.add("--infinite");
        }
        
        tree.configure(false, options.toArray(new String[0]));
        tree.setNodeFormat(nTreeNodeFormat);
        //the object must be second level (not root)
        tree.setValue(new AbstractMap.SimpleEntry<Object, Object>("ROOT",object));
        tree.println(getWriter());
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
