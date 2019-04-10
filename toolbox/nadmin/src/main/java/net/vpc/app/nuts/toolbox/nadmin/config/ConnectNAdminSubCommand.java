/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.NonBlockingInputStreamAdapter;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author vpc
 */
public class ConnectNAdminSubCommand extends AbstractNAdminSubCommand {
    public static final int DEFAULT_ADMIN_SERVER_PORT = 8898;
    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.readAll("connect")) {
            String password = null;
            String server = null;
            Argument a;
            while (cmdLine.hasNext()) {
                if (context.configure(cmdLine)) {
                    //
                } else if (cmdLine.readAllOnce("--password")) {
                    password = cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    server = cmdLine.readRequiredNonOption(new DefaultNonOption("ServerAddress")).getStringOrError();
                    cmdLine.unexpectedArgument("nadmin connect");
                }
            }
            if (!cmdLine.isExecMode()) {
                return true;
            }
            String login = null;
            int port = -1;
            if (server == null) {
                throw new NutsIllegalArgumentException("Missing address");
            }
            if (server.contains("@")) {
                login = server.substring(0, server.indexOf("@"));
                server = server.substring(server.indexOf("@") + 1);
            }
            if (server.contains(":")) {
                port = Integer.parseInt(server.substring(server.indexOf(":") + 1));
                server = server.substring(0, server.indexOf(":"));
            }
            if (!StringUtils.isEmpty(login) && StringUtils.isEmpty(password)) {
                password = context.getTerminal().readPassword("Password:");
            }
            Socket socket = null;
            try {
                try {
                    int validPort = port <= 0 ? DEFAULT_ADMIN_SERVER_PORT : port;
                    socket = new Socket(InetAddress.getByName(server), validPort);
                    IOUtils.pipe("pipe-out-socket-" + server + ":" + validPort, new NonBlockingInputStreamAdapter("pipe-out-socket-" + server + ":" + validPort, socket.getInputStream()), context.out());
                    PrintStream out = new PrintStream(socket.getOutputStream());
                    if (!StringUtils.isEmpty(login)) {
                        out.printf("connect ==%s %s== \n", login, password);
                    }
                    while (true) {
                        String line = context.getTerminal().readLine("");
                        if (line == null) {
                            break;
                        }
                        if (line.trim().length() > 0) {
                            if (line.trim().equals("quit") || line.trim().equals("exit")) {
                                break;
                            }
                            out.printf("%s\n", line);
                        }
                    }
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            } catch (Exception ex) {
                throw new NutsExecutionException(ex, 2);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
