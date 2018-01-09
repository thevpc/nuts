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
package net.vpc.app.nuts.boot;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsIdPatternFilter implements NutsDescriptorFilter {

    private boolean valid = false;
    private Pattern idPattern;
    private Pattern packagingPattern;
    private Pattern archPattern;

    private String[] ids;

    private String[] packagings;

    private String[] architectures;

    public NutsIdPatternFilter() {

    }

    public NutsIdPatternFilter(String[] id, String[] packaging, String[] arch) {
        this.ids = id;
        this.packagings = packaging;
        this.architectures = arch;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
        valid = false;
    }

    public String[] getPackagings() {
        return packagings;
    }

    public void setPackagings(String[] packagings) {
        this.packagings = packagings;
        valid = false;
    }

    public String[] getArchitectures() {
        return architectures;
    }

    public void setArchitectures(String[] architectures) {
        this.architectures = architectures;
        valid = false;
    }

    private void rebuild() {
        if (!valid) {
            this.archPattern = null;
            this.packagingPattern = null;
            this.archPattern = null;

            this.idPattern = compile(ids, false);
            this.packagingPattern = compile(packagings, true);
            this.archPattern = compile(architectures, true);
            valid = true;
        }
    }

    private Pattern compile(String[] arr, boolean addStars) {
        if (arr == null || arr.length < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String patternString : arr) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append("(");
            String str = StringUtils.simpexpToRegexp(patternString, true);
            if (addStars) {
                str = ".*" + str + ".*";
            }
            sb.append(str);
            sb.append(")");
        }
        return Pattern.compile(sb.toString());
    }

    public String[] getIds() {
        return ids;
    }

    public boolean accept(NutsDescriptor id) {
        rebuild();
        if (idPattern != null) {
            if (
                    !idPattern.matcher(id.getId().toString()).matches()
                    && !idPattern.matcher(id.getId().getFullName()).matches()
                    ) {
                return false;
            }
        }
        if (packagingPattern != null) {
            if (!packagingPattern.matcher(id.getPackaging()).matches()) {
                return false;
            }
        }
        if (archPattern != null) {
            boolean found = false;
            for (String a : id.getArch()) {
                if (archPattern.matcher(a).matches()) {
                    found = true;
                    break;
                }
            }
            return found;
        }
        return true;
    }
}
