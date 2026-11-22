package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.app.*;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.internal.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.*;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.Collectors;

final class DefaultNBootOptionsBuilderWorkspaceState {
    private final DefaultNBootOptionsBuilder owner;

    DefaultNBootOptionsBuilderWorkspaceState(DefaultNBootOptionsBuilder owner) {
        this.owner = owner;
    }

    Boolean installCompanions;
    Boolean skipWelcome;
    Boolean skipBoot;
    Boolean system;
    Boolean gui;
    List<String> excludedExtensions;
    List<String> repositories;
    List<String> bootRepositories;
    String archetype;
    Boolean switchWorkspace;
    Map<NStoreType, String> storeLocations;
    Map<NHomeLocation, String> homeLocations;
    NOsFamily storeLayout;
    NStoreStrategy storeStrategy;
    NStoreStrategy repositoryStoreStrategy;

    public NOptional<String> archetype() {
        return NOptional.ofNamed(archetype, "archetype");
    }

    public NBootOptionsBuilder archetype(String archetype) {
        this.archetype = archetype;
        return owner;
    }

    public NOptional<List<String>> excludedExtensions() {
        return NOptional.ofNamed(excludedExtensions, "excludedExtensions");
    }

    public NBootOptionsBuilder excludedExtensions(List<String> excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return owner;
    }

    public NOptional<Map<NHomeLocation, String>> homeLocations() {
        return NOptional.ofNamed(homeLocations, "homeLocations");
    }

    public NBootOptionsBuilder homeLocations(Map<NHomeLocation, String> homeLocations) {
        if (homeLocations != null) {
            if (this.homeLocations == null) {
                this.homeLocations = new HashMap<>();
            }
            this.homeLocations.putAll(homeLocations);
        } else {
            this.homeLocations = null;
        }
        return owner;
    }

    public NOptional<NStoreStrategy> repositoryStoreStrategy() {
        return NOptional.ofNamed(repositoryStoreStrategy, "repositoryStoreStrategy");
    }

    public NBootOptionsBuilder repositoryStoreStrategy(NStoreStrategy repositoryStoreStrategy) {
        this.repositoryStoreStrategy = repositoryStoreStrategy;
        return owner;
    }

    public NOptional<NOsFamily> storeLayout() {
        return NOptional.ofNamed(storeLayout, "storeLayout");
    }

    public NBootOptionsBuilder storeLayout(NOsFamily storeLayout) {
        this.storeLayout = storeLayout;
        return owner;
    }

    public NOptional<NStoreStrategy> storeStrategy() {
        return NOptional.ofNamed(storeStrategy, "storeStrategy");
    }

    public NBootOptionsBuilder storeStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return owner;
    }

    public NOptional<Map<NStoreType, String>> storeLocations() {
        return NOptional.ofNamed(storeLocations, "storeLocations");
    }

    public NBootOptionsBuilder storeLocations(Map<NStoreType, String> storeLocations) {
        if (storeLocations != null) {
            if (this.storeLocations == null) {
                this.storeLocations = new HashMap<>();
            }
            this.storeLocations.clear();
            this.storeLocations.putAll(NReservedLangUtils.nonNullMap(storeLocations));
        } else {
            this.storeLocations = null;
        }
        return owner;
    }

    public NOptional<List<String>> repositories() {
        return NOptional.ofNamed(repositories, "repositories");
    }

    public NOptional<List<String>> bootRepositories() {
        return NOptional.ofNamed(bootRepositories, "bootRepositories");
    }

    public NBootOptionsBuilder repositories(List<String> repositories) {
        this.repositories = repositories;
        return owner;
    }

    public NBootOptionsBuilder bootRepositories(List<String> bootRepositories) {
        this.bootRepositories = bootRepositories;
        return owner;
    }

    public NOptional<Boolean> system() {
        return NOptional.ofNamed(system, "system");
    }

    public NBootOptionsBuilder system(Boolean system) {
        this.system = system;
        return owner;
    }

    public NOptional<Boolean> gui() {
        return NOptional.ofNamed(gui, "gui");
    }

    public NBootOptionsBuilder gui(Boolean gui) {
        this.gui = gui;
        return owner;
    }

    public NOptional<Boolean> installCompanions() {
        return NOptional.ofNamed(installCompanions, "installCompanions");
    }

    public NBootOptionsBuilder installCompanions(Boolean skipInstallCompanions) {
        this.installCompanions = skipInstallCompanions;
        return owner;
    }

    public NOptional<Boolean> skipWelcome() {
        return NOptional.ofNamed(skipWelcome, "skipWelcome");
    }

    public NBootOptionsBuilder skipWelcome(Boolean skipWelcome) {
        this.skipWelcome = skipWelcome;
        return owner;
    }

    public NOptional<Boolean> skipBoot() {
        return NOptional.ofNamed(skipBoot, "skipBoot");
    }

    public NBootOptionsBuilder skipBoot(Boolean skipBoot) {
        this.skipBoot = skipBoot;
        return owner;
    }

    public NOptional<Boolean> switchWorkspace() {
        return NOptional.ofNamed(switchWorkspace, "switchWorkspace");
    }

    public NBootOptionsBuilder switchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return owner;
    }

    public NBootOptionsBuilder storeLocation(NStoreType location, String value) {
        if (NBlankable.isBlank(value)) {
            if (storeLocations != null) {
                storeLocations.remove(location);
            }
        } else {
            if (storeLocations == null) {
                storeLocations = new HashMap<>();
            }
            storeLocations.put(location, value);
        }
        return owner;
    }

    public NBootOptionsBuilder homeLocation(NHomeLocation location, String value) {
        if (NBlankable.isBlank(value)) {
            if (homeLocations != null) {
                homeLocations.remove(location);
            }
        } else {
            if (homeLocations == null) {
                homeLocations = new HashMap<>();
            }
            homeLocations.put(location, value);
        }
        return owner;
    }
}
