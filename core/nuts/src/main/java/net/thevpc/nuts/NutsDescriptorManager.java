package net.thevpc.nuts;

public interface NutsDescriptorManager {
    NutsDescriptorParser parser();

    /**
     * create descriptor builder.
     *
     * @return new instance of NutsDescriptorBuilder
     */
    NutsDescriptorBuilder descriptorBuilder();

    /**
     * create classifier mappings builder.
     *
     * @return new instance of NutsClassifierMappingBuilder
     */
    NutsClassifierMappingBuilder classifierBuilder();

    /**
     * create descriptor builder.
     *
     * @return new instance of NutsIdLocationBuilder
     */
    NutsIdLocationBuilder locationBuilder();

    /**
     * create executor builder.
     *
     * @return new instance of NutsExecutorDescriptorBuilder
     */
    NutsArtifactCallBuilder callBuilder();

    NutsDescriptorFormat formatter();

    NutsDescriptorFormat formatter(NutsDescriptor value);

    NutsDescriptorFilterManager filter();

}
