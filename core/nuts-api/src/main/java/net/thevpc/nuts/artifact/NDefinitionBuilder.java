package net.thevpc.nuts.artifact;

import net.thevpc.nuts.command.NInstallInformation;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.io.Serializable;
import java.util.Set;

/**
 * NDefinitionBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDefinitionBuilder extends Serializable, Comparable<NDefinitionBuilder> {
    /**
     * Dependency.
     *
     * @return dependency result
     */
    NDependency dependency();

    /**
     * Dependency.
     *
     * @param dependency dependency
     * @return dependency result
     */
    NDefinitionBuilder dependency(NDependency dependency);

    /**
     * Build.
     *
     * @return build result
     */
    NDefinition build();

    /**
     * Repository uuid.
     *
     * @return repository uuid result
     */
    @NGetter
    String repositoryUuid();

    /**
     * Repository name.
     *
     * @return repository name result
     */
    @NGetter
    String repositoryName();

    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    @NSetter
    NDefinitionBuilder id(NId id);

    /**
     * Id.
     *
     * @return id result
     */
    NId id();

    /**
     * Checks if is temporary.
     *
     * @return is temporary result
     */
    boolean isTemporary();

    /**
     * Descriptor.
     *
     * @return descriptor result
     */
    @NGetter
    NDescriptor descriptor();

    /**
     * Copy.
     *
     * @return copy result
     */
    NDefinitionBuilder copy();

    /**
     * Content.
     *
     * @return content result
     */
    NOptional<NPath> content();

    /**
     * Effective descriptor.
     *
     * @return effective descriptor result
     */
    NOptional<NDescriptor> effectiveDescriptor();

    /**
     * Install information.
     *
     * @return install information result
     */
    NOptional<NInstallInformation> installInformation();

    /**
     * Dependencies.
     *
     * @return dependencies result
     */
    NOptional<NDependencies> dependencies();

    /**
     * Content.
     *
     * @param content content
     * @return content result
     */
    NDefinitionBuilder content(NPath content);

    /**
     * Descriptor.
     *
     * @param descriptor descriptor
     * @return descriptor result
     */
    NDefinitionBuilder descriptor(NDescriptor descriptor);

    /**
     * Effective flags.
     *
     * @param effectiveFlags effective flags
     * @return effective flags result
     */
    NDefinitionBuilder effectiveFlags(Set<NDescriptorFlag> effectiveFlags);

    /**
     * Effective descriptor.
     *
     * @param effectiveDescriptor effective descriptor
     * @return effective descriptor result
     */
    NDefinitionBuilder effectiveDescriptor(NDescriptor effectiveDescriptor);

    /**
     * Install information.
     *
     * @param install install
     * @return install information result
     */
    NDefinitionBuilder installInformation(NInstallInformation install);

    /**
     * Dependencies.
     *
     * @param dependencies dependencies
     * @return dependencies result
     */
    NDefinitionBuilder dependencies(NDependencies dependencies);

    /**
     * Api id.
     *
     * @param apiId api id
     * @return api id result
     */
    NDefinitionBuilder apiId(NId apiId);

    /**
     * Api id.
     *
     * @return api id result
     */
    NId apiId();

    /**
     * Repository uuid.
     *
     * @param repositoryUuid repository uuid
     * @return repository uuid result
     */
    NDefinitionBuilder repositoryUuid(String repositoryUuid);

    /**
     * Repository name.
     *
     * @param repositoryName repository name
     * @return repository name result
     */
    NDefinitionBuilder repositoryName(String repositoryName);

    /**
     * Effective flags.
     *
     * @return effective flags result
     */
    NOptional<Set<NDescriptorFlag>> effectiveFlags();
}
