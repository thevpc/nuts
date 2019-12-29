/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.test.utils.TestUtils;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test06_UpateTest {

    private static String baseFolder;

    @Test
    public void testUpdateApi() throws Exception {
        testUpdate(false,TestUtils.getCallerMethodName());
    }

    @Test
    public void testUpdateImpl() throws Exception {
        testUpdate(true,TestUtils.getCallerMethodName());
    }

    private void testUpdate(boolean implOnly,String callerName) throws Exception {
        CoreIOUtils.delete(null,new File(baseFolder));
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
        NutsRepository updateRepo1 = uws.config().getRepository("local", false);
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
        nws.config().addRepository(new NutsCreateRepositoryOptions().setTemporary(true).setName("temp").setLocation(updateRepoPath)
                .setConfig(new NutsRepositoryConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE))
        );
        nws.info().showRepositories().println();
        TestUtils.println("\n------------------------------------------");

        NutsRepository r = nws.config().getRepository("temp", false);
        NutsDefinition api = nws.fetch().content().nutsApi().getResultDefinition();
        NutsDefinition rt = nws.fetch().content().nutsRuntime().getResultDefinition();

        NutsVersion apiv1 = api.getId().getVersion();
        NutsVersion apiv2 = implOnly?apiv1:apiv1.inc(-1, 10);
        FromTo fromToAPI = new FromTo(apiv1.toString(), apiv2.toString());

        NutsVersion rtv2 = rt.getId().getVersion().inc(-1, 10);
        FromTo fromToImpl = new FromTo(rt.getId().getVersion().toString(), rtv2.toString());

        if(!fromToAPI.isIdentity()) {
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
        TestUtils.println(uws.config().getRepository("local", false).config().getStoreLocationStrategy());
        TestUtils.println(uws.config().getRepository("local", false).config().getStoreLocation());
        TestUtils.println(uws.config().getRepository("local", false).config().getStoreLocation(NutsStoreLocation.LIB));

        TestUtils.println("[TEMP]");
        TestUtils.println(nws.config().getRepository("temp", false).config().getStoreLocationStrategy());
        TestUtils.println(nws.config().getRepository("temp", false).config().getStoreLocation());
        TestUtils.println(nws.config().getRepository("temp", false).config().getStoreLocation(NutsStoreLocation.LIB));
        Assert.assertEquals(
                uws.config().getRepository("local", false).config().getStoreLocation(NutsStoreLocation.LIB),
                nws.config().getRepository("temp", false).config().getStoreLocation(NutsStoreLocation.LIB));

        TestUtils.println(uws.search().id(api.getId().getShortNameId()).getResultIds().list());
        TestUtils.println(uws.search().id(rt.getId().getShortNameId()).getResultIds().list());
        Assert.assertEquals(implOnly?1:2, uws.search().id(api.getId().getShortNameId()).getResultIds().list().size());
        Assert.assertEquals(2, uws.search().id(rt.getId().getShortNameId()).getResultIds().list().size());
        TestUtils.println("========================");
        TestUtils.println(nws.search().id(api.getId().getShortNameId()).setRepository("temp").getResultIds().list());
        TestUtils.println(nws.search().id(rt.getId().getShortNameId()).setRepository("temp").getResultIds().list());
        TestUtils.println(nws.search().id(api.getId().getShortNameId()).getResultIds().list());
        TestUtils.println(nws.search().id(rt.getId().getShortNameId()).getResultIds().list());

        //check updates!
        NutsUpdateCommand foundUpdates = nws.update().all().checkUpdates();
        for (NutsUpdateResult u : foundUpdates.getResult().getAllUpdates()) {
            TestUtils.println(u.getAvailable());
        }
        Assert.assertEquals(implOnly?1:2, foundUpdates.getResultCount());
        foundUpdates.update();

        final String newApiVersion = foundUpdates.getResult().getApi().getAvailable().getId().getVersion().toString();
        final String newRuntimeVersion = foundUpdates.getResult().getRuntime().getAvailable().getId().getVersion().toString();
//        Path bootFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
//        Path bootCompFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
        Path bootCacheFolder=nws.config().getStoreLocation(NutsStoreLocation.CACHE).resolve(NutsConstants.Folders.ID);
        Path libFolder=nws.config().getStoreLocation(NutsStoreLocation.LIB).resolve(NutsConstants.Folders.ID);
        Path configFolder=nws.config().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.ID);
        Assert.assertTrue(Files.exists(libFolder.resolve("net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".jar")
        ));
        Assert.assertTrue(Files.exists(configFolder.resolve("net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME)
        ));

        Assert.assertTrue(Files.exists(bootCacheFolder.resolve("net/vpc/app/nuts/nuts-core/").resolve(newRuntimeVersion)
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
                "--boot-version="+newApiVersion,
                "--color=never",
                "--skip-companions",
                "--version",
                "--json"
        );
        TestUtils.println(uws.commandLine().create(b.createProcessCommandLine()).toString());

        String ss = uws.exec().usrCmd().command(b.createProcessCommandLine()).grabOutputString().run().getOutputString();
        TestUtils.println("================");
        TestUtils.println(ss);
        Map m = uws.json().parse(ss, Map.class);
        Assert.assertEquals(newApiVersion,m.get("nuts-api-version"));
        Assert.assertEquals(newRuntimeVersion,m.get("nuts-runtime-version"));
    }

    public static class FromTo {

        String from;
        String to;

        public FromTo(String from, String to) {
            this.from = from;
            this.to = to;
        }
        public boolean isIdentity(){
            return from.equals(to);
        }
    }

    private Path replaceAPIJar(Path p, FromTo api, NutsWorkspace ws) {
        try {
            Path zipFilePath = ws.io().createTempFile(".zip");
            Files.copy(p, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {

                Path fileInsideZipPath = fs.getPath("/META-INF/maven/net.vpc.app.nuts/nuts/pom.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.vpc.app.nuts/nuts/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(">" + api.from + "<", ">" + api.to + "<");
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties");
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
            Path zipFilePath = ws.io().createTempFile(".zip");
            Files.copy(p, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {

                Path fileInsideZipPath = fs.getPath("/META-INF/maven/net.vpc.app.nuts/nuts-core/pom.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath))
                            .replace("project.version=" + impl.from, "project.version=" + impl.to);
                    if(!api.isIdentity()) {
                        ss = ss.replace("net.vpc.app.nuts:nuts:" + api.from, "net.vpc.app.nuts:nuts:" + api.to);
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.vpc.app.nuts/nuts-core/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath))
                            .replace(">" + impl.from + "<", ">" + impl.to + "<");
                    if(!api.isIdentity()) {
                        ss=ss.replace(">" + api.from + "<", ">" + api.to + "<");
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                if(!api.isIdentity()) {
                    fileInsideZipPath = fs.getPath("/META-INF/nuts/net.vpc.app.nuts/nuts-core/nuts.properties");
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

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        //CoreIOUtils.delete(null,new File(baseFolder));
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
