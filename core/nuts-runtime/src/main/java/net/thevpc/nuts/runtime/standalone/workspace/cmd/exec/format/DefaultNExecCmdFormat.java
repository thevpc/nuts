package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.format;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.text.NExecCmdFormat;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.util.NScorableContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultNExecCmdFormat extends DefaultFormatBase<NExecCmdFormat> implements NExecCmdFormat {
    private Predicate<ArgEntry> argumentFilter;

    private Function<ArgEntry, String> argumentReplacer;

    private Predicate<EnvEntry> envFilter;

    private Function<EnvEntry, String> envReplacer;
    private boolean redirectInput=true;
    private boolean redirectOutput=true;
    private boolean redirectError=true;
    private NExecCmd value;

    public DefaultNExecCmdFormat(NWorkspace workspace) {
        super("exec-command");
    }

    @Override
    public boolean isRedirectInput() {
        return redirectInput;
    }

    @Override
    public NExecCmdFormat setRedirectInput(boolean redirectInput) {
        this.redirectInput = redirectInput;
        return this;
    }

    @Override
    public boolean isRedirectOutput() {
        return redirectOutput;
    }

    @Override
    public NExecCmdFormat setRedirectOutput(boolean redirectOutput) {
        this.redirectOutput = redirectOutput;
        return this;
    }

    @Override
    public boolean isRedirectError() {
        return redirectError;
    }

    @Override
    public NExecCmdFormat setRedirectError(boolean redirectError) {
        this.redirectError = redirectError;
        return this;
    }

    @Override
    public NExecCmd getValue() {
        return value;
    }

    @Override
    public NExecCmdFormat setValue(NExecCmd value) {
        this.value = value;
        return this;
    }

    @Override
    public Predicate<ArgEntry> getArgumentFilter() {
        return argumentFilter;
    }

    @Override
    public NExecCmdFormat setArgumentFilter(Predicate<ArgEntry> filter) {
        this.argumentFilter = filter;
        return this;
    }

    @Override
    public Function<ArgEntry, String> getArgumentReplacer() {
        return argumentReplacer;
    }

    @Override
    public DefaultNExecCmdFormat setArgumentReplacer(Function<ArgEntry, String> argumentReplacer) {
        this.argumentReplacer = argumentReplacer;
        return this;
    }

    @Override
    public Predicate<EnvEntry> getEnvFilter() {
        return envFilter;
    }

    @Override
    public NExecCmdFormat setEnvFilter(Predicate<EnvEntry> filter) {
        this.envFilter = filter;
        return this;
    }

    @Override
    public Function<EnvEntry, String> getEnvReplacer() {
        return envReplacer;
    }

    @Override
    public NExecCmdFormat setEnvReplacer(Function<EnvEntry, String> envReplacer) {
        this.envReplacer = envReplacer;
        return this;
    }

    @Override
    public void print(NPrintStream out) {
        StringBuilder sb = new StringBuilder();
        NExecCmd ec = getValue();
        NExecOutput _out = ec.getOut();
        NExecOutput err = ec.getErr();
        NExecInput in = ec.getIn();
        Map<String, String> env = ec.getEnv();
        List<String> command = ec.getCommand();
        if (env != null) {
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                DefaultEnvEntry ee = new DefaultEnvEntry(k, v);
                if (getEnvFilter()!=null && !getEnvFilter().test(ee)) {
                    continue;
                }
                if(getEnvReplacer()!=null){
                    String v2 = getEnvReplacer().apply(ee);
                    if(v2==null){
                        continue;
                    }
                    v=v2;
                }
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(enforceDoubleQuote(k)).append("=").append(enforceDoubleQuote(v));
            }
        }
        for (int i = 0; i < command.size(); i++) {
            String s = command.get(i);
            if(s==null){
                s="";
            }
            DefaultArgEntry aa = new DefaultArgEntry(i, s);
            if (getArgumentFilter()!=null && !getArgumentFilter().test(aa)) {
                continue;
            }
            if(getArgumentReplacer()!=null){
                String y=getArgumentReplacer().apply(aa);
                if(y==null){
                    continue;
                }
                s=y;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(enforceDoubleQuote(s));
        }
        if (isRedirectError()) {

            if (_out != null ){
                if (isRedirectOutput()) {
                    sb.append(" > ").append("{stream}");
                }
                if (isRedirectError()) {
                    sb.append(" 2>&1");
                }
            }
            if (in != null) {
                if (isRedirectInput()) {
                    sb.append(" < ").append("{stream}");
                }
            }
        } else {
            if (_out != null) {
                if (isRedirectOutput()) {
                    sb.append(" > ").append("{stream}");
                }
            }
            if (err != null) {
                if (isRedirectError()) {
                    sb.append(" 2> ").append("{stream}");
                }
            }
            if (in != null) {
                if (isRedirectInput()) {
                    sb.append(" < ").append("{stream}");
                }
            }
        }
        out.print(sb.toString());
    }

    private static class DefaultEnvEntry implements EnvEntry{
        private String name;
        private String value;

        public DefaultEnvEntry(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
    private static class DefaultArgEntry implements ArgEntry{
        private int index;
        private String value;

        public DefaultArgEntry(int index, String value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    private static String enforceDoubleQuote(String s) {
        if (s.isEmpty() || s.contains(" ") || s.contains("\"")) {
            s = "\"" + s.replace("\"", "\\\"") + "\"";
        }
        return s;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}
