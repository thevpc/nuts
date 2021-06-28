/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.JavaShellNonBlockingInputStream;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.JavaShellNonBlockingInputStreamAdapter;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.ShellUtils;

import java.io.*;
import java.util.StringTokenizer;

/**
 * @author thevpc
 */
public class DefaultJShellEvaluator implements JShellEvaluator {

    public static int readQuotes(char[] chars, int i, StringBuilder v) {
        return ShellUtils.readQuotes(chars, i, v);
    }

    protected int readAndEvalSimpleQuotesExpression(char[] chars, int i, StringBuilder out, JShellFileContext context) {
        StringBuilder v = new StringBuilder();
        int count = readQuotes(chars, i, v);
        out.append(evalSimpleQuotesExpression(v.toString(), context));
        return i + count;
    }

    protected int readAndEvalAntiQuotesString(char[] chars, int i, StringBuilder out, JShellFileContext context) {
        StringBuilder v = new StringBuilder();
        int count = readQuotes(chars, i, v);
        out.append(evalAntiQuotesExpression(v.toString(), context));
        return i + count;
    }

    protected int readAndEvalDblQuotesExpression(char[] chars, int i, StringBuilder out, JShellFileContext context) {
        StringBuilder v = new StringBuilder();
        int count = readQuotes(chars, i, v);
        out.append(evalDoubleQuotesExpression(v.toString(), context));
        return i + count;
    }

    protected int readAndEvalDollarExpression(char[] chars, int i, StringBuilder out, boolean escapeResultPath, JShellFileContext context) {
        if (i + 1 < chars.length) {
            i++;
            if (chars[i] == '{') {
                StringBuilder v = new StringBuilder();
                while (i < chars.length && chars[i] != '}') {
                    v.append(chars[i]);
                    i++;
                }
                String r = evalDollarExpression(v.toString(), context);
                if (escapeResultPath) {
                    r = context.getShell().escapePath(r);
                }
                out.append(r);
            } else {
                StringBuilder v = new StringBuilder();
                while (i < chars.length && chars[i] != ' ' && chars[i] != '\t') {
                    v.append(chars[i]);
                    i++;
                }
                String r = evalDollarExpression(String.valueOf(v.toString()), context);
                if (escapeResultPath) {
                    r = context.getShell().escapePath(r);
                }
                out.append(r);
            }
        }
        return i;
    }

    @Override
    public void evalSuffixOperation(String opString, JShellCommandNode node, JShellFileContext context) {
        switch (opString) {
            case "&": {
                evalSuffixAndOperation(node, context);
                return;
            }
        }
        throw new JShellException(1, "unsupported suffix operator " + opString);
    }

    @Override
    public void evalSuffixAndOperation(JShellCommandNode node, JShellFileContext context) {
        node.eval(context);
    }

    @Override
    public void evalBinaryAndOperation(JShellCommandNode left, JShellCommandNode right, JShellFileContext context) {
        right.eval(context);
        left.eval(context);
    }

    @Override
    public void evalBinaryOperation(String opString, JShellCommandNode left, JShellCommandNode right, JShellFileContext context) {
        context.getShell().traceExecution("(" + left + ") " + opString + "(" + right + ")", context);
        if (";".equals(opString)) {
            evalBinarySuiteOperation(left, right, context);
        } else if ("&&".equals(opString)) {
            evalBinaryAndOperation(left, right, context);
        } else if ("||".equals(opString)) {
            evalBinaryOrOperation(left, right, context);
        } else if ("|".equals(opString)) {
            evalBinaryPipeOperation(left, right, context);
        } else {
            throw new JShellException(1, "Unsupported operator " + opString);
        }
    }

    @Override
    public void evalBinaryOrOperation(final JShellCommandNode left, JShellCommandNode right, final JShellFileContext context) {
        try {
            context.getShell().uniformException(new JShellNodeUnsafeRunnable(left, context));
            return;
        } catch (JShellUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return;
            }
        }
        right.eval(context);
    }

    @Override
    public void evalBinaryPipeOperation(final JShellCommandNode left, JShellCommandNode right, final JShellFileContext context) {
        final PipedOutputStream out;
        final PipedInputStream in;
        final JavaShellNonBlockingInputStream in2;
        try {
            out = new PipedOutputStream();
            in = new PipedInputStream(out, 1024);
            in2 = (in instanceof JavaShellNonBlockingInputStream) ? (JavaShellNonBlockingInputStream) in : new JavaShellNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
        } catch (IOException ex) {
//            Logger.getLogger(BinoOp.class.getName()).log(Level.SEVERE, null, ex);
            throw new JShellException(1, ex);
        }
        final JShellUniformException[] a = new JShellUniformException[2];
        final PrintStream out1 = new PrintStream(out);
        final JShellFileContext leftContext = context.getShell().createNewContext(context).setOut(out1);
        Thread j1 = new Thread() {
            @Override
            public void run() {
                try {
                    context.getShell().uniformException(new JShellNodeUnsafeRunnable(left, leftContext));
                } catch (JShellUniformException e) {
                    if (e.isQuit()) {
                        e.throwQuit();
                        return;
                    }
                    a[0] = e;
                }
                in2.noMoreBytes();
            }

        };
        j1.start();
        JShellFileContext rightContext = context.getShell().createNewContext(context).setIn((InputStream) in2);
        try {
            context.getShell().uniformException(new JShellNodeUnsafeRunnable(right, rightContext));
        } catch (JShellUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return;
            }
            a[1] = e;
        }
        out1.flush();
        try {
            j1.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (a[1] != null) {
            a[1].throwAny();
        }
    }

    @Override
    public void evalBinarySuiteOperation(JShellCommandNode left, JShellCommandNode right, JShellFileContext context) {
        try {
            context.getShell().uniformException(new JShellNodeUnsafeRunnable(left, context));
        } catch (JShellUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return;
            }
        }
        right.eval(context);
    }

    @Override
    public String evalCommandAndReturnString(JShellCommandNode command, JShellFileContext context) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JShellFileContext c2 = context.getShell().createNewContext(context, context.getServiceName(), context.getArgsArray());
        PrintStream p = new PrintStream(out);
        c2.setOut(p);
        command.eval(c2);
        p.flush();
        String cc = evalFieldSubstitutionAfterCommandSubstitution(out.toString(), context);
        return (context.getShell().escapeString(cc));
    }

    @Override
    public String evalDollarSharp(JShellFileContext context) {
        return (String.valueOf(context.getArgsList().size()));
    }

    @Override
    public String evalDollarName(String name, JShellFileContext context) {
        return (String.valueOf(context.vars().get(name, "")));
    }

    @Override
    public String evalDollarInterrogation(JShellFileContext context) {
        return (String.valueOf(context.getLastResult().getCode()));
    }

    @Override
    public String evalDollarInteger(int index, JShellFileContext context) {
        if (index < context.getArgsList().size()) {
            return (String.valueOf(context.getArg(index)));
        }
        return "";
    }

    @Override
    public String evalDollarExpression(String stringExpression, JShellFileContext context) {
        String str = evalSimpleQuotesExpression(stringExpression, context);
        if (str.equals("#")) {
            return evalDollarSharp(context);
        } else if (str.equals("?")) {
            return evalDollarInterrogation(context);
        } else if (str.isEmpty()) {
            //do nothing
            return "";
        } else if (str.charAt(0) >= '0' && str.charAt(0) <= '9') {
            int index = Integer.parseInt(str, 10);
            return evalDollarInteger(index, context);
        } else {
            return evalDollarName(str, context);
        }
    }

    @Override
    public String evalSimpleQuotesExpression(String expressionString, JShellFileContext context) {
//should replace wildcards ...
        StringBuilder sb = new StringBuilder();
        char[] chars = expressionString.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    sb.append(chars[i]);
                    break;
                }
                case '\"': {
                    i = readAndEvalDblQuotesExpression(chars, i, sb, context);
                    break;
                }
                case '`': {
                    i = readAndEvalAntiQuotesString(chars, i, sb, context);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String evalDoubleQuotesExpression(String stringExpression, JShellFileContext context) {
//should replace wildcards ...
        StringBuilder sb = new StringBuilder();
        char[] chars = stringExpression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    i = readAndEvalDollarExpression(chars, i, sb, false, context);
                    break;
                }
                case '\'': {
                    i = readAndEvalSimpleQuotesExpression(chars, i, sb, context);
                    break;
                }
                case '`': {
                    i = readAndEvalAntiQuotesString(chars, i, sb, context);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String evalAntiQuotesExpression(String stringExpression, JShellFileContext context) {
        JShellCommandNode t = context.getShell().parseCommandLine(stringExpression);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JShellFileContext c2 = context.getShell().createNewContext(context);
        c2.setOut(new PrintStream(out));
        t.eval(c2);
        c2.out().flush();
        return out.toString();
    }

    public String evalNoQuotesExpression(String stringExpression, JShellFileContext context) {
        StringBuilder sb = new StringBuilder();
        char[] chars = stringExpression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    i = readAndEvalDollarExpression(chars, i, sb, true, context);
                    break;
                }
//                case '\"': {
//                    i=readAndEvalDblQuotesExpression(chars,i,sb,context);
//                    break;
//                }
//                case '\'': {
//                    i=readAndEvalSimpleQuotesExpression(chars,i,sb,context);
//                    break;
//                }
//                case '`': {
//                    i=readAndEvalAntiQuotesString(chars,i,sb,context);
//                    break;
//                }
                case '\\': {
                    i++;
                    sb.append(chars[i]);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    public String expandEnvVars(String stringExpression, boolean escapeResultPath, JShellFileContext context) {
        StringBuilder sb = new StringBuilder();
        char[] chars = stringExpression.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '$': {
                    i = readAndEvalDollarExpression(chars, i, sb, true, context);
                    break;
                }
                default: {
                    sb.append(chars[i]);
                }
            }
        }
        return sb.toString();
    }

    public String evalFieldSubstitutionAfterCommandSubstitution(String commandResult, JShellFileContext context) {

        String IFS = context.vars().get("IFS"," \t\n");
        if (!IFS.isEmpty()) {
            //https://pubs.opengroup.org/onlinepubs/9699919799/utilities/V3_chap02.html#tag_18_06_03
            //https://unix.stackexchange.com/questions/164508/why-do-newline-characters-get-lost-when-using-command-substitution
            StringTokenizer st = new StringTokenizer(commandResult, IFS);
            StringBuilder sb = new StringBuilder();
            while (st.hasMoreTokens()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(st.nextToken());
            }
            return sb.toString();
        }
        return commandResult;
    }

}
