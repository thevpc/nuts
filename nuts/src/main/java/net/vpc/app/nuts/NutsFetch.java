package net.vpc.app.nuts;

import java.nio.file.Path;

public interface NutsFetch extends NutsQueryBaseOptions<NutsFetch> {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsFetch setId(String id);

    NutsFetch setId(NutsId id);

//    NutsFetch copyFrom(NutsFetch other);

    ////////////////////////////////////////////////////////
    // Getter
    ////////////////////////////////////////////////////////
    NutsId getId();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    NutsContent fetchContent();

    NutsContent fetchContentOrNull();

    NutsId fetchId();

    NutsId fetchIdOrNull();

    String fetchContentHash();

    String fetchDescriptorHash();

    NutsDefinition fetchDefinition();

    NutsDefinition fetchDefinitionOrNull();

    NutsDescriptor fetchDescriptor();

    NutsDescriptor fetchDescriptorOrNull();

    Path fetchFile();

    Path fetchFileOrNull();
    
    ///////////////////////
    // REDIFNIED
    ///////////////////////

    NutsQueryOptions toOptions();

    NutsFetch copy();

    NutsFetch copyFrom(NutsFetch other);
    
}
