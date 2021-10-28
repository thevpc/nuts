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
package net.thevpc.nuts.runtime.standalone.parsers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.NutsRef;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.bundles.io.InputStreamVisitor;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreDigestHelper;
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
    public int getSupportLevel(NutsSupportLevelContext<NutsDescriptorContentParserContext> criteria) {
        this.ws = criteria.getSession();
        String e = NutsUtilStrings.trim(criteria.getConstraints().getFileExtension());
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
        return NO_SUPPORT;
    }

    @Override
    public NutsDescriptor parse(final NutsDescriptorContentParserContext parserContext) {
        if (!POSSIBLE_EXT.contains(parserContext.getFileExtension())) {
            return null;
        }
        final NutsId JAVA = ws.id().parser().parse("java");
        final NutsRef<NutsDescriptor> nutsjson = new NutsRef<>();
        final NutsRef<NutsDescriptor> metainf = new NutsRef<>();
        final NutsRef<NutsDescriptor> maven = new NutsRef<>();
//        final NutsRef<String> mainClass = new NutsRef<>();

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
                        case "META-INF/MANIFEST.MF": {
                            try {
                                nutsjson.set(parserContext.getSession().descriptor().parser()
                                        .setSession(parserContext.getSession())
                                        .setDescriptorStyle(NutsDescriptorStyle.MANIFEST)
                                        .setLenient(true)
                                        .parse(inputStream));
                            } finally {
                                inputStream.close();
                            }
                            break;
                        }
                        case ("META-INF/" + NutsConstants.Files.DESCRIPTOR_FILE_NAME): {
                            try {
                                nutsjson.set(parserContext.getSession().descriptor().parser()
                                        .setSession(parserContext.getSession())
                                        .setDescriptorStyle(NutsDescriptorStyle.NUTS)
                                        .parse(inputStream));
                            } finally {
                                inputStream.close();
                            }
                            break;
                        }
                        default: {
                            try {
                                maven.set(MavenUtils.of(parserContext.getSession()).parsePomXmlAndResolveParents(inputStream, NutsFetchMode.REMOTE, path, null));
                            } finally {
                                inputStream.close();
                            }
                            break;
                        }
                    }
                    //continue
                    return !nutsjson.isSet() || (!metainf.isSet() && !maven.isSet());
                }
            });
        } catch (IOException ex) {
            throw new NutsIOException(ws, ex);
        }
        if (nutsjson.isSet()) {
            return checkDescriptor(nutsjson.get(), parserContext.getSession());
        }
        String mainClassString = null;
        if (metainf.isSet()) {
            String[] args = metainf.get().getExecutor().getArguments();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("--main-class=")) {
                    mainClassString = NutsUtilStrings.trimToNull(arg.substring("--main-class=".length()));
                    break;
                } else if (arg.equals("--main-class")) {
                    i++;
                    if (i < args.length) {
                        mainClassString = NutsUtilStrings.trimToNull(args[i]);
                    }
                }
            }
        }
        NutsDescriptor baseNutsDescriptor = null;
        if (maven.isSet()) {
            baseNutsDescriptor = maven.get();
            if (!NutsBlankable.isBlank(mainClassString)) {
                return checkDescriptor(
                        baseNutsDescriptor.builder().setExecutor(new DefaultNutsArtifactCall(JAVA, new String[]{
                                "--main-class", mainClassString})).build()
                        , parserContext.getSession());
            }
        } else if (metainf.isSet()) {
            baseNutsDescriptor = metainf.get();
        }
        if (baseNutsDescriptor == null) {
            CoreDigestHelper d = new CoreDigestHelper();
            d.append(parserContext.getFullStream());
            String artifactId = d.getDigest();
            baseNutsDescriptor = parserContext.getSession().descriptor().descriptorBuilder()
                    .setId(ws.id().builder().setGroupId("temp").setArtifactId(artifactId).setVersion("1.0").build())
                    .addFlag(mainClassString != null ? NutsDescriptorFlag.EXEC : null)
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
        if (NutsBlankable.isBlank(mainClassString) || alwaysSelectAllMainClasses) {
            NutsExecutionEntry[] classes = parserContext.getSession().apps().execEntries()
                    .setSession(parserContext.getSession())
                    .parse(parserContext.getFullStream(), "java", parserContext.getFullStream().toString());
            if (classes.length == 0) {
                return checkDescriptor(baseNutsDescriptor, parserContext.getSession());
            } else {
                return checkDescriptor(baseNutsDescriptor.builder().setExecutor(new DefaultNutsArtifactCall(JAVA, new String[]{
                                "--main-class=" + String.join(":",
                                        Arrays.stream(classes)
                                                .map(x -> x.getName())
                                                .collect(Collectors.toList())
                                )}, null)).addFlag(NutsDescriptorFlag.EXEC).build()
                        , parserContext.getSession())
                        ;
            }
        } else {
            return checkDescriptor(baseNutsDescriptor, parserContext.getSession());
        }
    }

    private NutsDescriptor checkDescriptor(NutsDescriptor nutsDescriptor, NutsSession session) {
        NutsId id = nutsDescriptor.getId();
        String groupId = id == null ? null : id.getGroupId();
        String artifactId = id == null ? null : id.getArtifactId();
        NutsVersion version = id == null ? null : id.getVersion();
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            switch (session.getConfirm()) {
                case ASK:
                case ERROR: {
                    if (groupId == null) {
                        groupId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("group id"))
                                .setDefaultValue(groupId)
                                .setHintMessage(NutsBlankable.isBlank(groupId) ? null : NutsMessage.plain(groupId))
                                .getValue();
                    }
                    if (artifactId == null) {
                        artifactId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("artifact id"))
                                .setDefaultValue(artifactId)
                                .setHintMessage(NutsBlankable.isBlank(artifactId) ? null : NutsMessage.plain(artifactId))
                                .getValue();
                    }
                    if (NutsBlankable.isBlank(version)) {
                        String ov = version == null ? null : version.getValue();
                        String v = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("version"))
                                .setDefaultValue(ov)
                                .setHintMessage(NutsBlankable.isBlank(ov) ? null : NutsMessage.plain(ov))
                                .getValue();
                        version = session.version().parser()
                                .setAcceptBlank(true)
                                .setAcceptIntervals(true)
                                .setLenient(true).parse(v);
                    }
                    break;
                }
                case NO:
                case YES: {
                    //silently return null
                }
            }
        }
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid descriptor id %s:%s#%s", groupId, artifactId, version));
        }
        return nutsDescriptor.builder()
                .setId(session.id().builder().setGroupId(groupId).setArtifactId(artifactId).setVersion(version).build())
                .build()
                ;
    }
}
