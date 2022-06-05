/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsSessionTerminal;
import net.thevpc.nuts.io.NutsSystemTerminal;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test13_TerminalModeTest {
    static NutsSession session;

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
        testMode(session, NutsTerminalMode.FORMATTED,NutsTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.FILTERED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.FILTERED,Result.SUCCESS);
        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.ANSI,NutsTerminalMode.INHERITED,Result.FAIL);
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
                            NutsSystemTerminal systemTerminal = session.config().getSystemTerminal();
                            NutsPrintStream sysInitMode = systemTerminal.out();
                            TestUtils.println(
                                    "sys-init="+(sysInitMode.getTerminalMode()==null?"default": sysInitMode.getTerminalMode().id())
                                            +", sys-fixed="+(systemMode==null?"default":systemMode.id())
                                            +" ->"+sessionMode.id());

                            NutsSessionTerminal terminal = NutsSessionTerminal.of(session);
                            NutsPrintStream out = terminal.out().setTerminalMode(systemMode);
                            NutsTerminalMode initMode = out.getTerminalMode();
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
                NutsSystemTerminal systemTerminal = session.config().getSystemTerminal();
                NutsPrintStream sysInitMode = systemTerminal.out();
                NutsSessionTerminal terminal = NutsSessionTerminal.of(session);
                NutsPrintStream out = terminal.out().setTerminalMode(systemMode);
                NutsTerminalMode initMode = out.getTerminalMode();
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
        NutsText c = NutsTexts.of(session).ofCode("java", "public static void main(String[] args){}")
                .highlight(session);
        session.out().printlnf(c);

        NutsText word_static = c.builder().substring(7, 13);
        session.out().printlnf(word_static);
        Assertions.assertEquals("##{keyword:static}##\u001E",word_static.toString());

        NutsText portion_npar = c.builder().substring(22, 24);
        session.out().printlnf(portion_npar);
        Assertions.assertEquals("n##{separator:(}##\u001E",portion_npar.toString());
        NutsText rep=c.builder().replace(23,24,NutsTexts.of(session).ofStyled("()(", NutsTextStyle.danger())).build();
        session.out().printlnf(rep);
        Assertions.assertEquals("##{keyword:public}##\u001E ##{keyword:static}##\u001E ##{keyword:void}##\u001E main##{danger:()(}##\u001EString##{separator:[}##\u001E##{separator:]}##\u001E args##{separator:)}##\u001E##{separator:{}##\u001E##{separator:}}##\u001E",
                rep.toString());
    }
}
