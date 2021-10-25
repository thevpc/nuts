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
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.parsers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.bundles.common.Ref;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.bundles.io.InputStreamVisitor;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;
import net.thevpc.nuts.spi.NutsDescriptorContentParserComponent;
import net.thevpc.nuts.spi.NutsDescriptorContentParserContext;
import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

/**
 * Created by vpc on 1/15/17.
 */
@NutsSingleton
public class JarDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Collections.singletonList("jar"));//, "war", "ear"
    private NutsSession ws;
    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        this.ws=criteria.getSession();
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsDescriptor parse(final NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        final NutsId JAVA = ws.id().parser().parse("java");
        final Ref<NutsDescriptor> nutsjson = new Ref<>();
        final Ref<NutsDescriptor> metainf = new Ref<>();
        final Ref<NutsDescriptor> maven = new Ref<>();
        final Ref<String> mainClass = new Ref<>();

        try {
            ZipUtils.visitZipStream(parserContext.getFullStream(), new Predicate<String>() {
                @Override
                public boolean test(String path) {
                    if ("META-INF/MANIFEST.MF".equals(path)) {
                        return true;
                    }
                    if (("META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME).equals(path)) {
                        return true;
                    }
                    return path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml");
                }
            }, new InputStreamVisitor() {
                @Override
                public boolean visit(String path, InputStream inputStream) throws IOException {
                    switch (path) {
                        case "META-INF/MANIFEST.MF":
                            Manifest manifest = new Manifest(inputStream);
                            Attributes attrs = manifest.getMainAttributes();
                            for (Object o : attrs.keySet()) {
                                Attributes.Name attrName = (Attributes.Name) o;
                                if ("Main-Class".equals(attrName.toString())) {
                                    mainClass.set(attrs.getValue(attrName));
                                }
                            }
                            NutsDescriptor d = parserContext.getSession().descriptor().descriptorBuilder()
                                    .setId(ws.id().parser().parse("temp:jar#1.0"))
                                    .addFlag(mainClass.isSet()?NutsDescriptorFlag.EXEC : null)
                                    .setPackaging("jar")
                                    .setExecutor(new DefaultNutsArtifactCall(JAVA, 
                                            //new String[]{"-jar"}
                                            new String[0]
                                    ))
                                    .build();

                            metainf.set(d);
                            break;
                        case ("META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME):
                            try {
                                nutsjson.set(parserContext.getSession().descriptor().parser().setSession(parserContext.getSession()).parse(inputStream));
                            } finally {
                                inputStream.close();
                            }
                            break;
                        default:
                            try {
                                maven.set(MavenUtils.of(parserContext.getSession()).parsePomXmlAndResolveParents(inputStream, NutsFetchMode.REMOTE, path, null));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    //continue
                    return !nutsjson.isSet() || (!metainf.isSet() && !maven.isSet());
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        if (nutsjson.isSet()) {
            return nutsjson.get();
        }
        NutsDescriptor baseNutsDescriptor = null;
        if (maven.isSet()) {
            baseNutsDescriptor = maven.get();
            if (mainClass.isSet()) {
                return baseNutsDescriptor.builder().setExecutor(new DefaultNutsArtifactCall(JAVA, new String[]{
                        "--main-class", mainClass.get()})).build();
            }
        } else if (metainf.isSet()) {
            baseNutsDescriptor = metainf.get();
        }
        if (baseNutsDescriptor == null) {
            baseNutsDescriptor = parserContext.getSession().descriptor().descriptorBuilder()
                    .setId(ws.id().parser().parse("temp:jar#1.0"))
                    .addFlag(NutsDescriptorFlag.EXEC)
                    .setPackaging("jar")
                    .build();
        }
        boolean alwaysSelectAllMainClasses = false;
        NutsCommandLine cmd = parserContext.getSession().commandLine().create(parserContext.getParseOptions());
        NutsArgument a;
        while (!cmd.isEmpty()) {
            if ((a = cmd.nextBoolean("--all-mains")) != null) {
                alwaysSelectAllMainClasses = a.getValue().getBoolean();
            } else {
                cmd.skip();
            }
        }
        if (mainClass.get() == null || alwaysSelectAllMainClasses) {
            NutsExecutionEntry[] classes = parserContext.getSession().apps().execEntries()
                    .setSession(parserContext.getSession())
                    .parse(parserContext.getFullStream(), "java", parserContext.getFullStream().toString());
            if (classes.length == 0) {
                return baseNutsDescriptor;
            } else {
                return baseNutsDescriptor.builder().setExecutor(new DefaultNutsArtifactCall(JAVA, new String[]{
                        "--main-class=" + String.join(":",
                                Arrays.stream(classes)
                                        .map(x -> x.getName())
                                        .collect(Collectors.toList())
                        )}, null)).addFlag(NutsDescriptorFlag.EXEC).build();
            }
        } else {
            return baseNutsDescriptor;
        }
    }
}
