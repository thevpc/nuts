package net.vpc.app.nuts.core;

import java.io.ByteArrayOutputStream;
import net.vpc.app.nuts.*;

import java.io.PrintStream;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.io.ByteArrayPrintStream;

public class DefaultNutsQuestionExecutor<T> implements NutsQuestion<T> {

    private String message;
    private Object[] messageParameters;
    private Object[] acceptedValues;
    private Object defaultValue;
    private Class<T> valueType;
    private NutsResponseParser parser;

    private final NutsTerminal terminal;
    private final PrintStream out;
    private final NutsWorkspace ws;
    private NutsSession session;
    private boolean traceConfirmation = false;
    private boolean executed = false;
    private Object lastResult = null;

    public DefaultNutsQuestionExecutor(NutsWorkspace ws, NutsTerminal terminal, PrintStream out) {
        this.ws = ws;
        this.terminal = terminal;
        this.out = out;
    }

    @Override
    public NutsQuestion<T> run() {
        lastResult = execute();
        executed = true;
        return this;
    }

    @Override
    public Boolean getBooleanResult() {
        return (Boolean) getResult();
    }

    @Override
    public <T> T getResult() {
        if (!executed) {
            run();
        }
        return (T) lastResult;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsQuestion<T> session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsQuestion<T> setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    private NutsSession getValidSession() {
        if (session == null) {
            session = ws.createSession();
        }
        return session;
    }

    private T execute() {
        if (!traceConfirmation && (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE))) {
            switch (getValidSession().getConfirm()) {
                case YES: {
                    return (T) Boolean.TRUE;
                }
                case NO: {
                    return (T) Boolean.FALSE;
                }
                case CANCEL: {
                    ByteArrayPrintStream os = new ByteArrayPrintStream();
                    PrintStream os2 = ws.io().getTerminalFormat().prepare(os);
                    os2.printf(message, this.getMessageParameters());
                    os2.flush();
                    throw new NutsUserCancelException(ws, "Cancelled : " + os.toString());
                }
            }
        }
        if (!getValidSession().isPlainOut()) {
            ByteArrayPrintStream os = new ByteArrayPrintStream();
            PrintStream os2 = ws.io().getTerminalFormat().prepare(os);
            os2.printf(message, this.getMessageParameters());
            os2.flush();
            throw new NutsExecutionException(ws, "Unable to switch to interactive mode for non plain text output format. "
                    + "You need to provide default response for :" + os.toString(), 243);
        }
        String message = this.getMessage();
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        boolean extraInfo = false;
        while (true) {
            out.printf(message, this.getMessageParameters());
            NutsResponseParser p = this.getParser();
            if (p == null) {
                p = new DefaultNutsResponseParser(ws);
            }
            Object[] acceptedValues = this.getAcceptedValues();
            if (acceptedValues == null) {
                acceptedValues = p.getDefaultAcceptedValues(this.getValueType());
            }
            boolean first = true;

            if (this.getDefaultValue() != null) {
                if (first) {
                    first = false;
                    out.print(" \\(");
                } else {
                    out.print(", ");
                }
                out.printf("default is [[%s]]", p.format(this.getDefaultValue()));
            }

            if (acceptedValues != null && acceptedValues.length > 0) {
                if (first) {
                    first = false;
                    out.print(" \\(");
                } else {
                    out.print(", ");
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < acceptedValues.length; i++) {
                    Object acceptedValue = acceptedValues[i];
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(p.format(acceptedValue));
                }
                out.printf("accepts [[%s]]", sb.toString());
            }
            if (!first) {
                out.print("\\)");
            }
            out.flush();
            switch (getValidSession().getConfirm()) {
                case CANCEL: {
                    out.flush();
                    out.println(" ? : cancel");
                    throw new NutsUserCancelException(ws);
                }
            }
            if (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE)) {
                switch (getValidSession().getConfirm()) {
                    case YES: {
                        out.flush();
                        out.println(" ? : yes");
                        throw new NutsUserCancelException(ws);
                    }
                    case NO: {
                        out.flush();
                        out.println(" ? : no");
                        throw new NutsUserCancelException(ws);
                    }
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
                throw new NutsUserCancelException(ws);
            }
            T parsed = null;
            if (v == null || v.length() == 0) {
                try {
                    parsed = (T) p.parse(this.getDefaultValue(), this.getValueType());
                    return parsed;
                } catch (Exception ex) {
                    out.printf("@@ERROR@@ : %s%n", ex.getMessage() == null ? ex.toString() : ex.getMessage());
                }
            }
            try {
                parsed = (T) p.parse(v, this.getValueType());
                return parsed;
            } catch (Exception ex) {
                out.printf("@@ERROR@@ : %s%n", ex.getMessage() == null ? ex.toString() : ex.getMessage());
            }
            extraInfo = true;
        }
    }

    @Override
    public NutsQuestion<Boolean> forBoolean(String msg, Object... params) {
        return (NutsQuestion<Boolean>) setValueType(Boolean.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<String> forString(String msg, Object... params) {
        return (NutsQuestion<String>) setValueType(String.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Integer> forInteger(String msg, Object... params) {
        return (NutsQuestion<Integer>) setValueType(Integer.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Long> forLong(String msg, Object... params) {
        return (NutsQuestion<Long>) setValueType(Long.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Float> forFloat(String msg, Object... params) {
        return (NutsQuestion<Float>) setValueType(Float.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Double> forDouble(String msg, Object... params) {
        return (NutsQuestion<Double>) setValueType(Double.class).setMessage(msg, params);
    }

    @Override
    public <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, String msg, Object... params) {
        K[] values = enumType.getEnumConstants();
        setValueType(enumType)
                .setMessage(msg, params)
                .setAcceptedValues(values);
        return (NutsQuestion<K>) this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public NutsQuestion<T> message(String message, Object... messageParameters) {
        return setMessage(message, messageParameters);
    }

    @Override
    public NutsQuestion<T> setMessage(String message, Object... messageParameters) {
        this.message = message;
        this.messageParameters = messageParameters;
        return this;
    }

    @Override
    public NutsQuestion<T> message(String message) {
        return setMessage(message);
    }

    @Override
    public NutsQuestion<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public Object[] getMessageParameters() {
        return messageParameters;
    }

    @Override
    public NutsQuestion<T> messageParameters(Object... messageParameters) {
        return setMessageParameters(messageParameters);
    }

    @Override
    public NutsQuestion<T> setMessageParameters(Object... messageParameters) {
        this.messageParameters = messageParameters;
        return this;
    }

    @Override
    public Object[] getAcceptedValues() {
        return acceptedValues;
    }

    @Override
    public NutsQuestion<T> acceptedValues(Object[] acceptedValues) {
        return setAcceptedValues(acceptedValues);
    }

    @Override
    public NutsQuestion<T> setAcceptedValues(Object[] acceptedValues) {
        this.acceptedValues = acceptedValues;
        return this;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public NutsQuestion<T> defaultValue(Object defautValue) {
        return setDefaultValue(defautValue);
    }

    @Override
    public NutsQuestion<T> setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Class getValueType() {
        return valueType;
    }

    @Override
    public NutsQuestion<T> valueType(Class valueType) {
        return setValueType(valueType);
    }

    @Override
    public NutsQuestion<T> setValueType(Class valueType) {
        this.valueType = valueType;
        return this;
    }

    @Override
    public NutsResponseParser getParser() {
        return parser;
    }

    @Override
    public NutsQuestion<T> parser(NutsResponseParser parser) {
        return setParser(parser);
    }

    @Override
    public NutsQuestion<T> setParser(NutsResponseParser parser) {
        this.parser = parser;
        return this;
    }

    @Override
    public final NutsQuestion<T> configure(String... args) {
        return NutsConfigurableHelper.configure(this, ws, args, "question");
    }

    @Override
    public final boolean configure(NutsCommand commandLine, boolean skipIgnored) {
        return NutsConfigurableHelper.configure(this, ws, commandLine, skipIgnored);
    }

    @Override
    public boolean configureFirst(NutsCommand cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "trace-confirmation": {
                this.traceConfirmation = cmd.nextBoolean().getValue().getBoolean();
                break;
            }
        }
        return false;
    }
}
