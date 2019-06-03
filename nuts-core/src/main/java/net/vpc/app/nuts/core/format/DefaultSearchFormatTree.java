/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyTreeNode;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsTreeFormat;
import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsTreeNodeFormat;
import net.vpc.app.nuts.NutsIncrementalOutputFormat;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatTree extends DefaultSearchFormatBase<NutsIncrementalOutputFormat> {

    private Object lastObject;

    public DefaultSearchFormatTree(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.TREE);
    }

    @Override
    public void startImpl() {
    }
    
    @Override
    public void nextImpl(Object object, long index) {
        if (index > 0) {
            formatElement(lastObject, index - 1, false);
        }
        lastObject = object;
    }

    @Override
    public void completeImpl(long count) {
        if (count > 0) {
            formatElement(lastObject, count - 1, true);
        }
    }

    public void formatElement(Object object, long index, boolean last) {
        NutsTreeFormat tree = getWs().formatter().createTreeFormat();
        List<String> options = new ArrayList<>();
        options.add("--omit-root");
        if (!last) {
            options.add("--infinite");
        }
        tree.configure(options.toArray(new String[0]));
        tree.setNodeFormat(new NutsTreeNodeFormat() {
            @Override
            public String format(Object o, int depth) {
                FormattableNutsId fid = FormattableNutsId.of(o, getWs(), getValidSession());
                if (fid != null) {
                    return format(fid);
                } else {
                    return String.valueOf(o);
                }
            }

            public String format(FormattableNutsId id) {
                return id.getSingleColumnRow(getDisplayOptions());
            }
        });
        tree.setModel(new NutsTreeModel() {
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
                    NutsDependencyTreeNode[] z = d.getDependenciesNodes();
                    if (z != null) {
                        return Arrays.asList(z);
                    }
                    NutsDependency[] dz = d.getDependencies();
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
        });
        tree.println(getValidOut());
        getValidOut().flush();
    }
}
