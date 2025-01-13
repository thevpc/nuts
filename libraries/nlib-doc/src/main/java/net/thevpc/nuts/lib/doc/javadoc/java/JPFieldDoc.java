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
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import net.thevpc.nuts.lib.doc.javadoc.JDDoc;
import net.thevpc.nuts.lib.doc.javadoc.JDFieldDoc;
import net.thevpc.nuts.lib.doc.javadoc.JDType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class JPFieldDoc implements JDFieldDoc {

    private FieldDeclaration fieldDeclaration;
    private VariableDeclarator variableDeclarator;
    private JPClassDoc cls;

    public JPFieldDoc(FieldDeclaration fieldDeclaration, VariableDeclarator variableDeclarator, JPClassDoc cls) {
        this.fieldDeclaration = fieldDeclaration;
        this.variableDeclarator = variableDeclarator;
        this.cls = cls;
    }

    @Override
    public boolean isStatic() {
        for (Modifier modifier : fieldDeclaration.getModifiers()) {
            if (modifier.getKeyword() == Modifier.Keyword.STATIC) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String name() {
        return variableDeclarator.getName().getIdentifier();
    }

    @Override
    public String qualifiedName() {
        return cls.qualifiedName() + "." + name();
    }

    @Override
    public JDType type() {
        return new JPType(variableDeclarator.getType());
    }

    @Override
    public String constantValueExpression() {
        Expression e = variableDeclarator.getInitializer().orElse(null);
        return e == null ? null : e.toString();
    }

    @Override
    public boolean isFinal() {
        for (Modifier modifier : fieldDeclaration.getModifiers()) {
            if (modifier.getKeyword() == Modifier.Keyword.FINAL) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String modifiers() {
        return Arrays.stream(fieldDeclaration.getModifiers().toArray()).map(x -> x.toString()).collect(Collectors.joining(" "));
    }

    @Override
    public JDDoc commentText() {
        if (fieldDeclaration.getComment().isPresent() && fieldDeclaration.getComment().get() instanceof JavadocComment) {
            JavadocComment jc = (JavadocComment) fieldDeclaration.getComment().get();
            return new JPDoc(StaticJavaParser.parseJavadoc(jc.getContent()));
        }
        return null;
    }

}
