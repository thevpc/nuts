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
package net.thevpc.nuts.runtime.standalone.descriptor.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.NutsRef;

import java.util.*;

import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.spi.*;

/**
 * Created by vpc on 1/15/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class JarDescriptorContentParserComponent implements NutsDescriptorContentParserComponent {

    public static final Set<String> POSSIBLE_EXT = new HashSet<>(Collections.singletonList("jar"));//, "war", "ear"
    private NutsSession ws;

    @Override
    public int getSupportLevel(NutsSupportLevelContext criteria) {
        this.ws = criteria.getSession();
        NutsDescriptorContentParserContext cons = criteria.getConstraints(NutsDescriptorContentParserContext.class);
        if (cons != null) {
            String e = NutsUtilStrings.trim(cons.getFileExtension());
            switch (e) {
                case "jar":
                case "war":
                case "ear": {
                    return DEFAULT_SUPPORT + 10;
                }
                case "zip": {
                    return DEFAULT_SUPPORT + 5;
                }
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsDescriptor parse(final NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        final NutsId JAVA = NutsId.of("java").get(ws);
        final NutsRef<NutsDescriptor> nutsjson = new NutsRef<>();
        final NutsRef<NutsDescriptor> metainf = new NutsRef<>();
        final NutsRef<NutsDescriptor> maven = new NutsRef<>();
//        final NutsRef<String> mainClass = new NutsRef<>();

        NutsSession session = parserContext.getSession();
        ZipUtils.visitZipStream(parserContext.getFullStream(), (path, inputStream) -> {
            switch (path) {
                case "META-INF/MANIFEST.MF": {
                    try {
                        metainf.set(NutsDescriptorParser.of(session)
                                .setDescriptorStyle(NutsDescriptorStyle.MANIFEST)
                                .parse(inputStream).orNull());
                    } finally {
                        inputStream.close();
                    }
                    break;
                }
                case ("META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME): {
                    try {
                        nutsjson.set(NutsDescriptorParser.of(session)
                                .setDescriptorStyle(NutsDescriptorStyle.NUTS)
                                .parse(inputStream).get(session));
                    } finally {
                        inputStream.close();
                    }
                    break;
                }
                default: {
                    if (path.startsWith("META-INF/maven/") && path.endsWith("/pom.xml")) {
                        try {
                            maven.set(MavenUtils.of(session).parsePomXmlAndResolveParents(inputStream, NutsFetchMode.REMOTE, path, null));
                        } finally {
                            inputStream.close();
                        }
                        break;
                    } else if (path.startsWith("META-INF/nuts/") && path.endsWith("/nuts.json")) {
                        try {
                            nutsjson.set(NutsDescriptorParser.of(session)
                                    .setDescriptorStyle(NutsDescriptorStyle.NUTS)
                                    .parse(inputStream).get(session));
                        } finally {
                            inputStream.close();
                        }
                    }
                    break;
                }
            }
            //continue
            return !nutsjson.isSet() || (!metainf.isSet() && !maven.isSet());
        }, session);

        if (nutsjson.isSet()) {
            return nutsjson.get();
        }
        String mainClassString = null;
        if (metainf.isSet()) {
            if (metainf.get().getExecutor() != null) {
                List<String> args = metainf.get().getExecutor().getArguments();
                for (int i = 0; i < args.size(); i++) {
                    String arg = args.get(i);
                    if (arg.startsWith("--main-class=")) {
                        mainClassString = NutsUtilStrings.trimToNull(arg.substring("--main-class=".length()));
                        break;
                    } else if (arg.equals("--main-class")) {
                        i++;
                        if (i < args.size()) {
                            mainClassString = NutsUtilStrings.trimToNull(args.get(i));
                        }
                    }
                }
            }
        }
        NutsDescriptor baseNutsDescriptor = null;
        if (maven.isSet()) {
            baseNutsDescriptor = maven.get();
            if (!NutsBlankable.isBlank(mainClassString)) {
                return baseNutsDescriptor.builder().setExecutor(new DefaultNutsArtifactCall(JAVA,
                        Arrays.asList("--main-class", mainClassString))).build();
            }
        } else if (metainf.isSet()) {
            baseNutsDescriptor = metainf.get();
        }
        if (baseNutsDescriptor == null) {
            CoreDigestHelper d = new CoreDigestHelper(session);
            d.append(parserContext.getFullStream());
            String artifactId = d.getDigest();
            baseNutsDescriptor = new DefaultNutsDescriptorBuilder()
                    .setId(NutsIdBuilder.of("temp",artifactId).setVersion("1.0").build())
                    .addFlag(mainClassString != null ? NutsDescriptorFlag.EXEC : null)
                    .setPackaging("jar")
                    .build();
        }
        boolean alwaysSelectAllMainClasses = false;
        NutsCommandLine cmd = NutsCommandLine.of(parserContext.getParseOptions());
        NutsArgument a;
        while (!cmd.isEmpty()) {
            if ((a = cmd.nextBoolean("--all-mains").orNull()) != null) {
                alwaysSelectAllMainClasses = a.getBooleanValue().get(session);
            } else {
                cmd.skip();
            }
        }
//        if (NutsBlankable.isBlank(mainClassString) || alwaysSelectAllMainClasses) {
//            NutsExecutionEntry[] classes = NutsExecutionEntries.of(session)
//                    .parse(parserContext.getFullStream(), "jar", parserContext.getFullStream().toString());
//            if (classes.length == 0) {
//                return baseNutsDescriptor;
//            } else {
//                return baseNutsDescriptor.builder().setExecutor(new DefaultNutsArtifactCall(JAVA, new String[]{
//                        "--main-class=" + String.join(":",
//                                Arrays.stream(classes)
//                                        .map(x -> x.getName())
//                                        .collect(Collectors.toList())
//                        )}, null)).addFlag(NutsDescriptorFlag.EXEC).build();
//            }
//        } else {
        return baseNutsDescriptor;
//        }
    }

}
