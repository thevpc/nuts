package net.vpc.toolbox.tomcat.test;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.toolbox.tomcat.TomcatConfig;
import net.vpc.toolbox.tomcat.TomcatMain;
import org.junit.Test;

import java.io.IOException;

public class TestTomcatMain {
//    @Test
    public void testLinux() {
        String[] args = new String[0];
        NutsWorkspace ws = Nuts.openWorkspace(args);
        args = Nuts.skipNutsArgs(args);
        TomcatMain m = new TomcatMain(ws);
        m.removeAllConfigs();
        TomcatConfig c = new TomcatConfig();
        c.setName("test");
        c.setCatalinaBase("test");
        c.setCatalinaVersion("8.5");
//        c.setJavaHome("/usr/java/jdk1.8.0_171-amd64");
        m.saveConfig(c);
        try {
            m.start("test");
            m.shutdown("test");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
