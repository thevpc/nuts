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

final class DefaultNBootOptionsBuilderClassWorldState {
    private final DefaultNBootOptionsBuilder owner;

    DefaultNBootOptionsBuilderClassWorldState(DefaultNBootOptionsBuilder owner) {
        this.owner = owner;
    }

    NClassLoaderNode runtimeBootDependencyNode;
    NBootWorkspaceFactory bootWorkspaceFactory;
    List<URL> classWorldURLs;
    ClassLoader classWorldLoader;
    NBootDescriptor runtimeBootDescriptor;

    public NOptional<NClassLoaderNode> runtimeBootDependencyNode() {
        return NOptional.of(runtimeBootDependencyNode);
    }

    public DefaultNBootOptionsBuilder runtimeBootDependencyNode(NClassLoaderNode runtimeBootDependencyNode) {
        this.runtimeBootDependencyNode = runtimeBootDependencyNode;
        return owner;
    }

    public NOptional<NBootWorkspaceFactory> bootWorkspaceFactory() {
        return NOptional.of(bootWorkspaceFactory);
    }

    public DefaultNBootOptionsBuilder bootWorkspaceFactory(NBootWorkspaceFactory bootWorkspaceFactory) {
        this.bootWorkspaceFactory = bootWorkspaceFactory;
        return owner;
    }

    public NOptional<List<URL>> classWorldURLs() {
        return NOptional.of(classWorldURLs);
    }

    public DefaultNBootOptionsBuilder classWorldURLs(List<URL> classWorldURLs) {
        this.classWorldURLs = NReservedLangUtils.nonNullList(classWorldURLs);
        return owner;
    }

    public NOptional<ClassLoader> classWorldLoader() {
        return NOptional.of(classWorldLoader);
    }

    public DefaultNBootOptionsBuilder classWorldLoader(ClassLoader classWorldLoader) {
        this.classWorldLoader = classWorldLoader;
        return owner;
    }

    public NOptional<NBootDescriptor> runtimeBootDescriptor() {
        return NOptional.of(runtimeBootDescriptor);
    }

    public DefaultNBootOptionsBuilder runtimeBootDescriptor(NBootDescriptor runtimeBootDescriptor) {
        this.runtimeBootDescriptor = runtimeBootDescriptor;
        return owner;
    }
}
