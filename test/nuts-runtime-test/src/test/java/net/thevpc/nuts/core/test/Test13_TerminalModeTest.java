/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.io.NSystemTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test13_TerminalModeTest {
    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void test1() throws Exception {

//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.INHERITED,Result.SUCCESS);
//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.FORMATTED,Result.SUCCESS);
//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.FILTERED,Result.SUCCESS);
//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(session, NTerminalMode.FORMATTED, NTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(session, NTerminalMode.FORMATTED, NTerminalMode.FILTERED,Result.SUCCESS);
        testMode(session, NTerminalMode.FORMATTED, NTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(session, NTerminalMode.FILTERED, NTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(session, NTerminalMode.FILTERED, NTerminalMode.FILTERED,Result.SUCCESS);
        testMode(session, NTerminalMode.FILTERED, NTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.ANSI,NutsTerminalMode.INHERITED,Result.FAIL);
        testMode(session, NTerminalMode.ANSI, NTerminalMode.FORMATTED,Result.FAIL);
        testMode(session, NTerminalMode.ANSI, NTerminalMode.FILTERED,Result.FAIL);
        // How could we create in a save manner an ansi  sys terminal??
        testMode(session, NTerminalMode.ANSI, NTerminalMode.ANSI,Result.FAIL);
    }
    private enum Result{
        SUCCESS,
        FAIL
    }

    public static void testMode(NSession session, NTerminalMode systemMode, NTerminalMode sessionMode, Result result) {

        if(sessionMode!=null) {
            if(result==Result.FAIL){
                Assertions.assertThrows(NIllegalArgumentException.class,()->

                        {
                            NSystemTerminal systemTerminal = NConfigs.of(session).getSystemTerminal();
                            NPrintStream sysInitMode = systemTerminal.out();
                            TestUtils.println(
                                    "sys-init="+(sysInitMode.getTerminalMode()==null?"default": sysInitMode.getTerminalMode().id())
                                            +", sys-fixed="+(systemMode==null?"default":systemMode.id())
                                            +" ->"+sessionMode.id());

                            NSessionTerminal terminal = NSessionTerminal.of(session);
                            NPrintStream out = terminal.out().setTerminalMode(systemMode);
                            NTerminalMode initMode = out.getTerminalMode();
                            Assertions.assertEquals(systemMode,initMode);
                            TestUtils.println(
                                    "sys-init="+(sysInitMode.getTerminalMode()==null?"default": sysInitMode.getTerminalMode().id())
                                            +", sys-fixed="+(systemMode==null?"default":systemMode.id())
                                            +" ->"+sessionMode.id());
//        if(systemMode!=null) {
//            ws.term().getSystemTerminal().setMode(systemMode);
//        }

                            terminal.setOut(out.setTerminalMode(sessionMode));

                            TestUtils.print("      ");
                            out.print("{**aa");
                            out.print("aa**}");
                            out.println();

                        }
                );
                return;
            }else{
                NSystemTerminal systemTerminal = NConfigs.of(session).getSystemTerminal();
                NPrintStream sysInitMode = systemTerminal.out();
                NSessionTerminal terminal = NSessionTerminal.of(session);
                NPrintStream out = terminal.out().setTerminalMode(systemMode);
                NTerminalMode initMode = out.getTerminalMode();
                Assertions.assertEquals(systemMode,initMode);
                TestUtils.println(
                        "sys-init="+sysInitMode.getTerminalMode().id()
                                +", sys-fixed="+systemMode.id()
                                +" ->"+ sessionMode.id());
//        if(systemMode!=null) {
//            ws.term().getSystemTerminal().setMode(systemMode);
//        }
                terminal.setOut(out.setTerminalMode(sessionMode));
                TestUtils.print("      ");
                out.print("{**aa");
                out.print("aa**}");
                out.println();
            }
        }
    }

    @Test
    public void testBuilder(){
        NText c = NTexts.of(session).ofCode("java", "public static void main(String[] args){}")
                .highlight(session);
        session.out().println(c);

        NText word_static = c.builder().substring(7, 13);
        session.out().println(word_static);
        Assertions.assertEquals("##{keyword:static}##\u001E",word_static.toString());

        NText portion_npar = c.builder().substring(22, 24);
        session.out().println(portion_npar);
        Assertions.assertEquals("n##{separator:(}##\u001E",portion_npar.toString());
        NText rep=c.builder().replace(23,24, NTexts.of(session).ofStyled("()(", NTextStyle.danger())).build();
        session.out().println(rep);
        Assertions.assertEquals("##{keyword:public}##\u001E ##{keyword:static}##\u001E ##{keyword:void}##\u001E main##{danger:()(}##\u001EString##{separator:[}##\u001E##{separator:]}##\u001E args##{separator:)}##\u001E##{separator:{}##\u001E##{separator:}}##\u001E",
                rep.toString());
    }
}
