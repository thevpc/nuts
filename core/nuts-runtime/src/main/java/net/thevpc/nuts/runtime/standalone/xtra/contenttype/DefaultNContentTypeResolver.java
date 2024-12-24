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
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.xtra.contenttype;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NContentTypeResolver;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNContentTypeResolver implements NContentTypeResolver {

    private NWorkspace workspace;
    public DefaultNContentTypeResolver(NWorkspace workspace) {
        this.workspace=workspace;
    }

    public NCallableSupport<String> probeContentType(NPath path) {
        String contentType = null;
        if (path != null) {
            if (path.isRegularFile()) {
                Path file = path.toPath().orNull();
                if (file != null) {
                    contentType = probeFile(file);
                }
            }
            if (contentType == null) {
                URL url = path.toURL().orNull();
                try {
                    if (url != null) {
                        contentType = url.openConnection().getContentType();
                    }
                } catch (IOException e) {
                    //
                }
            }

            if (contentType == null) {
                String name = path.getName();
                try {
                    contentType = URLConnection.guessContentTypeFromName(name);
                } catch (Exception e) {
                    //ignore
                }
                if (contentType == null || "text/plain".equals(contentType)) {
                    String e = NPath.of(Paths.get(name)).getLastExtension();
                    if (e != null && e.equalsIgnoreCase("ntf")) {
                        return NCallableSupport.of(NConstants.Support.DEFAULT_SUPPORT + 10, "text/x-nuts-text-format");
                    }
                }
                if (contentType == null || "text/plain".equals(contentType)) {
                    String e = NPath.of(Paths.get(name)).getLastExtension();
                    if (e != null && e.equalsIgnoreCase("nuts")) {
                        return NCallableSupport.of(NConstants.Support.DEFAULT_SUPPORT + 10, "application/json");
                    }
                }
            }
            if (contentType != null) {
                return NCallableSupport.of(NConstants.Support.DEFAULT_SUPPORT, contentType);
            }
        }

        return NCallableSupport.invalid(() -> NMsg.ofInvalidValue("content-type"));
    }

    private String probeFile(Path file) {
        String contentType = null;
        try {
            contentType = Files.probeContentType(file);
        } catch (IOException e) {
            //ignore
        }
        if (contentType != null) {
            switch (contentType) {
                case "application/zip": {
                    NRef<Boolean> isJar = NRef.of(false);
                    NRef<Boolean> isWar = NRef.of(false);
                    ZipUtils.visitZipStream(file, (path, inputStream) -> {
                        switch (path) {
                            case "META-INF/MANIFEST.MF": {
                                isJar.set(true);
                                if (isJar.orElse(false) && isWar.orElse(false)) {
                                    return NVisitResult.TERMINATE;
                                }
                                break;
                            }
                            case "WEB-INF/web.xml": {
                                isWar.set(true);
                                if (isJar.orElse(false) && isWar.orElse(false)) {
                                    return NVisitResult.TERMINATE;
                                }
                                break;
                            }
                        }
                        return NVisitResult.CONTINUE;
                    });
                    if (isWar.get()) {
                        return "application/x-webarchive";
                    }
                    if (isJar.get()) {
                        return "application/java-archive";
                    }
                    break;
                }
                case "text/plain": {
                    if(file.getFileName().toString().endsWith(".hl")){
                        return "text/x-hl";
                    }
                    if(file.getFileName().toString().endsWith(".ntf")){
                        return "text/x-ntf";
                    }
                    break;
                }

            }
        }
        return contentType;
    }


    @Override
    public NCallableSupport<String> probeContentType(byte[] bytes) {
        String contentType = null;
        if (bytes != null) {
            try {
                contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                //ignore
            }
        }
        if (contentType != null) {
            return NCallableSupport.of(NConstants.Support.DEFAULT_SUPPORT, contentType);
        }
        return NCallableSupport.invalid(() ->NMsg.ofInvalidValue("content-type"));
    }

    @Override
    public List<String> findExtensionsByContentType(String contentType) {
        Set<String> v = model().contentTypesToExtensions.get(contentType);
        return v == null ? Collections.emptyList() : new ArrayList<>(v);
    }

    @Override
    public List<String> findContentTypesByExtension(String extension) {
        Set<String> v = model().extensionsToContentType.get(extension);
        return v == null ? Collections.emptyList() : new ArrayList<>(v);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public DefaultNContentTypeResolverModel model(){
        synchronized (workspace) {
            return NApp.of().getOrComputeProperty(
                    DefaultNContentTypeResolverModel.class.getName(), NScopeType.WORKSPACE,
                    () -> new DefaultNContentTypeResolverModel()
            );
        }
    }
    public static class DefaultNContentTypeResolverModel{
        private Map<String, Set<String>> contentTypesToExtensions;
        private Map<String, Set<String>> extensionsToContentType;

        public DefaultNContentTypeResolverModel() {
            contentTypesToExtensions = new HashMap<>();
            extensionsToContentType = new HashMap<>();
            Properties p = new Properties();
            try (InputStream is = getClass().getResource("/net/thevpc/nuts/runtime/content-types.properties").openStream()) {
                p.load(is);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            for (Map.Entry<Object, Object> e : p.entrySet()) {
                String extension = (String) e.getKey();
                for (String contentType : NStringUtils.split((String) e.getValue(), ",; ")) {
                    contentTypesToExtensions.computeIfAbsent(contentType, x -> new LinkedHashSet<>())
                            .add(extension);
                    extensionsToContentType.computeIfAbsent(extension, x -> new LinkedHashSet<>())
                            .add(contentType);
                }
            }
        }
    }
}

