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
package net.vpc.app.nuts;

import java.util.Arrays;

/**
 * Created by vpc on 6/23/17.
 * @since 0.5.4
 */
public final class NutsWorkspaceUpdateResult /*implements Iterable<NutsWorkspaceUpdateResultItem>*/ {

    private final NutsUpdateResult api;
    private final NutsUpdateResult runtime;
    private final NutsUpdateResult[] extensions;
    private final NutsUpdateResult[] components;

    public NutsWorkspaceUpdateResult(NutsUpdateResult api, NutsUpdateResult runtime, NutsUpdateResult[] extensions, NutsUpdateResult[] components) {
        this.api = api;
        this.runtime = runtime;
        this.extensions = extensions;
        this.components = components;
    }

    public NutsUpdateResult getApi() {
        return api;
    }

    public NutsUpdateResult getRuntime() {
        return runtime;
    }

    public NutsUpdateResult[] getExtensions() {
        return Arrays.copyOf(extensions, extensions.length);
    }

    public NutsUpdateResult[] getComponents() {
        return Arrays.copyOf(components, components.length);
    }

    public boolean isUpdatableApi() {
        return api != null;
    }

    public boolean isUpdatableRuntime() {
        return runtime != null;
    }

    public boolean isUpdatableExtensions() {
        return extensions.length > 0;
    }

//    public NutsWorkspaceUpdateResultItem[] toArray() {
//        List<NutsWorkspaceUpdateResultItem> all = new ArrayList<NutsWorkspaceUpdateResultItem>();
//        if (api != null) {
//            all.add(api);
//        }
//        if (runtime != null) {
//            all.add(runtime);
//        }
//        all.addAll(Arrays.asList(extensions));
//        return all.toArray(new NutsWorkspaceUpdateResultItem[0]);
//    }

    public boolean isUpdateAvailable() {
        return getUpdatesCount()>0;
    }
    
    public int getUpdatesCount() {
        int c = 0;
        if (api != null) {
            c++;
        }
        if (runtime != null) {
            c++;
        }
        return c + extensions.length+components.length;
    }

//    @Override
//    public Iterator<NutsWorkspaceUpdateResultItem> iterator() {
//        return Arrays.asList(toArray()).iterator();
//    }

}
