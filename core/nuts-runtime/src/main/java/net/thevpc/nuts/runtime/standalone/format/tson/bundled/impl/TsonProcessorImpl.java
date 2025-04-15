package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser.SimpleTsonParserVisitor;

import java.util.Map;
import java.util.Set;


public class TsonProcessorImpl implements TsonProcessor {

    @Override
    public TsonElement removeComments(TsonElement element) {
        SimpleTsonParserVisitor v = new SimpleTsonParserVisitor() {
            @Override
            public void visitComments(TsonComment comments) {
                super.visitComments(null);
            }
        };
        element.visit(v);
        element = v.getElement();
        return element;
    }

    @Override
    public TsonElement resolveAliases(TsonElement element) {
        SimpleTsonParserVisitor v = new SimpleTsonParserVisitor() {
            @Override
            protected TsonAnnotation onAddAnnotation(TsonAnnotation a) {
                if (a.name() == null && a.size() == 1) {
                    TsonElement aliasParam = a.param(0);
                    if (aliasParam.type() == TsonElementType.NAME) {
                        //this is an alias definition
                        //mark it
                        String aliasName = aliasParam.stringValue();
                        peek().addToSetContextValue("alias", aliasName);
                        //and remove it
                        return null;
                    }
                }
                return a;
            }

            @Override
            protected void repush(StackContext n) {
                Set<String> all = getMergedSetsContextValues("alias", 0);
                super.repush(n);
                StackContext a = peek();
                if (a instanceof ElementContext) {
                    ElementContext ac = (ElementContext) a;
//                    StackContext p = peekOrRoot(1);
                    for (String s : all) {
                        getRootContext().addToMapContextValue("vars", s, ac.value);
                    }
                    if (ac.value.type() == TsonElementType.ALIAS) {
                        Map<String, TsonElement> vars = getMergedMapsContextValues("vars", 1);
                        TsonElement tt = vars.get(ac.value.stringValue());
                        if (tt == null) {
                            throw new IllegalArgumentException("Alias " + ac.value + " not found");
                        }
                        super.repush(new ElementContext(tt));
                    }
                }
            }
        };
        element.visit(v);
        element = v.getElement();
        return element;
    }


}