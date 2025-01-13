package net.thevpc.nuts.lib.doc.executor.expr;

import net.thevpc.nuts.expr.*;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.fct.LoadPages;
import net.thevpc.nuts.lib.doc.executor.expr.fct.PageToHtml;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NDocExprEvaluator implements net.thevpc.nuts.lib.doc.executor.NDocExprEvaluator {
    public static final String NODC_CONTEXT_VAR_NAME = "ndoc";
    private NExprMutableDeclarations rootDecls;
    private NExprs nExprs;

    public NDocExprEvaluator() {

        nExprs = NExprs.of();
        NDocNExprVar v = new NDocNExprVar();
        rootDecls = nExprs.newMutableDeclarations(true, new NExprEvaluator() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprDeclarations context2) {
                return NOptional.of(v);
            }
        });
        rootDecls.declareFunction("exec", new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                if (args.size() != 1) {
                    throw new IllegalStateException(name + " : invalid arguments count");
                }
                NDocContext fcontext = fcontext(context);
                String pathString = (String) args.get(0).getValue().orNull();
                Path path = Paths.get(pathString);
                return fcontext.getExecutorManager().executeRegularFile(path, null);
            }
        });

        rootDecls.declareFunction("println", new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NDocContext fcontext = fcontext(context);

                List<String> all = new ArrayList<>();
                for (NExprNodeValue arg : args) {
                    all.add(String.valueOf(arg.getValue().orNull()));
                }
                StringBuilder sb = new StringBuilder();
                if (!all.isEmpty()) {
                    if (all.size() == 1) {
                        sb.append(all.get(0)).append("\n");
                    } else {
                        sb.append(String.join(", ", all)).append("\n");
                    }
                }
                fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(sb.toString()) + ")");
                return sb.toString();
            }
        });

        rootDecls.declareFunction("print", new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                NDocContext fcontext = fcontext(context);

                List<String> all = new ArrayList<>();
                for (NExprNodeValue arg : args) {
                    all.add(String.valueOf(arg.getValue().orNull()));
                }
                StringBuilder sb = new StringBuilder();
                if (!all.isEmpty()) {
                    if (all.size() == 1) {
                        sb.append(all.get(0));
                    } else {
                        sb.append(String.join(", ", all));
                    }
                }
                fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(sb.toString()) + ")");
                return sb.toString();
            }
        });

        rootDecls.declareFunction("string", new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                if (args.size() != 1) {
                    throw new IllegalStateException(name + " : invalid arguments count");
                }
                NDocContext fcontext = fcontext(context);

                String str = (String) args.get(0).getValue().orNull();
                fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
                return NLiteral.of(str).toStringLiteral();
            }
        });

        rootDecls.declareFunction("processFile", new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                if (args.size() != 1) {
                    throw new IllegalStateException(name + " : invalid arguments count");
                }
                NDocContext fcontext = fcontext(context);

                String str = (String) args.get(0).getValue().orNull();
                String path = FileProcessorUtils.toAbsolute(str, fcontext.getWorkingDirRequired());
                Path opath = Paths.get(path);
                fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(opath) + ")");
                fcontext.getProcessorManager().processSourceRegularFile(opath, null);
                return "";
            }
        });

        rootDecls.declareFunction("loadFile", new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                if (args.size() != 1) {
                    throw new IllegalStateException(name + " : invalid arguments count");
                }
                NDocContext fcontext = fcontext(context);
                String str = (String) args.get(0).getValue().orNull();
                fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
                return FileProcessorUtils.loadString(
                        Paths.get(FileProcessorUtils.toAbsolute(str, fcontext.getWorkingDirRequired()))
                );
            }
        });

        rootDecls.declareFunction("include", new NExprFct() {
            @Override
            public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
                if (args.size() != 1) {
                    throw new IllegalStateException(name + " : invalid arguments count");
                }
                NDocContext fcontext = fcontext(context);

                String str = (String) args.get(0).getValue().orNull();
                String path = FileProcessorUtils.toAbsolute(str, fcontext.getWorkingDirRequired());
                Path opath = Paths.get(path);
                fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(opath) + ")");
                try (InputStream in = Files.newInputStream(opath)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    fcontext.getProcessorManager().processStream(in, out, fcontext.getMimeTypeResolver().resolveMimetype(opath.toString()));
                    return out.toString();
                } catch (IOException io) {
                    throw new UncheckedIOException(io);
                }
            }
        });

        declareFunction(new LoadPages());
        declareFunction(new PageToHtml());

    }

    @Override
    public Object eval(String content, NDocContext fcontext) {
        content = content.trim();
        NExprMutableDeclarations decl = rootDecls.newMutableDeclarations();
        decl.declareConstant(NODC_CONTEXT_VAR_NAME, fcontext);
        decl.declareConstant("cwd", System.getProperty("user.dir"));
        decl.declareConstant("dir", fcontext.getWorkingDir().orNull());
        NExprDeclarations decl2 = decl.newDeclarations(new NExprEvaluator() {
            @Override
            public NOptional<NExprVar> getVar(String varName, NExprDeclarations context) {
                NOptional<Object> var = fcontext.getVar(varName);
                if (var.isPresent()) {
                    return NOptional.of(new NExprVar() {
                        @Override
                        public Object get(String name, NExprDeclarations context) {
                            return var.get();
                        }

                        @Override
                        public Object set(String name, Object value, NExprDeclarations context) {
                            return fcontext.setVar(name, value);
                        }
                    });
                }
                return NExprEvaluator.super.getVar(varName, context);
            }
        });
        NExprNode nExprNode = decl2.parse(content).get();
        NOptional<Object> eval = nExprNode.eval(decl2);
        if(!eval.isPresent()){
            eval = nExprNode.eval(decl2);
        }
        return  eval.get();
    }

    @Override
    public String toString() {
        return "NExpr";
    }

    protected void declareFunction(BaseNexprNExprFct d){
        rootDecls.declareFunction(d.getName(),d);
    }

    private static NDocContext fcontext(NExprDeclarations context) {
        NExprVarDeclaration vd = context.getVar(NODC_CONTEXT_VAR_NAME).get();
        return (NDocContext) vd.get(context);
    }

    private static class NDocNExprVar implements NExprVar {
        public NDocNExprVar() {
        }

        @Override
        public Object get(String name, NExprDeclarations context) {
            return fcontext(context).getVar(name).orNull();
        }

        @Override
        public Object set(String name, Object value, NExprDeclarations context) {
            return fcontext(context).setVar(name, value);
        }
    }
}
