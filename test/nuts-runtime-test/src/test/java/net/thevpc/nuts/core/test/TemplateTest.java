package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TemplateTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }


    /**
     *     {{: statement}}
     *     {{expression}}
     *     {{:for varName(,index):<expression}} ... \{{:end}}
     *     {{:if expression}} ... \{{:else if expression}} ... \{{:else if expression}} \{{:end}}
     * @throws Exception
     */
    @Test
    public void test1() throws Exception {
        Map<String,Object> varsMap = new HashMap<>();
        varsMap.put("world","Earth");
        varsMap.put("yellow",true);
        String s = render("hello {{:if yellow }} {{world}} {{:else}} World {{:end}}", varsMap);
        Assertions.assertEquals("hello  Earth",s);
    }

    public static String render(String text, Map<String,Object> varsMap) {
        NExprTemplate nExprTemplate = NExprs.of().newMutableDeclarations(new NExprEvaluator() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprDeclarations context) {
                NExprVar v = NExprVar.ofMap(varsMap);;
                if (context instanceof NExprMutableDeclarations) {
                    ((NExprMutableDeclarations) context).declareVar(varName, v);
                }
                return NOptional.of(v);
            }

            @Override
            public NOptional<NExprFct> getFunction(String fctName, NExprNodeValue[] args, NExprDeclarations context) {
                return NExprEvaluator.super.getFunction(fctName, args, context);
            }
        }).ofTemplate().withMoustacheStyle();
        return  nExprTemplate.processString(text);
    }
}
