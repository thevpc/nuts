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
import net.thevpc.nuts.util.NIdUtils;
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
        NDefinition apiDef1;
        NId apiId1;
        NId apiId2;
        NId rtId1;
        NId rtId2;
        NDefinition rtDef1;

        FromTo apiFromTo;

        FromTo rtFromTo;
        String api = NConstants.Ids.NUTS_API;
        String rt = NConstants.Ids.NUTS_RUNTIME;
    }

    private void prepareCustomUpdateRepository(boolean implOnly, Data data, NSession session) {
        TestUtils.println("\n------------------------------------------");
//        uws.config().save();
        //NutsRepository updateRepo1 = uws.config().getRepository("local", session);


        data.apiId2 = data.apiDef1.getId().builder().setVersion(implOnly ? data.apiId1.getVersion() : data.apiId1.getVersion().inc(-1, 10)).build();
        data.apiFromTo = new FromTo(data.apiId1.getVersion().toString(), data.apiId2.getVersion().toString());

        if (!data.apiFromTo.isIdentity()) {
            Path path = replaceAPIJar(data.apiDef1.getContent().flatMap(NPath::toPath).get(session), data.apiFromTo, session);
            NPath.of(path, session)
                    .copyTo(NPath.of(data.updateRepoPath, session).resolve(NIdUtils.resolveJarPath(data.apiId2)));
            data.apiDef1.getDescriptor().builder().setId(data.apiId2).build()
                    .formatter(session)
                    .print(NPath.of(data.updateRepoPath, session).resolve(NIdUtils.resolveNutsDescriptorPath(data.apiId2)));
        }


        data.rtId2 = data.rtDef1.getId().builder().setVersion(
                (implOnly)? data.rtDef1.getId().getVersion().inc(-1, 10)
                        :data.rtDef1.getId().getVersion().inc(-2, 10).inc(-2, 10)
        ).build();

        data.rtFromTo = new FromTo(data.rtDef1.getId().getVersion().toString(), data.rtId2.getVersion().toString());

        if (!data.rtFromTo.isIdentity()) {
            replaceRuntimeJar(data.rtDef1.getContent().flatMap(NPath::toPath).get(session), data.apiFromTo, data.rtFromTo,
                    NPath.of(data.updateRepoPath, session).resolve(NIdUtils.resolveJarPath(data.rtId2)).toPath().get(),
                    session);
            data.rtDef1.getDescriptor()
                    .builder()
                    .setId(data.rtId2)
                    .replaceDependency(
                            x -> x.getSimpleName().equals(data.apiId1.getShortName()),
                            x -> x.builder().setVersion(data.apiId2.getVersion()).build()
                    )
                    .build()
                    .formatter(session)
                    .print(NPath.of(data.updateRepoPath, session).resolve(NIdUtils.resolveNutsDescriptorPath(data.rtId2)));

        }
    }

    private NSession prepareWorkspaceToUpdate(boolean implOnly, Data data) {
        TestUtils.println("\n------------------------------------------");
        NSession session = TestUtils.openNewTestWorkspace(
                "--standalone",
                "--standalone-repositories",
                "--yes",
                "--install-companions=false"
        );
        NRepositories repos = NRepositories.of(session);
        NPath tempRepo = NPath.ofTempFolder(session);
        data.updateRepoPath = tempRepo.toString();
        data.apiDef1 = NFetchCmd.ofNutsApi(session).setContent(true).getResultDefinition();
        data.apiId1 = data.apiDef1.getId().builder().setVersion(data.apiDef1.getId().getVersion()).build();
        data.rtDef1 = NFetchCmd.ofNutsRuntime(session).setContent(true).getResultDefinition();
        data.rtId1 = data.rtDef1.getId().builder().setVersion(data.rtDef1.getId().getVersion()).build();

        repos.addRepository(new NAddRepositoryOptions().setTemporary(true).setName("temp").setLocation(data.updateRepoPath)
                .setConfig(new NRepositoryConfig().setStoreStrategy(NStoreStrategy.STANDALONE))
        );
        TestUtils.println(repos.findRepository("temp").get().config().getStoreStrategy());
        TestUtils.println(repos.findRepository("temp").get().config().getStoreLocation());
        TestUtils.println(repos.findRepository("temp").get().config().getStoreLocation(NStoreType.LIB));
        NInfoCmd.of(session).configure(false, "--repos").setShowRepositories(true).println();

        Assertions.assertEquals(data.updateRepoPath,
                repos.findRepository("temp").get().config().getLocationPath().toString()
        );
        TestUtils.println(NSearchCmd.of(session).addId(data.api).getResultIds().toList());
        TestUtils.println(NSearchCmd.of(session).addId(data.rt).getResultIds().toList());
        List<NId> foundApis = NSearchCmd.of(session).addId(data.api).getResultIds().toList();
        List<NId> foundRts = NSearchCmd.of(session).addId(data.rt).getResultIds().toList();
        Assertions.assertTrue(foundApis.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(data.apiId1.getLongName()));
        Assertions.assertTrue(foundRts.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(data.rtId1.getLongName()));

        TestUtils.println(NSearchCmd.of(session).addId(data.api).setRepositoryFilter("temp").getResultIds().toList());
        TestUtils.println(NSearchCmd.of(session).addId(data.rt).setRepositoryFilter("temp").getResultIds().toList());
        TestUtils.println(NSearchCmd.of(session).addId(data.api).getResultIds().toList());
        TestUtils.println(NSearchCmd.of(session).addId(data.rt).getResultIds().toList());
        TestUtils.println("========================");

        return session;
    }

    private void testUpdate(boolean implOnly, String callerName) throws Exception {
//        CoreIOUtils.delete(null, new File(baseFolder));
//        final String workspacePath = baseFolder + "/" + callerName;
        Data data = new Data();
        NSession session = prepareWorkspaceToUpdate(implOnly, data);
        prepareCustomUpdateRepository(implOnly, data, session);

        List<NId> foundApis = NSearchCmd.of(session).addId(data.api).getResultIds().toList();
        List<NId> foundRts = NSearchCmd.of(session).addId(data.rt).getResultIds().toList();
        if (!implOnly) {
            Assertions.assertTrue(foundApis.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(data.apiId2.getLongName()));
        }
        Assertions.assertTrue(foundRts.stream().map(NId::getLongName).collect(Collectors.toSet()).contains(data.rtId2.getLongName()));

        //check updates!
        NUpdateCmd foundUpdates = NUpdateCmd.of(session)
                .setRepositoryFilter(NRepositoryFilters.of(session).byName("temp"))
                .setAll().checkUpdates();
        for (NUpdateResult u : foundUpdates.getResult().getUpdatable()) {
            TestUtils.println(u.getAvailable());
        }
        Assertions.assertEquals(implOnly ? 1 : 2, foundUpdates.getResultCount(), "checkUpdates result count is incorrect");
        foundUpdates.update();

        final String newApiVersion = implOnly?
                foundUpdates.getResult().getApi().getInstalled().getId().getVersion().toString():
                foundUpdates.getResult().getApi().getAvailable().getId().getVersion().toString()
                ;
        final String newRuntimeVersion = foundUpdates.getResult().getRuntime().getAvailable().getId().getVersion().toString();
//        Path bootFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
//        Path bootCompFolder=Paths.get(workspacePath).resolve(NutsConstants.Folders.BOOT);
        NLocations nwsLocations = NLocations.of(session);
        Path bootCacheFolder = (nwsLocations.getStoreLocation(NStoreType.CACHE)).resolve(NConstants.Folders.ID).toPath().get();
        Path libFolder = (nwsLocations.getStoreLocation(NStoreType.LIB)).resolve(NConstants.Folders.ID).toPath().get();
        Path configFolder = (nwsLocations.getStoreLocation(NStoreType.CONF)).resolve(NConstants.Folders.ID).toPath().get();
        Assertions.assertTrue(Files.exists(libFolder.resolve("net/thevpc/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".jar")
        ));

        NExecCmd ee = NExecCmd.of(session).setExecutionType(NExecutionType.SPAWN)
                .addCommand(
                        "nuts#" + newApiVersion,
                        "--workspace", session.getWorkspace().getLocation().toString(),
                        "--boot-version=" + newApiVersion,
                        "--bot",
                        "--color=never",
                        "--install-companions=false",
                        "--json",
                        "version"
                )
                .setFailFast(false)
                .grabAll()
                .setSleepMillis(5000);
        TestUtils.println(ee.formatter().format().filteredText());
        ee.run();

        String ss = ee.getGrabbedOutString();
        TestUtils.println("================");
        TestUtils.println("OUT =" + ss);
        TestUtils.println("ERR =" + ee.getGrabbedErrString());
        TestUtils.println("CODE=" + ee.getResultCode());
        Assertions.assertEquals(0, ee.getResultCode());

        Map m = NElements.of(session).json().parse(ss, Map.class);
        Assertions.assertEquals(newApiVersion, m.get("nuts-api-version"));
        Assertions.assertEquals(newRuntimeVersion, m.get("nuts-runtime-version"));
    }

    private Path replaceAPIJar(Path p, FromTo api, NSession session) {
        try {
            Path zipFilePath = NPath.ofTempFile(".zip", session).toPath().get();
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

    private Path replaceRuntimeJar(Path p, FromTo api, FromTo rt, Path to,NSession session) {
        try {
            Path zipFilePath = NPath.ofTempFile(p.getFileName().toString(), session).toPath().get();
            Files.copy(p, zipFilePath, StandardCopyOption.REPLACE_EXISTING);
            try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {

                Path fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts-runtime/pom.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String xml = new String(Files.readAllBytes(fileInsideZipPath));
                    String ss = xml
                            .replace("project.version=" + rt.from, "project.version=" + rt.to);
                    if (!api.isIdentity()) {
                        ss = ss.replace(NConstants.Ids.NUTS_API+":" + api.from, NConstants.Ids.NUTS_API+":" + api.to);
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.thevpc.nuts/nuts-runtime/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String xml = new String(Files.readAllBytes(fileInsideZipPath));
                    String ss = xml
                            .replace(">" + rt.from + "<", ">" + rt.to + "<");
                    if (!api.isIdentity()) {
                        ss = ss.replace(">" + api.from + "<", ">" + api.to + "<");
                    }
//                    Files.write(fileInsideZipPath, ss.getBytes());
                    Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                if (!api.isIdentity()) {
                    fileInsideZipPath = fs.getPath("/META-INF/nuts/net.thevpc.nuts/nuts-runtime/nuts.properties");
                    if (Files.exists(fileInsideZipPath)) {
                        String xml = new String(Files.readAllBytes(fileInsideZipPath));
                        String ss = xml.replace(api.from, api.to);
//                    Files.write(fileInsideZipPath, ss.getBytes());
                        Files.copy(new ByteArrayInputStream(ss.getBytes()), fileInsideZipPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }

            } catch (IOException ex) {
                throw new NIOException(session, ex);
            }
            Files.createDirectories(to.getParent());
            Files.move(zipFilePath, to, StandardCopyOption.REPLACE_EXISTING);
            return to;
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
