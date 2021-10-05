package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.gui.CoreNutsUtilGui;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class DefaultNutsQuestion<T> implements NutsQuestion<T> {

    private final NutsSessionTerminal terminal;
    private final NutsPrintStream out;
    private final NutsWorkspace ws;
    private String message;
    private Object[] messageParameters;
    private String cancelMessage;
    private Object[] cancelMessageParameters;
    private Object[] acceptedValues;
    private String hintMessage;
    private Object[] hintMessageParameters;
    private T defaultValue;
    private boolean resetLine;
    private Class<T> valueType;
    private NutsQuestionFormat<T> format;
    private NutsQuestionParser<T> parser;
    private NutsQuestionValidator<T> validator;
    private NutsSession session;
    private boolean traceConfirmation = false;
    private boolean executed = false;
    private boolean password = false;
    private Object lastResult = null;

    public DefaultNutsQuestion(NutsWorkspace ws, NutsSessionTerminal terminal, NutsPrintStream out) {
        this.ws = ws;
        this.terminal = terminal;
        this.out = out;
    }

    private T execute() {
        checkSession();
        if (!traceConfirmation && (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE))) {
            switch (getSession().getConfirm()) {
                case YES: {
                    return (T) Boolean.TRUE;
                }
                case NO: {
                    return (T) Boolean.FALSE;
                }
                case ERROR: {
                    if (cancelMessage != null) {
                        NutsByteArrayPrintStream os = new NutsByteArrayPrintStream(getSession());
                        os.printf(message, this.getCancelMessage());
                        os.flush();
                        throw new NutsUserCancelException(getSession(), NutsMessage.formatted(os.toString()));
                    } else {
                        NutsByteArrayPrintStream os = new NutsByteArrayPrintStream(getSession());
                        os.printf(message, this.getCancelMessageParameters());
                        os.flush();
                        throw new NutsUserCancelException(getSession(), NutsMessage.cstyle("cancelled : %s", NutsMessage.formatted(os.toString())));
                    }
                }
            }
        }
        if (!getSession().isPlainOut()) {
            NutsByteArrayPrintStream os = new NutsByteArrayPrintStream(getSession());
            os.printf(message, this.getMessageParameters());
            os.flush();
            throw new NutsExecutionException(getSession(), NutsMessage.cstyle(
                    "unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for question : " + os
            ), 243);
        }

        boolean gui = session.isGui() && session.env().isGraphicalDesktopEnvironment();

        String message = this.getMessage();
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        boolean extraInfo = false;
        NutsQuestionParser<T> p = this.getParser();
        if (p == null) {
            p = new DefaultNutsResponseParser<>(getSession(), this.getValueType());
        }
        NutsQuestionFormat<T> ff = this.getFormat();
        if (ff == null) {
            ff = new DefaultNutsQuestionFormat<>(getSession());
        }
        Object[] _acceptedValues = this.getAcceptedValues();
        if (_acceptedValues == null) {
            _acceptedValues = ff.getDefaultValues(this.getValueType(), this);
        }
        if (_acceptedValues == null) {
            _acceptedValues = new Object[0];
        }
        while (true) {
            NutsPrintStream out = this.out;
            ByteArrayOutputStream bos = null;
            if (gui) {
                bos = new ByteArrayOutputStream();
                out = session.io().createPrintStream(bos);
            }
            if (resetLine) {
                out.resetLine();
            }
            out.printf(message, this.getMessageParameters());
            boolean first = true;
            if (this.getDefaultValue() != null) {
                if (first) {
                    first = false;
                    out.print(" (");
                } else {
                    out.print(", ");
                }
                out.printf("default is %s", ws.text().ofStyled(ff.format(this.getDefaultValue(), this), NutsTextStyle.primary1()));
            }
            if (getHintMessage() != null) {
                if (getHintMessage().length() > 0) {
                    out.print(" (");
                    out.printf(getHintMessage(), getHintMessageParameters());
                    out.print(")");
                }
            } else {
                if (_acceptedValues.length > 0) {
                    if (first) {
                        first = false;
                        out.print(" (");
                    } else {
                        out.print(", ");
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < _acceptedValues.length; i++) {
                        Object acceptedValue = _acceptedValues[i];
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(ff.format(acceptedValue, this));
                    }
                    out.printf("accepts %s", ws.text().ofStyled(sb.toString(), NutsTextStyle.primary4()));
                }
                if (!first) {
                    out.print(")");
                }
            }

            out.flush();
            switch (getSession().getConfirm()) {
                case ERROR: {
                    out.flush();
                    out.println(" : cancel");
                    throw new NutsUserCancelException(getSession());
                }
            }
            if (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE)) {
                switch (getSession().getConfirm()) {
                    case YES: {
                        out.flush();
                        out.println(" : yes");
                        throw new NutsUserCancelException(getSession());
                    }
                    case NO: {
                        out.flush();
                        out.println(" : no");
                        throw new NutsUserCancelException(getSession());
                    }
                }
            }
            if (password) {
                char[] v;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    if (gui) {
                        out.printf("\t Please enter value or ```error %s``` to cancel : ", "cancel!");
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), true);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0.toCharArray();
                    } else {
                        v = terminal.readPassword("\t Please enter value or ```error %s``` to cancel : ", "cancel!");
                    }
                } else {
                    out.flush();
                    if (gui) {
                        out.printf(" ");
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), true);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0.toCharArray();
                    } else {
                        v = terminal.readPassword(" ");
                    }
                }
                if (Arrays.equals("cancel!".toCharArray(), v)) {
                    throw new NutsUserCancelException(getSession());
                }
                try {
                    if (this.validator != null) {
                        v = (char[]) this.validator.validate((T) v, this);
                    }
                    return (T) v;
                } catch (NutsUserCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.printf("```error ERROR``` : %s%n", ex);
                }
            } else {
                String v;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    if (gui) {
                        out.printf("\t Please enter value or ```error %s``` to cancel : ", "cancel!");
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), false);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0;
                    } else {
                        v = terminal.readLine("\t Please enter value or ```error %s``` to cancel : ", "cancel!");
                    }
                } else {
                    out.flush();
                    if (gui) {
                        out.printf(" ? : ");
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), false);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0;
                    } else {
                        v = terminal.readLine(" ? : ");
                    }
                }
                try {
                    T parsed = p.parse(v, this.getDefaultValue(), this);
                    if (this.validator != null) {
                        parsed = this.validator.validate(parsed, this);
                    }
                    return parsed;
                } catch (NutsUserCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.printf("```error ERROR``` : %s%n", ex);
                }
            }
            extraInfo = true;
        }
    }

    private String showGuiInput(String str, boolean pwd) {
        String ft = getSession().text().parse(str).filteredText();
        NutsMessage title = NutsMessage.cstyle("Nuts Package Manager - %s",getSession().getWorkspace().getApiId().getVersion());
        if (session.getAppId() != null) {
            try {
                NutsDefinition def = session.search().setId(session.getAppId())
                        .setEffective(true).setLatest(true).getResultDefinitions().first();
                if (def != null) {
                    String n = def.getEffectiveDescriptor().getName();
                    if (!NutsBlankable.isBlank(n)) {
                        title = NutsMessage.cstyle("%s - %s",n, def.getEffectiveDescriptor().getId().getVersion());
                    }
                }
            } catch (Exception ex) {
                //
            }
        }
        if(password){
            return CoreNutsUtilGui.inputPassword(NutsMessage.formatted(str),title,getSession());
        }else {
            return CoreNutsUtilGui.inputString(NutsMessage.formatted(str),title,getSession());
        }
    }

    @Override
    public boolean isResetLine() {
        return resetLine;
    }

    @Override
    public NutsQuestion<T> resetLine() {
        return resetLine(true);
    }

    @Override
    public NutsQuestion<T> resetLine(boolean resetLine) {
        this.resetLine = resetLine;
        return this;
    }

    @Override
    public NutsQuestion<Boolean> forBoolean(String msg, Object... params) {
        return ((NutsQuestion<Boolean>) this).setValueType(Boolean.class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<char[]> forPassword(String msg, Object... params) {
        this.password = true;
        return ((NutsQuestion<char[]>) this).setValueType(char[].class).setMessage(msg, params);
    }

    @Override
    public NutsQuestion<String> forString(String msg, Object... params) {
        return ((NutsQuestion<String>) this).setValueType(String.class).setMessage(msg, params);
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
    public String getHintMessage() {
        return hintMessage;
    }

    @Override
    public Object[] getHintMessageParameters() {
        return hintMessageParameters;
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
    public String getCancelMessage() {
        return cancelMessage;
    }

    @Override
    public Object[] getCancelMessageParameters() {
        return cancelMessageParameters;
    }

    @Override
    public NutsQuestion<T> setMessage(String message, Object... messageParameters) {
        this.message = message;
        this.messageParameters = messageParameters;
        return this;
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
    public NutsQuestion<T> setAcceptedValues(Object[] acceptedValues) {
        this.acceptedValues = acceptedValues;
        return this;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
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
    public NutsQuestion<T> setValueType(Class<T> valueType) {
        this.valueType = valueType;
        return this;
    }

    @Override
    public NutsQuestionFormat<T> getFormat() {
        return format;
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
    public NutsQuestion<T> setParser(NutsQuestionParser<T> parser) {
        this.parser = parser;
        return this;
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
    public NutsQuestion<T> setCancelMessage(String message, Object... params) {
        if (message == null) {
            this.cancelMessage = null;
            this.cancelMessageParameters = null;
        } else {
            this.cancelMessage = message;
            this.cancelMessageParameters = params == null ? new Object[0] : params;
        }
        return this;
    }

    @Override
    public NutsQuestion<T> setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NutsQuestion<T> configure(boolean skipUnsupported, String... args) {
        checkSession();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, args, "question");
    }

    /**
     * configure the current forCommand with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     *                        silently
     * @param commandLine     arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        checkSession();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "trace-confirmation": {
                boolean val = cmd.nextBoolean().getValue().getBoolean();
                if (a.isEnabled()) {
                    this.traceConfirmation = val;
                }
                break;
            }
        }
        return false;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }
}
