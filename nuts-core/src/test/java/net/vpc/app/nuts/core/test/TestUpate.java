/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test;

import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsCreateRepositoryOptions;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryConfig;
import net.vpc.app.nuts.NutsStoreLocationStrategy;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import net.vpc.common.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class TestUpate {

    private static String baseFolder;
    private static String workpacePath;

    @Test
    public void customLayout() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace uws = Nuts.openWorkspace(new String[]{
            "--workspace", workpacePath + "_update",
            "--archetype", "minimal",
            "--standalone",
            "--yes",
            "--skip-install-companions"
        });
        
        String updateRepo = uws.repositories().getRepository("local").config().getStoreLocation().toString();
        uws.formatter().createWorkspaceInfoFormat().format(System.out);
        System.out.println("------------------------------------------");
        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", workpacePath,
            "--standalone",
//            "--verbose",
            "--yes",
            "--skip-install-companions"
        });
        ws.repositories().addRepository(new NutsCreateRepositoryOptions().setTemporay(true).setName("temp").setLocation(updateRepo)
                .setConfig(new NutsRepositoryConfig().setStoreLocationStrategy(NutsStoreLocationStrategy.STANDALONE))
        );
        ws.formatter().createWorkspaceInfoFormat().format(System.out);
        for (NutsRepository repository : ws.repositories().getRepositories()) {
            System.out.println(repository.isEnabled() + ":" + repository.getName() + " : " + repository.config().getStoreLocation());
            System.out.println("\t\t" + repository.config().getLocation(true));
        }
        NutsRepository r = ws.repositories().getRepository("temp");
        NutsDefinition api = ws.fetchApiDefinition(null);
        NutsDefinition rt = ws.fetchRuntimeDefinition(null);

        NutsVersion apiv2 = api.getId().getVersion().inc(0, 10);
        NutsVersion rtv2 = rt.getId().getVersion().inc(0, 10);

        uws.deploy(
                uws.createDeploymentBuilder()
                        .setContent(api.getContent().getPath())
                        .setDescriptor(api.getDescriptor().setId(api.getId().setVersion(apiv2)))
                        //                        .setRepository("local")
                        .build(), null);

        uws.deploy(
                uws.createDeploymentBuilder()
                        .setContent(rt.getContent().getPath())
                        .setDescriptor(
                                rt.getDescriptor().setId(rt.getId().setVersion(rtv2))
                                        .replaceDependency(
                                                x -> x.getSimpleName().equals(api.getId().getSimpleName()),
                                                x -> x.setVersion(apiv2)
                                        )
                        )
                        .build(), null);
        System.out.println(uws.createQuery().addId(api.getId().getSimpleNameId()).find());
        System.out.println(uws.createQuery().addId(rt.getId().getSimpleNameId()).find());
        org.junit.Assert.assertEquals(1, uws.createQuery().addId(api.getId().getSimpleNameId()).find().size()==1);
        org.junit.Assert.assertEquals(1, uws.createQuery().addId(rt.getId().getSimpleNameId()).find().size()==1);
        
        System.out.println(ws.createQuery().addId(api.getId().getSimpleNameId()).setRepositoryFilter("temp").find());
        System.out.println(ws.createQuery().addId(rt.getId().getSimpleNameId()).setRepositoryFilter("temp").find());
        ws.checkWorkspaceUpdates(null, null);
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        String test_id = TestUpate.class.getSimpleName();
        baseFolder = new File("./runtime/test/" + test_id).getCanonicalFile().getPath();
        workpacePath = baseFolder + "/default-workspace";
        IOUtils.delete(new File(baseFolder));
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
