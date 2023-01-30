/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.connect;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.io.util.NonBlockingInputStreamAdapter;
import net.thevpc.nuts.runtime.standalone.executor.system.PipeRunnable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NAssert;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author thevpc
 */
public class NSettingsConnectSubCommand extends AbstractNSettingsSubCommand {

    public static final int DEFAULT_ADMIN_SERVER_PORT = 8898;

    @Override
    public boolean exec(NCommandLine commandLine, Boolean autoSave, NSession session) {
        String cmd0 = commandLine.toString();
        if (commandLine.next("connect").isPresent()) {
            char[] password = null;
            String server = null;
            NArg a;
            while (commandLine.hasNext()) {
                if ((a = commandLine.nextEntry("--password").orNull()) != null) {
                    password = a.getValue().asString().orElse("").toCharArray();
                } else if (commandLine.isNextOption()) {
                    session.configureLast(commandLine);
                } else {
                    server = commandLine.nextNonOption(NArgName.of("ServerAddress",session)).flatMap(NLiteral::asString).get(session);
                    commandLine.setCommandName("settings connect").throwUnexpectedArgument();
                }
            }
            if (!commandLine.isExecMode()) {
                return true;
            }
            String login = null;
            int port = -1;
            NAssert.requireNonBlank(server, "server", session);
            if (server.contains("@")) {
                login = server.substring(0, server.indexOf("@"));
                server = server.substring(server.indexOf("@") + 1);
            }
            if (server.contains(":")) {
                port =  NLiteral.of(server.substring(server.indexOf(":") + 1)).asInt().orElse(-1);
                server = server.substring(0, server.indexOf(":"));
            }
            if (!NBlankable.isBlank(login) && NBlankable.isBlank(password)) {
                password = session.getTerminal().readPassword(NMsg.ofPlain("Password:"));
            }
            Socket socket = null;
            try {
                try {
                    int validPort = port <= 0 ? DEFAULT_ADMIN_SERVER_PORT : port;
                    socket = new Socket(InetAddress.getByName(server), validPort);
                    PipeRunnable rr = NSysExecUtils.pipe("pipe-out-socket-" + server + ":" + validPort,
                            cmd0, "connect-socket",
                            new NonBlockingInputStreamAdapter("pipe-out-socket-" + server + ":" + validPort, socket.getInputStream()), session.out().asPrintStream(), session);
                    NScheduler.of(session).executorService().submit(rr);
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    if (!NBlankable.isBlank(login)) {
                        out.printf("connect ==%s %s== %n", login, new String(password));
                    }
                    while (true) {
                        String line = session.getTerminal().readLine(NMsg.ofPlain(""));
                        if (line == null) {
                            break;
                        }
                        if (line.trim().length() > 0) {
                            if (line.trim().equals("quit") || line.trim().equals("exit")) {
                                break;
                            }
                            out.printf("%s%n", line);
                        }
                    }
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            } catch (Exception ex) {
                throw new NExecutionException(session, NMsg.ofPlain("settings connect failed"), ex, 2);
            }
            return true;
        }
        return false;
    }
}
