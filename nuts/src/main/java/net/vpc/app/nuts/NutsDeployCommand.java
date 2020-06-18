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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Nuts deploy command
 * @author vpc
 * @since 0.5.4
 */
public interface NutsDeployCommand extends NutsWorkspaceCommand {

    /**
     * set content
     * @param stream content
     * @return {@code this} instance
     */
    NutsDeployCommand setContent(InputStream stream);

    /**
     * set content
     * @param path content
     * @return {@code this} instance
     */
    NutsDeployCommand setContent(String path);

    /**
     * set content
     * @param file content
     * @return {@code this} instance
     */
    NutsDeployCommand setContent(File file);

    /**
     * set content
     * @param file content
     * @return {@code this} instance
     */
    NutsDeployCommand setContent(Path file);

    /**
     * set content
     * @param url content
     * @return {@code this} instance
     */
    NutsDeployCommand setContent(URL url);

    /**
     * set descriptor
     * @param stream descriptor
     * @return {@code this} instance
     */
    NutsDeployCommand setDescriptor(InputStream stream);

    /**
     * set descriptor
     * @param path descriptor
     * @return {@code this} instance
     */
    NutsDeployCommand setDescriptor(Path path);

    /**
     * set descriptor
     * @param path descriptor
     * @return {@code this} instance
     */
    NutsDeployCommand setDescriptor(String path);

    /**
     * set descriptor
     * @param file descriptor
     * @return {@code this} instance
     */
    NutsDeployCommand setDescriptor(File file);

    /**
     * set descriptor
     * @param url descriptor
     * @return {@code this} instance
     */
    NutsDeployCommand setDescriptor(URL url);

    /**
     * set descriptor
     * @param descriptor descriptor
     * @return {@code this} instance
     */
    NutsDeployCommand setDescriptor(NutsDescriptor descriptor);

    /**
     * return content sha1 hash
     * @return content sha1 hash
     */
    String getSha1();

    /**
     * set content sha1 hash
     * @param sha1 hash
     * @return {@code this} instance
     */
    NutsDeployCommand setSha1(String sha1);

    /**
     * set descriptor sha1 hash
     * @param descSHA1 descriptor hash
     * @return {@code this} instance
     */
    NutsDeployCommand setDescSha1(String descSHA1);

    /**
     * set target repository to deploy to
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    NutsDeployCommand setRepository(String repository);

    /**
     * set target repository to deploy to
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    NutsDeployCommand setTargetRepository(String repository);

    /**
     * set target repository to deploy to
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    NutsDeployCommand to(String repository);

    /**
     * return target repository to deploy to
     * @return target repository to deploy to
     */
    String getTargetRepository();

    /**
     * set source repository to deploy from the given ids
     * @param repository source repository to deploy from
     * @return {@code this} instance
     */
    NutsDeployCommand from(String repository);

    /**
     * set source repository to deploy from the given ids
     * @param repository source repository to deploy from
     * @return {@code this} instance
     */
    NutsDeployCommand setSourceRepository(String repository);

    /**
     * return ids to deploy from source repository
     * @return return ids to deploy from source repository
     */
    NutsId[] getIds();

    /**
     * add id to deploy from source repository
     * @param id id to deploy from source repository
     * @return {@code this} instance
     */
    NutsDeployCommand addId(String id);

    /**
     * add id to deploy from source repository
     * @param id id to deploy from source repository
     * @return {@code this} instance
     */
    NutsDeployCommand addId(NutsId id);

    /**
     * add ids to deploy from source repository
     * @param values ids to deploy from source repository
     * @return {@code this} instance
     */
    NutsDeployCommand addIds(NutsId... values);

    /**
     * add ids to deploy from source repository
     * @param values ids to deploy from source repository
     * @return {@code this} instance
     */
    NutsDeployCommand addIds(String... values);

    /**
     * remove id to deploy from source repository
     * @param id id to undo deploy from source repository
     * @return {@code this} instance
     */
    NutsDeployCommand removeId(String id);

    /**
     * remove id to deploy from source repository
     * @param id id to undo deploy from source repository
     * @return {@code this} instance
     */
    NutsDeployCommand removeId(NutsId id);

    /**
     * reset ids list to deploy
     * @return {@code this} instance
     */
    NutsDeployCommand clearIds();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsDeployCommand setSession(NutsSession session);

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
    NutsDeployCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsDeployCommand run();

    /**
     * run command (if not yet run) and return result
     * @return deploy result
     */
    NutsId[] getResult();

}
