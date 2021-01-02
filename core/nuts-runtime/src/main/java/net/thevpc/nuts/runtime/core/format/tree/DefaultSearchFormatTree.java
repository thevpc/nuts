/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.tree;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.NutsIdFormatHelper;
import net.thevpc.nuts.runtime.core.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;

/**
 * @author thevpc
 */
public class DefaultSearchFormatTree extends DefaultSearchFormatBase {

    private Object lastObject;

    public DefaultSearchFormatTree(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.TREE, options);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
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
        NutsTreeFormat tree = getWorkspace().formats().tree();
        List<String> options = new ArrayList<>();
        options.add("--omit-root");
        if (!last) {
            options.add("--infinite");
        }
        tree.configure(false, options.toArray(new String[0]));
        tree.setNodeFormat(new NutsTreeNodeFormat() {
            @Override
            public String format(Object o, int depth) {
                NutsIdFormatHelper fid = NutsIdFormatHelper.of(o, getSession());
                if (fid != null) {
                    return format(fid);
                } else {
                    return String.valueOf(o);
                }
            }

            public String format(NutsIdFormatHelper id) {
                return id.getSingleColumnRow(getDisplayOptions());
            }
        });
        tree.setValue(new SearchResultTreeModel(object));
        tree.println(getWriter());
        getWriter().flush();
    }

    private static class SearchResultTreeModel implements NutsTreeModel {

        private final Object object;

        public SearchResultTreeModel(Object object) {
            this.object = object;
        }

        @Override
        public Object getRoot() {
            return null;
        }

        @Override
        public List getChildren(Object o) {
            if (o == null) {
                return Arrays.asList(object);
            } else if (o instanceof NutsDefinition) {
                NutsDefinition d = (NutsDefinition) o;
                NutsDependencyTreeNode[] z = null;
                try {
                    z = d.getDependencyNodes();
                } catch (NutsElementNotFoundException ex) {
                    //this exception will be raised if dependencyNodes(...) was not called.
                    //so we will ignore dependencies.
                }
                if (z != null) {
                    return Arrays.asList(z);
                }
                NutsDependency[] dz = null;
                try {
                    dz = d.getDependencies();
                } catch (NutsElementNotFoundException ex) {
                    //this exception will be raised if dependencies(...) was not called.
                    //so we will ignore dependencies.
                }
                if (dz != null) {
                    return Arrays.asList(dz);
                }
                return null;
            } else if (o instanceof NutsDependencyTreeNode) {
                NutsDependencyTreeNode[] z = ((NutsDependencyTreeNode) o).getChildren();
                if (z == null) {
                    return null;
                }
                return Arrays.asList(z);
            }
            return null;
        }
    }
}
