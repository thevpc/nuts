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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 2/1/17.
 */
public class NutsSession implements Cloneable {

    private boolean transitive = true;
    private FetchMode fetchMode=FetchMode.ONLINE;
    private NutsTerminal terminal;
    private Map<String,Object> properties=new HashMap<>();

    public NutsSession() {
    }

    public boolean isTransitive() {
        return transitive;
    }

    public NutsSession setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    public FetchMode getFetchMode() {
        return fetchMode;
    }

    public NutsSession setFetchMode(FetchMode fetchMode) {
        this.fetchMode = (fetchMode==null)?FetchMode.ONLINE : fetchMode;
        return this;
    }

    public NutsSession copy() {
        try {
            NutsSession cloned = (NutsSession) clone();
            cloned.properties=new HashMap<>(properties);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    //    public InputStream getIn() {
//        return in;
//    }
//
//    public NutsSession setIn(InputStream in) {
//        this.in = in;
//        return this;
//    }
//
//    public NutsPrintStream getOut() {
//        return out;
//    }
//
//    public NutsSession setOut(PrintStream out) {
//        this.out = NutsPrintStream.create(out);
//        return this;
//    }
//
//    public NutsPrintStream getErr() {
//        return err;
//    }
//
//    public NutsSession setErr(PrintStream err) {
//        this.err = NutsPrintStream.create(err);
//        return this;
//    }
    public NutsTerminal getTerminal() {
        return terminal;
    }

    public NutsSession setTerminal(NutsTerminal terminal) {
        this.terminal = terminal;
        return this;
    }
}
