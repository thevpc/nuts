/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;

import java.io.File;
import java.io.InputStream;
import java.util.regex.Pattern;

import net.thevpc.nuts.spi.NutsApplicationContexts;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsTerminals;
import org.junit.jupiter.api.*;

/**
 * @author thevpc
 */
public class Test01_CreateTest {

    @Test
    public void minimal1()  {
        String wsPath = TestUtils.getTestBaseFolder().getPath();

        NutsSession session = TestUtils.openNewTestWorkspace("--workspace", wsPath,
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions",
                "--verbose"
        );
        Assertions.assertEquals(
                NutsPath.of(new File(wsPath, "cache"),session),
                session.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(0, session.repos().getRepositories().length);
//        Assertions.assertEquals(new File(wsPath,  "cache/" + NutsConstants.Folders.REPOSITORIES + "/" +
//                        session.repos().getRepositories()[0].getName() +
//                        "/" + session.repos().getRepositories()[0].getUuid()).getPath(),
//                session.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE));

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
        NutsText txt = NutsTexts.of(session).parse(str);
        TestUtils.println("-----------------------");
        TestUtils.println(txt);
    }

    @Test
    public void minimal2()  {
        TestUtils.openNewTestWorkspace(
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions");
    }

    @Test
    public void minimal3()  {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions");
    }

    @Test
    public void default1() throws Exception {
        String wsPath = TestUtils.getTestBaseFolder().getPath();

        NutsSession session = TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "default",
                "--verbose",
                "--skip-companions");
        String base = "";
        switch (NutsOsFamily.getCurrent()) {
            case WINDOWS: {
                base = new File(System.getProperty("user.home"), "AppData\\Local\\nuts\\cache").getPath();
                break;
            }
            case MACOS:
            case LINUX:
            case UNIX:
            case UNKNOWN: {
                base = new File(System.getProperty("user.home"), ".cache/nuts").getPath();
                break;
            }
        }
        Assertions.assertEquals(
                NutsPath.of(new File(base, new File(wsPath).getName()),session),
                session.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(
                NutsPath.of(new File(base, new File(wsPath).getName() + "/"
                        + NutsConstants.Folders.REPOSITORIES + "/"
                        + session.repos().getRepositories()[0].getName()
                        + "/" + session.repos().getRepositories()[0].getUuid()
                ),session),
                session.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE));
    }

    @Test
    public void default2() throws Exception {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions");
    }

    @Test
    public void default3() throws Exception {
        TestUtils.openNewTestWorkspace(
                "--reset", // required for exploded repos
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions");
    }

    @Test
    public void createUtilTypes() {
        NutsSession s = TestUtils.runNewTestWorkspace("--verbose","-b");

        {
            NutsPath home = NutsPath.of(new File(System.getProperty("user.home")), s);
            Assertions.assertNotNull(home);
        }

        {
            NutsCommandLine cmd = NutsCommandLine.of(new String[]{"cmd", "--test"}, s);
            Assertions.assertNotNull(cmd);
        }

        {
            NutsArgument arg = NutsArgument.of("arg", s);
            Assertions.assertNotNull(arg);
        }

        {
            NutsExpr expr = NutsExpr.of(s);
            Assertions.assertNotNull(expr);
        }

        {
            NutsStream<String> stream = NutsStream.of(new String[]{"a"}, s);
            Assertions.assertNotNull(stream);
        }

        {
            Pattern g = NutsGlob.of(s).toPattern("a.*");
            Assertions.assertNotNull(g);
        }

        {
            InputStream stdin = NutsInputStreams.of(s).stdin();
            Assertions.assertNotNull(stdin);
        }

        {
            NutsPrintStream stdout = NutsPrintStreams.of(s).stdout();
            Assertions.assertNotNull(stdout);
        }

        {
            NutsCommandHistory h = NutsCommandHistory.of(s);
            Assertions.assertNotNull(h);
        }

        {
            NutsApplicationContexts c = NutsApplicationContexts.of(s);
            Assertions.assertNotNull(c);
        }

        {
            NutsExecutionEntries c = NutsExecutionEntries.of(s);
            Assertions.assertNotNull(c);
        }

        {
            NutsHash c = NutsHash.of(s);
            Assertions.assertNotNull(c);
        }

        {
            NutsInputStreamMonitor c = NutsInputStreamMonitor.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsLocks c = NutsLocks.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsTerminals c = NutsTerminals.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsTmp c = NutsTmp.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsRm c = NutsRm.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsCp c = NutsCp.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsPs c = NutsPs.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsCompress c = NutsCompress.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsUncompress c = NutsUncompress.of(s);
            Assertions.assertNotNull(c);
        }
        {
            NutsLogger log = NutsLogger.of(Test01_CreateTest.class, s);
            Assertions.assertNotNull(log);
            NutsLoggerOp logop = NutsLoggerOp.of(Test01_CreateTest.class, s);
            Assertions.assertNotNull(logop);
        }
        {
            NutsIdResolver r = NutsIdResolver.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsIdParser r = NutsIdParser.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsIdBuilder r = NutsIdBuilder.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsIdFormat r = NutsIdFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsIdFilters r = NutsIdFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsVersionFilters r = NutsVersionFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDependencyFilters r = NutsDependencyFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDescriptorFilters r = NutsDescriptorFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsInstallStatusFilters r = NutsInstallStatusFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsRepositoryFilters r = NutsRepositoryFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsElements r = NutsElements.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDescriptorParser r = NutsDescriptorParser.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDescriptorBuilder r = NutsDescriptorBuilder.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsEnvConditionBuilder r = NutsEnvConditionBuilder.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDescriptorPropertyBuilder r = NutsDescriptorPropertyBuilder.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsArtifactCallBuilder r = NutsArtifactCallBuilder.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDescriptorFormat r = NutsDescriptorFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDependencySolver r = NutsDependencySolver.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDependencyBuilder r = NutsDependencyBuilder.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsDependencyParser r = NutsDependencyParser.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsVersionParser r = NutsVersionParser.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsVersionFormat r = NutsVersionFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsFilters r = NutsFilters.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsTexts r = NutsTexts.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsObjectFormat r = NutsObjectFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsInfoCommand r = NutsInfoCommand.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsExecCommandFormat r = NutsExecCommandFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsCommandLineFormat r = NutsCommandLineFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsTableFormat r = NutsTableFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsTreeFormat r = NutsTreeFormat.of(s);
            Assertions.assertNotNull(r);
        }
        {
            NutsPropertiesFormat r = NutsPropertiesFormat.of(s);
            Assertions.assertNotNull(r);
        }

    }

    @Test
    public void testHomePath(){
        Assertions.assertEquals(
                NutsHomeLocation.of(null, NutsStoreLocation.APPS),
                NutsHomeLocation.parse("system-apps", null)
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsHomeLocation.parse("", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsHomeLocation.parseLenient("", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertNull(NutsHomeLocation.parseLenient("any error", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE), null));
        Assertions.assertEquals(
                NutsHomeLocation.of(null, NutsStoreLocation.APPS),
                NutsEnum.parse(NutsHomeLocation.class, "system-apps", null)
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsEnum.parse(NutsHomeLocation.class,"", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsEnum.parseLenient(NutsHomeLocation.class,"", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertNull(NutsEnum.parseLenient(NutsHomeLocation.class,"any error", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE), null));
    }
}
