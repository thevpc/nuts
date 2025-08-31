/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.xtra.time.CProgressBar;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thevpc
 */
public class TerminalTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--progress");
    }


    @Test
    public void test1() throws Exception {

//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.INHERITED,Result.SUCCESS);
//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.FORMATTED,Result.SUCCESS);
//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.FILTERED,Result.SUCCESS);
//        testMode(session,NutsTerminalMode.INHERITED,NutsTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.FORMATTED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(NTerminalMode.FORMATTED, NTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(NTerminalMode.FORMATTED, NTerminalMode.FILTERED,Result.SUCCESS);
        //testMode(NTerminalMode.FORMATTED, NTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.FILTERED,NutsTerminalMode.INHERITED,Result.SUCCESS);
        testMode(NTerminalMode.FILTERED, NTerminalMode.FORMATTED,Result.SUCCESS);
        testMode(NTerminalMode.FILTERED, NTerminalMode.FILTERED,Result.SUCCESS);
        //testMode(NTerminalMode.FILTERED, NTerminalMode.ANSI,Result.FAIL);

//        testMode(session,NutsTerminalMode.ANSI,NutsTerminalMode.INHERITED,Result.FAIL);
        //testMode(NTerminalMode.ANSI, NTerminalMode.FORMATTED,Result.FAIL);
        //testMode(NTerminalMode.ANSI, NTerminalMode.FILTERED,Result.FAIL);
        // How could we create in a save manner an ansi  sys terminal??
        //testMode(NTerminalMode.ANSI, NTerminalMode.ANSI,Result.FAIL);
    }
    private enum Result{
        SUCCESS,
        FAIL
    }

    public static void testMode(NTerminalMode systemMode, NTerminalMode sessionMode, Result result) {

        if(sessionMode!=null) {
            if(result==Result.FAIL){
                Assertions.assertThrows(NIllegalArgumentException.class,()->

                        {
                            NSystemTerminal systemTerminal = NIO.of().getSystemTerminal();
                            NPrintStream sysInitMode = systemTerminal.out();
                            TestUtils.println(
                                    "sys-init="+(sysInitMode.getTerminalMode()==null?"default": sysInitMode.getTerminalMode().id())
                                            +", sys-fixed="+(systemMode==null?"default":systemMode.id())
                                            +" ->"+sessionMode.id());

                            NTerminal terminal = NTerminal.of();
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
                            NTerminalMode newMode = terminal.getOut().getTerminalMode();
                            TestUtils.println(
                                    "sys-init="+(sysInitMode.getTerminalMode()==null?"default": sysInitMode.getTerminalMode().id())
                                            +", sys-fixed="+(systemMode==null?"default":systemMode.id())
                                            +" ->"+sessionMode.id()+"->"+newMode.id());
                            Assertions.assertEquals(sessionMode,newMode);
                            TestUtils.print("      ");
                            out.print("{**aa");
                            out.print("aa**}");
                            out.println();

                        }
                );
                return;
            }else{
                NSystemTerminal systemTerminal = NIO.of().getSystemTerminal();
                NPrintStream sysInitMode = systemTerminal.out();
                NTerminal terminal = NTerminal.of();
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
        NText c = NText.ofCode("java", "public static void main(String[] args){}")
                .highlight();
        NOut.println(c);

        NText word_static = c.builder().substring(7, 13);
        NOut.println(word_static);
        Assertions.assertEquals("##{keyword:static}##\u001E",word_static.toString());

        NText portion_npar = c.builder().substring(22, 24);
        NOut.println(portion_npar);
        Assertions.assertEquals("n##{separator:(}##\u001E",portion_npar.toString());
        NText rep=c.builder().replace(23,24, NText.ofStyled("()(", NTextStyle.danger())).build();
        NOut.println(rep);
        Assertions.assertEquals("##{keyword:public}##\u001E ##{keyword:static}##\u001E ##{keyword:void}##\u001E main##{danger:()(}##\u001EString##{separator:[}##\u001E##{separator:]}##\u001E args##{separator:)}##\u001E##{separator:{}##\u001E##{separator:}}##\u001E",
                rep.toString());
    }


    @Test
    public void test5() throws Exception {
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            NSession.of().getTerminal().printProgress((i / 100f), NMsg.ofC("message %s", i));
        }
    }

    @Test
    public void test2() {
        CProgressBar rr = CProgressBar.of();
        rr.setMinPeriod(-1);
        for (String formatter : rr.getFormatterNames()) {
            rr.setFormatter(formatter);
            for (int i = 0; i < 100; i++) {
                System.out.printf("%2d ::" + rr.progress(i) + "\n", i);
            }
            for (int i = 0; i < 12; i++) {
                int finalI = i;
                rr.setIndeterminatePosition((CProgressBar bar, int size) -> finalI % size);
                System.out.printf("%2d ::" + rr.progress(-1) + "\n", i);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(TestCProgressBar.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
    }

    @Test
    public void test3() {
        CProgressBar rr = new CProgressBar();
        rr.setMinPeriod(-1);
        for (String formatter : rr.getFormatterNames()) {
            rr.setFormatter(formatter);
            for (int i = 0; i < 100; i++) {
//                System.out.printf("%2d ::" + rr.progress(i) + "\n", i);
                NOut.println(NMsg.ofC("%2d ::" + rr.progress(i), i));
            }
            for (int i = 0; i < 12; i++) {
                int finalI = i;
                rr.setIndeterminatePosition((CProgressBar bar, int size) -> finalI % size);
                NOut.println(NMsg.ofC("%2d ::" + rr.progress(-1), i));
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(TestCProgressBar.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }
        }
    }

    @Test
    public void test4() {
        CProgressBar rr = new CProgressBar();
        for (int i = 0; i < 12; i++) {
            int finalI = i;
            rr.setIndeterminatePosition(new CProgressBar.IndeterminatePosition() {
                @Override
                public int evalIndeterminatePos(CProgressBar bar, int size) {
                    return finalI % size;
                }
            });
            TestUtils.println(i + " ::" + rr.progress(-1));
        }
        Assertions.assertTrue(true);
    }

//    @Test
    public void test6() {
        char[] youDontLike = NAsk.of()
                .forPassword(NMsg.ofC("Ask me something %s", "you dont like"))
                .getValue();
        NOut.println(new String(youDontLike));
    }
}
