package net.vpc.app.nuts.toolbox.mvn;

import java.util.Arrays;
import java.util.HashSet;

public class AA {

    public static void main(String[] args) {
        String s1 = "/home/vpc/.m2/repository/net/vpc/app/nuts/toolbox/mvn/0.5.2.0/mvn-0.5.2.0.jar:/home/vpc/.m2/repository/aopalliance/aopalliance/1.0/aopalliance-1.0.jar:/home/vpc/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar:/home/vpc/.m2/repository/com/google/guava/guava/10.0.1/guava-10.0.1.jar:/home/vpc/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:/home/vpc/.m2/repository/commons-io/commons-io/2.2/commons-io-2.2.jar:/home/vpc/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar:/home/vpc/.m2/repository/javax/annotation/jsr250-api/1.0/jsr250-api-1.0.jar:/home/vpc/.m2/repository/javax/enterprise/cdi-api/1.0/cdi-api-1.0.jar:/home/vpc/.m2/repository/javax/inject/javax.inject/1/javax.inject-1.jar:/home/vpc/.m2/repository/junit/junit/3.8.2/junit-3.8.2.jar:/home/vpc/.m2/repository/net/vpc/app/nuts/toolbox/mvn/0.5.2.0/mvn-0.5.2.0.jar:/home/vpc/.m2/repository/net/vpc/app/nuts/nuts/0.5.2/nuts-0.5.2.jar:/home/vpc/.m2/repository/net/vpc/app/nuts/nuts-cmd-app/0.5.2.0/nuts-cmd-app-0.5.2.0.jar:/home/vpc/.m2/repository/net/vpc/common/vpc-common-commandline/1.1.2/vpc-common-commandline-1.1.2.jar:/home/vpc/.m2/repository/org/apache/maven/wagon/wagon-http-lightweight/2.5/wagon-http-lightweight-2.5.jar:/home/vpc/.m2/repository/org/apache/maven/wagon/wagon-http-shared/2.5/wagon-http-shared-2.5.jar:/home/vpc/.m2/repository/org/apache/maven/wagon/wagon-provider-api/1.0/wagon-provider-api-1.0.jar:/home/vpc/.m2/repository/org/apache/maven/maven-aether-provider/3.1.1/maven-aether-provider-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-artifact/3.1.1/maven-artifact-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-core/3.1.1/maven-core-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-embedder/3.1.1/maven-embedder-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-model/3.1.1/maven-model-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-model-builder/3.1.1/maven-model-builder-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-plugin-api/3.1.1/maven-plugin-api-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-repository-metadata/3.1.1/maven-repository-metadata-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-settings/3.1.1/maven-settings-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-settings-builder/3.1.1/maven-settings-builder-3.1.1.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-classworlds/2.5.1/plexus-classworlds-2.5.1.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-component-annotations/1.5.5/plexus-component-annotations-1.5.5.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-interpolation/1.19/plexus-interpolation-1.19.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-utils/3.0.15/plexus-utils-3.0.15.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-api/0.9.0.M2/aether-api-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-connector-wagon/0.9.0.M2/aether-connector-wagon-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-impl/0.9.0.M2/aether-impl-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-spi/0.9.0.M2/aether-spi-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-util/0.9.0.M2/aether-util-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/sisu/org.eclipse.sisu.inject/0.0.0.M5/org.eclipse.sisu.inject-0.0.0.M5.jar:/home/vpc/.m2/repository/org/eclipse/sisu/org.eclipse.sisu.plexus/0.0.0.M5/org.eclipse.sisu.plexus-0.0.0.M5.jar:/home/vpc/.m2/repository/org/jsoup/jsoup/1.7.2/jsoup-1.7.2.jar:/home/vpc/.m2/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar:/home/vpc/.m2/repository/org/slf4j/slf4j-simple/1.7.5/slf4j-simple-1.7.5.jar:/home/vpc/.m2/repository/org/sonatype/plexus/plexus-cipher/1.7/plexus-cipher-1.7.jar:/home/vpc/.m2/repository/org/sonatype/plexus/plexus-sec-dispatcher/1.3/plexus-sec-dispatcher-1.3.jar:/home/vpc/.m2/repository/org/sonatype/sisu/sisu-guice/3.1.0/sisu-guice-3.1.0-no_aop.jar";
        String s2 = "/home/vpc/.m2/repository/net/vpc/app/nuts/toolbox/mvn/0.5.2.0/mvn-0.5.2.0.jar:/home/vpc/.m2/repository/net/vpc/app/nuts/nuts/0.5.2/nuts-0.5.2.jar:/home/vpc/.m2/repository/net/vpc/app/nuts/nuts-cmd-app/0.5.2.0/nuts-cmd-app-0.5.2.0.jar:/home/vpc/.m2/repository/net/vpc/common/vpc-common-commandline/1.1.2/vpc-common-commandline-1.1.2.jar:/home/vpc/.m2/repository/org/apache/maven/maven-embedder/3.1.1/maven-embedder-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-settings/3.1.1/maven-settings-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-core/3.1.1/maven-core-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-model/3.1.1/maven-model-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-settings-builder/3.1.1/maven-settings-builder-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-repository-metadata/3.1.1/maven-repository-metadata-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-artifact/3.1.1/maven-artifact-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-aether-provider/3.1.1/maven-aether-provider-3.1.1.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-impl/0.9.0.M2/aether-impl-0.9.0.M2.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-interpolation/1.19/plexus-interpolation-1.19.jar:/home/vpc/.m2/repository/org/apache/maven/maven-plugin-api/3.1.1/maven-plugin-api-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-model-builder/3.1.1/maven-model-builder-3.1.1.jar:/home/vpc/.m2/repository/org/apache/maven/maven-compat/3.1.1/maven-compat-3.1.1.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-utils/3.0.15/plexus-utils-3.0.15.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-classworlds/2.5.1/plexus-classworlds-2.5.1.jar:/home/vpc/.m2/repository/org/eclipse/sisu/org.eclipse.sisu.plexus/0.0.0.M5/org.eclipse.sisu.plexus-0.0.0.M5.jar:/home/vpc/.m2/repository/javax/enterprise/cdi-api/1.0/cdi-api-1.0.jar:/home/vpc/.m2/repository/javax/annotation/jsr250-api/1.0/jsr250-api-1.0.jar:/home/vpc/.m2/repository/javax/inject/javax.inject/1/javax.inject-1.jar:/home/vpc/.m2/repository/com/google/guava/guava/10.0.1/guava-10.0.1.jar:/home/vpc/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar:/home/vpc/.m2/repository/org/sonatype/sisu/sisu-guice/3.1.0/sisu-guice-3.1.0-no_aop.jar:/home/vpc/.m2/repository/aopalliance/aopalliance/1.0/aopalliance-1.0.jar:/home/vpc/.m2/repository/org/eclipse/sisu/org.eclipse.sisu.inject/0.0.0.M5/org.eclipse.sisu.inject-0.0.0.M5.jar:/home/vpc/.m2/repository/org/codehaus/plexus/plexus-component-annotations/1.5.5/plexus-component-annotations-1.5.5.jar:/home/vpc/.m2/repository/org/sonatype/plexus/plexus-sec-dispatcher/1.3/plexus-sec-dispatcher-1.3.jar:/home/vpc/.m2/repository/org/sonatype/plexus/plexus-cipher/1.7/plexus-cipher-1.7.jar:/home/vpc/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:/home/vpc/.m2/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar:/home/vpc/.m2/repository/org/slf4j/slf4j-simple/1.7.5/slf4j-simple-1.7.5.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-connector-wagon/0.9.0.M2/aether-connector-wagon-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-api/0.9.0.M2/aether-api-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-spi/0.9.0.M2/aether-spi-0.9.0.M2.jar:/home/vpc/.m2/repository/org/eclipse/aether/aether-util/0.9.0.M2/aether-util-0.9.0.M2.jar:/home/vpc/.m2/repository/org/apache/maven/wagon/wagon-provider-api/1.0/wagon-provider-api-1.0.jar:/home/vpc/.m2/repository/org/apache/maven/wagon/wagon-http-lightweight/2.5/wagon-http-lightweight-2.5.jar:/home/vpc/.m2/repository/org/apache/maven/wagon/wagon-http-shared/2.5/wagon-http-shared-2.5.jar:/home/vpc/.m2/repository/org/jsoup/jsoup/1.7.2/jsoup-1.7.2.jar:/home/vpc/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar:/home/vpc/.m2/repository/commons-io/commons-io/2.2/commons-io-2.2.jar";
        HashSet<String> ss1 = new HashSet<>(Arrays.asList(s1.split(":")));
        HashSet<String> ss2 = new HashSet<>(Arrays.asList(s2.split(":")));
        ss1.removeAll(ss2);
        for (String s : ss1) {
            System.out.println("ADDED   : " + s);
        }

        ss1 = new HashSet<>(Arrays.asList(s1.split(":")));
        ss2 = new HashSet<>(Arrays.asList(s2.split(":")));
        ss2.removeAll(ss1);
        for (String s : ss2) {
            System.out.println("MISSING : " + s);
        }
    }
}