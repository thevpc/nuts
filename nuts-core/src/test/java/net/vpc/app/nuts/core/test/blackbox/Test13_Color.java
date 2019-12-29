/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.junit.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author vpc
 */
public class Test13_Color {

    private static String baseFolder;

    @Test
    public void test1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = Nuts.openWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--yes",
                "--log-info",
                "--skip-companions");

        for (NutsTerminalMode sysMode : new NutsTerminalMode[]{NutsTerminalMode.INHERITED, NutsTerminalMode.FORMATTED, NutsTerminalMode.FILTERED}) {
            for (NutsTerminalMode sessionMode : new NutsTerminalMode[]{NutsTerminalMode.INHERITED, NutsTerminalMode.FORMATTED, NutsTerminalMode.FILTERED}) {
                testMode(ws,sysMode,sessionMode);
            }
        }
    }

    public static void testMode(NutsWorkspace ws,NutsTerminalMode systemMode,NutsTerminalMode sessionMode) {
        TestUtils.println((systemMode==null?"default":systemMode==NutsTerminalMode.INHERITED?"raw":systemMode.id())
                +"->"+(sessionMode==null?"default":sessionMode==NutsTerminalMode.INHERITED?"raw":sessionMode.id()));
        if(systemMode!=null) {
            ws.io().getSystemTerminal().setMode(systemMode);
        }
        NutsSession session = ws.createSession();
        if(sessionMode!=null) {
            session.getTerminal().setOutMode(sessionMode);
        }
        TestUtils.print("      ");
        PrintStream out = session.getTerminal().out();
        out.print("{**aa");
        out.print("aa**}");
        out.println();
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
