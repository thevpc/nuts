/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDependencyTreeNode;
import net.vpc.app.nuts.NutsIncrementalFormatContext;
import net.vpc.app.nuts.NutsTreeFormat;
import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsTreeNodeFormat;
import net.vpc.app.nuts.core.format.FormattableNutsId;
import net.vpc.app.nuts.core.format.NutsFetchDisplayOptions;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsIncrementalFormatHandler;

/**
 *
 * @author vpc
 */
public class DefaultSearchFormatTree implements NutsIncrementalFormatHandler {

    private Object lastObject;
    private NutsFetchDisplayOptions displayOptions;
    @Override
    public NutsOutputFormat getOutputFormat() {
        return NutsOutputFormat.TREE;
    }

    @Override
    public void init(NutsIncrementalFormatContext context) {
        displayOptions = new NutsFetchDisplayOptions(context.getWorkspace());
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        if(displayOptions.configureFirst(commandLine)){
            return true;
        }
        return false;
    }

    @Override
    public void start(NutsIncrementalFormatContext context) {

    }

    @Override
    public void next(NutsIncrementalFormatContext context, Object object, long index) {
        if (index > 0) {
            formatElement(context, lastObject, index - 1, false);
        }
        lastObject = object;
    }

    @Override
    public void complete(NutsIncrementalFormatContext context, long count) {
        if (count > 0) {
            formatElement(context, lastObject, count - 1, true);
        }
    }

    public void formatElement(NutsIncrementalFormatContext context, Object object, long index, boolean last) {
        NutsTreeFormat tree = context.getWorkspace().format().tree();
        List<String> options = new ArrayList<>();
        options.add("--omit-root");
        if (!last) {
            options.add("--infinite");
        }
        tree.configure(false, options.toArray(new String[0]));
        tree.setNodeFormat(new NutsTreeNodeFormat() {
            @Override
            public String format(Object o, int depth) {
                FormattableNutsId fid = FormattableNutsId.of(o, context.getWorkspace(), context.getSession());
                if (fid != null) {
                    return format(fid);
                } else {
                    return String.valueOf(o);
                }
            }

            public String format(FormattableNutsId id) {
                return id.getSingleColumnRow(displayOptions);
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
        tree.println(context.getWriter());
        context.getWriter().flush();
    }
}
