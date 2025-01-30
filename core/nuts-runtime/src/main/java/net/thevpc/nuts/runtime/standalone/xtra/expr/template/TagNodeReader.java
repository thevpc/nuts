package net.thevpc.nuts.runtime.standalone.xtra.expr.template;


import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNode;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;

public class TagNodeReader {
    private TagTokenReader tr;
    private NExprDeclarations context;

    public TagNodeReader(TagTokenReader tr, NExprDeclarations context) {
        this.tr = tr;
        this.context = context;
    }

    public TagNode next() {
        TagToken t = tr.peek();
        if (t == null) {
            return null;
        }
        switch (t.type) {
            case PLAIN: {
                t = tr.next();
                return new PlainTagNode(t.value);
            }
            case EXPR: {
                t = tr.next();
                return new ExpressionTagNode(parseExpression(t.value));
            }
            case STATEMENT: {
                t = tr.next();
                return new StatementTagNode(parseExpression(t.value));
            }
            case INCLUDE: {
                t = tr.next();
                return new IncludeNode(parseExpression(t.value));
            }
            case IF: {
                return _nextCtrlIf();
            }
            case FOR: {
                return _nextCtrlFor();
            }
        }
        throw new IllegalArgumentException("unexpected token: " + t);
    }

    private NExprNode parseExpression(String s) {
        if (NBlankable.isBlank(s)) {
            return null;
        }
        return context.parse(s).get();
    }

    private TagNode _nextCtrlIf() {
        TagToken t = tr.next();
        IfTagNode nn = new IfTagNode(parseExpression(t.value));
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
                nn.elseIfBranches.add(new IfTagNode.ElseIf(parseExpression(t2.value), ListTagNode.of(all)));
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
                nn.elseExpr = ListTagNode.of(all);
            } else if (t2.type == TagTokenType.CTRL_END) {
                tr.next();
                break;
            } else {
                ifBody.add(next());
            }
        }
        nn.elseIfBranches.get(0).body = ListTagNode.of(ifBody);
        return nn;
    }

    private TagNode _nextCtrlFor() {
        TagToken t = tr.next();
        ForTagNode nn = new ForTagNode(t.value);
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
        nn.body = ListTagNode.of(all);
        return nn;
    }
}
