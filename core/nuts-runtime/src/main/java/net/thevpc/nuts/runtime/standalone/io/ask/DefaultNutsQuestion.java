package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.cmdline.NutsCommandLineConfigurable;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsSessionTerminal;
import net.thevpc.nuts.runtime.standalone.io.printstream.NutsByteArrayPrintStream;
import net.thevpc.nuts.runtime.standalone.app.gui.CoreNutsUtilGui;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsQuestion;
import net.thevpc.nuts.util.NutsQuestionFormat;
import net.thevpc.nuts.util.NutsQuestionParser;
import net.thevpc.nuts.util.NutsQuestionValidator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultNutsQuestion<T> implements NutsQuestion<T> {

    private final NutsSessionTerminal terminal;
    private final NutsPrintStream out;
    private final NutsWorkspace ws;
    private NutsMessage message;
    private NutsMessage cancelMessage;
    private List<Object> acceptedValues;
    private NutsMessage hintMessage;
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
                        os.printf(cancelMessage);
                        os.flush();
                        throw new NutsCancelException(getSession(), NutsMessage.ofNtf(os.toString()));
                    } else {
                        NutsByteArrayPrintStream os = new NutsByteArrayPrintStream(getSession());
                        os.printf(message);
                        os.flush();
                        throw new NutsCancelException(getSession(), NutsMessage.ofCstyle("cancelled : %s", NutsMessage.ofNtf(os.toString())));
                    }
                }
            }
        }
        if (!getSession().isPlainOut()) {
            NutsByteArrayPrintStream os = new NutsByteArrayPrintStream(getSession());
            os.printf(message);
            os.flush();
            throw new NutsExecutionException(getSession(), NutsMessage.ofCstyle(
                    "unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for question : %s", os
            ), 243);
        }

        boolean gui = session.isGui() && session.env().isGraphicalDesktopEnvironment();

        NutsMessage message = this.getMessage();
//        if (message.endsWith("\n")) {
//            message = message.substring(0, message.length() - 1);
//        }
        boolean extraInfo = false;
        NutsQuestionParser<T> p = this.getParser();
        if (p == null) {
            p = new DefaultNutsResponseParser<>(getSession(), this.getValueType());
        }
        NutsQuestionFormat<T> ff = this.getFormat();
        if (ff == null) {
            ff = new DefaultNutsQuestionFormat<>(getSession());
        }
        List<Object> _acceptedValues = this.getAcceptedValues();
        if (_acceptedValues == null) {
            _acceptedValues = ff.getDefaultValues(this.getValueType(), this);
        }
        if (_acceptedValues == null) {
            _acceptedValues = new ArrayList<>();
        }
        while (true) {
            NutsPrintStream out = this.out;
            ByteArrayOutputStream bos = null;
            if (gui) {
                bos = new ByteArrayOutputStream();
                out = NutsPrintStream.of(bos, session);
            }
            if (resetLine) {
                out.resetLine();
            }
            out.printf(message);
            boolean first = true;
            if (this.getDefaultValue() != null) {
                if (first) {
                    first = false;
                    out.print(" (");
                } else {
                    out.print(", ");
                }
                out.printf("default is %s", NutsTexts.of(session).ofStyled(ff.format(this.getDefaultValue(), this), NutsTextStyle.primary1()));
            }
            if (getHintMessage() != null) {
                out.print(" (");
                out.printf(getHintMessage());
                out.print(")");
            } else {
                if (_acceptedValues.size() > 0) {
                    if (first) {
                        first = false;
                        out.print(" (");
                    } else {
                        out.print(", ");
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < _acceptedValues.size(); i++) {
                        Object acceptedValue = _acceptedValues.get(i);
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(ff.format(acceptedValue, this));
                    }
                    out.printf("accepts %s", NutsTexts.of(session).ofStyled(sb.toString(), NutsTextStyle.primary4()));
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
                    throw new NutsCancelException(getSession());
                }
            }
            if (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE)) {
                switch (getSession().getConfirm()) {
                    case YES: {
                        out.flush();
                        out.println(" : yes");
                        throw new NutsCancelException(getSession());
                    }
                    case NO: {
                        out.flush();
                        out.println(" : no");
                        throw new NutsCancelException(getSession());
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
                    throw new NutsCancelException(getSession());
                }
                try {
                    if (this.validator != null) {
                        v = (char[]) this.validator.validate((T) v, this);
                    }
                    return (T) v;
                } catch (NutsCancelException ex) {
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
                } catch (NutsCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.printf("```error ERROR``` : %s%n", ex);
                }
            }
            extraInfo = true;
        }
    }

    private String showGuiInput(String str, boolean pwd) {
        String ft = NutsTexts.of(getSession()).parse(str).filteredText();
        NutsMessage title = NutsMessage.ofCstyle("Nuts Package Manager - %s", getSession().getWorkspace().getApiId().getVersion());
        if (session.getAppId() != null) {
            try {
                NutsDefinition def = session.search().setId(session.getAppId())
                        .setEffective(true).setLatest(true).getResultDefinitions().first();
                if (def != null) {
                    String n = def.getEffectiveDescriptor().get(session).getName();
                    if (!NutsBlankable.isBlank(n)) {
                        title = NutsMessage.ofCstyle("%s - %s", n, def.getEffectiveDescriptor().get(session).getId().getVersion());
                    }
                }
            } catch (Exception ex) {
                //
            }
        }
        if (password) {
            return CoreNutsUtilGui.inputPassword(NutsMessage.ofNtf(str), title, getSession());
        } else {
            return CoreNutsUtilGui.inputString(NutsMessage.ofNtf(str), title, getSession());
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
    public NutsQuestion<Boolean> forBoolean(NutsMessage msg) {
        return ((NutsQuestion<Boolean>) this).setValueType(Boolean.class).setMessage(msg);
    }

    @Override
    public NutsQuestion<char[]> forPassword(NutsMessage msg) {
        this.password = true;
        return ((NutsQuestion<char[]>) this).setValueType(char[].class).setMessage(msg);
    }

    @Override
    public NutsQuestion<String> forString(NutsMessage msg) {
        return ((NutsQuestion<String>) this).setValueType(String.class).setMessage(msg);
    }

    @Override
    public NutsQuestion<Integer> forInteger(NutsMessage msg) {
        return ((NutsQuestion<Integer>) this).setValueType(Integer.class).setMessage(msg);
    }

    @Override
    public NutsQuestion<Long> forLong(NutsMessage msg) {
        return ((NutsQuestion<Long>) this).setValueType(Long.class).setMessage(msg);
    }

    @Override
    public NutsQuestion<Float> forFloat(NutsMessage msg) {
        return ((NutsQuestion<Float>) this).setValueType(Float.class).setMessage(msg);
    }

    @Override
    public NutsQuestion<Double> forDouble(NutsMessage msg) {
        return ((NutsQuestion<Double>) this).setValueType(Double.class).setMessage(msg);
    }

    @Override
    public <K extends Enum> NutsQuestion<K> forEnum(Class<K> enumType, NutsMessage msg) {
        K[] values = enumType.getEnumConstants();
        return ((NutsQuestion<K>) this).setValueType(enumType)
                .setMessage(msg)
                .setAcceptedValues(Arrays.asList(values));
    }

    @Override
    public NutsMessage getHintMessage() {
        return hintMessage;
    }

    @Override
    public NutsMessage getMessage() {
        return message;
    }

    @Override
    public NutsMessage getCancelMessage() {
        return cancelMessage;
    }

    @Override
    public NutsQuestion<T> setMessage(NutsMessage message) {
        this.message = message;
        return this;
    }

    @Override
    public NutsQuestion<T> setHintMessage(NutsMessage message) {
        this.hintMessage = message;
        return this;
    }

    @Override
    public List<Object> getAcceptedValues() {
        return acceptedValues;
    }

    @Override
    public NutsQuestion<T> setAcceptedValues(List<Object> acceptedValues) {
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
    public NutsQuestionValidator<T> getValidator() {
        return this.validator;
    }

    @Override
    public NutsQuestion<T> setValidator(NutsQuestionValidator<T> validator) {
        this.validator = validator;
        return this;
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
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
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

    @Override
    public NutsQuestion<T> setCancelMessage(NutsMessage message) {
        this.cancelMessage = message;
        return this;
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
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument aa = commandLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "trace-confirmation": {
                commandLine.withNextBoolean((v, a, s) -> this.traceConfirmation = v, session);
                break;
            }
        }
        return false;
    }

    private void checkSession() {
        NutsSessionUtils.checkSession(ws, session);
    }

    @Override
    public void configureLast(NutsCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.throwUnexpectedArgument(getSession());
        }
    }
}
