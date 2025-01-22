/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.mimetype;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NPath;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
public class DefaultNDocMimeTypeResolver implements NDocMimeTypeResolver {

    public static final NDocMimeTypeResolver DEFAULT = new DefaultNDocMimeTypeResolver()
            .setExtensionMimeType("nexpr", MimeTypeConstants.NEXPR)
            .setExtensionMimeType("ntf", NConstants.Ntf.MIME_TYPE)
            .setImmutable();
    private final Map<String, String> extensionToMimeType = new HashMap<>();
    private final Map<String, String> nameToMimeType = new HashMap<>();
    private boolean immutable = false;

    public DefaultNDocMimeTypeResolver() {
    }

    /**
     * create a non immutable copy of {@code this}
     * @return a copy of this resolver
     */
    public DefaultNDocMimeTypeResolver copy(){
        DefaultNDocMimeTypeResolver copy = new DefaultNDocMimeTypeResolver();
        for (Map.Entry<String, String> e : extensionToMimeType.entrySet()) {
            copy.setExtensionMimeType(e.getKey(),e.getValue());
        }
        for (Map.Entry<String, String> e : nameToMimeType.entrySet()) {
            copy.setNameMimeType(e.getKey(),e.getValue());
        }
        return copy;
    }

    public DefaultNDocMimeTypeResolver setNameMimeType(String name, String mimeType) {
        checkImmutable();
        if (mimeType == null) {
            nameToMimeType.remove(name);
        } else {
            nameToMimeType.put(name, mimeType);
        }
        return this;
    }

    public DefaultNDocMimeTypeResolver setExtensionMimeType(String extension, String mimeType) {
        checkImmutable();
        if (mimeType == null) {
            extensionToMimeType.remove(extension);
        } else {
            extensionToMimeType.put(extension, mimeType);
        }
        return this;
    }

    public NDocMimeTypeResolver setImmutable() throws IllegalArgumentException {
        this.immutable = true;
        return this;
    }

    private void checkImmutable() throws IllegalArgumentException {
        if (immutable) {
            throw new IllegalArgumentException("Immutable Resolver");
        }
    }

    protected String[] getExtensions(String path) {
        List<String> all = new ArrayList<>();
        int index = path.length();
        while (index > 0) {
            int li = path.lastIndexOf('.', index);
            if (li > 0) {
                all.add(path.substring(li + 1));
                index = li - 1;
            } else {
                break;
            }
        }
        return all.toArray(new String[0]);
    }

    @Override
    public String resolveMimetype(String path) {
        try {
            String s = nameToMimeType.get(NPath.of(path).getName().toString());
            if(s!=null){
                return s;
            }
            for (String extension : getExtensions(path)) {
                String r = extensionToMimeType.get(extension);
                if(r!=null){
                    return r;
                }
            }
            String mimeType = Files.probeContentType(NPath.of(path).toPath().get());
            if (mimeType != null) {
                return mimeType;
            }
        } catch (IOException ex) {
            Logger.getLogger(DefaultNDocMimeTypeResolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "application/binary";
    }

}
