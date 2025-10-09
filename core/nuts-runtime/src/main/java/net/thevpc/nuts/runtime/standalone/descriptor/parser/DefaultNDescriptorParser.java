package net.thevpc.nuts.runtime.standalone.descriptor.parser;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.expr.NParseException;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.DefaultNArtifactCallBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNDescriptorParser implements NDescriptorParser {

    private NDescriptorStyle descriptorStyle;
    private String format;

    public DefaultNDescriptorParser() {
    }

    @Override
    public NOptional<NDescriptor> parse(URL url) {
        return parse(NPath.of(url));
    }

    @Override
    public NOptional<NDescriptor> parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes), null, true);
    }

    @Override
    public NOptional<NDescriptor> parse(Path path) {
        return parse(NPath.of(path));
    }

    @Override
    public NOptional<NDescriptor> parse(File file) {
        return parse(file.toPath());
    }

    @Override
    public NOptional<NDescriptor> parse(InputStream stream) {
        return parse(stream, null, false);
    }

    @Override
    public NOptional<NDescriptor> parse(NPath path) {
        try {
            boolean startParsing = false;
            try {
                try (InputStream is = path.getInputStream()) {
                    startParsing = true;
                    NDescriptorStyle defaultDescriptorStyle = null;
                    String canonicalPathName = path.getName().toLowerCase();
                    switch (canonicalPathName) {
                        case "pom.xml": {
                            defaultDescriptorStyle = NDescriptorStyle.MAVEN;
                            break;
                        }
                        case "manifest.mf": {
                            defaultDescriptorStyle = NDescriptorStyle.MANIFEST;
                            break;
                        }
                        case "nuts.json": {
                            defaultDescriptorStyle = NDescriptorStyle.NUTS;
                            break;
                        }
                        default: {
                            if (canonicalPathName.endsWith(".pom")) {
                                defaultDescriptorStyle = NDescriptorStyle.MAVEN;
                            } else if (canonicalPathName.endsWith(".nuts")) {
                                defaultDescriptorStyle = NDescriptorStyle.NUTS;
                            }
                        }
                    }
                    NOptional<NDescriptor> r = parse(is, defaultDescriptorStyle, true);
                    if (r.isError()) {
                        return NOptional.ofError(() -> NMsg.ofC("unable to parse descriptor from %s : %s", path,
                                r.getMessage().get()
                        ));
                    }
                    return r;
                } catch (RuntimeException ex) {
                    return NOptional.ofError(() -> NMsg.ofC("unable to parse descriptor from %s : %s", path, ex), ex);
                }
            } catch (IOException ex) {
                if (!startParsing) {
                    return NOptional.ofError(() -> NMsg.ofC("unable to parse descriptor from %s : file not found", path), ex);
                }
                return NOptional.ofError(() -> NMsg.ofC("unable to parse descriptor from %s : %s", path, ex), ex);
            }
        } catch (Exception ex) {
            return NOptional.ofError(() -> NMsg.ofC("unable to parse descriptor from %s : %s", path, ex), ex);
        }
    }

    @Override
    public NOptional<NDescriptor> parse(String str) {
        if (NBlankable.isBlank(str)) {
            return null;
        }
        return parse(new ByteArrayInputStream(str.getBytes()), null, true);
    }

    @Override
    public NDescriptorStyle getDescriptorStyle() {
        return descriptorStyle;
    }

    @Override
    public DefaultNDescriptorParser setDescriptorStyle(NDescriptorStyle descriptorStyle) {
        this.descriptorStyle = descriptorStyle;
        return this;
    }


    private NOptional<NDescriptor> parse(InputStream in, NDescriptorStyle defaultDescriptorStyle, boolean closeStream) {
        try {
            return NOptional.of(parseNonLenient(in, defaultDescriptorStyle, closeStream));
        } catch (Exception ex) {
            return NOptional.ofError(() -> NMsg.ofC("unable to parse descriptor from %s : %s",
                    in,
                    ex), ex);
        }
    }

    private NDescriptor parseNonLenient(InputStream in, NDescriptorStyle defaultDescriptorStyle, boolean closeStream) {
        NDescriptorStyle style = getDescriptorStyle();
        if (style == null) {
            style = defaultDescriptorStyle;
        }
        if (style == null) {
            style = NDescriptorStyle.NUTS;
        }
        switch (style) {
            case MAVEN: {
                try {
                    return MavenUtils.of().parsePomXml(in, NFetchMode.LOCAL, "descriptor", null);
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new NIOException(ex);
                        }
                    }
                }
            }
            case NUTS: {
                try {
                    Reader rr = new InputStreamReader(in);
                    return NElementParser.ofJson().parse(rr, NDescriptor.class);
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new NIOException(ex);
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
                        String implTitle = null;
                        Set<NDependency> deps = new LinkedHashSet<>();
                        NId explicitId = null;
                        Map<String, String> all = new HashMap<>();
                        for (Object o : attrs.keySet()) {
                            Attributes.Name attrName = (Attributes.Name) o;
                            if ("Main-Class".equals(attrName.toString())) {
                                mainClass = (NStringUtils.trimToNull(attrs.getValue(attrName)));
                            }
                            if ("Automatic-Module-Name".equals(attrName.toString())) {
                                automaticModuleName = NStringUtils.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Implementation-Version".equals(attrName.toString())) {
                                mainVersion = NStringUtils.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Implementation-Vendor-Id".equals(attrName.toString())) {
                                implVendorId = NStringUtils.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Implementation-Title".equals(attrName.toString())) {
                                implTitle = NStringUtils.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Implementation-Vendor-Title".equals(attrName.toString())) {
                                implVendorTitle = NStringUtils.trimToNull(attrs.getValue(attrName));
                            }
                            if ("Nuts-Id".equals(attrName.toString())) {
                                explicitId = NId.get(NStringUtils.trimToNull(attrs.getValue(attrName))).orNull();
                            }
                            if ("Nuts-Dependencies".equals(attrName.toString())) {
                                String nutsDependencies = NStringUtils.trimToNull(attrs.getValue(attrName));
                                deps = nutsDependencies == null ? Collections.emptySet() :
                                        StringTokenizerUtils.splitSemiColon(nutsDependencies).stream()
                                                .map(String::trim)
                                                .filter(x -> x.length() > 0)
                                                .map(x -> NDependency.get(x).orNull())
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toCollection(LinkedHashSet::new));
                            }
                            all.put(attrName.toString(), NStringUtils.trimToNull(attrs.getValue(attrName)));
                        }
                        String groupId=null;
                        String artifactId=null;
                        String artifactVersion=NBlankable.isBlank(mainVersion) ? "1.0" : mainVersion.trim();
                        if(!NBlankable.isBlank(implVendorId) &&  validGroupId(implVendorId.trim())){
                            groupId=implVendorId.trim();
                        }
                        if(!NBlankable.isBlank(implTitle) &&  validArtifactId(implTitle)){
                            artifactId=implTitle.trim();
                        }
                        if (explicitId == null && !NBlankable.isBlank(groupId) && !NBlankable.isBlank(artifactId)) {
                            explicitId = NIdBuilder.of(groupId, artifactId)
                                    .setVersion(
                                            NBlankable.isBlank(artifactVersion) ? "1.0" : artifactVersion.trim()
                                    ).build();
                        }
                        if (explicitId == null) {
                            if (automaticModuleName == null && implVendorId == null) {
                                if (!NBlankable.isBlank(mainClass)) {
                                    automaticModuleName = CorePlatformUtils.getPackageName(mainClass);
                                }
                            } else if (automaticModuleName == null && implVendorId != null) {
                                automaticModuleName = implVendorId;
                            }
                            if (!NBlankable.isBlank(automaticModuleName)) {
                                if(NBlankable.isBlank(groupId) && !NBlankable.isBlank(CorePlatformUtils.getPackageName(automaticModuleName))) {
                                    groupId = NStringUtils.trim(CorePlatformUtils.getPackageName(automaticModuleName));
                                }
                                if(NBlankable.isBlank(artifactId) && !NBlankable.isBlank(CorePlatformUtils.getSimpleClassName(automaticModuleName))) {
                                    artifactId = NStringUtils.trim(CorePlatformUtils.getSimpleClassName(automaticModuleName));
                                }
                                explicitId = NIdBuilder.of(groupId, artifactId)
                                        .setVersion(artifactVersion).build();
                            }
                        }
                        if (explicitId != null || !deps.isEmpty()) {
                            String nutsName = NStringUtils.trimToNull(all.get("Nuts-Name"));
                            if (nutsName == null) {
                                nutsName = implVendorTitle;
                            }
                            return new DefaultNDescriptorBuilder()
                                    .setId(explicitId)
                                    .setName(nutsName)
                                    .addFlag(NBlankable.isBlank(mainClass) ? NDescriptorFlag.EXEC : null)
                                    .addFlags(
                                            StringTokenizerUtils.splitDefault(
                                                            all.get("Nuts-Flags")
                                                    ).stream()
                                                    .map(x -> NDescriptorFlag.parse(x).orNull())
                                                    .filter(Objects::nonNull)
                                                    .toArray(NDescriptorFlag[]::new)
                                    )
                                    .setPackaging(CoreStringUtils.coalesce(
                                            NStringUtils.trimToNull(all.get("Nuts-Packaging")),
                                            "jar"
                                    ))
                                    .setCategories(
                                            StringTokenizerUtils.splitDefault(
                                                            all.get("Nuts-Categories")
                                                    ).stream()
                                                    .map(NStringUtils::trimToNull)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList())
                                    )
                                    .setIcons(
                                            StringTokenizerUtils.splitDefault(
                                                            all.get("Nuts-Icons")
                                                    ).stream()
                                                    .map(NStringUtils::trimToNull)
                                                    .filter(Objects::nonNull)
                                                    .collect(Collectors.toList())
                                    )
                                    .setName(nutsName)
                                    .setDescription(NStringUtils.trimToNull(all.get("Nuts-Description")))
                                    .setGenericName(NStringUtils.trimToNull(all.get("Nuts-Generic-Name")))
                                    .setProperties(all.entrySet().stream()
                                            .filter(x -> x.getKey().startsWith("Nuts-Property-"))
                                            .map(x -> new DefaultNDescriptorPropertyBuilder()
                                                    .setName(x.getKey().substring("Nuts-Property-".length()))
                                                    .setValue(x.getValue())
                                                    //.setCondition()
                                                    .build())
                                            .collect(Collectors.toList()))
                                    //.setCondition()
                                    .setExecutor(
                                            new DefaultNArtifactCallBuilder()
                                                    .setId(NId.get("java").get())
                                                    .setArguments(NBlankable.isBlank(mainClass) ? null : new String[]{"--main-class=", mainClass})
                                                    .build()
                                    )
                                    .setDependencies(new ArrayList<>(deps))
                                    .build();
                        }
                        throw new NParseException(NMsg.ofC("Missing Explicit Nuts Id in Manifest from %s", in));
                    } catch (IOException ex) {
                        throw new NIOException(ex);
                    }
                } finally {
                    if (closeStream) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            throw new NIOException(ex);
                        }
                    }
                }
            }
            default: {
                throw new NUnsupportedEnumException(style);
            }
        }
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

    public static boolean validGroupId(String g){
        return g != null && NId.GROUP_ID_PATTERN.matcher(g).matches();
    }
    public static boolean validArtifactId(String a){
        return a != null && NId.ARTIFACT_ID_PATTERN.matcher(a).matches();
    }
}
