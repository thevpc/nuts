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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.command;

import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

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
public interface NDeploy extends NWorkspaceCmd {

    static NDeploy of() {
        return NExtensions.of(NDeploy.class);
    }
    /**
     * set content
     *
     * @param stream content
     * @return {@code this} instance
     */
    NDeploy content(InputStream stream);

    /**
     * set content
     *
     * @param path content
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDeploy content(NPath path);

    /**
     * set content
     *
     * @param content content
     * @return {@code this} instance
     * @since 0.8.3
     */
    NDeploy content(byte[] content);

    /**
     * set content
     *
     * @param file content
     * @return {@code this} instance
     */
    NDeploy content(File file);

    /**
     * set content
     *
     * @param file content
     * @return {@code this} instance
     */
    NDeploy content(Path file);

    /**
     * set content
     *
     * @param url content
     * @return {@code this} instance
     */
    NDeploy content(URL url);

    /**
     * set descriptor
     *
     * @param stream descriptor
     * @return {@code this} instance
     */
    NDeploy descriptor(InputStream stream);

    /**
     * set descriptor
     *
     * @param path descriptor
     * @return {@code this} instance
     */
    NDeploy descriptor(Path path);

    /**
     * set descriptor
     *
     * @param path descriptor
     * @return {@code this} instance
     */
    NDeploy descriptor(String path);

    /**
     * set descriptor
     *
     * @param file descriptor
     * @return {@code this} instance
     */
    NDeploy descriptor(File file);

    /**
     * set descriptor
     *
     * @param url descriptor
     * @return {@code this} instance
     */
    NDeploy descriptor(URL url);

    /**
     * set descriptor
     *
     * @param descriptor descriptor
     * @return {@code this} instance
     */
    @NSetter
    NDeploy descriptor(NDescriptor descriptor);

    /**
     * return content sha1 hash
     *
     * @return content sha1 hash
     */
    @NGetter
    String sha1();

    /**
     * set content sha1 hash
     *
     * @param sha1 hash
     * @return {@code this} instance
     */
    @NSetter
    NDeploy sha1(String sha1);

    /**
     * set descriptor sha1 hash
     *
     * @param descSHA1 descriptor hash
     * @return {@code this} instance
     */
    @NSetter
    NDeploy descriptorSha1(String descSHA1);

    /**
     * set target repository to deploy to
     *
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    @NSetter
    NDeploy repository(String repository);

    /**
     * set target repository to deploy to
     *
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    @NSetter
    NDeploy to(String repository);

    /**
     * return target repository to deploy to
     *
     * @return target repository to deploy to
     */
    String targetRepository();

    /**
     * set target repository to deploy to
     *
     * @param repository target repository to deploy to
     * @return {@code this} instance
     */
    @NSetter
    NDeploy targetRepository(String repository);

    /**
     * set source repository to deploy from the given ids
     *
     * @param repository source repository to deploy from
     * @return {@code this} instance
     */
    @NSetter
    NDeploy from(String repository);

    /**
     * set source repository to deploy from the given ids
     *
     * @param repository source repository to deploy from
     * @return {@code this} instance
     */
    @NSetter
    NDeploy sourceRepository(String repository);

    /**
     * return ids to deploy from source repository
     *
     * @return return ids to deploy from source repository
     */
    @NGetter
    List<NId> ids();

    /**
     * add id to deploy from source repository
     *
     * @param id id to deploy from source repository
     * @return {@code this} instance
     */
    NDeploy addId(String id);

    /**
     * add id to deploy from source repository
     *
     * @param id id to deploy from source repository
     * @return {@code this} instance
     */
    NDeploy addId(NId id);

    /**
     * add ids to deploy from source repository
     *
     * @param values ids to deploy from source repository
     * @return {@code this} instance
     */
    NDeploy addIds(NId... values);

    /**
     * add ids to deploy from source repository
     *
     * @param values ids to deploy from source repository
     * @return {@code this} instance
     */
    NDeploy addIds(String... values);

    /**
     * remove id to deploy from source repository
     *
     * @param id id to undo deploy from source repository
     * @return {@code this} instance
     */
    NDeploy removeId(String id);

    /**
     * remove id to deploy from source repository
     *
     * @param id id to undo deploy from source repository
     * @return {@code this} instance
     */
    NDeploy removeId(NId id);

    /**
     * reset ids list to deploy
     *
     * @return {@code this} instance
     */
    NDeploy clearIds();

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
    NDeploy configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NDeploy run();

    /**
     * run command (if not yet run) and return result
     *
     * @return deploy result
     */
    // not a getter
    List<NId> getResult();

    @NGetter
    NInputSource content();

    @NSetter
    NDeploy content(NInputSource content);
}
