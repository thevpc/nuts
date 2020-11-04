package net.thevpc.nuts;

/**
 * Modes Application can run with
 *
 * @since 0.5.5
 * @category Application
 */
public enum NutsApplicationMode {
    /**
     * Default application execution Mode. This mode is considered if the
     * --nuts-exec-mode=... is not present (or is not the very first argument).
     */
    RUN,
    /**
     * application execution Mode in auto-complete mode in which case
     * application MUST accept FIRST argument in the form of
     * "--nuts-exec-mode=auto-complete &lt;WORD-INDEX&gt;" where &lt;WORD-INDEX&gt; is
     * an optional argument to auto-complete mode. It is important to notice
     * that "--nuts-exec-mode=auto-complete &lt;WORD-INDEX&gt;" is a SINGLE
     * argument, so spaces must be escaped.
     */
    AUTO_COMPLETE,
    /**
     * application execution Mode in install mode in which case application MUST
     * accept FIRST argument in the form of "--nuts-exec-mode=install &lt;ARG&gt;
     * ..." where &lt;ARG&gt; arg an optional arguments to install mode. It is
     * important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is a
     * SINGLE argument, so spaces must be escaped.
     */
    INSTALL,
    /**
     * application execution Mode in uninstall mode in which case application
     * MUST accept FIRST argument in the form of "--nuts-exec-mode=uninstall
     * &lt;ARG&gt; ..." where &lt;ARG&gt; arg an optional arguments to uninstall mode.
     * It is important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is
     * a SINGLE argument, so spaces must be escaped.
     */
    UNINSTALL,
    /**
     * application execution Mode in update mode in which case application MUST
     * accept FIRST argument in the form of "--nuts-exec-mode=update &lt;ARG&gt;
     * ..." where &lt;ARG&gt; arg an optional arguments to update mode. It is
     * important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is a
     * SINGLE argument, so spaces must be escaped.
     */
    UPDATE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsApplicationMode() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
