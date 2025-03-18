package net.thevpc.nuts;

import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.reserved.cmdline.NBootWorkspaceCmdLineParser;
import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.NApiUtilsRPI;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.NoSuchElementException;
import java.util.logging.Level;

public class NExceptionHandler {
    private Throwable ex;
    private int code;
    private NLog out;
    private boolean stacktrace;
    private boolean showMessage;
    private boolean gui;
    private NSession session;
    private NPrintStream sessionOut;
    private NMsg messageFormatted;
    private String messageString;
    private boolean built;

    public static NExceptionHandler of(Throwable ex) {
        return of(ex, null);
    }

    public static NExceptionHandler of(Throwable ex, NLog out) {
        if (ex == null) {
            return new NExceptionHandler();
        }
        NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
        if (session != null) {
            return session.callWith(() -> {
                NWorkspaceOptions bo = null;
                bo = session.getWorkspace().getBootOptions().builder().toWorkspaceOptions();
                return new NExceptionHandler()
                        .setEx(ex)
                        .setShowMessage(true)
                        .setOut(out)
                        .setStacktrace(NApiUtilsRPI.resolveShowStackTrace(bo))
                        .setGui(NApiUtilsRPI.resolveGui(bo))
                        .build();
            });
        } else {
            //load inherited
            String nutsArgs = NStringUtils.trim(
                    NStringUtils.trim(System.getProperty("nuts.boot.args"))
                            + " " + NStringUtils.trim(System.getProperty("nuts.args"))
            );
            try {
                NBootOptionsInfo options = new NBootOptionsInfo();
                NBootWorkspaceCmdLineParser.parseNutsArguments(NCmdLine.parseDefault(nutsArgs).get().toStringArray(), options);
                return new NExceptionHandler()
                        .setEx(ex)
                        .setShowMessage(true)
                        .setOut(out)
                        .setStacktrace(NApiUtilsRPI.resolveShowStackTrace(options))
                        .setGui(NApiUtilsRPI.resolveGui(options))
                        .build();
            } catch (Exception e) {
                //any, ignore...
                return new NExceptionHandler()
                        .setEx(ex)
                        .setShowMessage(true)
                        .setOut(out)
                        .setStacktrace(true)
                        .setGui(false)
                        .build();
            }
        }
    }

    public NExceptionHandler() {
    }

    public static RuntimeException ofSafeIllegalArgumentException(NMsg e) {
        if (!NWorkspace.get().isPresent()) {
            return new IllegalArgumentException(e.toString());
        }
        return new NIllegalArgumentException(e);
    }

    public static RuntimeException ofSafeIllegalArgumentException(NMsg message, Throwable ex) {
        if (!NWorkspace.get().isPresent()) {
            return new IllegalArgumentException(message.toString(), ex);
        }
        return new NIllegalArgumentException(message, ex);
    }

    public static RuntimeException ofSafeNoSuchElementException(NMsg message) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(message.toString());
        }
        return new NNoSuchElementException(message);
    }

    public static RuntimeException ofSafeUnexpectedException(NMsg message) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException(message.toString());
        }
        return new NNoSuchElementException(message);
    }

    public static RuntimeException ofSafeUnsupportedEnumException(Enum e) {
        if (!NWorkspace.get().isPresent()) {
            return new NoSuchElementException("unsupported enum value " + e);
        }
        return new NUnsupportedEnumException(e);
    }

    public static NOptional<NExceptionBase> resolveExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionBase.class, null);
    }

    public static NOptional<NExceptionWithExitCodeBase> resolveWithExitCodeExceptionBase(Throwable th) {
        return NReservedLangUtils.findThrowable(th, NExceptionWithExitCodeBase.class, null);
    }

    public static NOptional<Integer> resolveExitCode(Throwable th) {
        return resolveWithExitCodeExceptionBase(th).map(NExceptionWithExitCodeBase::getExitCode);
    }


    public NSession getSession() {
        return session;
    }

    public NExceptionHandler setSession(NSession session) {
        this.session = session;
        return this;
    }

    public boolean isShowMessage() {
        return showMessage;
    }

    public NExceptionHandler setShowMessage(boolean showMessage) {
        this.showMessage = showMessage;
        return this;
    }

    public Throwable getEx() {
        return ex;
    }

    public NExceptionHandler setEx(Throwable ex) {
        this.ex = ex;
        return this;
    }

    public int getCode() {
        return code;
    }

    public NExceptionHandler setCode(int code) {
        this.code = code;
        return this;
    }

    public NLog getOut() {
        return out;
    }

    public NExceptionHandler setOut(NLog out) {
        this.out = out;
        return this;
    }

    public boolean isStacktrace() {
        return stacktrace;
    }

    public NExceptionHandler setStacktrace(boolean stacktrace) {
        this.stacktrace = stacktrace;
        return this;
    }

    public boolean isGui() {
        return gui;
    }

    public NExceptionHandler setGui(boolean gui) {
        this.gui = gui;
        return this;
    }

    public NExceptionHandler build() {
        if (built) {
            return this;
        }
        built = true;
        if (ex == null) {
            setCode(0);
            return this;
        }
        int errorCode = resolveExitCode(ex).orElse(204);
        setCode(errorCode);
        if (errorCode == 0) {
            return this;
        }
        setSession(NSessionAwareExceptionBase.resolveSession(ex).orNull());
        messageFormatted = NSessionAwareExceptionBase.resolveSessionAwareExceptionBase(ex).map(NSessionAwareExceptionBase::getFormattedMessage)
                .orNull();
        messageString = NExceptionHandler.getErrorMessage(ex);
        if (getOut() == null) {
            if (getSession() != null) {
                try {
                    sessionOut = NIO.of().getSystemTerminal().getErr();
                    if (messageFormatted != null) {
                        messageFormatted = NMsg.ofNtf(NTextBuilder.of().append(messageFormatted, NTextStyle.error()).build());
                    } else {
                        messageFormatted = NMsg.ofStyledError(messageString);
                    }
                } catch (Exception ex2) {
                    NLogOp.of(NApplications.class).level(Level.FINE).error(ex2).log(
                            NMsg.ofPlain("unable to get system terminal")
                    );
                    //
                }
            } else {
                if (messageFormatted != null) {
                    // session is null but the exception is of NutsException type
                    // This is kind of odd, so will ignore message fm
                    messageFormatted = null;
                } else {
                    //setOut(NLog.NULL);
                }
            }
        } else {
            if (getSession() != null) {
//                fout = NutsPrintStream.of(out, NutsTerminalMode.FORMATTED, null, session);
                sessionOut = getSession().err();
            } else {
                sessionOut = null;
            }
        }
        return this;
    }

    public NExceptionHandler reThrow() {
        if (ex == null) {
            return this;
        }
        NOptional<NExceptionWithExitCodeBase> u = resolveWithExitCodeExceptionBase(ex);
        if (u.isPresent()) {
            NExceptionWithExitCodeBase o = u.get();
            if (o instanceof RuntimeException) {
                throw (RuntimeException) o;
            }
            if (session != null) {
                session.runWith(() -> {
                    throw new NException(NMsg.ofC("%s", o.toString(), o.getExitCode()));
                });
            }
            throw new NBootException(NBootMsg.ofC("%s", o.toString(), o.getExitCode()));
        }
        throw new NBootException(NBootMsg.ofC("%s", ex.toString(), 255));
    }


    public NExceptionHandler showError() {
        build();
        if (ex == null) {
            return this;
        }
        if (showMessage) {
            if (sessionOut != null) {
                session.runWith(()->{
                    if (session.getOutputFormat().orDefault() == NContentType.PLAIN) {
                        if (messageFormatted != null) {
                            sessionOut.println(messageFormatted);
                        } else {
                            sessionOut.println(messageString);
                        }
                        if (stacktrace) {
                            ex.printStackTrace(sessionOut.asPrintStream());
                        }
                        sessionOut.flush();
                    } else {
                        if (messageFormatted != null) {
                            session.eout().add(NElements.of().ofObjectBuilder()
                                    .set("app-id", NStringUtils.toStringOrEmpty(NApp.of().getId().get()))
                                    .set("error", NText.of(messageFormatted).filteredText())
                                    .build()
                            );
                            if (stacktrace) {
                                session.eout().add(NElements.of().ofObjectBuilder().set("errorTrace",
                                        NElements.of().ofArrayBuilder().addAll(NStringUtils.stacktraceArray(ex)).build()
                                ).build());
                            }
                            NArrayElementBuilder e = session.eout();
                            if (e.size() > 0) {
                                sessionOut.println(e.build());
                                e.clear();
                            }
                            sessionOut.flush();
                        } else {
                            session.eout().add(NElements.of().ofObjectBuilder()
                                    .set("app-id", NStringUtils.toStringOrEmpty(NApp.of().getId().get()))
                                    .set("error", messageString)
                                    .build());
                            if (stacktrace) {
                                session.eout().add(NElements.of().ofObjectBuilder().set("errorTrace",
                                        NElements.of().ofArrayBuilder().addAll(NStringUtils.stacktraceArray(ex)).build()
                                ).build());
                            }
                            NArrayElementBuilder e = session.eout();
                            if (e.size() > 0) {
                                sessionOut.println(e.build());
                                e.clear();
                            }
                            sessionOut.flush();
                        }
                        sessionOut.flush();
                    }
                });
            } else {
                if (out != null) {
                    NLogOp logOp = out.with().level(Level.OFF).verb(NLogVerb.FAIL);
                    if (messageFormatted != null) {
                        logOp.log(messageFormatted);
                    } else {
                        logOp.log(NMsg.ofPlain(messageString));
                    }
                    if (stacktrace) {
                        logOp.log(NMsg.ofPlain("---------------"));
                        logOp.log(NMsg.ofPlain(">  STACKTRACE :"));
                        logOp.log(NMsg.ofPlain("---------------"));
                        logOp.log(NMsg.ofPlain(
                                NStringUtils.stacktrace(ex)
                        ));
                    }
                }else{
                    if (messageFormatted != null) {
                        System.err.println(messageFormatted);
                    } else {
                        System.err.println(NMsg.ofPlain(messageString));
                    }
                    if (stacktrace) {
                        System.err.println(NMsg.ofPlain("---------------"));
                        System.err.println(NMsg.ofPlain(">  STACKTRACE :"));
                        System.err.println(NMsg.ofPlain("---------------"));
                        System.err.println(NMsg.ofPlain(
                                NStringUtils.stacktrace(ex)
                        ));
                    }
                }
            }
        }
        if (gui) {
            StringBuilder sb = new StringBuilder();
            if (messageFormatted != null) {
                if (session != null) {
                    sb.append(NText.of(messageFormatted).filteredText());
                } else {
                    sb.append(messageFormatted);
                }
            } else {
                sb.append(messageString);
            }
            if (stacktrace) {
                if (sb.length() > 0) {
                    sb.append("\n");
                    sb.append(NStringUtils.stacktrace(ex));
                }
            }
            if (session != null) {
                //TODO should we delegate to the workspace implementation?
                NReservedLangUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            } else {
                NReservedLangUtils.showMessage(NMsg.ofPlain(sb.toString()).toString(), "Nuts Package Manager - Error", out);
            }
        }
        return this;
    }

    public NExceptionHandler propagate() {
        return showError().reThrow();
    }

    public NExceptionHandler handle() {
        return showError();
    }

    public void handleFatal() {
        System.exit(showError().getCode());
    }

    public static String getErrorMessage(Throwable ex) {
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        return m;
    }
}
