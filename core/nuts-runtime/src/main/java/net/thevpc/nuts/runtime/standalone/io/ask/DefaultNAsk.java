package net.thevpc.nuts.runtime.standalone.io.ask;

import net.thevpc.nuts.*;

import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.runtime.standalone.app.gui.CoreNUtilGui;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class DefaultNAsk<T> implements NAsk<T> {

    private final NTerminal terminal;
    private final NPrintStream out;
    private NMsg message;
    private NMsg cancelMessage;
    private List<Object> acceptedValues;
    private NMsg hintMessage;
    private T defaultValue;
    private String rememberMeKey;
    private boolean resetLine = true;
    private Class<T> valueType;
    private NAskFormat<T> format;
    private NAskParser<T> parser;
    private NAskValidator<T> validator;
    private boolean traceConfirmation = false;
    private boolean executed = false;
    private boolean password = false;
    private Object lastResult = null;

    public DefaultNAsk(NTerminal terminal, NPrintStream out) {
        this.terminal = terminal;
        this.out = out;
    }

    public String getRememberMeKey() {
        return rememberMeKey;
    }

    public NAsk<T> setRememberMeKey(String rememberMeKey) {
        this.rememberMeKey = rememberMeKey;
        return this;
    }

    private T execute() {
        NSession session = NSession.of();
        NAskCache askCache = NApp.of().getOrComputeProperty(NAskCache.class.getName(), NScopeType.SESSION, () -> new NAskCache());
        if (rememberMeKey != null) {
            Object o = askCache.get(rememberMeKey);
            if (o != null) {
                try {
                    return (T) o;
                } catch (Exception e) {
                    //
                    askCache.remove(rememberMeKey);
                }
            }
        }
        if (!traceConfirmation && isBooleanType()) {
            switch (session.getConfirm().orDefault()) {
                case YES: {
                    return (T) Boolean.TRUE;
                }
                case NO: {
                    return (T) Boolean.FALSE;
                }
                case ERROR: {
                    if (cancelMessage != null) {
                        NMemoryPrintStream os = NMemoryPrintStream.of();
                        os.print(cancelMessage);
                        os.flush();
                        throw new NCancelException(NMsg.ofNtf(os.toString()));
                    } else {
                        NMemoryPrintStream os = NMemoryPrintStream.of();
                        os.print(message);
                        os.flush();
                        throw new NCancelException(NMsg.ofC("cancelled : %s", NMsg.ofNtf(os.toString())));
                    }
                }
            }
        }
        if (!session.isPlainOut()) {
            NMemoryPrintStream os = NMemoryPrintStream.of();
            os.print(message);
            os.flush();
            throw new NExecutionException(NMsg.ofC(
                    "unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for question : %s", os
            ), NExecutionException.ERROR_255);
        }
        boolean gui = session.isGui() && NWorkspace.of().isGraphicalDesktopEnvironment();

        NMsg message = this.getMessage();
//        if (message.endsWith("\n")) {
//            message = message.substring(0, message.length() - 1);
//        }
        boolean extraInfo = false;
        NAskParser<T> p = this.getParser();
        if (p == null) {
            p = new DefaultNResponseParser<>(session, this.getValueType());
        }
        NAskFormat<T> ff = this.getFormat();
        if (ff == null) {
            ff = new DefaultNAskFormat<>();
        }
        List<Object> _acceptedValues = this.getAcceptedValues();
        if (_acceptedValues == null) {
            _acceptedValues = ff.getDefaultValues(this.getValueType(), this);
        }
        if (_acceptedValues == null) {
            _acceptedValues = new ArrayList<>();
        }
        String alwaysKey = null;
        while (true) {
            NPrintStream out = this.out;
            ByteArrayOutputStream bos = null;
            if (gui) {
                bos = new ByteArrayOutputStream();
                out = NPrintStream.of(bos);
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
                out.print(NMsg.ofC("default is %s", NText.ofStyled(ff.format(this.getDefaultValue(), this), NTextStyle.primary1())));
            }
            if (getHintMessage() != null) {
                out.print(" (");
                out.print(getHintMessage());
                out.print(")");
            } else {
                if (!_acceptedValues.isEmpty()) {
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
                    out.print(NMsg.ofC("accepts %s", NText.ofStyled(sb.toString(), NTextStyle.primary4())));
                }
                if (!first) {
                    out.print(")");
                }
            }

            out.flush();
            switch (session.getConfirm().orDefault()) {
                case ERROR: {
                    out.flush();
                    out.println(" : cancel");
                    throw new NCancelException();
                }
            }
            if (isBooleanType()) {
                switch (session.getConfirm().orDefault()) {
                    case YES: {
                        out.flush();
                        out.println(" : yes");
                        throw new NCancelException();
                    }
                    case NO: {
                        out.flush();
                        out.println(" : no");
                        throw new NCancelException();
                    }
                }
            }
            boolean always = false;
            if (password) {
                char[] v;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    if (gui) {
                        out.print(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                        out.flush();
                        CoreNUtilGui.GuiResult v0 = showGuiInput(bos.toString(), true, rememberMeKey != null);
                        if (v0 == null || v0.getValue() ==null) {
                            v = new char[0];
                        }else {
                            v = ((String)v0.getValue()).toCharArray();
                        }
                        always=v0!=null && v0.isRememberMe();
                    } else {
                        v = terminal.readPassword(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                    }
                } else {
                    out.flush();
                    if (gui) {
                        out.print(" ");
                        out.flush();
                        CoreNUtilGui.GuiResult v0 = showGuiInput(bos.toString(), true, rememberMeKey != null);
                        if (v0 == null || v0.getValue() ==null) {
                            v = new char[0];
                        }else {
                            v = ((String)v0.getValue()).toCharArray();
                        }
                        always=v0!=null && v0.isRememberMe();
                    } else {
                        v = terminal.readPassword(NMsg.ofPlain(" "));
                    }
                }
                if (Arrays.equals("cancel!".toCharArray(), v)) {
                    throw new NCancelException();
                }
                try {
                    if (this.validator != null) {
                        v = (char[]) this.validator.validate((T) v, this);
                    }
                    if (always && rememberMeKey != null) {
                        askCache.set(rememberMeKey, v);
                    }
                    return (T) v;
                } catch (NCancelException ex) {
                    throw ex;
                } catch (Exception ex) {
                    out.println(NMsg.ofC("```error ERROR``` : %s", ex));
                }
            } else {
                String responseStr;
                if (extraInfo) {
                    out.print("?\n");
                    out.flush();
                    if (gui) {
                        out.print(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                        out.flush();
                        CoreNUtilGui.GuiResult v0 = showGuiInput(bos.toString(), false, rememberMeKey != null);
                        if (v0 == null || v0.getValue() ==null) {
                            responseStr = "";
                        }else {
                            responseStr = ((String)v0.getValue());
                        }
                        always=v0!=null && v0.isRememberMe();
                    } else {
                        responseStr = terminal.readLine(NMsg.ofC("\t Please enter value or ```error %s``` to cancel : ", "cancel!"));
                        if (responseStr != null) {
                            CoreNUtilGui.GuiResult n = parseGuiResult(responseStr);
                            responseStr = (String) n.getValue();
                            always = n.isRememberMe();
                        }
                    }
                } else {
                    out.flush();
                    if (gui) {
                        out.print(" ? : ");
                        out.flush();
                        CoreNUtilGui.GuiResult v0 = showGuiInput(bos.toString(), false, rememberMeKey != null);
                        if (v0 == null || v0.getValue() ==null) {
                            responseStr = "";
                        }else {
                            responseStr = ((String)v0.getValue());
                        }
                    } else {
                        responseStr = terminal.readLine(NMsg.ofPlain(" ? : "));
                    }
                    if (responseStr != null) {
                        CoreNUtilGui.GuiResult n = parseGuiResult(responseStr);
                        responseStr = (String) n.getValue();
                        always = n.isRememberMe();
                    }
                }
                try {
                    NAskParseContext<T> cc = new MyNAskParseContext<T>(responseStr, this);
                    T parsed = p.parse(cc);
                    if (this.validator != null) {
                        parsed = this.validator.validate(parsed, this);
                    }
                    if (always && rememberMeKey != null) {
                        askCache.set(rememberMeKey, parsed);
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

    private CoreNUtilGui.GuiResult parseGuiResult(String responseStr){
        if (responseStr != null) {
            if (isBooleanType()) {
                if (responseStr.equals("a")) {
                    return new CoreNUtilGui.GuiResult("true",true);
                } else if (responseStr.startsWith("!!")) {
                    responseStr = responseStr.substring(2);
                    return new CoreNUtilGui.GuiResult(responseStr,true);
                } else if (responseStr.equals(responseStr.toUpperCase())) {
                    return new CoreNUtilGui.GuiResult(responseStr,true);
                }
            } else if (responseStr.startsWith("!!")) {
                responseStr = responseStr.substring(2);
                return new CoreNUtilGui.GuiResult(responseStr,true);
            }
        }
        return new CoreNUtilGui.GuiResult(responseStr,false);
    }

    private CoreNUtilGui.GuiResult showGuiInput(String str, boolean pwd, boolean rememberMe) {
        NSession session = NSession.of();
        String ft = NText.of(str).filteredText();
        NMsg title = NMsg.ofC("Nuts Package Manager - %s", session.getWorkspace().getApiId().getVersion());
        if (NApp.of().getId().orNull() != null) {
            try {
                NDefinition def = NSearchCmd.of().setId(NApp.of().getId().get())
                        .setLatest(true).getResultDefinitions()
                        .findFirst().orNull();
                if (def != null) {
                    String n = def.getEffectiveDescriptor().get().getName();
                    if (!NBlankable.isBlank(n)) {
                        title = NMsg.ofC("%s - %s", n, def.getEffectiveDescriptor().get().getId().getVersion());
                    }
                }
            } catch (Exception ex) {
                //
            }
        }
        if (password) {
            return CoreNUtilGui.inputPassword(NMsg.ofNtf(str), title,rememberMe);
        } else {
            return CoreNUtilGui.inputString(NMsg.ofNtf(str), title,rememberMe);
        }
    }

    @Override
    public boolean isResetLine() {
        return resetLine;
    }

    @Override
    public NAsk<T> resetLine() {
        return resetLine(true);
    }

    @Override
    public NAsk<T> resetLine(boolean resetLine) {
        this.resetLine = resetLine;
        return this;
    }

    @Override
    public NAsk<Boolean> forBoolean(NMsg msg) {
        return ((NAsk<Boolean>) this).setValueType(Boolean.class).setMessage(msg);
    }

    @Override
    public NAsk<char[]> forPassword(NMsg msg) {
        this.password = true;
        return ((NAsk<char[]>) this).setValueType(char[].class).setMessage(msg);
    }

    @Override
    public NAsk<String> forString(NMsg msg) {
        return ((NAsk<String>) this).setValueType(String.class).setMessage(msg);
    }

    @Override
    public NAsk<Integer> forInteger(NMsg msg) {
        return ((NAsk<Integer>) this).setValueType(Integer.class).setMessage(msg);
    }

    @Override
    public NAsk<Long> forLong(NMsg msg) {
        return ((NAsk<Long>) this).setValueType(Long.class).setMessage(msg);
    }

    @Override
    public NAsk<Float> forFloat(NMsg msg) {
        return ((NAsk<Float>) this).setValueType(Float.class).setMessage(msg);
    }

    @Override
    public NAsk<Double> forDouble(NMsg msg) {
        return ((NAsk<Double>) this).setValueType(Double.class).setMessage(msg);
    }

    @Override
    public <K extends Enum> NAsk<K> forEnum(Class<K> enumType, NMsg msg) {
        K[] values = enumType.getEnumConstants();
        return ((NAsk<K>) this).setValueType(enumType)
                .setMessage(msg)
                .setAcceptedValues(Arrays.asList((Object[]) values));
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
    public NAsk<T> setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    @Override
    public NAsk<T> setHintMessage(NMsg message) {
        this.hintMessage = message;
        return this;
    }

    @Override
    public List<Object> getAcceptedValues() {
        return acceptedValues;
    }

    @Override
    public NAsk<T> setAcceptedValues(List<Object> acceptedValues) {
        this.acceptedValues = acceptedValues;
        return this;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public NAsk<T> setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Class<T> getValueType() {
        return valueType;
    }

    @Override
    public NAsk<T> setValueType(Class<T> valueType) {
        this.valueType = valueType;
        return this;
    }

    @Override
    public NAskFormat<T> getFormat() {
        return format;
    }

    @Override
    public NAsk<T> setFormat(NAskFormat<T> parser) {
        this.format = parser;
        return this;
    }

    @Override
    public NAskParser<T> getParser() {
        return parser;
    }

    @Override
    public NAsk<T> setParser(NAskParser<T> parser) {
        this.parser = parser;
        return this;
    }

    @Override
    public NAskValidator<T> getValidator() {
        return this.validator;
    }

    @Override
    public NAsk<T> setValidator(NAskValidator<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public NAsk<T> run() {
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

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NAsk<T> configure(boolean skipUnsupported, String... args) {
        return NCmdLineConfigurable.configure(this, skipUnsupported, args, "question");
    }

    @Override
    public NAsk<T> setCancelMessage(NMsg message) {
        this.cancelMessage = message;
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "trace-confirmation": {
                cmdLine.withNextFlag((v, a) -> this.traceConfirmation = v);
                break;
            }
        }
        return false;
    }

    private boolean isBooleanType() {
        return (this.getValueType().equals(Boolean.class) || this.getValueType().equals(Boolean.TYPE));
    }


    private static class MyNAskParseContext<T> implements NAskParseContext<T> {
        private final Object response;
        private final NAsk<T> question;

        public MyNAskParseContext(String response, NAsk<T> question) {
            this.response = response;
            this.question = question;
        }

        @Override
        public Object response() {
            return response;
        }

        @Override
        public NAsk<T> question() {
            return question;
        }
    }

    public static class NAskCache {
        Map<String, Object> cachedResponses = new HashMap<>();

        public Object get(String id) {
            return cachedResponses.get(id);
        }

        public void remove(String id) {
            cachedResponses.remove(id);
        }

        public void set(String id, Object value) {
            if (value != null) {
                cachedResponses.put(id, value);
            } else {
                cachedResponses.remove(id);
            }
        }
    }
}
