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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

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

import net.thevpc.nuts.NutsWorkspace;

public class NutsURLClassLoader extends URLClassLoader {

    private String name;
    private NutsWorkspace ws;
    private Set<NutsId> ids=new LinkedHashSet<>();
    private Set<URL> urls=new LinkedHashSet<>();

    public NutsURLClassLoader(String name,NutsWorkspace ws, URL[] urls, NutsId[] ids,ClassLoader parent) {
        super(urls, parent);
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(urls));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, URL[] urls,NutsId[] ids) {
        super(urls);
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(urls));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, URL[] urls,NutsId[] ids, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        this.name = name;
        this.ws = ws;
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, String[] urls,NutsId[] ids, ClassLoader parent) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent);
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, String[] urls,NutsId[] ids) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls));
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, String[] urls,NutsId[] ids, ClassLoader parent, URLStreamHandlerFactory factory) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent, factory);
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, File[] urls,NutsId[] ids, ClassLoader parent) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent);
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, File[] urls,NutsId[] ids) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls));
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public NutsURLClassLoader(String name,NutsWorkspace ws, File[] urls,NutsId[] ids, ClassLoader parent, URLStreamHandlerFactory factory) throws MalformedURLException {
        super(CoreIOUtils.toURL(urls), parent, factory);
        this.name = name;
        this.ws = ws;
        this.urls.addAll(Arrays.asList(CoreIOUtils.toURL(urls)));
        for (NutsId id : ids) {
            this.ids.add(id);
        }
    }

    public String getName() {
        return name;
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

    @Override
    public String toString() {
        return "NutsURLClassLoader{" +
                "name='" + name + '\'' +
                '}';
    }
}
