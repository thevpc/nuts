/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.special;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;


/**
 *
 * @author thevpc
 */
public class TestSpecial_ExecURLTest {
    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }



    private void printlnNode(NDependencyTreeNode d, String s) {
        TestUtils.println(s+d.getDependency());
        for (NDependencyTreeNode child : d.getChildren()) {
            printlnNode(child,"  ");
        }
    }


    public void testNtf2() {
        TestUtils.println(NVersionFormat.of());
        String result = NExecCmd.of()
                .setTarget("ssh://vpc:a@192.168.1.36")
                //.addCommand("ls","-l")
                .addCommand("nuts","info")
                .failFast()
                //.system()
                .getGrabbedAllString();
        session.out().println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

}
