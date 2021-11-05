package net.thevpc.nuts.runtime.core.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorPropertyBuilder;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class DefaultNutsDescriptorParser implements NutsDescriptorParser {

    private final NutsWorkspace ws;
    private NutsSession session;
    private boolean lenient = true;
    private NutsDescriptorStyle descriptorStyle;
    private String format;

    public DefaultNutsDescriptorParser(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public NutsDescriptor parse(URL url) {
        checkSession();
        return parse(NutsPath.of(url, getSession()));
    }

    @Override
    public NutsDescriptor parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes), true);
    }

    @Override
    public NutsDescriptor parse(Path path) {
        checkSession();
        return parse(NutsPath.of(path, getSession()));
    }

    @Override
    public NutsDescriptor parse(File file) {
        checkSession();
        return parse(file.toPath());
    }

    @Override
    public NutsDescriptor parse(InputStream stream) {
        checkSession();
        return parse(stream, false);
    }

    @Override
    public NutsDescriptor parse(NutsPath path) {
        checkSession();
        boolean startParsing = false;
        try {
            try (InputStream is = path.getInputStream()) {
                startParsing = true;
                return parse(is, true);
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse url %s", path), ex);
            }
        } catch (IOException ex) {
            if (!startParsing) {
                throw new NutsNotFoundException(getSession(), null, NutsMessage.cstyle("at file %s", path), null);
            }
            throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse url %s", path), ex);
        }
    }

    @Override
    public NutsDescriptor parse(String str) {
        checkSession();
        if (NutsBlankable.isBlank(str)) {
            return null;
        }
        return parse(new ByteArrayInputStream(str.getBytes()), true);
    }

    @Override
    public NutsDescriptorStyle getDescriptorStyle() {
        return descriptorStyle;
    }

    @Override
    public DefaultNutsDescriptorParser setDescriptorStyle(NutsDescriptorStyle descriptorStyle) {
        this.descriptorStyle = descriptorStyle;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public NutsDescriptorParser setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }


    @Override
    public NutsDescriptorParser setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(getWorkspace(), getSession());
    }

    private NutsDescriptor parse(InputStream in, boolean closeStream) {
        if (isLenient()) {
            try {
                return parseNonLenient(in, closeStream);
            } catch (Exception ex) {
                return null;
            }
        }
        return parseNonLenient(in, closeStream);
    }

    private NutsDescriptor parseNonLenient(InputStream in, boolean closeStream) {
        checkSession();
        NutsDescriptorStyle style = getDescriptorStyle();
        if (style == null) {
            style = NutsDescriptorStyle.NUTS;
        }
        switch (style) {
            case MAVEN: {
                try {
                    return MavenUtils.of(session).parsePomXml(in, NutsFetchMode.LOCAL, "descriptor", null);
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new NutsIOException(getSession(), ex);
                        }
                    }
                }
            }
            case NUTS: {
                try {
                    Reader rr = new InputStreamReader(in);
                    return NutsElements.of(getSession())
                            .setSession(session)
                            .json().parse(rr, NutsDescriptor.class);
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new NutsIOException(getSession(), ex);
                        }
                    }
                }
            }
            case MANIFEST: {
                try {
                    try {
                        Manifest manifest = new Manifest(in);
                        Attributes attrs = manifest.getMainAttributes();
                        String automaticModuleName = null;
                        String mainVersion = null;
                        String mainClass = null;
                        String implVendorId = null;
                        String implVendorTitle = null;
                        Set<NutsDependency> deps = new LinkedHashSet<>();
                        NutsId explicitId = null;
                        Map<String, String> all = new HashMap<>();
                        for (Object o : attrs.keySet()) {
                            Attributes.Name attrName = (Attributes.Name) o;
                            if ("Main-Class".equals(attrName.toString())) {
                                mainClass = (NutsUtilStrings.trimToNull(attrs.getValue(attrName)));
                            }
                            if ("Automatic-Module-Name".equals(attrName.toString())) {
                                automaticModuleName = NutsUtilStrings.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Implementation-Version".equals(attrName.toString())) {
                                mainVersion = NutsUtilStrings.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Implementation-Vendor-Id".equals(attrName.toString())) {
                                implVendorId = NutsUtilStrings.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Implementation-Vendor-Title".equals(attrName.toString())) {
                                implVendorTitle = NutsUtilStrings.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Nuts-Id".equals(attrName.toString())) {
                                explicitId = NutsIdParser.of(getSession()).setLenient(true).parse(NutsUtilStrings.trimToNull(attrs.getValue(attrName)));
                            }
                            if ("Nuts-Dependencies".equals(attrName.toString())) {
                                String nutsDependencies = NutsUtilStrings.trimToNull(attrs.getValue(attrName));
                                deps = nutsDependencies == null ? Collections.emptySet() :
                                        Arrays.stream(nutsDependencies.split(";"))
                                                .map(String::trim)
                                                .filter(x -> x.length() > 0)
                                                .map(NutsDependencyParser.of(session).setLenient(true)
                                                        ::parse)
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toCollection(LinkedHashSet::new));
                            }
                            all.put(attrName.toString(), NutsUtilStrings.trimToNull(attrs.getValue(attrName)));
                        }
                        if (explicitId == null) {
                            if (automaticModuleName == null && implVendorId == null) {
                                if (!NutsBlankable.isBlank(mainClass)) {
                                    automaticModuleName = CorePlatformUtils.getPackageName(mainClass);
                                }
                            } else if (automaticModuleName == null && implVendorId != null) {
                                automaticModuleName = implVendorId;
                            }
                            if (automaticModuleName != null
                                    || mainVersion != null
                                    || !deps.isEmpty()
                            ) {
                                String groupId = automaticModuleName == null ? "" : CorePlatformUtils.getPackageName(automaticModuleName);
                                String artifactId = automaticModuleName == null ? "" : CorePlatformUtils.getSimpleClassName(automaticModuleName);
                                explicitId = NutsIdBuilder.of(session).setGroupId(groupId).setArtifactId(artifactId)
                                        .setVersion(
                                                NutsBlankable.isBlank(mainVersion) ? "1.0" : mainVersion.trim()
                                        ).build();
                            }
                        }
                        if (explicitId != null || !deps.isEmpty()) {
                            String nutsName = NutsUtilStrings.trimToNull(all.get("Nuts-Name"));
                            if(nutsName==null){
                                nutsName=implVendorTitle;
                            }
                            return NutsDescriptorBuilder.of(getSession())
                                    .setId(explicitId)
                                    .setName(nutsName)
                                    .addFlag(NutsBlankable.isBlank(mainClass) ? NutsDescriptorFlag.EXEC : null)
                                    .addFlags(
                                            Arrays.stream(NutsUtilStrings.trim(
                                                            all.get("Nuts-Flags")
                                                    ).split("; ,"))
                                                    .map(NutsDescriptorFlag::parseLenient)
                                                    .filter(Objects::nonNull)
                                                    .toArray(NutsDescriptorFlag[]::new)
                                    )
                                    .setPackaging(CoreStringUtils.coalesce(
                                            NutsUtilStrings.trimToNull(all.get("Nuts-Packaging")),
                                            "jar"
                                    ))
                                    .setCategories(
                                            Arrays.stream(NutsUtilStrings.trim(
                                                            all.get("Nuts-Categories")
                                                    ).split("; ,"))
                                                    .map(NutsUtilStrings::trimToNull)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList())
                                    )
                                    .setIcons(
                                            Arrays.stream(NutsUtilStrings.trim(
                                                            all.get("Nuts-Icons")
                                                    ).split("; ,"))
                                                    .map(NutsUtilStrings::trimToNull)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList())
                                    )
                                    .setName(nutsName)
                                    .setDescription(NutsUtilStrings.trimToNull(all.get("Nuts-Description")))
                                    .setGenericName(NutsUtilStrings.trimToNull(all.get("Nuts-Generic-Name")))
                                    .setProperties(all.entrySet().stream()
                                            .filter(x -> x.getKey().startsWith("Nuts-Property-"))
                                            .map(x -> new DefaultNutsDescriptorPropertyBuilder(getSession())
                                                    .setName(x.getKey().substring("Nuts-Property-".length()))
                                                    .setValue(x.getValue())
                                                    //.setCondition()
                                                    .build())
                                            .toArray(NutsDescriptorProperty[]::new))
                                    //.setCondition()
                                    .setExecutor(new DefaultNutsArtifactCall(
                                            NutsId.of("java", getSession()),
                                            //new String[]{"-jar"}
                                            NutsBlankable.isBlank(mainClass) ? new String[0]
                                                    : new String[]{
                                                    "--main-class=", mainClass
                                            }
                                    ))
                                    .setDependencies(deps.toArray(new NutsDependency[0]))
                                    .build();
                        }
                        throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse Descriptor for Manifest from %s", in));
                    } catch (IOException ex) {
                        throw new NutsIOException(getSession(), ex);
                    }
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new NutsIOException(getSession(), ex);
                        }
                    }
                }
            }
            default: {
                throw new NutsUnsupportedEnumException(getSession(), style);
            }
        }
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
