package net.thevpc.nuts.lib.doc.processor.base;

import java.util.ArrayList;
import java.util.List;

public class TagNodeReader {
    private TagTokenReader tr;
    private String exprLang;

    public TagNodeReader(TagTokenReader tr, String exprLang) {
        this.tr = tr;
        this.exprLang = exprLang;
    }

    public TagNode next() {
        TagToken t = tr.peek();
        if (t == null) {
            return null;
        }
        switch (t.type) {
            case PLAIN:{
                t = tr.next();
                return new PlainAstNode(t.value);
            }
            case EXPR: {
                t = tr.next();
                return new VarAstNode(exprLang, t.value);
            }
            case CTRL_EVAL: {
                t = tr.next();
                return new EvalAstNode(exprLang, t.value);
            }
            case CTRL_IF: {
                return _nextCtrlIf();
            }
            case CTRL_FOR: {
                return _nextCtrlFor();
            }
        }
        throw new IllegalArgumentException("unexpected token: " + t);
    }

    private TagNode _nextCtrlIf() {
        TagToken t = tr.next();
        IfTagNode nn = new IfTagNode(exprLang, t.value.substring(":if".length()).trim());
        List<TagNode> ifBody = new ArrayList<>();
        while (true) {
            TagToken t2 = tr.peek();
            if (t2 == null) {
                break;
            } else if (t2.type == TagTokenType.CTRL_ELSE_IF) {
                t2 = tr.next();
                List<TagNode> all = new ArrayList<>();
                while (true) {
                    TagToken t3 = tr.peek();
                    if (t3 == null || t3.type == TagTokenType.CTRL_ELSE_IF || t3.type == TagTokenType.CTRL_ELSE || t3.type == TagTokenType.CTRL_END) {
                        break;
                    } else {
                        all.add(next());
                    }
                }
                nn.elseIfBranches.add(new IfTagNode.ElseIf(t2.value, ListAstNode.of(all)));
            } else if (t2.type == TagTokenType.CTRL_ELSE) {
                t2 = tr.next();
                List<TagNode> all = new ArrayList<>();
                while (true) {
                    TagToken t3 = tr.peek();
                    if (t3 == null || t3.type == TagTokenType.CTRL_END) {
                        break;
                    } else {
                        all.add(next());
                    }
                }
                nn.elseExpr = ListAstNode.of(all);
            } else if (t2.type == TagTokenType.CTRL_END) {
                tr.next();
                break;
            } else {
                ifBody.add(next());
            }
        }
        nn.elseIfBranches.get(0).body = ListAstNode.of(ifBody);
        return nn;
    }

    private TagNode _nextCtrlFor() {
        TagToken t = tr.next();
        ForTagNode nn = new ForTagNode(exprLang, t.value.substring(":for".length()).trim());
        List<TagNode> all = new ArrayList<>();
        while (true) {
            TagToken t3 = tr.peek();
            if (t3 == null) {
                break;
            }
            if (t3.type == TagTokenType.CTRL_END) {
                tr.next();
                break;
            } else {
                all.add(next());
            }
        }
        nn.body = ListAstNode.of(all);
        return nn;
    }
}
