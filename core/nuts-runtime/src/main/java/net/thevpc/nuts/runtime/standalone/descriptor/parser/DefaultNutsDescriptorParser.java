package net.thevpc.nuts.runtime.standalone.descriptor.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
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
        NutsSessionUtils.checkSession(getWorkspace(), getSession());
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
                                explicitId = NutsId.of(NutsUtilStrings.trimToNull(attrs.getValue(attrName))).orNull();
                            }
                            if ("Nuts-Dependencies".equals(attrName.toString())) {
                                String nutsDependencies = NutsUtilStrings.trimToNull(attrs.getValue(attrName));
                                deps = nutsDependencies == null ? Collections.emptySet() :
                                        StringTokenizerUtils.splitSemiColon(nutsDependencies).stream()
                                                .map(String::trim)
                                                .filter(x -> x.length() > 0)
                                                .map(x->NutsDependency.of(x).orNull())
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
                                explicitId = new DefaultNutsIdBuilder().setGroupId(groupId).setArtifactId(artifactId)
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
                            return new DefaultNutsDescriptorBuilder()
                                    .setId(explicitId)
                                    .setName(nutsName)
                                    .addFlag(NutsBlankable.isBlank(mainClass) ? NutsDescriptorFlag.EXEC : null)
                                    .addFlags(
                                            StringTokenizerUtils.splitDefault(
                                                            all.get("Nuts-Flags")
                                                    ).stream()
                                                    .map(x->NutsDescriptorFlag.parse(x).orNull())
                                                    .filter(Objects::nonNull)
                                                    .toArray(NutsDescriptorFlag[]::new)
                                    )
                                    .setPackaging(CoreStringUtils.coalesce(
                                            NutsUtilStrings.trimToNull(all.get("Nuts-Packaging")),
                                            "jar"
                                    ))
                                    .setCategories(
                                            StringTokenizerUtils.splitDefault(
                                                            all.get("Nuts-Categories")
                                                    ).stream()
                                                    .map(NutsUtilStrings::trimToNull)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList())
                                    )
                                    .setIcons(
                                            StringTokenizerUtils.splitDefault(
                                                            all.get("Nuts-Icons")
                                                    ).stream()
                                                    .map(NutsUtilStrings::trimToNull)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList())
                                    )
                                    .setName(nutsName)
                                    .setDescription(NutsUtilStrings.trimToNull(all.get("Nuts-Description")))
                                    .setGenericName(NutsUtilStrings.trimToNull(all.get("Nuts-Generic-Name")))
                                    .setProperties(all.entrySet().stream()
                                            .filter(x -> x.getKey().startsWith("Nuts-Property-"))
                                            .map(x -> new DefaultNutsDescriptorPropertyBuilder()
                                                    .setName(x.getKey().substring("Nuts-Property-".length()))
                                                    .setValue(x.getValue())
                                                    //.setCondition()
                                                    .build())
                                            .collect(Collectors.toList()))
                                    //.setCondition()
                                    .setExecutor(new DefaultNutsArtifactCall(
                                            NutsId.of("java").get( getSession()),
                                            //new String[]{"-jar"}
                                            NutsBlankable.isBlank(mainClass) ? Collections.emptyList()
                                                    :Arrays.asList(
                                                    "--main-class=", mainClass
                                            )
                                    ))
                                    .setDependencies(new ArrayList<>(deps))
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
