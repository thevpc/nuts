/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.util;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NVersion;

/**
 *
 * @author vpc
 */
public class Mvn {

    public static String localMaven() {
        return System.getProperty("user.home") + "/.m2/repository";
    }

    public static String localFolder(NId id) {
        return localMaven() + "/" + localFolder(id);
    }

    public static String folder(NId id) {
        String g = id.getGroupId().replace(".", "/");
        String a = id.getArtifactId();
        NVersion v = id.getVersion();
        if (v.isBlank()) {
            return g + "/" + a;
        } else {
            String ga = g + "/" + a;
            return ga + "/" + v;
        }
    }

    public static String localPath(NId id, MvnArtifactType type) {
        return localMaven() + "/" + file(id, type);
    }

    public static String jar(NId id) {
        return file(id, MvnArtifactType.JAR);
    }

    public static String file(NId id, MvnArtifactType type) {
        String g = id.getGroupId().replace(".", "/");
        String a = id.getArtifactId();
        NVersion v = id.getVersion();
        String ga = g + "/" + a;
        String gav = ga + "/" + v;
        String gavn = gav + "/" + a + "-" + v;
        switch (type) {
            case JAR:
                return gavn + ".jar";
            case JAR_SHA1:
                return gavn + ".jar.sha1";
            case JAR_ASC:
                return gavn + ".jar.asc";

            case POM:
                return gavn + ".pom";
            case POM_SHA1:
                return gavn + ".pom.sha1";
            case POM_ASC:
                return gavn + ".pom.asc";

            case JAVADOC:
                return gavn + "-javadoc.jar";
            case JAVADOC_SHA1:
                return gavn + "-javadoc.jar.sha1";
            case JAVADOC_ASC:
                return gavn + "-javadoc.jar.asc";

            case SOURCES:
                return gavn + "-sources.jar";
            case SOURCES_SHA1:
                return gavn + "-sources.jar.sha1";
            case SOURCES_ASC:
                return gavn + "-sources.jar.asc";

            case REMOTE_REPOSITORIES:
                return gav + "/_remote.repositories";

            case MAVEN_METADATA_LOCAL:
                return ga + "/maven-metadata-local.xml";
            default:
                throw new AssertionError();
        }
    }
}
