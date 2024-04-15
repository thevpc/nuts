/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.connect;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.concurrent.NScheduler;
import net.thevpc.nuts.io.DefaultNContentMetadata;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NInputSourceBuilder;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.executor.system.PipeRunnable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author thevpc
 */
public class NSettingsConnectSubCommand extends AbstractNSettingsSubCommand {

    public static final int DEFAULT_ADMIN_SERVER_PORT = 8898;

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave, NSession session) {
        String cmd0 = cmdLine.toString();
        if (cmdLine.next("connect").isPresent()) {
            char[] password = null;
            String server = null;
            NArg a;
            while (cmdLine.hasNext()) {
                if ((a = cmdLine.nextEntry("--password").orNull()) != null) {
                    password = a.getValue().asString().orElse("").toCharArray();
                } else if (cmdLine.isNextOption()) {
                    session.configureLast(cmdLine);
                } else {
                    server = cmdLine.nextNonOption(NArgName.of("ServerAddress", session)).flatMap(NLiteral::asString).get(session);
                    cmdLine.setCommandName("settings connect").throwUnexpectedArgument();
                }
            }
            if (!cmdLine.isExecMode()) {
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
                port = NLiteral.of(server.substring(server.indexOf(":") + 1)).asInt().orElse(-1);
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
                            NInputSourceBuilder.of(socket.getInputStream(), session)
                                    .setMetadata(new DefaultNContentMetadata().setMessage(NMsg.ofC("pipe-out-socket-%s:%s", server, validPort)))
                                    .createNonBlockingInputStream(), session.out().asPrintStream(), session);
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
                throw new NExecutionException(session, NMsg.ofPlain("settings connect failed"), ex, NExecutionException.ERROR_2);
            }
            return true;
        }
        return false;
    }
}
