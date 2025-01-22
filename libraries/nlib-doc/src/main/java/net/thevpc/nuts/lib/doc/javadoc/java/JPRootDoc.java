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
package net.thevpc.nuts.lib.doc.javadoc.java;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.javadoc.JDClassDoc;
import net.thevpc.nuts.lib.doc.javadoc.JDRootDoc;

import java.io.IOException;
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

    public void parseSrcFolder(NPath path, Predicate<String> packageFilter) {
        //support for maven
        if(path.resolve("pom.xml").isRegularFile()
                && path.resolve("src/main/java").isDirectory()
        ){
            path=path.resolve("src/main/java");
        }
        NPath path0=path;
        path0.walk().filter(x -> x.isRegularFile()
                && x.getName().toString().endsWith(".java")
        ).forEach(file -> {
            String pck =
                    StreamSupport.stream(file.subpath(path0.getNameCount(), file.getNameCount()).getNames().spliterator(), false)
                            .collect(Collectors.joining("."));
            if (packageFilter == null || packageFilter.test(pck)) {
                parseFile(file);
            }
        });
    }

    public void parseFile(NPath path) {
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

            }.visit(StaticJavaParser.parse(path.toPath().get()), null);
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
