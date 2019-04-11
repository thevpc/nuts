package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.PrintStream;
import java.util.Arrays;

public class DefaultNutsQuestionExecutor<T> {

    private NutsQuestion<T> question;
    private NutsTerminal terminal;
    private PrintStream out;
    private NutsWorkspace ws;

    public DefaultNutsQuestionExecutor(NutsWorkspace ws, NutsQuestion<T> question, NutsTerminal terminal, PrintStream out) {
        this.ws = ws;
        this.question = question;
        this.terminal = terminal;
        this.out = out;
    }

    public T execute() {
        String message = question.getMessage();
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        boolean extraInfo = false;
        while (true) {
            out.printf(message, question.getMessageParameters());
            NutsResponseParser p = question.getParser();
            if (p == null) {
                p = DefaultNutsResponseParser.INSTANCE;
            }
            Object[] acceptedValues = question.getAcceptedValues();
            if (acceptedValues != null && acceptedValues.length > 0) {
                acceptedValues = p.getDefaultAcceptedValues(question.getValueType());
            }
            boolean first = true;

            if (question.getDefautValue() != null) {
                if (first) {
                    first = false;
                    out.print(" \\(");
                } else {
                    out.print(", ");
                }
                out.printf("default is [[%s]]", question.getDefautValue());
            }

            if (acceptedValues != null && acceptedValues.length > 0) {
                if (first) {
                    first = false;
                    out.print(" \\(");
                } else {
                    out.print(", ");
                }
                out.printf("accepts [[%s]]", Arrays.toString(acceptedValues));
            }
            if (!first) {
                out.print("\\)");
            }
            out.flush();
            if (question.getValueType().equals(Boolean.class) || question.getValueType().equals(Boolean.TYPE)) {
                if (ws != null && ws.config().getOptions().isYes()) {
                    out.flush();
                    out.println(" ? : yes");
                    return (T) Boolean.TRUE;
                }
                if (ws != null && ws.config().getOptions().isNo()) {
                    out.flush();
                    out.println(" ? : no");
                    return (T) Boolean.FALSE;
                }
            }

            String v = null;
            if (extraInfo) {
                out.print("?\n");
                out.flush();
                v = terminal.readLine("\t Please enter value or @@%s@@ to cancel : ", "cancel!");
            } else {
                out.flush();
                v = terminal.readLine(" ? : ");
            }
            if ("cancel!".equals(v)) {
                throw new NutsUserCancelException();
            }
            T parsed = null;
            if (v == null || v.length() == 0) {
                try {
                    parsed = (T) p.parse(question.getDefautValue(), question.getValueType());
                    return parsed;
                } catch (Exception ex) {
                    out.printf("@@ERROR@@ : %s\n", ex.getMessage() == null ? ex.toString() : ex.getMessage());
                }
            }
            try {
                parsed = (T) p.parse(v, question.getValueType());
                return parsed;
            } catch (Exception ex) {
                out.printf("@@ERROR@@ : %s\n", ex.getMessage() == null ? ex.toString() : ex.getMessage());
            }
            extraInfo = true;
        }
    }
}
