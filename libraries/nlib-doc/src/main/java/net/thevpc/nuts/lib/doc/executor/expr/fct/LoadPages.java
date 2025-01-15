package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.pages.MdPage;
import net.thevpc.nuts.lib.doc.pages.MdPageLoader;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoadPages extends BaseNexprNExprFct {
    public LoadPages() {
        super("loadPages");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);

        Object strOrGroup = args.get(0).getValue().orNull();
        String str = null;
        int level;
        MdPage parent=null;
        if (strOrGroup instanceof String) {
            level = 0;
            str = (String) strOrGroup;
            try {
                parent = MdPageLoader.load(NPath.of(str));
                if (parent != null) {
                    level = parent.getLevel() + 1;
                }
            } catch (Exception e) {
                return null;
            }
        } else if (strOrGroup instanceof MdPage) {
            parent=((MdPage) strOrGroup);
            str = ((MdPage) strOrGroup).path;
            level = ((MdPage) strOrGroup).getLevel() + 1;
        } else {
            parent=null;
            level = 0;
            return new ArrayList<>();
        }
        boolean sortAsc = true;
        if(parent!=null){
            sortAsc=parent.isSortAsc();
        }
        if (NPath.of(str).getName().equals(".folder-info.md")) {
            str = NPath.of(str).getParent().toString();
        }
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
        int finalLevel = level;
        boolean finalSortAsc = sortAsc;
        List<MdPage> pages = NPath.of(str).list().stream()
                .map(x -> {
                    try {
                        return MdPageLoader.load(x);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(x -> x != null)
                .peek(x -> {
                    x.setLevel(finalLevel);
                }).sorted(new NComparator<MdPage>() {
                    @Override
                    public int compare(MdPage o1, MdPage o2) {
                        int r1 = o1.getOrder();
                        int r2 = o2.getOrder();
                        if (r1 == r2) {
                            int i = NStringUtils.trim(o1.getPathName()).compareTo(o2.getPathName());
                            if (i != 0) {
                                return finalSortAsc ?i:-i;
                            }
                        }
                        int v = r1 - r2;
                        return finalSortAsc ?v:-v;
                    }
                })
                .collect(Collectors.toList());
        return pages;
    }
}
