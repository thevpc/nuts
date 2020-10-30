/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.NutsIOException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.vpc.app.nuts.NutsWorkspace;

public class NutsURLClassLoader extends URLClassLoader {

    private NutsWorkspace ws;
    private Set<NutsId> ids=new LinkedHashSet<>();
    private Set<URL> urls=new LinkedHashSet<>();

    public NutsURLClassLoader(NutsWorkspace ws, URL[] urls, NutsId[] ids,ClassLoader parent) {
        super(urls, parent);
        this.ws = ws;
        this.urls.addAll(Arrays.asList(urls));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, URL[] urls,NutsId[] ids) {
        super(urls);
        this.ws = ws;
        this.urls.addAll(Arrays.asList(urls));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, URL[] urls,NutsId[] ids, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.ws = ws;
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, String[] urls,NutsId[] ids, ClassLoader parent) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent);
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, String[] urls,NutsId[] ids) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls));
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, String[] urls,NutsId[] ids, ClassLoader parent, URLStreamHandlerFactory factory) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent, factory);
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, File[] urls,NutsId[] ids, ClassLoader parent) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent);
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, File[] urls,NutsId[] ids) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls));
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(NutsWorkspace ws, File[] urls,NutsId[] ids, ClassLoader parent, URLStreamHandlerFactory factory) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent, factory);
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    @Override
    public void addURL(URL url) {
        if(!urls.contains(url)) {
            super.addURL(url);
            urls.add(url);
        }
    }

    public boolean addId(NutsId id){
        return ids.add(id);
    }

    public Set<NutsId> getIds() {
        return ids;
    }

    public void addPath(Path url) {
        try {
            addURL(url.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new NutsIllegalArgumentException(ws, url.toString());
        }
    }

    public void addPath(String path) {
        try {
            addURL(Paths.get(path).toUri().toURL());
        } catch (MalformedURLException e) {
            throw new NutsIllegalArgumentException(ws, path);
        }
    }

    public void addFile(File url) {
        try {
            addURL(url.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new NutsIllegalArgumentException(ws, url.toString());
        }
    }

    public void addURL(String url) {
        try {
            addURL(CoreIOUtils.toURL(new String[]{url})[0]);
        } catch (UncheckedIOException| NutsIOException e) {
            throw new NutsIllegalArgumentException(ws, url.toString());
        }
    }
}
