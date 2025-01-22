package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.processor.pages.MPage;
import net.thevpc.nuts.lib.doc.processor.pages.MPageLoader;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LoadPagesFct extends BaseNexprNExprFct {
    public LoadPagesFct() {
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
        MPage parent=null;
        if (strOrGroup instanceof String) {
            level = 0;
            str = (String) strOrGroup;
            try {
                parent = MPageLoader.load(NPath.of(str),fcontext);
                if (parent != null) {
                    level = parent.getLevel() + 1;
                }
            } catch (Exception e) {
                return null;
            }
        } else if (strOrGroup instanceof MPage) {
            parent=((MPage) strOrGroup);
            str = ((MPage) strOrGroup).path;
            level = ((MPage) strOrGroup).getLevel() + 1;
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
        }else if (NPath.of(str).getName().equals(".folder-info.ntf")) {
            str = NPath.of(str).getParent().toString();
        }
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
        int finalLevel = level;
        boolean finalSortAsc = sortAsc;
        List<MPage> pages = NPath.of(str).list().stream()
                .map(x -> {
                    try {
                        return MPageLoader.load(x,fcontext);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(x -> x != null)
                .peek(x -> {
                    if(NBlankable.isBlank(x.getId())){
                        String id="U"+NDigest.of().setSource(x.getPath().getBytes()).computeString();
                        x.setId(id);
                    }
                    x.setLevel(finalLevel);
                }).sorted(new NComparator<MPage>() {
                    @Override
                    public int compare(MPage o1, MPage o2) {
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
