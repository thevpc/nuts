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

import java.util.Collection;

/**
 * Command for installing artifacts
 * @author vpc
 * @since 0.5.4
 */
public interface NutsInstallCommand extends NutsWorkspaceCommand {

    /**
     * add artifact id to install
     * @param id id to install
     * @return {@code this} instance
     */
    NutsInstallCommand id(NutsId id);

    /**
     * add artifact id to install
     * @param id id to install
     * @return {@code this} instance
     */
    NutsInstallCommand id(String id);

    /**
     * remove artifact id to install
     * @param id id to install
     * @return {@code this} instance
     */
    NutsInstallCommand removeId(NutsId id);

    /**
     * remove artifact id to install
     * @param id id to install
     * @return {@code this} instance
     */
    NutsInstallCommand removeId(String id);

    /**
     * add artifact ids to install
     * @param ids id to install
     * @return {@code this} instance
     */
    NutsInstallCommand ids(NutsId... ids);

    /**
     * add artifact ids to install
     * @param ids id to install
     * @return {@code this} instance
     */
    NutsInstallCommand ids(String... ids);

    /**
     * add artifact id to install
     * @param id id to install
     * @return {@code this} instance
     */
    NutsInstallCommand addId(NutsId id);

    /**
     * add artifact id to install
     * @param id id to install
     * @return {@code this} instance
     */
    NutsInstallCommand addId(String id);

    /**
     * add artifact ids to install
     * @param ids ids to install
     * @return {@code this} instance
     */
    NutsInstallCommand addIds(NutsId... ids);

    /**
     * add artifact ids to install
     * @param ids ids to install
     * @return {@code this} instance
     */
    NutsInstallCommand addIds(String... ids);

    /**
     * clear ids to install
     * @return {@code this} instance
     */
    NutsInstallCommand clearIds();

    /**
     * return all ids to install
     * @return all ids to install
     */
    NutsId[] getIds();

    /**
     * add argument to pass to the install command
     * @param arg argument
     * @return {@code this} instance
     */
    NutsInstallCommand arg(String arg);

    /**
     * add argument to pass to the install command
     * @param arg argument
     * @return {@code this} instance
     */
    NutsInstallCommand addArg(String arg);

    /**
     * add arguments to pass to the install command
     * @param args argument
     * @return {@code this} instance
     */
    NutsInstallCommand args(Collection<String> args);

    /**
     * add arguments to pass to the install command
     * @param args argument
     * @return {@code this} instance
     */
    NutsInstallCommand addArgs(Collection<String> args);

    /**
     * add arguments to pass to the install command
     * @param args argument
     * @return {@code this} instance
     */
    NutsInstallCommand addArgs(String... args);

    /**
     * add arguments to pass to the install command
     * @param args argument
     * @return {@code this} instance
     */
    NutsInstallCommand args(String... args);

    /**
     * clear all arguments to pass to the install command
     * @return {@code this} instance
     */
    NutsInstallCommand clearArgs();

    /**
     * return all arguments to pass to the install command
     * @return all arguments to pass to the install command
     */
    String[] getArgs();

    /**
     * set default version flag. the installed version will be defined as default.
     * @return {@code this} instance
     */
    NutsInstallCommand defaultVersion();

    /**
     * set default version flag. when true, the installed version will be defined as default
     * @param defaultVersion when true, the installed version will be defined as
     * default
     * @return {@code this} instance
     */
    NutsInstallCommand defaultVersion(boolean defaultVersion);

    /**
     * set default version flag. when true, the installed version will be defined as default
     * @param defaultVersion when true, the installed version will be defined as
     * default
     * @return {@code this} instance
     */
    NutsInstallCommand setDefaultVersion(boolean defaultVersion);

    /**
     * return true if the installer will update the default version
     * @return true if the installer will update the default version
     */
    boolean isDefaultVersion();

    /**
     * return true companions should be installed as well
     * @return return true companions should be installed as well
     */
    boolean isCompanions();

    /**
     * update companions
     * @return return {@code this} instance
     */
    NutsInstallCommand companions();

    /**
     * if true update companions
     * @param value flag
     * @return return {@code this} instance
     */
    NutsInstallCommand companions(boolean value);

    /**
     * if true update companions
     * @param value flag
     * @return return {@code this} instance
     */
    NutsInstallCommand setCompanions(boolean value);

    /**
     * return true installed artifacts should be re-installed as well
     * @return true installed artifacts should be re-installed as well
     */
    boolean isInstalled();

    /**
     * reinstall installed artifacts
     * @return return {@code this} instance
     */
    NutsInstallCommand installed();

    /**
     * if true reinstall installed artifacts
     * @param value flag
     * @return return {@code this} instance
     */
    NutsInstallCommand installed(boolean value);

    /**
     * if true reinstall installed artifacts
     * @param value flag
     * @return {@code this} instance
     */
    NutsInstallCommand setInstalled(boolean value);

    /**
     * execute installation and return result.
     * @return installation result
     */
    NutsResultList<NutsDefinition> getResult();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsInstallCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsInstallCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsInstallCommand run();
}
