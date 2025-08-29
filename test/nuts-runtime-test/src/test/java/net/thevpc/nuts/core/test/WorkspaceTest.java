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
public class WorkspaceTest {

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
        ).share();
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
                "--install-companions=false")
                .share();
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
        TestUtils.runNewTestWorkspace("--verbose","-b")
                .share();

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

    private static final int NSH_BUILTINS = 0;// 34;
    private static final int NDI_COMPANIONS = 0;//1;

    @Test
    public void customLayout_reload() throws Exception {
//        String test_id = TestUtils.getCallerMethodId();
//        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
//        CoreIOUtils.delete(null, base);
//        TestUtils.resetLinuxFolders();
        File testBaseFolder = TestUtils.getTestBaseFolder();
        TestUtils.openNewTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "--trace",
                        "info"
                )
        ).share();
        for (NStoreType value : NStoreType.values()) {
            NOut.println(NMsg.ofC("%s %s", value, NWorkspace.of().getStoreLocation(value)));
        }

        TestUtils.openExistingTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "-!Z",
                        "--trace",
                        "info"
                )).share();

        for (NStoreType value : NStoreType.values()) {
            Assertions.assertEquals(
                    NWorkspace.of().getStoreLocation(value),
                    NWorkspace.of().getStoreLocation(value)
            );
        }
    }

//    @Test
//    public void customLayout_use_standard() {
//        String test_id = TestUtils.getCallerMethodId();
//        Assumptions.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace("--verbose", "--yes", "info");
//        NutsId ndiId = ws.search().installed().id("nsh").getResultIds().singleton();
//        Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));
//        Assertions.assertEquals(
//                createNamesSet("nsh"),
//                listNamesSet(new File(TestUtils.LINUX_CONFIG, "default-workspace/config/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
//        );
//        Assertions.assertEquals(
//                NSH_BUILTINS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
//        );
//        Assertions.assertEquals(
//                NDI_COMPANIONS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/nuts/" + ndiId.getVersion()+"/bin"), x -> x.isFile() && !x.getName().startsWith(".")).size()
//        );
//        Assertions.assertEquals(
//                3,
//                listNamesSet(new File(TestUtils.LINUX_CACHE, "default-workspace/cache/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
//        );
    ////        Assertions.assertEquals(
    ////                false,
    ////                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
    ////        );
//    }


    @Test
    public void customLayout_use_standalone() {
        TestUtils.openNewTestWorkspace("--embedded", "--standalone");
        if (NDI_COMPANIONS > 0) {
            NId nshId = null;
            try {
                nshId = NSearchCmd.of("nsh").setDefinitionFilter(NDefinitionFilters.of().byInstalled(true))
                        .setDistinct(true).getResultIds()
                        .findSingleton().get();
            } catch (Exception ex) {
                nshId = NSearchCmd.of("nsh").setDefinitionFilter(NDefinitionFilters.of().byInstalled(true))
                        .setDistinct(true).getResultIds()
                        .findSingleton().get();
            }
            Assertions.assertTrue(nshId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        }
        NPath c = NWorkspace.of().getStoreLocation(NStoreType.CONF);
        TestUtils.println(c);
        File base = NWorkspace.of().getLocation().toFile().get();
        TestUtils.println(new File(base, "config").getPath());
        for (NStoreType value : NStoreType.values()) {
            NOut.println(NMsg.ofC("%s %s", value, NWorkspace.of().getStoreLocation(value)));
        }
        Assertions.assertEquals(
                NPath.of(base).resolve("bin"),
                NWorkspace.of().getStoreLocation(NStoreType.BIN)
        );
        Assertions.assertEquals(
                NPath.of(base).resolve("cache"),
                NWorkspace.of().getStoreLocation(NStoreType.CACHE)
        );
    }

    @Test
    public void customLayout() throws Exception {
        String test_id = "customLayout_use_export";
        File base = TestUtils.getTestBaseFolder();

//        CoreIOUtils.delete(null,base);
        NWorkspace ws1=TestUtils.runNewTestWorkspace(
                "--system-bin-home", new File(base, "system.bin").getPath(),
                "--system-conf-home", new File(base, "system.conf").getPath(),
                "--system-var-home", new File(base, "system.var").getPath(),
                "--system-log-home", new File(base, "system.log").getPath(),
                "--system-temp-home", new File(base, "system.temp").getPath(),
                "--system-cache-home", new File(base, "system.cache").getPath(),
                "--system-lib-home", new File(base, "system.lib").getPath(),
                "--system-run-home", new File(base, "system.run").getPath(),
                //            "--verbose",
                "--install-companions=false",
                "info").share();

        NWorkspace ws2=TestUtils.runExistingTestWorkspace("--system-conf-home", new File(base, "system.conf.ignored").getPath(),
                "info").share();
        TestUtils.println("==========================");
        NInfoCmd.of().println();
        TestUtils.println("==========================");
        TestUtils.println(new File(base, "system.bin").getPath());
        NWorkspace workspace = NWorkspace.of();
        NWorkspace ws3=workspace;
        TestUtils.println(workspace.getStoreLocation(NStoreType.BIN));
        Assertions.assertEquals(
                NPath.of(new File(base, "system.bin")),
                workspace.getStoreLocation(NStoreType.BIN)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.conf")),
                workspace.getStoreLocation(NStoreType.CONF)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.var")),
                workspace.getStoreLocation(NStoreType.VAR)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.log")),
                workspace.getStoreLocation(NStoreType.LOG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.temp")),
                workspace.getStoreLocation(NStoreType.TEMP)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.cache")),
                workspace.getStoreLocation(NStoreType.CACHE)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.lib")),
                workspace.getStoreLocation(NStoreType.LIB)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.run")),
                workspace.getStoreLocation(NStoreType.RUN)
        );

        TestUtils.openNewTestWorkspace(//            "--workspace", "default-workspace",
//            "--workspace", new File(base, "system.config/default-workspace").getPath(),
                "info");
        TestUtils.println(workspace.getStoreLocation(NStoreType.BIN));
        Assertions.assertEquals(
                NPath.of(new File(base, "bin")),
                workspace.getStoreLocation(NStoreType.BIN)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "conf")),
                workspace.getStoreLocation(NStoreType.CONF)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "var")),
                workspace.getStoreLocation(NStoreType.VAR)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "log")),
                workspace.getStoreLocation(NStoreType.LOG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "temp")),
                workspace.getStoreLocation(NStoreType.TEMP)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "cache")),
                workspace.getStoreLocation(NStoreType.CACHE)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "lib")),
                workspace.getStoreLocation(NStoreType.LIB)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "run")),
                workspace.getStoreLocation(NStoreType.RUN)
        );
    }

    @Test
    public void load1() throws Exception {

        NWorkspace w1 = TestUtils.openNewTestWorkspace("--install-companions=false");
        NWorkspace w2 = TestUtils.openNewTestWorkspace("--install-companions=false");
    }
}
