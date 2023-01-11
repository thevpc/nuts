package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.cmdline.NCommandLineConfigurable;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.io.printstream.NByteArrayOutputStream;
import net.thevpc.nuts.runtime.standalone.app.gui.CoreNUtilGui;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NQuestion;
import net.thevpc.nuts.util.NQuestionFormat;
import net.thevpc.nuts.util.NQuestionParser;
import net.thevpc.nuts.util.NQuestionValidator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultNQuestion<T> implements NQuestion<T> {

    private final NSessionTerminal terminal;
    private final NOutputStream out;
    private final NWorkspace ws;
    private NMsg message;
    private NMsg cancelMessage;
    private List<Object> acceptedValues;
    private NMsg hintMessage;
    private T defaultValue;
    private boolean resetLine;
    private Class<T> valueType;
    private NQuestionFormat<T> format;
    private NQuestionParser<T> parser;
    private NQuestionValidator<T> validator;
    private NSession session;
    private boolean traceConfirmation = false;
    private boolean executed = false;
    private boolean password = false;
    private Object lastResult = null;

    public DefaultNQuestion(NWorkspace ws, NSessionTerminal terminal, NOutputStream out) {
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
                        NByteArrayOutputStream os = new NByteArrayOutputStream(getSession());
                        os.print(cancelMessage);
                        os.flush();
                        throw new NCancelException(getSession(), NMsg.ofNtf(os.toString()));
                    } else {
                        NByteArrayOutputStream os = new NByteArrayOutputStream(getSession());
                        os.print(message);
                        os.flush();
                        throw new NCancelException(getSession(), NMsg.ofC("cancelled : %s", NMsg.ofNtf(os.toString())));
                    }
                }
            }
        }
        if (!getSession().isPlainOut()) {
            NByteArrayOutputStream os = new NByteArrayOutputStream(getSession());
            os.print(message);
            os.flush();
            throw new NExecutionException(getSession(), NMsg.ofC(
                    "unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for question : %s", os
            ), 243);
        }

        boolean gui = session.isGui() && NEnvs.of(session).isGraphicalDesktopEnvironment();

        NMsg message = this.getMessage();
//        if (message.endsWith("\n")) {
//            message = message.substring(0, message.length() - 1);
//        }
        boolean extraInfo = false;
        NQuestionParser<T> p = this.getParser();
        if (p == null) {
            p = new DefaultNResponseParser<>(getSession(), this.getValueType());
        }
        NQuestionFormat<T> ff = this.getFormat();
        if (ff == null) {
            ff = new DefaultNQuestionFormat<>(getSession());
        }
        List<Object> _acceptedValues = this.getAcceptedValues();
        if (_acceptedValues == null) {
            _acceptedValues = ff.getDefaultValues(this.getValueType(), this);
        }
        if (_acceptedValues == null) {
            _acceptedValues = new ArrayList<>();
        }
        while (true) {
            NOutputStream out = this.out;
            ByteArrayOutputStream bos = null;
            if (gui) {
                bos = new ByteArrayOutputStream();
                out = NOutputStream.of(bos, session);
            }
            if (resetLine) {
                out.resetLine();
            }
            out.print(message);
            boolean first = true;
            if (this.getDefaultValue() != null) {
                if (first) {
                    first = false;
                    out.print(" (");
                } else {
                    out.print(", ");
                }
                out.print(NMsg.ofC("default is %s", NTexts.of(session).ofStyled(ff.format(this.getDefaultValue(), this), NTextStyle.primary1())));
            }
            if (getHintMessage() != null) {
                out.print(" (");
                out.print(getHintMessage());
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
                    out.print(NMsg.ofC("accepts %s", NTexts.of(session).ofStyled(sb.toString(), NTextStyle.primary4())));
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
                    throw new NCancelException(getSession());
                }
            }
            if (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE)) {
                switch (getSession().getConfirm()) {
                    case YES: {
                        out.flush();
                        out.println(" : yes");
                        throw new NCancelException(getSession());
                    }
                    case NO: {
                        out.flush();
                        out.println(" : no");
                        throw new NCancelException(getSession());
                    }
                }
            }
            if (password) {
                char[] v;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    if (gui) {
                        out.print(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), true);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0.toCharArray();
                    } else {
                        v = terminal.readPassword(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                    }
                } else {
                    out.flush();
                    if (gui) {
                        out.print(" ");
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), true);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0.toCharArray();
                    } else {
                        v = terminal.readPassword(NMsg.ofPlain(" "));
                    }
                }
                if (Arrays.equals("cancel!".toCharArray(), v)) {
                    throw new NCancelException(getSession());
                }
                try {
                    if (this.validator != null) {
                        v = (char[]) this.validator.validate((T) v, this);
                    }
                    return (T) v;
                } catch (NCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.println(NMsg.ofC("```error ERROR``` : %s", ex));
                }
            } else {
                String v;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    if (gui) {
                        out.print(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), false);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0;
                    } else {
                        v = terminal.readLine(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                    }
                } else {
                    out.flush();
                    if (gui) {
                        out.print(" ? : ");
                        out.flush();
                        String v0 = showGuiInput(bos.toString(), false);
                        if (v0 == null) {
                            v0 = "";
                        }
                        v = v0;
                    } else {
                        v = terminal.readLine(NMsg.ofPlain(" ? : "));
                    }
                }
                try {
                    T parsed = p.parse(v, this.getDefaultValue(), this);
                    if (this.validator != null) {
                        parsed = this.validator.validate(parsed, this);
                    }
                    return parsed;
                } catch (NCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.println(NMsg.ofC("```error ERROR``` : %s", ex));
                }
            }
            extraInfo = true;
        }
    }

    private String showGuiInput(String str, boolean pwd) {
        String ft = NTexts.of(getSession()).parse(str).filteredText();
        NMsg title = NMsg.ofC("Nuts Package Manager - %s", getSession().getWorkspace().getApiId().getVersion());
        if (session.getAppId() != null) {
            try {
                NDefinition def = NSearchCommand.of(session).setId(session.getAppId())
                        .setEffective(true).setLatest(true).getResultDefinitions().first();
                if (def != null) {
                    String n = def.getEffectiveDescriptor().get(session).getName();
                    if (!NBlankable.isBlank(n)) {
                        title = NMsg.ofC("%s - %s", n, def.getEffectiveDescriptor().get(session).getId().getVersion());
                    }
                }
            } catch (Exception ex) {
                //
            }
        }
        if (password) {
            return CoreNUtilGui.inputPassword(NMsg.ofNtf(str), title, getSession());
        } else {
            return CoreNUtilGui.inputString(NMsg.ofNtf(str), title, getSession());
        }
    }

    @Override
    public boolean isResetLine() {
        return resetLine;
    }

    @Override
    public NQuestion<T> resetLine() {
        return resetLine(true);
    }

    @Override
    public NQuestion<T> resetLine(boolean resetLine) {
        this.resetLine = resetLine;
        return this;
    }

    @Override
    public NQuestion<Boolean> forBoolean(NMsg msg) {
        return ((NQuestion<Boolean>) this).setValueType(Boolean.class).setMessage(msg);
    }

    @Override
    public NQuestion<char[]> forPassword(NMsg msg) {
        this.password = true;
        return ((NQuestion<char[]>) this).setValueType(char[].class).setMessage(msg);
    }

    @Override
    public NQuestion<String> forString(NMsg msg) {
        return ((NQuestion<String>) this).setValueType(String.class).setMessage(msg);
    }

    @Override
    public NQuestion<Integer> forInteger(NMsg msg) {
        return ((NQuestion<Integer>) this).setValueType(Integer.class).setMessage(msg);
    }

    @Override
    public NQuestion<Long> forLong(NMsg msg) {
        return ((NQuestion<Long>) this).setValueType(Long.class).setMessage(msg);
    }

    @Override
    public NQuestion<Float> forFloat(NMsg msg) {
        return ((NQuestion<Float>) this).setValueType(Float.class).setMessage(msg);
    }

    @Override
    public NQuestion<Double> forDouble(NMsg msg) {
        return ((NQuestion<Double>) this).setValueType(Double.class).setMessage(msg);
    }

    @Override
    public <K extends Enum> NQuestion<K> forEnum(Class<K> enumType, NMsg msg) {
        K[] values = enumType.getEnumConstants();
        return ((NQuestion<K>) this).setValueType(enumType)
                .setMessage(msg)
                .setAcceptedValues(Arrays.asList(values));
    }

    @Override
    public NMsg getHintMessage() {
        return hintMessage;
    }

    @Override
    public NMsg getMessage() {
        return message;
    }

    @Override
    public NMsg getCancelMessage() {
        return cancelMessage;
    }

    @Override
    public NQuestion<T> setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    @Override
    public NQuestion<T> setHintMessage(NMsg message) {
        this.hintMessage = message;
        return this;
    }

    @Override
    public List<Object> getAcceptedValues() {
        return acceptedValues;
    }

    @Override
    public NQuestion<T> setAcceptedValues(List<Object> acceptedValues) {
        this.acceptedValues = acceptedValues;
        return this;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public NQuestion<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Class<T> getValueType() {
        return valueType;
    }

    @Override
    public NQuestion<T> setValueType(Class<T> valueType) {
        this.valueType = valueType;
        return this;
    }

    @Override
    public NQuestionFormat<T> getFormat() {
        return format;
    }

    @Override
    public NQuestion<T> setFormat(NQuestionFormat<T> parser) {
        this.format = parser;
        return this;
    }

    @Override
    public NQuestionParser<T> getParser() {
        return parser;
    }

    @Override
    public NQuestion<T> setParser(NQuestionParser<T> parser) {
        this.parser = parser;
        return this;
    }

    @Override
    public NQuestionValidator<T> getValidator() {
        return this.validator;
    }

    @Override
    public NQuestion<T> setValidator(NQuestionValidator<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public NQuestion<T> run() {
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
    public NSession getSession() {
        return session;
    }

    @Override
    public NQuestion<T> setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NQuestion<T> configure(boolean skipUnsupported, String... args) {
        checkSession();
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, args, "question");
    }

    @Override
    public NQuestion<T> setCancelMessage(NMsg message) {
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
    public final boolean configure(boolean skipUnsupported, NCommandLine commandLine) {
        checkSession();
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        NArg aa = commandLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "trace-confirmation": {
                commandLine.withNextBoolean((v, a, s) -> this.traceConfirmation = v);
                break;
            }
        }
        return false;
    }

    private void checkSession() {
        NSessionUtils.checkSession(ws, session);
    }

    @Override
    public void configureLast(NCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.throwUnexpectedArgument();
        }
    }
}
