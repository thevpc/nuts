/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.javadoc;

//import com.sun.javadoc.FieldDoc;

//import com.sun.javadoc.MethodDoc;
//import com.sun.javadoc.Type;

/**
 *
 * @author thevpc
 */
public class PropertyDoc {

    public JDMethodDoc getter;
    public JDMethodDoc setter;
    public JDFieldDoc field;
    public String name;
    public String type;

    public JDType type() {
        if (field != null && field.type() != null) {
            return field.type();
        }
        if (setter != null && setter.returnType() != null) {
            return setter.returnType();
        }
        if (getter != null && getter.returnType() != null) {
            return getter.returnType();
        }
        return null;
    }

    public JDDoc commentText() {
        if (field != null && field.commentText()!=null) {
            return field.commentText();
        }
        if (setter != null && setter.commentText()!=null) {
            return setter.commentText();
        }
        if (getter != null && getter.commentText()!=null) {
            return getter.commentText();
        }
        return null;
    }

    public boolean isRW() {
        return setter != null && getter != null;
    }

    public boolean isWO() {
        return setter != null && getter == null;
    }

    public boolean isRO() {
        return setter == null && getter != null;
    }

    public boolean isStatic() {
        if (field != null && field.type() != null) {
            return field.isStatic();
        }
        if (setter != null && setter.returnType() != null) {
            return setter.isStatic();
        }
        if (getter != null && getter.returnType() != null) {
            return getter.isStatic();
        }
        return false;
    }

}
