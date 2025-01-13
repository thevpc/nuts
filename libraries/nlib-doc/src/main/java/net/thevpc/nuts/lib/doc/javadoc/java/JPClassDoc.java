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
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import net.thevpc.nuts.lib.doc.javadoc.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class JPClassDoc implements JDClassDoc {

    private JPRootDoc root;
    private String packageName;
    private ClassOrInterfaceDeclaration declaration;
    private List<JDMethodDoc> methods = new ArrayList<>();
    private List<JDConstructorDoc> constructors = new ArrayList<>();
    private List<JDFieldDoc> fields = new ArrayList<>();

    public JPClassDoc(JPRootDoc root, ClassOrInterfaceDeclaration declaration, String packageName) {
        this.root = root;
        this.declaration = declaration;
        this.packageName = packageName;

//                    if (n.getComment().isPresent() && n.getComment().get() instanceof JavadocComment) {
//                        JavadocComment jc = (JavadocComment) n.getComment().get();
//                        Javadoc d = StaticJavaParser.parseJavadoc(jc.getContent());
//                        String title = String.format("%s (%s)", n.getName(), file);
//                        System.out.println(title);
//                        System.out.println(Strings.repeat("=", title.length()));
//                        System.out.println(n.getComment());
//                    }
        for (BodyDeclaration<?> member : declaration.getMembers()) {
            if (member instanceof MethodDeclaration) {
                methods.add(new JPMethodDoc((MethodDeclaration) member, this));
            }
            if (member instanceof ConstructorDeclaration) {
                constructors.add(new JPConstructorDoc((ConstructorDeclaration) member, this));
            }
            if (member instanceof FieldDeclaration) {
                FieldDeclaration vv = (FieldDeclaration) member;
                for (VariableDeclarator variable : vv.getVariables()) {
                    fields.add(new JPFieldDoc(vv, variable, this));
                }
            }
        }
    }

    @Override
    public JDFieldDoc[] fields() {
        return fields.toArray(new JDFieldDoc[0]);
    }

    @Override
    public JDConstructorDoc[] constructors() {
        return constructors.toArray(new JDConstructorDoc[0]);
    }

    @Override
    public JDMethodDoc[] methods() {
        return methods.toArray(new JDMethodDoc[0]);
    }

    @Override
    public String name() {
        return declaration.getName().asString();
    }

    @Override
    public String modifiers() {
        return Arrays.stream(declaration.getModifiers().toArray()).map(Object::toString).collect(Collectors.joining(" "));
    }

    @Override
    public String qualifiedName() {
        if (packageName != null) {
            return packageName + "." + name();
        }
        return name();
    }

    @Override
    public JDDoc comments() {
        if (declaration.getComment().isPresent() && declaration.getComment().get() instanceof JavadocComment) {
            JavadocComment jc = (JavadocComment) declaration.getComment().get();
            return new JPDoc(StaticJavaParser.parseJavadoc(jc.getContent()));
        }
        return null;
    }

    @Override
    public boolean isClass() {
        return !isInterface() && !isEnum() && !isRecord() && !isAnnotation();
    }

    @Override
    public boolean isInterface() {
        return declaration.isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return declaration.isAnnotationDeclaration();
    }

    @Override
    public boolean isEnum() {
        return declaration.isEnumDeclaration();
    }

    @Override
    public boolean isRecord() {
        return false;
    }

    public JPRootDoc getRoot() {
        return root;
    }

}
