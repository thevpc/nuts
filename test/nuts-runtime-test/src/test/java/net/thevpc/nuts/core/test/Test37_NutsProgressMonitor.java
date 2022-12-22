/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.util.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author thevpc
 */
public class Test37_NutsProgressMonitor {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test01() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);
        NutsProgressMonitor m = NutsProgressMonitors.of(session).of(event -> {
            String msg = event.toString();
            System.out.println(msg);
            out.println(msg);
            out.flush();
        });
        m.setProgress(0);
        m.setProgress(0.2);
        m.setProgress(1);
        m.setProgress(0.2);
        m.setProgress(0);
        Assertions.assertEquals(
                f(NutsProgressEventType.START,true,false,false,false,false,0.0,null)+"\n" +
                f(NutsProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                f(NutsProgressEventType.PROGRESS,true,false,false,false,false,1.0,null)+"\n" +
                f(NutsProgressEventType.COMPLETE,true,false,false,false,true,1.0,null)+"\n" +
                f(NutsProgressEventType.UNDO_COMPLETE,true,false,false,false,false,1.0,null)+"\n" +
                f(NutsProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                f(NutsProgressEventType.PROGRESS,true,false,false,false,false,0.0,null)+"\n"
                , bout.toString());
    }

    private String f(NutsProgressEventType eventType,boolean started,boolean suspended,boolean blocked,boolean cancelled,boolean completed,double progress,String message){
        return NutsStringUtils.formatAlign(eventType.toString(), 13, NutsPositionType.FIRST)
                + " "
                + (started ? "S" : " ")
                + (suspended ? "P" : " ")
                + (blocked ? "B" : " ")
                + (cancelled ? "C" : " ")
                + (completed ? "T" : " ")
                + " " + progress
                + " " + message;
    }
    @Test
    public void test02() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);
        NutsProgressMonitor m = NutsProgressMonitors.of(session).of(event -> {
            String msg = event.toString();
            System.out.println(msg);
            out.println(msg);
            out.flush();
        });
        m.setProgress(0);
        m.setProgress(0.2);
        m.setProgress(1);
        m.setProgress(0.2);
        m.setProgress(0);
        Assertions.assertEquals(
                f(NutsProgressEventType.START,true,false,false,false,false,0.0,null)+"\n" +
                        f(NutsProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                        f(NutsProgressEventType.PROGRESS,true,false,false,false,false,1.0,null)+"\n" +
                        f(NutsProgressEventType.COMPLETE,true,false,false,false,true,1.0,null)+"\n" +
                        f(NutsProgressEventType.UNDO_COMPLETE,true,false,false,false,false,1.0,null)+"\n" +
                        f(NutsProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                        f(NutsProgressEventType.PROGRESS,true,false,false,false,false,0.0,null)+"\n"
                , bout.toString());
    }
}
