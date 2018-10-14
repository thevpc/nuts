/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Set;
import net.vpc.app.nuts.NullInputStream;
import net.vpc.app.nuts.NullOutputStream;
import net.vpc.app.nuts.NutsComponent;
import net.vpc.app.nuts.NutsConsole;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsDependencySearch;
import net.vpc.app.nuts.NutsExtensionMissingException;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsSessionImpl;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceFactory;
import net.vpc.app.nuts.NutsWorkspaceObjectFactory;
import net.vpc.app.nuts.extensions.terminals.DefaultNutsPrintStream;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsWorkspaceFactory implements NutsWorkspaceFactory {

    private NutsWorkspaceObjectFactory objectFactory;
    private NutsWorkspace ws;

    public DefaultNutsWorkspaceFactory(NutsWorkspaceObjectFactory objectFactory, NutsWorkspace ws) {
        this.objectFactory = objectFactory;
        this.ws = ws;
    }

    @Override
    public List<Class> discoverTypes(Class type, ClassLoader bootClassLoader) {
        return objectFactory.discoverTypes(type, bootClassLoader);
    }

    @Override
    public <T> List<T> discoverInstances(Class<T> type, ClassLoader bootClassLoader) {
        return objectFactory.discoverInstances(type, bootClassLoader);
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria) {
        return objectFactory.createSupported(type, supportCriteria);
    }

    @Override
    public <T extends NutsComponent> T createSupported(Class<T> type, Object supportCriteria, Class[] constructorParameterTypes, Object[] constructorParameters) {
        return objectFactory.createSupported(type, supportCriteria, constructorParameterTypes, constructorParameterTypes);
    }

    @Override
    public <T extends NutsComponent> List<T> createAllSupported(Class<T> type, Object supportCriteria) {
        return objectFactory.createAllSupported(type, supportCriteria);
    }

    @Override
    public <T> List<T> createAll(Class<T> type) {
        return objectFactory.createAll(type);
    }

    @Override
    public Set<Class> getExtensionPoints() {
        return objectFactory.getExtensionPoints();
    }

    @Override
    public Set<Class> getExtensionTypes(Class extensionPoint) {
        return objectFactory.getExtensionTypes(extensionPoint);
    }

    @Override
    public List<Object> getExtensionObjects(Class extensionPoint) {
        return objectFactory.getExtensionObjects(extensionPoint);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, String name) {
        return objectFactory.isRegisteredType(extensionPointType, name);
    }

    @Override
    public boolean isRegisteredInstance(Class extensionPointType, Object extensionImpl) {
        return objectFactory.isRegisteredInstance(extensionPointType, extensionImpl);
    }

    @Override
    public <T> void registerInstance(Class<T> extensionPoint, T implementation) {
        objectFactory.registerInstance(extensionPoint, implementation);
    }

    @Override
    public void registerType(Class extensionPointType, Class extensionType) {
        objectFactory.registerType(extensionPointType, extensionType);
    }

    @Override
    public boolean isRegisteredType(Class extensionPointType, Class extensionType) {
        return objectFactory.isRegisteredType(extensionPointType, extensionType);
    }

    @Override
    public NutsConsole createConsole(NutsSession session) {
        session = validateSession(session);
        NutsConsole cmd = objectFactory.createSupported(NutsConsole.class, ws);
        if (cmd == null) {
            throw new NutsExtensionMissingException(NutsConsole.class, "Console");
        }
        cmd.init(ws, session);
        return cmd;
    }

    @Override
    public NutsSession createSession() {
        NutsSession nutsSession = new NutsSessionImpl();
        nutsSession.setTerminal(createTerminal());
        return nutsSession;
    }

    @Override
    public NutsTerminal createTerminal() {
        return createTerminal(null, null, null);
    }

    @Override
    public NutsTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err) {
        NutsTerminal term = objectFactory.createSupported(NutsTerminal.class, null);
        if (term == null) {
            throw new NutsExtensionMissingException(NutsConsole.class, "Terminal");
        }
        term.install(ws, in, out, err);
        return term;
    }

    protected NutsSession validateSession(NutsSession session) {
        if (session == null) {
            session = createSession();
        }
        return session;
    }

    @Override
    public ClassLoader createClassLoader(String[] nutsIds, ClassLoader parentClassLoader, NutsSession session) {
        session = validateSession(session);
        NutsFile[] nutsFiles = ws.fetchDependencies(
                new NutsDependencySearch(nutsIds)
                        .setIncludeMain(true)
                        .setScope(NutsDependencyScope.RUN),
                session);
        URL[] all = new URL[nutsFiles.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = CoreIOUtils.getURL(nutsFiles[i].getFile());
        }
        return new NutsURLClassLoader(all, parentClassLoader);
    }

    @Override
    public NutsPrintStream createPrintStream(OutputStream out) {
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStream) {
            return (NutsPrintStream) out;
        }
        return objectFactory.createSupported(NutsPrintStream.class, ws, new Class[]{OutputStream.class}, new Object[]{out});
    }

    @Override
    public NutsPrintStream createPrintStream(File out) {
        if (out == null) {
            return null;
        }
        try {
            return new DefaultNutsPrintStream(out);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public InputStream createNullInputStream() {
        return NullInputStream.INSTANCE;
    }
    
    @Override
    public NutsPrintStream createNullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE);
    }

    @Override
    public NutsId parseNutsId(String nutsId) {
        return CoreNutsUtils.parseNutsId(nutsId);
    }

}
