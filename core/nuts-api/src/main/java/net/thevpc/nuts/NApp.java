package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.env.NStoreType;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface NApp extends NComponent {
    static NApp of() {
        return NExtensions.of(NApp.class);
    }

    void prepare(NAppInitInfo appInitInfo);

    NApp copy();

    NApp copyFrom(NApp other);

    NOptional<NId> getId();

    NApplicationMode getMode();

    List<String> getModeArguments();

    NCmdLineAutoComplete getAutoComplete();

    NOptional<NText> getHelpText();

    void printHelp();

    Class<?> getAppClass();

    NPath getBinFolder();

    NPath getConfFolder();

    NPath getLogFolder();

    NPath getTempFolder();

    NPath getVarFolder();

    NPath getLibFolder();

    NPath getRunFolder();

    NPath getCacheFolder();

    NPath getVersionFolder(NStoreType location, String version);

    NPath getSharedAppsFolder();

    NPath getSharedConfFolder();

    NPath getSharedLogFolder();

    NPath getSharedTempFolder();

    NPath getSharedVarFolder();

    NPath getSharedLibFolder();

    NPath getSharedRunFolder();

    NPath getSharedFolder(NStoreType location);

    NOptional<NVersion> getVersion();

    List<String> getArguments();

    NClock getStartTime();

    NOptional<NVersion> getPreviousVersion();

    NCmdLine getCmdLine();

    void processCmdLine(NCmdLineRunner commandLineRunner);

    NPath getFolder(NStoreType location);

    boolean isExecMode();

    NAppStoreLocationResolver getStoreLocationResolver();

    NApp setFolder(NStoreType location, NPath folder);

    NApp setSharedFolder(NStoreType location, NPath folder);

    NApp setId(NId appId);

    NApp setArguments(List<String> args);

    NApp setArguments(String[] args);

    NApp setStartTime(NClock startTime);

}
