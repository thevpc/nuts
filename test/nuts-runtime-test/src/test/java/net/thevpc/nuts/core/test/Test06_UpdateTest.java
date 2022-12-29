/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NPaths;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class Test06_UpdateTest {

    private static String baseFolder;

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getPath();
        CoreIOUtils.delete(null, new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() {
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

    private static class Data {
        String updateRepoPath;
        NDefinition apiDef;
        NId apiId1;
        NId apiId2;
        NId rtId1;
        NId rtId2;
        NDefinition rtDef;

        NVersion apiVersion1;
        NVersion apiVersion2;
        FromTo fromToAPI;

        NVersion rtVersion1;
        NVersion rtVersion2;
        FromTo fromToImpl;
    }

    private NSession prepareCustomUpdateRepository(boolean implOnly, Data data) {
        TestUtils.println("\n------------------------------------------");
        NSession uws = TestUtils.openNewTestWorkspace(
                "--archetype", "minimal",
                "--standalone",
                "--standalone-repositories",
                "--yes",
                "--skip-companions"
        );
        NRepository updateRepo1 = uws.repos().addRepository("local");
//        uws.config().save();
        //NutsRepository updateRepo1 = uws.config().getRepository("local", session);
        data.updateRepoPath = updateRepo1.config().getStoreLocation().toString();
        TestUtils.println(updateRepo1.config().getStoreLocationStrategy());
        uws.info().configure(false, "--repos").println();

        data.apiDef = uws.fetch().setContent(true).setNutsApi().getResultDefinition();
        data.rtDef = uws.fetch().setContent(true).setNutsRuntime().getResultDefinition();
        data.apiVersion1 = data.apiDef.getId().getVersion();
        data.rtVersion1 = data.rtDef.getId().getVersion();
        data.apiId1 = data.apiDef.getId().builder().setVersion(data.apiVersion1).build();

        data.apiVersion2 = implOnly ? data.apiVersion1 : data.apiVersion1.inc(-1, 10);
        data.apiId2 = data.apiDef.getId().builder().setVersion(data.apiVersion2).build();

        data.fromToAPI = new FromTo(data.apiVersion1.toString(), data.apiVersion2.toString());

        data.rtVersion2 = data.rtDef.getId().getVersion().inc(-2, 10);
        data.rtId1 = data.rtDef.getId().builder().setVersion(data.rtVersion1).build();
        data.rtId2 = data.rtDef.getId().builder().setVersion(data.rtVersion2).build();

        data.fromToImpl = new FromTo(data.rtDef.getId().getVersion().toString(), data.rtVersion2.toString());

        if (!data.fromToAPI.isIdentity()) {
            uws.deploy()
                    .setContent(replaceAPIJar(data.apiDef.getContent().map(NPath::toFile).get(uws), data.fromToAPI, uws))
                    .setDescriptor(data.apiDef.getDescriptor().builder().setId(data.apiDef.getId().builder().setVersion(data.apiVersion2).build()).build())
                    //                        .setRepository("local")
                    .run();
        }
        uws.deploy()
                .setContent(replaceRuntimeJar(data.rtDef.getContent().map(NPath::toFile).get(uws), data.fromToAPI, data.fromToImpl, uws))
                .setDescriptor(
                        data.rtDef.getDescriptor()
                                .builder()
                                .setId(data.rtDef.getId().builder().setVersion(data.rtVersion2).build())
                                .replaceDependency(
                                        x -> x.getSimpleName().equals(data.apiDef.getId().getShortName()),
                                        x -> x.builder().setVersion(data.apiVersion2).build()
                                )
                                .build()
                )
                .run();
        TestUtils.println(uws.repos().getRepository("local").config().getStoreLocationStrategy());
        TestUtils.println(uws.repos().getRepository("local").config().getStoreLocation());
        TestUtils.println(uws.repos().getRepository("local").config().getStoreLocation(NStoreLocation.LIB));

        String api = "net.thevpc.nuts:nuts";
        String rt = "net.thevpc.nuts:nuts-runtime";
        TestUtils.println(uws.search().addId(api).getResultIds().toList());
        TestUtils.println(uws.search().addId(rt).getResultIds().toList());
        List<NId> foundApis = uws.search().addId(api).getResultIds().toList();
        List<NId> foundRts = uws.search().addId(rt).getResultIds().toList();
        Assertions.assertTrue(foundApis.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(data.apiId1.getLongName()));
        if (!implOnly) {
            Assertions.assertTrue(foundApis.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(data.apiId2.getLongName()));
        }

        return uws;
    }

    private NSession prepareWorkspaceToUpdate(boolean implOnly, Data d) {
        TestUtils.println("\n------------------------------------------");
        NSession nws = TestUtils.openNewTestWorkspace(
                "--standalone",
                "--standalone-repositories",
                "--yes",
                "--skip-companions"
        );
        nws.repos().addRepository(new NAddRepositoryOptions().setTemporary(true).setName("temp").setLocation(d.updateRepoPath)
                .setConfig(new NRepositoryConfig().setStoreLocationStrategy(NStoreLocationStrategy.STANDALONE))
        );
        TestUtils.println(nws.repos().getRepository("temp").config().getStoreLocationStrategy());
        TestUtils.println(nws.repos().getRepository("temp").config().getStoreLocation());
        TestUtils.println(nws.repos().getRepository("temp").config().getStoreLocation(NStoreLocation.LIB));
        nws.info().configure(false, "--repos").setShowRepositories(true).println();

        Assertions.assertEquals(d.updateRepoPath,
                nws.repos().getRepository("temp").config().getLocationPath().toString()
        );
        String api = "net.thevpc.nuts:nuts";
        String rt = "net.thevpc.nuts:nuts-runtime";
        TestUtils.println(nws.search().addId(api).getResultIds().toList());
        TestUtils.println(nws.search().addId(rt).getResultIds().toList());
        List<NId> foundApis = nws.search().addId(api).getResultIds().toList();
        List<NId> foundRts = nws.search().addId(rt).getResultIds().toList();
        Assertions.assertTrue(foundApis.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(d.apiId1.getLongName()));
        if (!implOnly) {
            Assertions.assertTrue(foundApis.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(d.apiId2.getLongName()));
        }
        Assertions.assertTrue(foundRts.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(d.rtId1.getLongName()));
        Assertions.assertTrue(foundRts.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(d.rtId2.getLongName()));

        TestUtils.println(nws.search().addId(api).setRepositoryFilter("temp").getResultIds().toList());
        TestUtils.println(nws.search().addId(rt).setRepositoryFilter("temp").getResultIds().toList());
        TestUtils.println(nws.search().addId(api).getResultIds().toList());
        TestUtils.println(nws.search().addId(rt).getResultIds().toList());
        TestUtils.println("========================");

        return nws;
    }

    private void testUpdate(boolean implOnly, String callerName) throws Exception {
//        CoreIOUtils.delete(null, new File(baseFolder));
//        final String workspacePath = baseFolder + "/" + callerName;
        Data d = new Data();
        NSession uws = prepareCustomUpdateRepository(implOnly, d);
        NSession nws = prepareWorkspaceToUpdate(implOnly, d);

        //check updates!
        NUpdateCommand foundUpdates = nws.update().setAll().checkUpdates();
        for (NUpdateResult u : foundUpdates.getResult().getUpdatable()) {
            TestUtils.println(u.getAvailable());
        }
        Assertions.assertEquals(implOnly ? 1 : 2, foundUpdates.getResultCount(), "checkUpdates result count is incorrect");
        foundUpdates.update();

        final String newApiVersion = foundUpdates.getResult().getApi().getAvailable().getId().getVersion().toString();
        final String newRuntimeVersion = foundUpdates.getResult().getRuntime().getAvailable().getId().getVersion().toString();
//        Path bootFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
//        Path bootCompFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
        Path bootCacheFolder = (nws.locations().getStoreLocation(NStoreLocation.CACHE)).resolve(NConstants.Folders.ID).toFile();
        Path libFolder = (nws.locations().getStoreLocation(NStoreLocation.LIB)).resolve(NConstants.Folders.ID).toFile();
        Path configFolder = (nws.locations().getStoreLocation(NStoreLocation.CONFIG)).resolve(NConstants.Folders.ID).toFile();
        Assertions.assertTrue(Files.exists(libFolder.resolve("net/thevpc/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".jar")
        ));

        NExecCommand ee = uws.exec().setExecutionType(NExecutionType.SPAWN)
                .addCommand(
                        "nuts#" + newApiVersion,
                        "--workspace", nws.getWorkspace().getLocation().toString(),
                        "--boot-version=" + newApiVersion,
                        "--bot",
                        "--color=never",
                        "--skip-companions",
                        "--json",
                        "version"
                )
                .setFailFast(false)
                .grabOutputString()
                .grabErrorString()
                .setSleepMillis(5000);
        TestUtils.println(ee.formatter().format().filteredText());
        ee.run();

        String ss = ee.getOutputString();
        TestUtils.println("================");
        TestUtils.println("OUT =" + ss);
        TestUtils.println("ERR =" + ee.getErrorString());
        TestUtils.println("CODE=" + ee.getResult());
        Assertions.assertEquals(0,ee.getResult());

        Map m = NElements.of(uws).json().parse(ss, Map.class);
        Assertions.assertEquals(newApiVersion, m.get("nuts-api-version"));
        Assertions.assertEquals(newRuntimeVersion, m.get("nuts-runtime-version"));
    }

    private Path replaceAPIJar(Path p, FromTo api, NSession session) {
        try {
            Path zipFilePath = NPaths.of(session)
                    .createTempFile(".zip").toFile();
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
                throw new NIOException(session, ex);
            }
            Path jar = zipFilePath.resolveSibling("nuts-" + api.to + ".jar");
            Files.move(zipFilePath, jar, StandardCopyOption.REPLACE_EXISTING);
            return jar;
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    private Path replaceRuntimeJar(Path p, FromTo api, FromTo impl, NSession session) {
        try {
            Path zipFilePath = NPaths.of(session)
                    .createTempFile(".zip").toFile();
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
                throw new NIOException(session, ex);
            }
            Path jar = zipFilePath.resolveSibling("nuts-" + api.to + ".jar");
            Files.move(zipFilePath, jar, StandardCopyOption.REPLACE_EXISTING);
            return jar;
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent() == NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
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
