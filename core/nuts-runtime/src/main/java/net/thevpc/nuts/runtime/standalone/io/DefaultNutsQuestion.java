package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.util.Arrays;

import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.ByteArrayPrintStream;

public class DefaultNutsQuestion<T> implements NutsQuestion<T> {

    private String message;
    private Object[] messageParameters;
    private String cancelMessage;
    private Object[] cancelMessageParameters;
    private Object[] acceptedValues;
    private String hintMessage;
    private Object[] hintMessageParameters;
    private T defaultValue;
    private Class<T> valueType;
    private NutsQuestionFormat<T> format;
    private NutsQuestionParser<T> parser;
    private NutsQuestionValidator<T> validator;

    private final NutsTerminal terminal;
    private final PrintStream out;
    private final NutsWorkspace ws;
    private NutsSession session;
    private boolean traceConfirmation = false;
    private boolean executed = false;
    private boolean password = false;
    private Object lastResult = null;

    public DefaultNutsQuestion(NutsWorkspace ws, NutsTerminal terminal, PrintStream out) {
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
    public Boolean getBooleanValue() {
        return (Boolean) getValue();
    }

    @Override
    public T getValue() {
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
                case ERROR: {
                    if(cancelMessage!=null){
                        ByteArrayPrintStream os = new ByteArrayPrintStream();
                        PrintStream os2 = ws.io().term().prepare(os, session);
                        os2.printf(message, this.getCancelMessage());
                        os2.flush();
                        throw new NutsUserCancelException(ws, os.toString());
                    }else {
                        ByteArrayPrintStream os = new ByteArrayPrintStream();
                        PrintStream os2 = ws.io().term().prepare(os, session);
                        os2.printf(message, this.getCancelMessageParameters());
                        os2.flush();
                        throw new NutsUserCancelException(ws, "cancelled : " + os.toString());
                    }
                }
            }
        }
        if (!getValidSession().isPlainOut()) {
            ByteArrayPrintStream os = new ByteArrayPrintStream();
            PrintStream os2 = ws.io().term().prepare(os, session);
            os2.printf(message, this.getMessageParameters());
            os2.flush();
            throw new NutsExecutionException(ws, "Unable to switch to interactive mode for non plain text output format. "
                    + "You need to provide default response (-y|-n) for question : " + os.toString(), 243);
        }
        String message = this.getMessage();
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        boolean extraInfo = false;
        NutsQuestionParser<T> p = this.getParser();
        if (p == null) {
            p = new DefaultNutsResponseParser<>(ws, this.getValueType());
        }
        NutsQuestionFormat<T> ff = this.getFormat();
        if (ff == null) {
            ff = new DefaultNutsQuestionFormat<>(ws);
        }
        Object[] _acceptedValues = this.getAcceptedValues();
        if (_acceptedValues == null) {
            _acceptedValues = ff.getDefaultValues(this.getValueType(),this);
        }
        if (_acceptedValues == null) {
            _acceptedValues = new Object[0];
        }
        while (true) {
            out.printf(message, this.getMessageParameters());
            boolean first = true;
            if (this.getDefaultValue() != null) {
                if (first) {
                    first = false;
                    out.print(" \\(");
                } else {
                    out.print(", ");
                }
                out.printf("default is %s", ws.formats().text().factory().styled(ff.format(this.getDefaultValue(),this),NutsTextNodeStyle.primary(1)));
            }
            if(getHintMessage()!=null){
                if(getHintMessage().length()>0) {
                    out.print(" \\(");
                    out.printf(getHintMessage(), getHintMessageParameters());
                    out.print("\\)");
                }
            }else{
                if (_acceptedValues.length > 0) {
                    if (first) {
                        first = false;
                        out.print(" \\(");
                    } else {
                        out.print(", ");
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < _acceptedValues.length; i++) {
                        Object acceptedValue = _acceptedValues[i];
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(ff.format(acceptedValue,this));
                    }
                    out.printf("accepts %s", ws.formats().text().factory().styled(sb.toString(),NutsTextNodeStyle.primary(4)));
                }
                if (!first) {
                    out.print("\\)");
                }
            }

            out.flush();
            switch (getValidSession().getConfirm()) {
                case ERROR: {
                    out.flush();
                    out.println(" : cancel");
                    throw new NutsUserCancelException(ws);
                }
            }
            if (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE)) {
                switch (getValidSession().getConfirm()) {
                    case YES: {
                        out.flush();
                        out.println(" : yes");
                        throw new NutsUserCancelException(ws);
                    }
                    case NO: {
                        out.flush();
                        out.println(" : no");
                        throw new NutsUserCancelException(ws);
                    }
                }
            }
            if (password) {
                char[] v;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    v = terminal.readPassword("\t Please enter value or ```error %s``` to cancel : ", "cancel!");
                } else {
                    out.flush();
                    v = terminal.readPassword(" ");
                }
                if (Arrays.equals("cancel!".toCharArray(), v)) {
                    throw new NutsUserCancelException(ws);
                }
                try {
                    if (this.validator != null) {
                        v = (char[]) this.validator.validate((T) v, this);
                    }
                    return (T) v;
                } catch (NutsUserCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.printf("```error ERROR``` : %s%n", CoreStringUtils.exceptionToString(ex));
                }
            } else {
                String v;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    v = terminal.readLine("\t Please enter value or ```error %s``` to cancel : ", "cancel!");
                } else {
                    out.flush();
                    v = terminal.readLine(" ? : ");
                }
                try {
                    T parsed = (T) p.parse(v, (T) this.getDefaultValue(),this);
                    if (this.validator != null) {
                        parsed = this.validator.validate(parsed, this);
                    }
                    return parsed;
                } catch (NutsUserCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.printf("```error ERROR``` : %s%n", CoreStringUtils.exceptionToString(ex));
                }
            }
            extraInfo = true;
        }
    }

    @Override
    public NutsQuestion<Boolean> forBoolean(String msg, Object... params) {
        return ((NutsQuestion<Boolean>) this).setValueType(Boolean.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<String> forString(String msg, Object... params) {
        return ((NutsQuestion<String>) this).setValueType(String.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<char[]> forPassword(String msg, Object... params) {
        this.password = true;
        return ((NutsQuestion<char[]>) this).setValueType(char[].class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Integer> forInteger(String msg, Object... params) {
        return ((NutsQuestion<Integer>) this).setValueType(Integer.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Long> forLong(String msg, Object... params) {
        return ((NutsQuestion<Long>) this).setValueType(Long.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Float> forFloat(String msg, Object... params) {
        return ((NutsQuestion<Float>) this).setValueType(Float.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<Double> forDouble(String msg, Object... params) {
        return ((NutsQuestion<Double>) this).setValueType(Double.class).setMessage(msg, params);
    }

    @Override
    public <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, String msg, Object... params) {
        K[] values = enumType.getEnumConstants();
        return ((NutsQuestion<K>) this).setValueType(enumType)
                .setMessage(msg, params)
                .setAcceptedValues(values);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Object[] getMessageParameters() {
        return messageParameters;
    }

    @Override
    public String getHintMessage() {
        return hintMessage;
    }

    @Override
    public Object[] getHintMessageParameters() {
        return hintMessageParameters;
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
    public NutsQuestion<T> hintMessage(String message, Object... messageParameters) {
        return setHintMessage(message, messageParameters);
    }

    @Override
    public NutsQuestion<T> setHintMessage(String message, Object... messageParameters) {
        this.hintMessage = message;
        this.hintMessageParameters = messageParameters;
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
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public NutsQuestion<T> defaultValue(T defautValue) {
        return setDefaultValue(defautValue);
    }

    @Override
    public NutsQuestion<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Class<T> getValueType() {
        return valueType;
    }

    @Override
    public NutsQuestion<T> valueType(Class<T> valueType) {
        return setValueType(valueType);
    }

    @Override
    public NutsQuestion<T> setValueType(Class<T> valueType) {
        this.valueType = valueType;
        return this;
    }

    @Override
    public NutsQuestionFormat<T> getFormat() {
        return format;
    }

    @Override
    public NutsQuestion<T> format(NutsQuestionFormat<T> parser) {
        return setFormat(parser);
    }

    @Override
    public NutsQuestion<T> setFormat(NutsQuestionFormat<T> parser) {
        this.format = parser;
        return this;
    }

    @Override
    public NutsQuestionParser<T> getParser() {
        return parser;
    }

    @Override
    public NutsQuestion<T> parser(NutsQuestionParser<T> parser) {
        return setParser(parser);
    }

    @Override
    public NutsQuestion<T> setParser(NutsQuestionParser<T> parser) {
        this.parser = parser;
        return this;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NutsQuestion<T> configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, "question");
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param commandLine arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "trace-confirmation": {
                boolean val = cmd.nextBoolean().getBooleanValue();
                if(a.isEnabled()) {
                    this.traceConfirmation = val;
                }
                break;
            }
        }
        return false;
    }

    @Override
    public NutsQuestion<T> validator(NutsQuestionValidator<T> validator) {
        return setValidator(validator);
    }

    @Override
    public NutsQuestion<T> setValidator(NutsQuestionValidator<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public NutsQuestionValidator<T> getValidator() {
        return this.validator;
    }

    @Override
    public String getCancelMessage() {
        return cancelMessage;
    }

    @Override
    public Object[] getCancelMessageParameters() {
        return cancelMessageParameters;
    }

    @Override
    public NutsQuestion<T> setCancelMessage(String message, Object... params) {
        if(message==null){
            this.cancelMessage=null;
            this.cancelMessageParameters=null;
        }else{
            this.cancelMessage=message;
            this.cancelMessageParameters=params==null?new Object[0] : params;
        }
        return this;
    }
}
