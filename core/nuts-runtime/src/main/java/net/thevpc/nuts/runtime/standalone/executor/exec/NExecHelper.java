package net.thevpc.nuts.runtime.standalone.executor.exec;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.concurrent.NConcurrent;


import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.text.NExecWriter;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NNameFormat;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class NExecHelper extends AbstractSyncIProcessExecHelper {

    NExec pb;
    NPrintStream out;

    public NExecHelper(NExec pb, NPrintStream out) {
        super();
        this.pb = pb;
        this.out = out;
    }

    public static NExecHelper ofArgs(String[] args, Map<String, String> env, Path directory,
                                     boolean showCommand, boolean failFast, NDuration sleep,
                                     NExecInput in,
                                     NExecOutput out,
                                     NExecOutput err,
                                     NRunAs runAs) {
        NExec pb = NExec.of();
        NCmdLineUtils.OptionsAndArgs optionsAndArgs = NCmdLineUtils.parseOptionsFirst(args);
        pb.command(optionsAndArgs.getArgs())
                .addExecutorOptions(optionsAndArgs.getOptions())
                .runAs(runAs)
                .env(env)
                .directory(directory == null ? null : NPath.of(directory))
                .sleepDuration(sleep)
                .failFast(failFast);
        pb.in(CoreIOUtils.validateIn(in));
        pb.out(CoreIOUtils.validateOut(out));
        pb.err(CoreIOUtils.validateErr(err));

        NLog _LL = NLog.of(NWorkspaceUtils.class);
        NCmdLine commandOut = NCmdLine.of(pb.command());
        if (_LL.isLoggable(Level.FINEST)) {
            _LL.log(
                    NMsg.ofC("[exec] %s",
                            commandOut
                    ).asFinest().withIntent(NMsgIntent.START));
        }
        NSession session = NSession.of();
        if (showCommand || NWorkspace.of().getCustomBootOption("---show-command")
                .flatMap(NLiteral::asBoolean)
                .orElse(false)) {

            if (NOut.terminalMode() == NTerminalMode.FORMATTED) {
                NOut.print(NMsg.ofC("%s ", NText.ofStyled("[exec]", NTextStyle.primary4())));
                NOut.println(NText.of(commandOut));
            } else {
                NOut.print("exec ");
                NOut.println(commandOut);
            }
        }
        return new NExecHelper(pb, session.out());
    }

    public static Map<String, String> asConstVarNames(Map<String, String> env) {
        return env.entrySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(
                NNameFormat.CONST_NAME.format(x.getKey())
                , x.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, String> defVarMap(NDefinition def, Map<String, String> env) {
        Map<String, String> mm = new ConcurrentHashMap<>();
        if (env != null) {
            mm.putAll(env);
        }
        if (def != null) {
            mm.put("nutsIdContentPath", def.content().map(NPath::toString).orNull());
            mm.put("nutsIdBinPath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.BIN)).toString());
            mm.put("nutsIdConfPath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.CONF)).toString());
            mm.put("nutsIdVarPath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.VAR)).toString());
            mm.put("nutsIdLogPath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.LOG)).toString());
            mm.put("nutsIdTempPath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.TEMP)).toString());
            mm.put("nutsIdCachePath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.CACHE)).toString());
            mm.put("nutsIdLibPath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.LIB)).toString());
            mm.put("nutsRunLibPath", NPath.of(NStoreKey.of(def.id()).type(NStoreType.RUN)).toString());
        }
        return mm;
    }

    public static Function<String, String> defVarMapper(NDefinition def, Map<String, String> env, Function<String, String> extra) {
        return new Function<String, String>() {
            @Override
            public String apply(String str) {
                if (str == null) {
                    return null;
                } else if (str.indexOf('$') >= 0) {
                    return NMsg.ofV(str, n -> {
                        if (extra != null) {
                            String v = extra.apply(n);
                            if (v != null) {
                                return v;
                            }
                        }
                        String v = env.get(n);
                        if (v != null) {
                            return v;
                        }
                        if (def != null) {
                            switch (n) {
                                case "nutsIdContentPath":
                                    return def.content().map(NPath::toString).orNull();
                                case "nutsIdBinPath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.BIN)).toString();
                                case "nutsIdConfPath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.CONF)).toString();
                                case "nutsIdVarPath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.VAR)).toString();
                                case "nutsIdLogPath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.LOG)).toString();
                                case "nutsIdTempPath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.TEMP)).toString();
                                case "nutsIdCachePath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.CACHE)).toString();
                                case "nutsIdLibPath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.LIB)).toString();
                                case "nutsRunLibPath":
                                    return NPath.of(NStoreKey.of(def.id()).type(NStoreType.RUN)).toString();
                            }
                        }
                        return null;
                    }).toString();
                } else {
                    return str;
                }
            }
        };
    }

    public static NExecHelper ofDefinition(NDefinition def,
                                           String[] args, Map<String, String> env, String directory, boolean showCommand, boolean failFast, NDuration sleep,
                                           NExecInput in, NExecOutput out, NExecOutput err,
                                           NRunAs runAs
    ) throws NExecutionException {
        Path wsLocation = NWorkspace.of().workspaceLocation().toPath().get();
        Path pdirectory = null;
        if (NBlankable.isBlank(directory)) {
            pdirectory = wsLocation;
        } else {
            pdirectory = wsLocation.resolve(directory);
        }
        Function<String, String> mm = defVarMapper(def, env,null);
        Map<String, String> env2 = new HashMap<>(env);
        env2.putAll(asConstVarNames(defVarMap(def, null)));
        String[] args2 = Arrays.stream(args).map(x -> (x == null) ? "" : mm.apply(x)).toArray(String[]::new);
        return ofArgs(args2, env2, pdirectory, showCommand, failFast,
                sleep,
                in, out, err, runAs
        );
    }


    public int exec() {
        NSession session = NSession.of();
        if (session.isDry()) {
            if (out.terminalMode() == NTerminalMode.FORMATTED) {
                out.print("[dry] ==[exec]== ");
                out.println(NExecWriter.of().format(pb));
            } else {
                out.print("[dry] exec ");
                out.println(NExecWriter.of().format(pb));
            }
            return NExecutionException.SUCCESS;
        }
        if (out != null) {
            out.flush();
        }
        return pb.exitCode();
    }

    public Future<Integer> execAsync() {
        if (out != null) {
            out.run(NTerminalCmd.MOVE_LINE_START);
        }
        return NConcurrent.of().executorService().submit(() -> pb.exitCode());
    }
}
