package net.thevpc.nuts.build.util;

import java.io.File;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

/**
 *
 * @author vpc
 */
public enum MvnArtifactType {
    JAR(3), 
    JAR_SHA1(4), 
    JAR_ASC(4), 
    
    SOURCES(7), 
    SOURCES_SHA1(8), 
    SOURCES_ASC(8), 
    
    JAVADOC(5), 
    JAVADOC_SHA1(6), 
    JAVADOC_ASC(6), 
    
    POM(1), 
    POM_SHA1(2), 
    POM_ASC(2), 
    
    REMOTE_REPOSITORIES(100), 
    MAVEN_METADATA_LOCAL(100);
    
    private final int uploadPrio;

    public static NOptional<MvnArtifactType> byFile(String file) {
        if (!NBlankable.isBlank(file)) {
            String name = new File(file.trim()).getName().toLowerCase();
            if ("maven-metadata-local.xml".equals(name)) {
                return NOptional.of(MvnArtifactType.MAVEN_METADATA_LOCAL);
            }
            if ("_remote.repositories".equals(name)) {
                return NOptional.of(MvnArtifactType.REMOTE_REPOSITORIES);
            }

            if (name.endsWith("-sources.jar.asc")) {
                return NOptional.of(MvnArtifactType.SOURCES_ASC);
            }
            if (name.endsWith("-sources.jar.sha1")) {
                return NOptional.of(MvnArtifactType.SOURCES_SHA1);
            }
            if (name.endsWith("-sources.jar")) {
                return NOptional.of(MvnArtifactType.SOURCES);
            }

            if (name.endsWith("-javadoc.jar.asc")) {
                return NOptional.of(MvnArtifactType.JAVADOC_ASC);
            }
            if (name.endsWith("-javadoc.jar.sha1")) {
                return NOptional.of(MvnArtifactType.JAVADOC_SHA1);
            }
            if (name.endsWith("-javadoc.jar")) {
                return NOptional.of(MvnArtifactType.JAVADOC);
            }

            if (name.endsWith(".jar.asc")) {
                return NOptional.of(MvnArtifactType.JAR_ASC);
            }
            if (name.endsWith(".jar.sha1")) {
                return NOptional.of(MvnArtifactType.JAR_SHA1);
            }
            if (name.endsWith(".jar")) {
                return NOptional.of(MvnArtifactType.JAR);
            }

            if (name.endsWith(".pom.asc")) {
                return NOptional.of(MvnArtifactType.POM_ASC);
            }
            if (name.endsWith(".pom.sha1")) {
                return NOptional.of(MvnArtifactType.POM_SHA1);
            }
            if (name.endsWith(".pom")) {
                return NOptional.of(MvnArtifactType.POM);
            }

        }
        return NOptional.ofNamedEmpty("artifact type for " + file);
    }
    
    private MvnArtifactType(int uploadPrio) {
        this.uploadPrio = uploadPrio;
    }

    public int uploadPrio() {
        return uploadPrio;
    }
}
