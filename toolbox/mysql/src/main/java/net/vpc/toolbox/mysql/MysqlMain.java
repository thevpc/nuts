package net.vpc.toolbox.mysql;

import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.toolbox.mysql.remote.RemoteMysql;
import net.vpc.toolbox.mysql.local.LocalMysql;

import java.util.Arrays;

public class MysqlMain extends NutsApplication {
    public static void main(String[] args) {
        new MysqlMain().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        String[] args=appContext.getArgs();
        if (args.length == 0) {
            throw new IllegalArgumentException("Expected --remote or --local");
        }
        if (args[0].equals("--remote") || args[0].equals("-c")) {
            RemoteMysql m = new RemoteMysql(appContext);
            return m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        }
        if (args[0].equals("--local") || args[0].equals("-s")) {
            LocalMysql m = new LocalMysql(appContext);
            return m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        } else {
            LocalMysql m = new LocalMysql(appContext);
            return m.runArgs(args);
        }
    }
}
