/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.concurrent.NLockBuilder;
import net.thevpc.nuts.expr.NGlob;
import net.thevpc.nuts.format.NCmdLineFormat;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.expr.NExprs;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class Test01_CreateTest {

    @Test
    public void minimal1()  {
        long a=System.currentTimeMillis();

        String wsPath = TestUtils.getTestBaseFolder().getPath();

        TestUtils.openNewTestWorkspace("--workspace", wsPath,
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false",
                "--verbose"
        );
        Assertions.assertEquals(
                NPath.of(new File(wsPath, "cache")),
                NWorkspace.of().getStoreLocation(NStoreType.CACHE));
        Assertions.assertEquals(0, NWorkspace.of().getRepositories().size());
//        Assertions.assertEquals(new File(wsPath,  "cache/" + NutsConstants.Folders.REPOSITORIES + "/" +
//                        NRepositories.of().getRepositories()[0].getName() +
//                        "/" + NRepositories.of().getRepositories()[0].getUuid()).getPath(),
//                NRepositories.of().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE));

//        String str="     __        __    \n" +
//                "  /\\ \\ \\ _  __/ /______\n" +
//                " /  \\/ / / / / __/ ___/\n" +
//                "/ /\\  / /_/ / /_(__  )\n" +
//                "\\_\\ \\/\\__,_/\\__/____/\n";
//
//        String str="  /\\ _";
//        String str=" ```underlined prototype``` ";

//        TestUtils.println("---------------------------------");
//        TestUtils.println(str);
//        TestUtils.println("---------------------------------");
//        session.out().println(str);
//        NutsLogger _log = session.log().of("example");
//        _log.with()
//                .level(Level.INFO)
//                .log(str);
        String str =
                "a\n\nb";
        TestUtils.println("-----------------------");
        TestUtils.println(str);
        NText txt = NText.of(str);
        TestUtils.println("-----------------------");
        TestUtils.println(txt);
        long b=System.currentTimeMillis();
        System.out.println(b-a);
    }

    @Test
    public void minimal2()  {
        TestUtils.openNewTestWorkspace(
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false");
    }

    @Test
    public void minimal3()  {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false");
    }

    @Test
    public void default1() throws Exception {
        String wsPath = TestUtils.getTestBaseFolder().getPath();

        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "default",
                "--verbose",
                "--install-companions=false");
        String base = "";
        switch (NOsFamily.getCurrent()) {
            case WINDOWS: {
                base = new File(System.getProperty("user.home"), "AppData\\Local\\nuts\\cache\\ws").getPath();
                break;
            }
            case MACOS:
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                base = new File(System.getProperty("user.home"), ".cache/nuts/ws").getPath();
                break;
            }
        }
        NWorkspace ws = NWorkspace.of();
        Assertions.assertEquals(
                NPath.of(new File(base, new File(wsPath).getName())),
                ws.getStoreLocation(NStoreType.CACHE));
        NRepository localRepo = ws.getRepositories().stream().filter(x -> x.getName().equals("local")).findFirst().get();
        Assertions.assertEquals(
                NPath.of(new File(base, new File(wsPath).getName() + "/"
                        + NConstants.Folders.REPOSITORIES + "/"
                        + localRepo.getName()
                        + "/" + localRepo.getUuid()
                )),
                localRepo.config().getStoreLocation(NStoreType.CACHE));
    }

    @Test
    public void default2() throws Exception {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false");
    }

    @Test
    public void default3() throws Exception {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--install-companions=false"
        );
    }
    @Test
    public void default4() throws Exception {
        TestUtils.openNewTestWorkspace(
                "--verbose"
                ,"--install-companions=false"
        );
    }

    @Test
    public void createUtilTypes() {
        TestUtils.runNewTestWorkspace("--verbose","-b");

        {
            NPath home = NPath.of(new File(System.getProperty("user.home")));
            Assertions.assertNotNull(home);
        }

        {
            NCmdLine cmd = NCmdLine.of(new String[]{"cmd", "--test"});
            Assertions.assertNotNull(cmd);
        }

        {
            NArg arg = NArg.of("arg");
            Assertions.assertNotNull(arg);
        }

        {
            NExprs expr = NExprs.of();
            Assertions.assertNotNull(expr);
        }

        {
            NStream<String> stream = NStream.ofArray(new String[]{"a"});
            Assertions.assertNotNull(stream);
        }

        {
            Pattern g = NGlob.of().toPattern("a.*");
            Assertions.assertNotNull(g);
        }

        {
            InputStream stdin = NIO.of().stdin();
            Assertions.assertNotNull(stdin);
        }

        {
            NPrintStream stdout = NIO.of().stdout();
            Assertions.assertNotNull(stdout);
        }

        {
            NCmdLineHistory h = NCmdLineHistory.of();
            Assertions.assertNotNull(h);
        }


        {
            NLibPaths c = NLibPaths.of();
            Assertions.assertNotNull(c);
        }

        {
            NDigest c = NDigest.of();
            Assertions.assertNotNull(c);
        }

        {
            NInputStreamMonitor c = NInputStreamMonitor.of();
            Assertions.assertNotNull(c);
        }
        {
            NLockBuilder c = NLockBuilder.of();
            Assertions.assertNotNull(c);
        }
        {
            NCp c = NCp.of();
            Assertions.assertNotNull(c);
        }
        {
            NPs c = NPs.of();
            Assertions.assertNotNull(c);
        }
        {
            NCompress c = NCompress.of();
            Assertions.assertNotNull(c);
        }
        {
            NUncompress c = NUncompress.of();
            Assertions.assertNotNull(c);
        }
        {
            NLog log = NLog.of(Test01_CreateTest.class);
            Assertions.assertNotNull(log);
            NLogOp logop = NLogOp.of(Test01_CreateTest.class);
            Assertions.assertNotNull(logop);
        }
        {
            NIdFormat r = NIdFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NIdFilters r = NIdFilters.of();
            Assertions.assertNotNull(r);
        }
        {
            NVersionFilters r = NVersionFilters.of();
            Assertions.assertNotNull(r);
        }
        {
            NDependencyFilters r = NDependencyFilters.of();
            Assertions.assertNotNull(r);
        }
        {
            NDefinitionFilters r = NDefinitionFilters.of();
            Assertions.assertNotNull(r);
        }
        {
            NRepositoryFilters r = NRepositoryFilters.of();
            Assertions.assertNotNull(r);
        }
        {
            NElements r = NElements.of();
            Assertions.assertNotNull(r);
        }
        {
            NDescriptorParser r = NDescriptorParser.of();
            Assertions.assertNotNull(r);
        }
        {
            NArtifactCallBuilder r = NArtifactCallBuilder.of();
            Assertions.assertNotNull(r);
        }
        {
            NDescriptorFormat r = NDescriptorFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NDependencySolver r = NDependencySolver.of();
            Assertions.assertNotNull(r);
        }
        {
            NVersionFormat r = NVersionFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NFilters r = NFilters.of();
            Assertions.assertNotNull(r);
        }
        {
            NTexts r = NTexts.of();
            Assertions.assertNotNull(r);
        }
        {
            NObjectFormat r = NObjectFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NInfoCmd r = NInfoCmd.of();
            Assertions.assertNotNull(r);
        }
        {
            NExecCmdFormat r = NExecCmdFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NCmdLineFormat r = NCmdLineFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NTableFormat r = NTableFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NTreeFormat r = NTreeFormat.of();
            Assertions.assertNotNull(r);
        }
        {
            NPropertiesFormat r = NPropertiesFormat.of();
            Assertions.assertNotNull(r);
        }

    }

    @Test
    public void testHomePath(){
        Assertions.assertEquals(
                NHomeLocation.of(null, NStoreType.BIN),
                NHomeLocation.parse("system-bin").orElse(null)
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NHomeLocation.parse("").orElse(NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE) )
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NHomeLocation.parse("").orElse( NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE))
        );
        Assertions.assertNull(NHomeLocation.parse("any error").orElse(null));
        Assertions.assertEquals(
                NHomeLocation.of(null, NStoreType.BIN),
                NEnum.parse(NHomeLocation.class, "system-bin").orElse(null)
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NEnum.parse(NHomeLocation.class,"").orElse(NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE))
        );
        Assertions.assertEquals(
                NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE),
                NEnum.parse(NHomeLocation.class,"").orElse(NHomeLocation.of(NOsFamily.MACOS, NStoreType.CACHE) )
        );
        Assertions.assertNull(NEnum.parse(NHomeLocation.class,"any error")
                .orElse(null));
    }
}
