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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class Test13_Color {


    @Test
    public void test1() throws Exception {
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--log-info",
                "--skip-companions");

        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.FILTERED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.ANSI,Result.FAIL);

        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.FILTERED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.ANSI,Result.FAIL);

        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.FILTERED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.ANSI,Result.FAIL);

        testMode(session,NutsTerminalMode.ANSI,NutsTerminalMode.INHERITED,Result.FAIL);
        testMode(session,NutsTerminalMode.ANSI,NutsTerminalMode.FORMATTED,Result.FAIL);
        testMode(session,NutsTerminalMode.ANSI,NutsTerminalMode.FILTERED,Result.FAIL);
        // How could we create in a save manner an ansi  sys terminal??
        testMode(session,NutsTerminalMode.ANSI,NutsTerminalMode.ANSI,Result.FAIL);
    }
    private enum Result{
        SUCCESS,
        FAIL
    }

    public static void testMode(NutsSession session,NutsTerminalMode systemMode,NutsTerminalMode sessionMode,Result result) {

        if(sessionMode!=null) {
            if(result==Result.FAIL){
                Assertions.assertThrows(NutsIllegalArgumentException.class,()->

                        {
                            NutsSystemTerminal systemTerminal = session.term().getSystemTerminal();
                            NutsPrintStream sysInitMode = systemTerminal.out();
                            TestUtils.println(
                                    "sys-init="+(sysInitMode.mode()==null?"default": sysInitMode.mode().id())
                                            +", sys-fixed="+(systemMode==null?"default":systemMode.id())
                                            +" ->"+sessionMode.id());

                            NutsSessionTerminal terminal = session.term().createTerminal();
                            NutsPrintStream out = terminal.out().convertMode(systemMode);
                            NutsTerminalMode initMode = out.mode();
                            Assertions.assertEquals(systemMode,initMode);
                            TestUtils.println(
                                    "sys-init="+(sysInitMode.mode()==null?"default": sysInitMode.mode().id())
                                            +", sys-fixed="+(systemMode==null?"default":systemMode.id())
                                            +" ->"+sessionMode.id());
//        if(systemMode!=null) {
//            ws.term().getSystemTerminal().setMode(systemMode);
//        }

                            terminal.setOut(out.convertMode(sessionMode));

                            TestUtils.print("      ");
                            out.print("{**aa");
                            out.print("aa**}");
                            out.println();

                        }
                );
                return;
            }else{
                NutsSystemTerminal systemTerminal = session.term().getSystemTerminal();
                NutsPrintStream sysInitMode = systemTerminal.out();
                NutsSessionTerminal terminal = session.term().createTerminal();
                NutsPrintStream out = terminal.out().convertMode(systemMode);
                NutsTerminalMode initMode = out.mode();
                Assertions.assertEquals(systemMode,initMode);
                TestUtils.println(
                        "sys-init="+sysInitMode.mode().id()
                                +", sys-fixed="+systemMode.id()
                                +" ->"+ sessionMode.id());
//        if(systemMode!=null) {
//            ws.term().getSystemTerminal().setMode(systemMode);
//        }
                terminal.setOut(out.convertMode(sessionMode));
                TestUtils.print("      ");
                out.print("{**aa");
                out.print("aa**}");
                out.println();
            }
        }
    }

}
