/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsWorkspaceOptions;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * @author thevpc
 * @app.category Internal
 */
public class PrivateNutsLog {

    /**
     * Universal Data and time format "yyyy-MM-dd HH:mm:ss.SSS"
     */
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    private NutsWorkspaceOptions options;
    private final NutsBootTerminal bootTerminal;
    private Scanner inScanner;

    public PrivateNutsLog(NutsBootTerminal bootTerminal) {
        InputStream in = (bootTerminal == null || bootTerminal.getIn() == null) ? System.in : bootTerminal.getIn();
        PrintStream out = (bootTerminal == null || bootTerminal.getOut() == null) ? System.out : bootTerminal.getOut();
        PrintStream err = (bootTerminal == null || bootTerminal.getErr() == null) ? out : bootTerminal.getErr();
        this.bootTerminal = new NutsBootTerminal(in, out, err);
    }

    public void log(Level lvl, NutsLogVerb logVerb, NutsMessage message) {
        if (isLoggable(lvl)) {
            doLog(lvl, logVerb, message == null ? "" : message.toString());
        }
    }

    public void log(Level lvl, NutsMessage message, Throwable err) {
        if (isLoggable(lvl)) {
            doLog(lvl, NutsLogVerb.FAIL, message == null ? "" : message.toString());
            err.printStackTrace(System.err);
        }
    }

    private void doLog(Level lvl, NutsLogVerb logVerb, String s) {
        errln("%s %-7s %-7s : %s", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, logVerb, s);
    }

    public boolean isLoggable(Level lvl) {
        if (lvl.intValue() == Level.OFF.intValue()) {
            //this is a special case where we do log in all cases!
            return true;
        }
        if (options == null || options.getLogConfig() == null) {
            return false;
        }
        return lvl.intValue() >= options.getLogConfig().getLogTermLevel().intValue();
    }

    public void setOptions(NutsWorkspaceOptions options) {
        this.options = options;
    }


    void errln(String msg, Object... p) {
        this.bootTerminal.getErr().printf(msg, p);
        this.bootTerminal.getErr().printf("%n");
        this.bootTerminal.getErr().flush();
    }

    public PrintStream err() {
        return bootTerminal.getErr();
    }

    void err(String msg, Object... p) {
        this.bootTerminal.getErr().printf(msg, p);
        this.bootTerminal.getErr().flush();
    }

    void outln(String msg, Object... p) {
        this.bootTerminal.getOut().printf(msg, p);
        this.bootTerminal.getOut().printf("%n");
        this.bootTerminal.getOut().flush();
    }

    void errln(Throwable exception) {
        exception.printStackTrace(this.bootTerminal.getErr());
    }

    public String readLine() {
        if (inScanner == null) {
            inScanner = new Scanner(System.in);
        }
        return inScanner.nextLine();
    }
}
