package net.vpc.toolbox.mysql;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.mysql.remote.RemoteMysql;
import net.vpc.toolbox.mysql.local.LocalMysql;

import java.util.Arrays;

public class MysqlMain extends NutsApplication {

    public static void main(String[] args) {
        new MysqlMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArgs();
        if (args.length == 0) {
            throw new NutsExecutionException("Expected --remote or --local", 2);
        }
        if (args[0].equals("--remote") || args[0].equals("-c")) {
            RemoteMysql m = new RemoteMysql(appContext);
            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        } else if (args[0].equals("--local") || args[0].equals("-s")) {
            LocalMysql m = new LocalMysql(appContext);
            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        } else {
            LocalMysql m = new LocalMysql(appContext);
            m.runArgs(args);
        }
    }
}
