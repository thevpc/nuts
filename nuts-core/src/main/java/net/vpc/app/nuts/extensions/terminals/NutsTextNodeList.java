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
package net.vpc.app.nuts.extensions.terminals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vpc on 5/23/17.
 */
public class NutsTextNodeList implements NutsTextNode, Iterable<NutsTextNode> {
    private List<NutsTextNode> children = new ArrayList<NutsTextNode>();

    public NutsTextNodeList(NutsTextNode... children) {
        for (NutsTextNode c : children) {
            add(c);
        }
    }

    public void add(NutsTextNode item) {
        children.add(item);
    }

    public NutsTextNode get(int index) {
        return children.get(index);
    }

    public int size() {
        return children.size();
    }

    @Override
    public Iterator<NutsTextNode> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        return children.toString();
    }
}
