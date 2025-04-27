/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.oldws.OldWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;


import net.thevpc.nuts.io.NIOUtils;
import org.junit.jupiter.api.*;

import java.io.*;

/**
 * @author thevpc
 */
public class WorkspaceUpdateTest {

    private static String baseFolder;

    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.openNewMinTestWorkspace();
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getPath();
        NIOUtils.delete(new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

//    @Test
//    public void testV083() throws Exception {
//        testByVersion("0.8.3");
//    }

    @Test
    public void testV084() throws Exception {
        testByVersion("0.8.4");
    }

    @Test
    public void testV085() throws Exception {
        testByVersion("0.8.5");
    }

    private void testByVersion(String version){
        OldWorkspace ows = OldWorkspace.ofTest(version,baseFolder);
        ows.reset();
        ows.installWs();
        ows.upgrade();
        ows.showVersion();
    }


}
