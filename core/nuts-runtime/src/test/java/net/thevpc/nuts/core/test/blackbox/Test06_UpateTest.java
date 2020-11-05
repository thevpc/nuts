/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class Test06_UpateTest {

    private static String baseFolder;

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getPath();
        CoreIOUtils.delete(null, new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @Test
    public void testUpdateApi() throws Exception {
        testUpdate(false, TestUtils.getCallerMethodName());
    }

    @Test
    public void testUpdateImpl() throws Exception {
        testUpdate(true, TestUtils.getCallerMethodName());
    }

    private void testUpdate(boolean implOnly, String callerName) throws Exception {
        CoreIOUtils.delete(null, new File(baseFolder));
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        final String workspacePath = baseFolder + "/" + callerName;

        NutsWorkspace uws = Nuts.openWorkspace(
                "--workspace", workspacePath + "-update",
                "--archetype", "minimal",
                "--standalone",
                "--standalone-repositories",
                "--yes",
                "--progress=newline",
                "--skip-companions"
        );
        NutsSession session = uws.createSession();
        NutsRepository updateRepo1 = uws.repos().addRepository("local", session);
        uws.config().save(session);
        //NutsRepository updateRepo1 = uws.config().getRepository("local", session);
        String updateRepoPath = updateRepo1.config().getStoreLocation().toString();
        TestUtils.println(updateRepo1.config().getStoreLocationStrategy());
        uws.info().println();
        TestUtils.println("\n------------------------------------------");
        NutsWorkspace nws = Nuts.openWorkspace(
                "--workspace", workspacePath,
                "--standalone",
                "--standalone-repositories",
                "--progress=newline",
                "--yes",
                "--skip-companions"
        );
        nws.repos().addRepository(new NutsAddRepositoryOptions().setTemporary(true).setName("temp").setLocation(updateRepoPath)
                .setConfig(new NutsRepositoryConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE))
        );
        nws.info().showRepositories().println();
        TestUtils.println("\n------------------------------------------");

        NutsRepository r = nws.repos().getRepository("temp", session);
        NutsDefinition api = nws.fetch().setContent(true).setNutsApi().getResultDefinition();
        NutsDefinition rt = nws.fetch().setContent(true).setNutsRuntime().getResultDefinition();

        NutsVersion apiv1 = api.getId().getVersion();
        NutsVersion apiv2 = implOnly ? apiv1 : apiv1.inc(-1, 10);
        FromTo fromToAPI = new FromTo(apiv1.toString(), apiv2.toString());

        NutsVersion rtv2 = rt.getId().getVersion().inc(-1, 10);
        FromTo fromToImpl = new FromTo(rt.getId().getVersion().toString(), rtv2.toString());

        if (!fromToAPI.isIdentity()) {
            uws.deploy()
                    .setContent(replaceAPIJar(api.getPath(), fromToAPI, uws))
                    .setDescriptor(api.getDescriptor().builder().setId(api.getId().builder().setVersion(apiv2).build()).build())
                    //                        .setRepository("local")
                    .run();
        }
        uws.deploy()
                .setContent(replaceRuntimeJar(rt.getPath(), fromToAPI, fromToImpl, uws))
                .setDescriptor(
                        rt.getDescriptor()
                                .builder()
                                .setId(rt.getId().builder().setVersion(rtv2).build())
                                .replaceDependency(
                                        x -> x.getSimpleName().equals(api.getId().getShortName()),
                                        x -> x.builder().setVersion(apiv2).build()
                                )
                                .build()
                )
                .run();

        TestUtils.println("[LOCAL]");
        TestUtils.println(uws.repos().getRepository("local", session).config().getStoreLocationStrategy());
        TestUtils.println(uws.repos().getRepository("local", session).config().getStoreLocation());
        TestUtils.println(uws.repos().getRepository("local", session).config().getStoreLocation(NutsStoreLocation.LIB));

        TestUtils.println("[TEMP]");
        TestUtils.println(nws.repos().getRepository("temp", session).config().getStoreLocationStrategy());
        TestUtils.println(nws.repos().getRepository("temp", session).config().getStoreLocation());
        TestUtils.println(nws.repos().getRepository("temp", session).config().getStoreLocation(NutsStoreLocation.LIB));
        Assert.assertEquals(
                uws.repos().getRepository("local", session).config().getStoreLocation(NutsStoreLocation.LIB),
                nws.repos().getRepository("temp", session).config().getStoreLocation(NutsStoreLocation.LIB));

        TestUtils.println(uws.search().addId(api.getId().getShortNameId()).getResultIds().list());
        TestUtils.println(uws.search().addId(rt.getId().getShortNameId()).getResultIds().list());
        List<NutsId> foundApis = uws.search().addId(api.getId().getShortNameId()).getResultIds().list();
        List<NutsId> foundRts = uws.search().addId(rt.getId().getShortNameId()).getResultIds().list();
        Assert.assertTrue(foundApis.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(api.getId().builder().setVersion(apiv1).getLongName()));
        if (!implOnly) {
            Assert.assertTrue(foundApis.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(api.getId().builder().setVersion(apiv2).getLongName()));
        }
        Assert.assertTrue(foundRts.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(rt.getId().builder().setVersion(rt.getId().getVersion()).getLongName()));
        Assert.assertTrue(foundRts.stream().map(NutsId::getLongName).collect(Collectors.toSet()).contains(rt.getId().builder().setVersion(rtv2).getLongName()));

        TestUtils.println("========================");
        TestUtils.println(nws.search().addId(api.getId().getShortNameId()).setRepository("temp").getResultIds().list());
        TestUtils.println(nws.search().addId(rt.getId().getShortNameId()).setRepository("temp").getResultIds().list());
        TestUtils.println(nws.search().addId(api.getId().getShortNameId()).getResultIds().list());
        TestUtils.println(nws.search().addId(rt.getId().getShortNameId()).getResultIds().list());

        //check updates!
        NutsUpdateCommand foundUpdates = nws.update().setAll().checkUpdates();
        for (NutsUpdateResult u : foundUpdates.getResult().getAllUpdates()) {
            TestUtils.println(u.getAvailable());
        }
        Assert.assertEquals(implOnly ? 1 : 2, foundUpdates.getResultCount());
        foundUpdates.update();

        final String newApiVersion = foundUpdates.getResult().getApi().getAvailable().getId().getVersion().toString();
        final String newRuntimeVersion = foundUpdates.getResult().getRuntime().getAvailable().getId().getVersion().toString();
//        Path bootFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
//        Path bootCompFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
        Path bootCacheFolder = nws.locations().getStoreLocation(NutsStoreLocation.CACHE).resolve(NutsConstants.Folders.ID);
        Path libFolder = nws.locations().getStoreLocation(NutsStoreLocation.LIB).resolve(NutsConstants.Folders.ID);
        Path configFolder = nws.locations().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.ID);
        Assert.assertTrue(Files.exists(libFolder.resolve("net/thevpc/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".jar")
        ));
        Assert.assertTrue(Files.exists(configFolder.resolve("net/thevpc/nuts/nuts/").resolve(newApiVersion)
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME)
        ));

        Assert.assertTrue(Files.exists(bootCacheFolder.resolve("net/thevpc/nuts/nuts-runtime/").resolve(newRuntimeVersion)
                .resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME)
        ));
//        try {
//            NutsWorkspace updatedws = Nuts.openWorkspace(new String[]{
//                "--workspace", workpacePath});
//            Assert.assertFalse(true);
//        } catch (NutsUnsatisfiedRequirementsException e) {
//            Assert.assertTrue(true);
//        }
        NutsBootWorkspace b = new NutsBootWorkspace(
                "--workspace", workspacePath,
                "--boot-version=" + newApiVersion,
                "--bot",
                "--color=never",
                "--skip-companions",
                "--version",
                "--json"
        );
        TestUtils.println(uws.commandLine().create(b.createProcessCommandLine()).toString());

        String ss = uws.exec().userCmd().addCommand(b.createProcessCommandLine()).grabOutputString().run().getOutputString();
        TestUtils.println("================");
        TestUtils.println(ss);
        Map m = uws.formats().json().parse(ss, Map.class);
        Assert.assertEquals(newApiVersion, m.get("nuts-api-version"));
        Assert.assertEquals(newRuntimeVersion, m.get("nuts-runtime-version"));
    }

    private Path replaceAPIJar(Path p, FromTo api, NutsWorkspace ws) {
        try {
            Path zipFilePath = ws.io().tmp().createTempFile(".zip");
            Files.copy(p, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {

                Path fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts/pom.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(">" + api.from + "<", ">" + api.to + "<");
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/nuts/net.thevpc.nuts/nuts/nuts.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            Path jar = zipFilePath.resolveSibling("nuts-" + api.to + ".jar");
            Files.move(zipFilePath, jar, StandardCopyOption.REPLACE_EXISTING);
            return jar;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Path replaceRuntimeJar(Path p, FromTo api, FromTo impl, NutsWorkspace ws) {
        try {
            Path zipFilePath = ws.io().tmp().createTempFile(".zip");
            Files.copy(p, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {

                Path fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts-runtime/pom.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath))
                            .replace("project.version=" + impl.from, "project.version=" + impl.to);
                    if (!api.isIdentity()) {
                        ss = ss.replace("net.thevpc.nuts:nuts:" + api.from, "net.thevpc.nuts:nuts:" + api.to);
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts-runtime/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath))
                            .replace(">" + impl.from + "<", ">" + impl.to + "<");
                    if (!api.isIdentity()) {
                        ss = ss.replace(">" + api.from + "<", ">" + api.to + "<");
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                if (!api.isIdentity()) {
                    fileInsideZipPath = fs.getPath("/META-INF/nuts/net.thevpc.nuts/nuts-runtime/nuts.properties");
                    if (Files.exists(fileInsideZipPath)) {
                        String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                        Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }

            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            Path jar = zipFilePath.resolveSibling("nuts-" + api.to + ".jar");
            Files.move(zipFilePath, jar, StandardCopyOption.REPLACE_EXISTING);
            return jar;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

    public static class FromTo {

        String from;
        String to;

        public FromTo(String from, String to) {
            this.from = from;
            this.to = to;
        }

        public boolean isIdentity() {
            return from.equals(to);
        }
    }

}
