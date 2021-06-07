/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
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
//        if(systemMode!=null) {
//            ws.term().getSystemTerminal().setMode(systemMode);
//        }
        NutsSession session = ws.createSession();
        if(sessionMode!=null) {
            session.getTerminal().setOut(session.getTerminal().out().convert(sessionMode));
        }
        TestUtils.print("      ");
        NutsPrintStream out = session.getTerminal().out();
        out.print("{**aa");
        out.print("aa**}");
        out.println();
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @BeforeEach
    public void startup() throws IOException {
        Assumptions.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
