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
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import net.thevpc.nuts.toolbox.ndoc.doc.JDConstructorDoc;
import net.thevpc.nuts.toolbox.ndoc.doc.JDDoc;
import net.thevpc.nuts.toolbox.ndoc.doc.JDParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class JPConstructorDoc implements JDConstructorDoc {

    private ConstructorDeclaration declaration;
    private JPClassDoc cls;

    public JPConstructorDoc(ConstructorDeclaration declaration, JPClassDoc cls) {
        this.declaration = declaration;
        this.cls = cls;
    }

    @Override
    public JDParameter[] parameters() {
        List<JDParameter> param = new ArrayList<>();
        for (Parameter parameter : declaration.getParameters()) {
            String n = parameter.getName().toString();
            String javadocContent = null;
            Javadoc jd = declaration.getJavadoc().orElse(null);
            if (jd != null) {
                for (JavadocBlockTag blockTag : jd.getBlockTags()) {
                    if (blockTag.getType() == JavadocBlockTag.Type.PARAM && blockTag.getName().orElse("").equals(n)) {
                        javadocContent = blockTag.getContent().toText();
                    }
                }
            }
            param.add(new JPParameter(parameter, javadocContent));
        }
        return param.toArray(new JDParameter[0]);
    }

    @Override
    public String name() {
        return declaration.getName().toString();
    }

    @Override
    public String qualifiedName() {
        return cls.qualifiedName() + "." + name();
    }

    @Override
    public String modifiers() {
        return Arrays.stream(declaration.getModifiers().toArray()).map(x -> x.toString()).collect(Collectors.joining(" "));
    }

    @Override
    public JDDoc commentText() {
        if (declaration.getComment().isPresent() && declaration.getComment().get() instanceof JavadocComment) {
            JavadocComment jc = (JavadocComment) declaration.getComment().get();
            return new JPDoc(StaticJavaParser.parseJavadoc(jc.getContent()));
        }
        return null;
    }

}
