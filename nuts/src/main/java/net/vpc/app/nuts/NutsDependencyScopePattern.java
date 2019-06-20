/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.EnumSet;

/**
 *
 * @author vpc
 * @since 0.5.6
 */
public enum NutsDependencyScopePattern {
    API,
    IMPLEMENTATION,
    PROVIDED,
    IMPORT,
    RUNTIME,
    SYSTEM,
    TEST_COMPILE,
    TEST_PROVIDED,
    TEST_RUNTIME,
    OTHER,

    /**
     * dependencies needed for running/executing unit tests the nuts : includes
     * 'test,compile,system,runtime' witch are NOT optional
     */
    TEST,
    
    
    COMPILE,
    /**
     * dependencies needed for running/executing the nuts : includes
     * 'compile,system,runtime' witch are NOT optional
     */
    RUN,
    RUN_TEST,
    /**
     * all dependencies (no restriction)
     */
    ALL();

    private String id;

    private NutsDependencyScopePattern() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    public String id() {
        return id;
    }

    public EnumSet<NutsDependencyScope> expand() {
        EnumSet<NutsDependencyScope> v = EnumSet.noneOf(NutsDependencyScope.class);
        switch (this) {
            case RUN: 
            {
                v.add(NutsDependencyScope.API);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
                break;
            }
            case RUN_TEST: {
                v.addAll(NutsDependencyScopePattern.RUN.expand());
                v.add(NutsDependencyScope.TEST_COMPILE);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                break;
            }
            case COMPILE: 
            {
                v.add(NutsDependencyScope.API);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.PROVIDED);
                break;
            }
            case TEST: {
                v.add(NutsDependencyScope.TEST_COMPILE);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                v.add(NutsDependencyScope.TEST_PROVIDED);
                break;
            }
            case ALL: {
                v.add(NutsDependencyScope.API);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.PROVIDED);
                v.add(NutsDependencyScope.TEST_COMPILE);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                v.add(NutsDependencyScope.TEST_PROVIDED);
                v.add(NutsDependencyScope.OTHER);
                break;
            }
            case API:{
                v.add(NutsDependencyScope.API);
            }
            case IMPORT:{
                v.add(NutsDependencyScope.IMPORT);
            }
            case IMPLEMENTATION:{
                v.add(NutsDependencyScope.IMPLEMENTATION);
            }
            case PROVIDED:{
                v.add(NutsDependencyScope.PROVIDED);
            }
            case RUNTIME:{
                v.add(NutsDependencyScope.RUNTIME);
            }
            case SYSTEM:{
                v.add(NutsDependencyScope.SYSTEM);
            }
            case TEST_COMPILE:{
                v.add(NutsDependencyScope.TEST_COMPILE);
            }
            case TEST_PROVIDED:{
                v.add(NutsDependencyScope.TEST_PROVIDED);
            }
            case TEST_RUNTIME:{
                v.add(NutsDependencyScope.TEST_RUNTIME);
            }
            case OTHER:{
                v.add(NutsDependencyScope.OTHER);
            }
            default:{
                throw new IllegalArgumentException("Unsupported "+this);
            }
        }
        return v;
    }
}
