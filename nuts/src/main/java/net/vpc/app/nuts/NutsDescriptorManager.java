package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

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
