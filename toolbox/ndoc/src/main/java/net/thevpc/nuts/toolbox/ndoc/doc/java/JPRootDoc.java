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
package net.thevpc.nuts.toolbox.ndoc.doc.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import net.thevpc.nuts.toolbox.ndoc.doc.JDClassDoc;
import net.thevpc.nuts.toolbox.ndoc.doc.JDRootDoc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 * @author thevpc
 */
public class JPRootDoc implements JDRootDoc {

    private Map<String, JDClassDoc> classes = new HashMap<String, JDClassDoc>();

    public void parseSrcFolder(Path path, Predicate<String> packageFilter) {
        try {
            //support for maven
            if(Files.isRegularFile(path.resolve("pom.xml"))
                    && Files.isDirectory(path.resolve("src/main/java"))
            ){
                path=path.resolve("src/main/java");
            }
            Path path0=path;
            Files.walk(path0).filter(x -> Files.isRegularFile(x)
                    && x.getFileName().toString().endsWith(".java")
            ).forEach(file -> {
                String pck =
                        StreamSupport.stream(file.subpath(path0.getNameCount(), file.getNameCount()).spliterator(), false)
                                .map(Path::toString)
                                .collect(Collectors.joining("."));
                if (packageFilter == null || packageFilter.test(pck)) {
                    parseFile(file);
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void parseFile(Path path) {
        try {
            new VoidVisitorAdapter<Object>() {
                PackageDeclaration p;

                @Override
                public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                    super.visit(n, arg);
                    add(new JPClassDoc(JPRootDoc.this, n, p.getName().asString()));
                }

                @Override
                public void visit(PackageDeclaration p, Object arg) {
                    super.visit(p, arg);
                    this.p = p;
                }

            }.visit(StaticJavaParser.parse(path), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JDClassDoc get(String qualifiedName) {
        return classes.get(qualifiedName);
    }

    public JPRootDoc add(JDClassDoc c) {
        classes.put(c.qualifiedName(), c);
        return this;
    }

    @Override
    public JDClassDoc[] classes() {
        return classes.values().toArray(new JDClassDoc[0]);
    }

}
