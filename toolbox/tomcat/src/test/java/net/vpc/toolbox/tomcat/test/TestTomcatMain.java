package net.vpc.toolbox.tomcat.test;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.toolbox.tomcat.server.TomcatServerConfigService;
import net.vpc.toolbox.tomcat.server.TomcatServer;

public class TestTomcatMain {
    public static void main(String[] args) {
        new TestTomcatMain().testLinux();
    }

    //    @Test
    public void testLinux() {
        String[] args = new String[]{"--nuts-args", "--verbose", "--nuts-no-more-args"};
        NutsWorkspace ws = Nuts.openWorkspace(args);
        args = Nuts.skipNutsArgs(args);
        TomcatServer m = new TomcatServer(ws);
        m.removeAllConfigs();
        TomcatServerConfigService s = m.loadOrCreateTomcatConfig("intertek");
        s.getConfig().setCatalinaBase("intertek");
        s.getConfig().setCatalinaVersion("8.5");
        s.getAppOrCreate("intertek")
                .getConfig().setDeployName("ROOT");
//        s.getConfig().setJavaHome("/usr/java/jdk1.8.0_171-amd64");
        s.saveConfig();

        s.getApp("intertek")
                .install("3.2", "/home/vpc/data-vpc/Data/eniso/projects/Current/2017-2018/Giz-Intertek/git/Dev/Web/intertek-application/app/intertek-app-web/target/intertek.war", true);
        s.start(new String[]{"intertek"}, true);
//      ;
//        s.start();
        s.printStatus();
        //s.start();
//        s.shutdown();
    }
}
