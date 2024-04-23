/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.io.NPath;

import java.util.Arrays;

/**
 * @author vpc
 */
public class InstallerRunner extends AbstractRunner {

    NamedStringParam NUTS_JAVA_HOME = NParams.ofString("NUTS_JAVA_HOME", null);
    NamedStringParam NUTS_INSTALLER_BUILD_JAVA_HOME = NParams.ofString("NUTS_INSTALLER_BUILD_JAVA_HOME", null);
    NamedStringParam NUTS_GRAALVM_DIR = NParams.ofString("NUTS_GRAALVM_DIR", null);

    NamedStringParam INSTALLER_JRE8_LINUX64 = NParams.ofString("INSTALLER_JRE8_LINUX64", null);
    NamedStringParam INSTALLER_JRE8_LINUX32 = NParams.ofString("INSTALLER_JRE8_LINUX32", null);
    NamedStringParam INSTALLER_JRE8_WINDOWS64 = NParams.ofString("INSTALLER_JRE8_WINDOWS64", null);
    NamedStringParam INSTALLER_JRE8_WINDOWS32 = NParams.ofString("INSTALLER_JRE8_WINDOWS32", null);
    NamedStringParam INSTALLER_JRE8_MAC64 = NParams.ofString("INSTALLER_JRE8_MAC64", null);
    boolean buildNative = false;
    boolean buildInstaller = false;
    boolean buildNuts = false;

    @Override
    public void configureAfterOptions() {
        context().NUTS_WEBSITE_BASE = context().root.resolve("documentation/website");
        NUTS_JAVA_HOME.update(context()).ensureDirectory(session);
        NUTS_INSTALLER_BUILD_JAVA_HOME.update(context()).ensureDirectory(session);
        NUTS_GRAALVM_DIR.update(context()).ensureDirectory(session);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "--native": {
                cmdLine.withNextFlag((v, a, s) -> buildNative = v);
                return true;
            }
            case "build-installer": {
                cmdLine.withNextFlag((v, a, s) -> {
                    buildInstaller = v;
                    buildNuts = v;
                });
                return true;
            }
        }
        return false;
    }

    public InstallerRunner(NSession session) {
        super(session);
    }

    @Override
    public void configureBeforeOptions(NCmdLine cmdLine) {

    }

    @Override
    public void run() {
        NativeBuilder r = new NativeBuilder(session);
        r.setJpackageHome(NUTS_INSTALLER_BUILD_JAVA_HOME.update(context()).getValue());
        r.setGraalvmHome(NUTS_GRAALVM_DIR.update(context()).getValue());
        r.setJre8Linux64(INSTALLER_JRE8_LINUX64.update(context()).getValue());
        r.setJre8Linux32(INSTALLER_JRE8_LINUX32.update(context()).getValue());
        r.setJre8Windows64(INSTALLER_JRE8_WINDOWS64.update(context()).getValue());
        r.setJre8Windows32(INSTALLER_JRE8_WINDOWS32.update(context()).getValue());
        r.setJre8Mac64(INSTALLER_JRE8_MAC64.update(context()).getValue());
        r.setVendor("thevpc");
        r.setCopyright("(c) 2024 thevpc");
        r.setIcons(Arrays.asList(context().root.resolve("documentation/media/nuts-icon.icns")));

        NPath sharedDistFolder = context().root.resolve("installers/nuts-release-tool").resolve("dist");
        if (buildInstaller) {
            r.setSupported(NativeBuilder.PackageType.PORTABLE);
            if(buildNative){
                r.addSupported(NativeBuilder.PackageType.NATIVE,NativeBuilder.PackageType.BIN,NativeBuilder.PackageType.JRE_BUNDLE);
            }
            r.setMainClass("net.thevpc.nuts.installer.NutsInstaller");
            r.setProjectFolder(context().root.resolve("installers/nuts-installer"), null, null);
            r.setDist(sharedDistFolder);
            r.setProfilingArgs(new String[0]);
            r.build();
            if (context().publish) {
                for (NPath nPath : r.getGeneratedFiles()) {
                    upload(nPath,remoteTheVpcNuts().resolve(nPath.getName()).toString());
                }
            }
        }

        if (buildNuts) {
            r.setSupported(NativeBuilder.PackageType.PORTABLE);
            if(buildNative){
                r.addSupported(NativeBuilder.PackageType.NATIVE,NativeBuilder.PackageType.BIN,NativeBuilder.PackageType.JRE_BUNDLE);
            }
            r.setMainClass("net.thevpc.nuts.Nuts");
            r.setProjectFolder(context().root.resolve("installers/nuts-portable"), null, "nuts-portable-$version-shaded.jar");
            r.setDist(sharedDistFolder);
            r.setProfilingArgs(new String[]{"--sandbox","--verbose"});
            r.build();
            if (context().publish) {
                for (NPath nPath : r.getGeneratedFiles()) {
                    upload(nPath,remoteTheVpcNuts().resolve(nPath.getName()).toString());
                }
            }
        }

    }


}
