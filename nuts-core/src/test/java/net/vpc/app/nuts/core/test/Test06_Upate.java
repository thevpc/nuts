/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsBootWorkspace;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsCreateRepositoryOptions;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsMinimalCommandLine;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryConfig;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsStoreLocationStrategy;
import net.vpc.app.nuts.NutsUnsatisfiedRequirementsExeption;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.io.ProcessBuilder2;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import net.vpc.app.nuts.NutsUpdateCommand;

/**
 *
 * @author vpc
 */
public class Test06_Upate {

    private static String baseFolder;

    @Test
    public void testUpdate() throws Exception {
        CoreIOUtils.delete(new File(baseFolder));
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        final String workpacePath = baseFolder + "/" + TestUtils.getCallerMethodName();

        NutsWorkspace uws = Nuts.openWorkspace(
                "--workspace", workpacePath + "-update",
                "--archetype", "minimal",
                "--standalone",
                "--yes",
                "--skip-install-companions"
        );
        NutsRepository updateRepo1 = uws.config().getRepository("local");
        String updateRepoPath = updateRepo1.config().getStoreLocation().toString();
        System.out.println(updateRepo1.config().getStoreLocationStrategy());
        uws.formatter().createWorkspaceInfoFormat().println();
        System.out.println("\n------------------------------------------");
        NutsWorkspace nws = Nuts.openWorkspace(
                "--workspace", workpacePath,
                "--standalone",
                "--yes",
                "--skip-install-companions"
        );
        nws.config().addRepository(new NutsCreateRepositoryOptions().setTemporay(true).setName("temp").setLocation(updateRepoPath)
                .setConfig(new NutsRepositoryConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE))
        );
        nws.formatter().createWorkspaceInfoFormat().println();
        System.out.println("\n------------------------------------------");

        NutsRepository r = nws.config().getRepository("temp");
        NutsDefinition api = nws.fetch().nutsApi().getResultDefinition();
        NutsDefinition rt = nws.fetch().nutsRuntime().getResultDefinition();

        NutsVersion apiv2 = api.getId().getVersion().inc(3, 10);
        NutsVersion rtv2 = rt.getId().getVersion().inc(3, 10);
        FromTo fromToAPI = new FromTo(api.getId().getVersion().toString(), apiv2.toString());
        FromTo fromToImpl = new FromTo(rt.getId().getVersion().toString(), rtv2.toString());

        uws.deploy()
                .setContent(replaceAPIJar(api.getPath(), fromToAPI, uws))
                .setDescriptor(api.getDescriptor().setId(api.getId().setVersion(apiv2)))
                //                        .setRepository("local")
                .run();

        uws.deploy()
                .setContent(replaceRuntimeJar(rt.getPath(), fromToAPI, fromToImpl, uws))
                .setDescriptor(
                        rt.getDescriptor().setId(rt.getId().setVersion(rtv2))
                                .replaceDependency(
                                        x -> x.getSimpleName().equals(api.getId().getSimpleName()),
                                        x -> x.setVersion(apiv2)
                                )
                )
                .run();

        System.out.println("[LOCAL]");
        System.out.println(uws.config().getRepository("local").config().getStoreLocationStrategy());
        System.out.println(uws.config().getRepository("local").config().getStoreLocation());
        System.out.println(uws.config().getRepository("local").config().getStoreLocation(NutsStoreLocation.LIB));

        System.out.println("[TEMP]");
        System.out.println(nws.config().getRepository("temp").config().getStoreLocationStrategy());
        System.out.println(nws.config().getRepository("temp").config().getStoreLocation());
        System.out.println(nws.config().getRepository("temp").config().getStoreLocation(NutsStoreLocation.LIB));
        Assert.assertEquals(
                uws.config().getRepository("local").config().getStoreLocation(NutsStoreLocation.LIB),
                nws.config().getRepository("temp").config().getStoreLocation(NutsStoreLocation.LIB));

        System.out.println(uws.find().id(api.getId().getSimpleNameId()).getResultIds());
        System.out.println(uws.find().id(rt.getId().getSimpleNameId()).getResultIds());
       Assert.assertEquals(1, uws.find().id(api.getId().getSimpleNameId()).getResultIds().list().size());
        Assert.assertEquals(1, uws.find().id(rt.getId().getSimpleNameId()).getResultIds().list().size());
        System.out.println("========================");
        System.out.println(nws.find().id(api.getId().getSimpleNameId()).setRepository("temp").getResultIds());
        System.out.println(nws.find().id(rt.getId().getSimpleNameId()).setRepository("temp").getResultIds());
        System.out.println(nws.find().id(api.getId().getSimpleNameId()).getResultIds());
        System.out.println(nws.find().id(rt.getId().getSimpleNameId()).getResultIds());

        //check updates!
        NutsUpdateCommand foundUpdates = nws.update().all().checkUpdates();

        Assert.assertEquals(2, foundUpdates == null ? 0 : foundUpdates.getResultCount());
        foundUpdates.update();

        final String newApiVersion = foundUpdates.getResult().getApi().getAvailable().getId().getVersion().toString();
        final String newRuntimeVersion = foundUpdates.getResult().getRuntime().getAvailable().getId().getVersion().toString();
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve(NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".jar")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve(NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts-" + newApiVersion + ".nuts")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve(NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts/").resolve(newApiVersion)
                .resolve("nuts.properties")
        ));

        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve(NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts-core/").resolve(newRuntimeVersion)
                .resolve("nuts-core-" + newRuntimeVersion + ".jar")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve(NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts-core/").resolve(newRuntimeVersion)
                .resolve("nuts-core-" + newRuntimeVersion + ".nuts")
        ));
        Assert.assertEquals(true, Files.exists(Paths.get(workpacePath).resolve(NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts-core/").resolve(newRuntimeVersion)
                .resolve("nuts.properties")
        ));

        try {
            NutsWorkspace updatedws = Nuts.openWorkspace(new String[]{
                "--workspace", workpacePath});
            Assert.assertFalse(true);
        } catch (NutsUnsatisfiedRequirementsExeption e) {
            Assert.assertTrue(true);
        }
        NutsBootWorkspace b = new NutsBootWorkspace(
                "--workspace", workpacePath,
                "--color=never",
                "--version",
                "--min",
                "--json"
        );
        System.out.println(NutsMinimalCommandLine.escapeArguments(b.createProcessCommandLine()));
        ProcessBuilder2 cb = new ProcessBuilder2().setCommand(b.createProcessCommandLine());
        String ss = cb.setRedirectErrorStream().grabOutputString().start().waitFor().getOutputString();
        System.out.println("================");
        System.out.println(ss);
        Assert.assertTrue(true);
    }

    public static class FromTo {

        String from;
        String to;

        public FromTo(String from, String to) {
            this.from = from;
            this.to = to;
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
                    Files.write(fileInsideZipPath, ss.getBytes());
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.vpc.app.nuts/nuts/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(">" + api.from + "<", ">" + api.to + "<");
                    Files.write(fileInsideZipPath, ss.getBytes());
                }

                fileInsideZipPath = fs.getPath("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
                    Files.write(fileInsideZipPath, ss.getBytes());
                }

            } catch (IOException ex) {
                // TODO Auto-generated catch block
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
                            .replace("project.version=" + impl.from, "project.version=" + impl.to)
                            .replace("net.vpc.app.nuts:nuts:" + api.from, "net.vpc.app.nuts:nuts:" + api.to);
                    Files.write(fileInsideZipPath, ss.getBytes());
                }

                fileInsideZipPath = fs.getPath("/META-INF/maven/net.vpc.app.nuts/nuts-core/pom.xml");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath))
                            .replace(">" + api.from + "<", ">" + api.to + "<")
                            .replace(">" + impl.from + "<", ">" + impl.to + "<");
                    Files.write(fileInsideZipPath, ss.getBytes());
                }

                fileInsideZipPath = fs.getPath("/META-INF/nuts/net.vpc.app.nuts/nuts-core/nuts.properties");
                if (Files.exists(fileInsideZipPath)) {
                    String ss = new String(Files.readAllBytes(fileInsideZipPath)).replace(api.from, api.to);
                    Files.write(fileInsideZipPath, ss.getBytes());
                }

            } catch (IOException ex) {
                // TODO Auto-generated catch block
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
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(CorePlatformUtils.getPlatformOsFamily().equals("linux"));
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
