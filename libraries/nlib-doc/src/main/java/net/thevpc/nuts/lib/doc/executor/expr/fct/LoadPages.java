package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.pages.PageGroup;
import net.thevpc.nuts.lib.doc.pages.PageGroupMdLoader;
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
        if(strOrGroup instanceof String) {
            level = 0;
            str = (String) strOrGroup;
        }else if(strOrGroup instanceof PageGroup) {
            str = ((PageGroup)strOrGroup).path;
            level=((PageGroup)strOrGroup).getLevel()+1;
        }else{
            level = 0;
            return new ArrayList<>();
        }
        if(NPath.of(str).getName().equals(".folder-info.md")){
            str=NPath.of(str).getParent().toString();
        }
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
        List<PageGroup> pages = NPath.of(str).list().stream()
                .map(x -> {
                    if (x.isDirectory()) {
                        try {
                            return PageGroupMdLoader.load(x);
                        } catch (Exception e) {
                            return null;
                        }
                    } else if (x.isRegularFile()) {
                        if (!x.getName().startsWith(".") && x.getName().endsWith(".md")) {
                            return PageGroupMdLoader.load(x);
                        }
                    }
                    return null;
                })
                .filter(x -> x != null)
                .peek(x -> {
                    x.setLevel(level);
                })                .sorted(new NComparator<PageGroup>() {
                    @Override
                    public int compare(PageGroup o1, PageGroup o2) {
                        int r1 = o1.getOrder();
                        int r2 = o2.getOrder();
                        if (r1 == r2) {
                            int i = NStringUtils.trim(o1.getPathName()).compareTo(o2.getPathName());
                            if (i != 0) {
                                return i;
                            }
                        }
                        return r1 - r2;
                    }
                })
                .collect(Collectors.toList());
        return pages;
    }
}
