/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Nuts deploy command
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NDeployCmd extends NWorkspaceCmd {

    static NDeployCmd of(NSession session) {
        return NExtensions.of(session).createComponent(NDeployCmd.class).get();
    }
    /**
     * set content
     *
     * @param stream content
     * @return {@code this} instance
     */
    NDeployCmd setContent(InputStream stream);

    /**
     * set content
     *
     * @param path content
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDeployCmd setContent(NPath path);

    /**
     * set content
     *
     * @param content content
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDeployCmd setContent(byte[] content);

    /**
     * set content
     *
     * @param file content
     * @return {@code this} instance
     */
    NDeployCmd setContent(File file);

    /**
     * set content
     *
     * @param file content
     * @return {@code this} instance
     */
    NDeployCmd setContent(Path file);

    /**
     * set content
     *
     * @param url content
     * @return {@code this} instance
     */
    NDeployCmd setContent(URL url);

    /**
     * set descriptor
     *
     * @param stream descriptor
     * @return {@code this} instance
     */
    NDeployCmd setDescriptor(InputStream stream);

    /**
     * set descriptor
     *
     * @param path descriptor
     * @return {@code this} instance
     */
    NDeployCmd setDescriptor(Path path);

    /**
     * set descriptor
     *
     * @param path descriptor
     * @return {@code this} instance
     */
    NDeployCmd setDescriptor(String path);

    /**
     * set descriptor
     *
     * @param file descriptor
     * @return {@code this} instance
     */
    NDeployCmd setDescriptor(File file);

    /**
     * set descriptor
     *
     * @param url descriptor
     * @return {@code this} instance
     */
    NDeployCmd setDescriptor(URL url);

    /**
     * set descriptor
     *
     * @param descriptor descriptor
     * @return {@code this} instance
     */
    NDeployCmd setDescriptor(NDescriptor descriptor);

    /**
     * return content sha1 hash
     *
     * @return content sha1 hash
     */
    String getSha1();

    /**
     * set content sha1 hash
     *
     * @param sha1 hash
     * @return {@code this} instance
     */
    NDeployCmd setSha1(String sha1);

    /**
     * set descriptor sha1 hash
     *
     * @param descSHA1 descriptor hash
     * @return {@code this} instance
     */
    NDeployCmd setDescSha1(String descSHA1);

    /**
     * set target repository to deploy to
     *
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    NDeployCmd setRepository(String repository);

    /**
     * set target repository to deploy to
     *
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    NDeployCmd to(String repository);

    /**
     * return target repository to deploy to
     *
     * @return target repository to deploy to
     */
    String getTargetRepository();

    /**
     * set target repository to deploy to
     *
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    NDeployCmd setTargetRepository(String repository);

    /**
     * set source repository to deploy from the given ids
     *
     * @param repository source repository to deploy from
     * @return {@code this} instance
     */
    NDeployCmd from(String repository);

    /**
     * set source repository to deploy from the given ids
     *
     * @param repository source repository to deploy from
     * @return {@code this} instance
     */
    NDeployCmd setSourceRepository(String repository);

    /**
     * return ids to deploy from source repository
     *
     * @return return ids to deploy from source repository
     */
    List<NId> getIds();

    /**
     * add id to deploy from source repository
     *
     * @param id id to deploy from source repository
     * @return {@code this} instance
     */
    NDeployCmd addId(String id);

    /**
     * add id to deploy from source repository
     *
     * @param id id to deploy from source repository
     * @return {@code this} instance
     */
    NDeployCmd addId(NId id);

    /**
     * add ids to deploy from source repository
     *
     * @param values ids to deploy from source repository
     * @return {@code this} instance
     */
    NDeployCmd addIds(NId... values);

    /**
     * add ids to deploy from source repository
     *
     * @param values ids to deploy from source repository
     * @return {@code this} instance
     */
    NDeployCmd addIds(String... values);

    /**
     * remove id to deploy from source repository
     *
     * @param id id to undo deploy from source repository
     * @return {@code this} instance
     */
    NDeployCmd removeId(String id);

    /**
     * remove id to deploy from source repository
     *
     * @param id id to undo deploy from source repository
     * @return {@code this} instance
     */
    NDeployCmd removeId(NId id);

    /**
     * reset ids list to deploy
     *
     * @return {@code this} instance
     */
    NDeployCmd clearIds();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NDeployCmd setSession(NSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NDeployCmd copySession();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NDeployCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NDeployCmd run();

    /**
     * run command (if not yet run) and return result
     *
     * @return deploy result
     */
    List<NId> getResult();

    NInputSource getContent();

    NDeployCmd setContent(NInputSource content);
}
