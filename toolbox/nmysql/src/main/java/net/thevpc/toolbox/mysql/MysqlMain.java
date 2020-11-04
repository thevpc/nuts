package net.thevpc.toolbox.mysql;

import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.NutsApplication;
import net.thevpc.toolbox.mysql.local.LocalMysql;
import net.thevpc.toolbox.mysql.remote.RemoteMysql;

import java.util.Arrays;
import net.thevpc.nuts.NutsApplicationContext;

public class MysqlMain extends NutsApplication {

    public static void main(String[] args) {
        new MysqlMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        context.getWorkspace().info().print();
        String[] args = context.getArguments();
        if (args.length == 0) {
            throw new NutsExecutionException(context.getWorkspace(), "Expected --remote or --local", 2);
        }
        if (args[0].equals("--remote") || args[0].equals("-c")) {
            RemoteMysql m = new RemoteMysql(context);
            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        } else if (args[0].equals("--local") || args[0].equals("-s")) {
            LocalMysql m = new LocalMysql(context);
            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        } else {
            LocalMysql m = new LocalMysql(context);
            m.runArgs(args);
        }
    }
}
