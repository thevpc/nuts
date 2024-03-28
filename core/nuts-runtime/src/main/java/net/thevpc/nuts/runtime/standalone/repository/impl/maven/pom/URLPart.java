/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author thevpc
 */
public class URLPart {

    private Type type;
    private String path;

    public URLPart(Type type, String path) {
        this.type = type;
        this.path = path;
    }

    public String getName() {
        String n = path;
        final int p = n.replace('\\','/').lastIndexOf('/');
        if (p > 0) {
            n = path.substring(p + 1);
        }
        return n;
    }

    public Type getType() {
        return type;
    }

    public File getFile() {
        String path = getPath();
        if (getType() == URLPart.Type.FS_FILE) {
            return new File(path);
        } else if(getType() == Type.URL_FILE){
            try {
                if (path.startsWith("file:")) {
                    return URLPath._toFile(new URL(path));
                } else {
                    return new File(path);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("not a file : "+path);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "URLPart{" + "type=" + type + ", path=" + path + '}';
    }

    public enum Type{
        WEB, FS_FILE,URL_FILE,URL,JAR
    }

}
