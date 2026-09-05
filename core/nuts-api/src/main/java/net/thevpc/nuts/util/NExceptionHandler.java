package net.thevpc.nuts.util;

import net.thevpc.nuts.app.NApplication;
import net.thevpc.nuts.app.NApplicationHandler;
import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.core.NExceptionWithExitCodeBase;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.boot.internal.cmdline.NBootWorkspaceCmdLineParser;
import net.thevpc.nuts.boot.internal.util.NBootMsg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.internal.NApiUtilsRPI;
import net.thevpc.nuts.internal.NReservedLangUtils;

import java.util.logging.Level;

/**
 * NExceptionHandler class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NExceptionHandler {
    private Throwable throwable;
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

    /**
     * Creates a new instance of of.
     *
     * @param ex ex
     * @return of result
     */
    public static NExceptionHandler of(Throwable ex) {
        /**
         * Creates a new instance of of.
         *
         * @param ex ex
         * @param null null
         * @return of result
         */
        return of(ex, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param ex ex
     * @param out out
     * @return of result
     */
    public static NExceptionHandler of(Throwable ex, NLog out) {
        if (ex == null) {
            return new NExceptionHandler();
        }
        NSession session = NSessionAwareExceptionBase.resolveSession(ex).orNull();
        if (session != null) {
            return session.callWith(() -> {
                NWorkspaceOptions bo = null;
                bo = session.workspace().bootOptions().toWorkspaceOptions();
                return new NExceptionHandler()
                        .throwable(ex)
                        .showMessage(true)
                        .out(out)
                        .stacktrace(NApiUtilsRPI.resolveShowStackTrace(bo))
                        .gui(NApiUtilsRPI.resolveGui(bo))
                        .build();
            });
        } else {
            //load inherited
            String nutsArgs = NStringUtils.strip(
                    NStringUtils.strip(System.getenv(NConstants.Env.NUTS_BOOT_ARGS))
                            + " " + NStringUtils.strip(System.getProperty(NConstants.SysProps.NUTS_ARGS))
            );
            try {
                NBootOptionsInfo options = new NBootOptionsInfo();
                NBootWorkspaceCmdLineParser.denullProperties(options);
                NBootWorkspaceCmdLineParser.parseNutsArguments(NCmdLine.parseDefault(nutsArgs).get().toStringArray(), options);
                return new NExceptionHandler()
                        .throwable(ex)
                        .showMessage(true)
                        .out(out)
                        .stacktrace(NApiUtilsRPI.resolveShowStackTrace(options))
                        .gui(NApiUtilsRPI.resolveGui(options))
                        .build();
            } catch (Exception e) {
                //any, ignore...
                return new NExceptionHandler()
                        .throwable(ex)
                        .showMessage(true)
                        .out(out)
                        .stacktrace(true)
                        .gui(false)
                        .build();
            }
        }
    }

    /**
     * N exception handler.
     *
     * @return n exception handler result
     */
    public NExceptionHandler() {
    }


    /**
     * Session.
     *
     * @return session result
     */
    public NSession session() {
        return session;
    }

    /**
     * Session.
     *
     * @param session session
     * @return session result
     */
    public NExceptionHandler session(NSession session) {
        this.session = session;
        return this;
    }

    /**
     * Checks if is show message.
     *
     * @return is show message result
     */
    public boolean isShowMessage() {
        return showMessage;
    }

    /**
     * Show message.
     *
     * @param showMessage show message
     * @return show message result
     */
    public NExceptionHandler showMessage(boolean showMessage) {
        this.showMessage = showMessage;
        return this;
    }

    /**
     * Throwable.
     *
     * @return throwable result
     */
    public Throwable throwable() {
        return throwable;
    }

    /**
     * Throwable.
     *
     * @param ex ex
     * @return throwable result
     */
    public NExceptionHandler throwable(Throwable ex) {
        this.throwable = ex;
        return this;
    }

    /**
     * Code.
     *
     * @return code result
     */
    public int code() {
        return code;
    }

    /**
     * Code.
     *
     * @param code code
     * @return code result
     */
    public NExceptionHandler code(int code) {
        this.code = code;
        return this;
    }

    /**
     * Out.
     *
     * @return out result
     */
    public NLog out() {
        return out;
    }

    /**
     * Out.
     *
     * @param out out
     * @return out result
     */
    public NExceptionHandler out(NLog out) {
        this.out = out;
        return this;
    }

    /**
     * Checks if is stacktrace.
     *
     * @return is stacktrace result
     */
    public boolean isStacktrace() {
        return stacktrace;
    }

    /**
     * Stacktrace.
     *
     * @param stacktrace stacktrace
     * @return stacktrace result
     */
    public NExceptionHandler stacktrace(boolean stacktrace) {
        this.stacktrace = stacktrace;
        return this;
    }

    /**
     * Checks if is gui.
     *
     * @return is gui result
     */
    public boolean isGui() {
        return gui;
    }

    /**
     * Gui.
     *
     * @param gui gui
     * @return gui result
     */
    public NExceptionHandler gui(boolean gui) {
        this.gui = gui;
        return this;
    }

    /**
     * Build.
     *
     * @return build result
     */
    public NExceptionHandler build() {
        if (built) {
            return this;
        }
        built = true;
        if (throwable == null) {
          /**
           * Code.
           *
           * @param 0 0
           */
            code(0);
            return this;
        }
        int errorCode = NException.resolveExitCode(throwable).orElse(204);
      /**
       * Code.
       *
       * @param errorCode error code
       */
        code(errorCode);
        if (errorCode == 0) {
            return this;
        }
      /**
       * Session.
       *
       * @param NSessionAwareExceptionBase.resolveSession(throwable).orNull() n session aware exception base.resolve session(throwable).or null()
       */
        session(NSessionAwareExceptionBase.resolveSession(throwable).orNull());
        messageFormatted = NSessionAwareExceptionBase.resolveSessionAwareExceptionBase(throwable).map(NSessionAwareExceptionBase::formattedMessage)
                .orNull();
        messageString = NException.getErrorMessage(throwable);
        if (out() == null) {
            if (session() != null) {
                try {
                    sessionOut = NIO.of().systemTerminal().err();
                    if (messageFormatted != null) {
                        messageFormatted = NMsg.ofNtf(NTextBuilder.of().append(messageFormatted, NTextStyle.error()).build());
                    } else {
                        messageFormatted = NMsg.ofStyledError(messageString);
                    }
                } catch (Exception ex2) {
                    NLog.of(NApplicationHandler.class).log(
                            NMsg.ofP("unable to get system terminal").asFine(ex2)
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
            if (session() != null) {
//                fout = NutsPrintStream.of(out, NutsTerminalMode.FORMATTED, null, session);
                sessionOut = session().err();
            } else {
                sessionOut = null;
            }
        }
        return this;
    }

    /**
     * Re throw.
     *
     * @return re throw result
     */
    public NExceptionHandler reThrow() {
        if (throwable == null) {
            return this;
        }
        NOptional<NExceptionWithExitCodeBase> u = NException.resolveWithExitCodeExceptionBase(throwable);
        if (u.isPresent()) {
            NExceptionWithExitCodeBase o = u.get();
            if (o instanceof RuntimeException) {
                throw (RuntimeException) o;
            }
            if (session != null) {
                session.runWith(() -> {
                    /**
                     * N exception.
                     *
                     * @param o.exitCode()) o.exit code())
                     * @return n exception result
                     */
                    throw new NException(NMsg.ofC("%s", o.toString(), o.exitCode()));
                });
            }
            /**
             * N boot exception.
             *
             * @param o.exitCode()) o.exit code())
             * @return n boot exception result
             */
            throw new NBootException(NBootMsg.ofC("%s", o.toString(), o.exitCode()));
        }
        /**
         * N boot exception.
         *
         * @param 255) 255)
         * @return n boot exception result
         */
        throw new NBootException(NBootMsg.ofC("%s", throwable.toString(), 255));
    }


    /**
     * Show error.
     *
     * @return show error result
     */
    public NExceptionHandler showError() {
      /**
       * Build.
       */
        build();
        if (throwable == null) {
            return this;
        }
        if (showMessage) {
            if (sessionOut != null) {
                session.runWith(()->{
                    if (session.outputFormat().orDefault() == NContentType.PLAIN) {
                        if (messageFormatted != null) {
                            sessionOut.println(messageFormatted);
                        } else {
                            sessionOut.println(messageString);
                        }
                        if (stacktrace) {
                            throwable.printStackTrace(sessionOut.asPrintStream());
                        }
                        sessionOut.flush();
                    } else {
                        if (messageFormatted != null) {
                            session.eout().add(NElement.ofObjectBuilder()
                                    .set("app-id", NStringUtils.toStringOrEmpty(NApplication.of().id().get()))
                                    .set("error", NText.of(messageFormatted).filteredText())
                                    .build()
                            );
                            if (stacktrace) {
                                session.eout().add(NElement.ofObjectBuilder().set("errorTrace",
                                        NElement.ofArrayBuilder().addAll(NStringUtils.stacktraceArray(throwable)).build()
                                ).build());
                            }
                            NArrayElementBuilder e = session.eout();
                            if (e.size() > 0) {
                                sessionOut.println(e.build());
                                e.clear();
                            }
                            sessionOut.flush();
                        } else {
                            session.eout().add(NElement.ofObjectBuilder()
                                    .set("app-id", NStringUtils.toStringOrEmpty(NApplication.of().id().get()))
                                    .set("error", messageString)
                                    .build());
                            if (stacktrace) {
                                session.eout().add(NElement.ofObjectBuilder().set("errorTrace",
                                        NElement.ofArrayBuilder().addAll(NStringUtils.stacktraceArray(throwable)).build()
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
                    NMsgBuilder msgBuilder = NMsgBuilder.of().withLevel(Level.OFF).withIntent(NMsgIntent.FAIL);
                    if (messageFormatted != null) {
                        out.log(msgBuilder.withMsg(messageFormatted));
                    } else {
                        out.log(msgBuilder.withMsg(NMsg.ofP(messageString)));
                    }
                    if (stacktrace) {
                        out.log(msgBuilder.withMsgPlain("---------------"));
                        out.log(msgBuilder.withMsgPlain(">  STACKTRACE :"));
                        out.log(msgBuilder.withMsgPlain("---------------"));
                        out.log(msgBuilder.withMsgPlain(
                                NStringUtils.stacktrace(throwable)
                        ));
                    }
                }else{
                    if (messageFormatted != null) {
                        System.err.println(messageFormatted);
                    } else {
                        System.err.println(NMsg.ofP(messageString));
                    }
                    if (stacktrace) {
                        System.err.println(NMsg.ofP("---------------"));
                        System.err.println(NMsg.ofP(">  STACKTRACE :"));
                        System.err.println(NMsg.ofP("---------------"));
                        System.err.println(NMsg.ofP(
                                NStringUtils.stacktrace(throwable)
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
                    sb.append(NStringUtils.stacktrace(throwable));
                }
            }
            if (session != null) {
                //TODO should we delegate to the workspace implementation?
                NReservedLangUtils.showMessage(NMsg.ofP(sb.toString()).toString(), NI18n.of("Nuts Package Manager - Error"), out);
            } else {
                NReservedLangUtils.showMessage(NMsg.ofP(sb.toString()).toString(), NI18n.of("Nuts Package Manager - Error"), out);
            }
        }
        return this;
    }

    /**
     * Propagate.
     *
     * @return propagate result
     */
    public NExceptionHandler propagate() {
        /**
         * Show error.
         *
         * @param ).reThrow( ).re throw(
         * @return show error result
         */
        return showError().reThrow();
    }

    /**
     * Handle.
     *
     * @return handle result
     */
    public NExceptionHandler handle() {
        /**
         * Show error.
         *
         * @return show error result
         */
        return showError();
    }

    /**
     * Handle fatal.
     */
    public void handleFatal() {
        System.exit(showError().code());
    }

}
