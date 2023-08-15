/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.time.NProgressEventType;
import net.thevpc.nuts.time.NProgressMonitor;
import net.thevpc.nuts.time.NProgressMonitors;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author thevpc
 */
public class Test37_NProgressMonitor {
    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test01() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);
        NProgressMonitor m = NProgressMonitors.of(session).of(event -> {
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
                f(NProgressEventType.START,true,false,false,false,false,0.0,null)+"\n" +
                f(NProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                f(NProgressEventType.PROGRESS,true,false,false,false,false,1.0,null)+"\n" +
                f(NProgressEventType.COMPLETE,true,false,false,false,true,1.0,null)+"\n" +
                f(NProgressEventType.UNDO_COMPLETE,true,false,false,false,false,1.0,null)+"\n" +
                f(NProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                f(NProgressEventType.PROGRESS,true,false,false,false,false,0.0,null)+"\n"
                , bout.toString());
    }

    private String f(NProgressEventType eventType, boolean started, boolean suspended, boolean blocked, boolean cancelled, boolean completed, double progress, String message){
        return NStringUtils.formatAlign(eventType.toString(), 13, NPositionType.FIRST)
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
        NProgressMonitor m = NProgressMonitors.of(session).of(event -> {
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
                f(NProgressEventType.START,true,false,false,false,false,0.0,null)+"\n" +
                        f(NProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                        f(NProgressEventType.PROGRESS,true,false,false,false,false,1.0,null)+"\n" +
                        f(NProgressEventType.COMPLETE,true,false,false,false,true,1.0,null)+"\n" +
                        f(NProgressEventType.UNDO_COMPLETE,true,false,false,false,false,1.0,null)+"\n" +
                        f(NProgressEventType.PROGRESS,true,false,false,false,false,0.2,null)+"\n" +
                        f(NProgressEventType.PROGRESS,true,false,false,false,false,0.0,null)+"\n"
                , bout.toString());
    }
}
