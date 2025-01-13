/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.javadoc;

//import com.sun.javadoc.ClassDoc;
//import com.sun.javadoc.ConstructorDoc;
//import com.sun.javadoc.FieldDoc;
//import com.sun.javadoc.MethodDoc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class ToClassDoc {

    private JDClassDoc cls;
    private List<PropertyDoc> staticProperties = new ArrayList<>();
    private List<PropertyDoc> instanceProperties = new ArrayList<>();
    private List<JDFieldDoc> consts = new ArrayList<>();
    private List<JDFieldDoc> staticFields = new ArrayList<>();
    private List<JDFieldDoc> instanceFields = new ArrayList<>();
    private List<JDMethodDoc> staticMethods = new ArrayList<>();
    private List<JDMethodDoc> instanceMethods = new ArrayList<>();
    private List<JDConstructorDoc> constructors = new ArrayList<>();

    public ToClassDoc(JDClassDoc cls) {
        this.cls = cls;
        for (JDFieldDoc field : cls.fields()) {
            if (field.isStatic() && field.isFinal()) {
                //constant!
                consts.add(field);
            } else if (field.isStatic()) {
                staticFields.add(field);
            } else {
                instanceFields.add(field);
            }
        }
        for (JDConstructorDoc constructor : cls.constructors()) {
            constructors.add(constructor);
        }
        for (JDMethodDoc m : cls.methods()) {
            PropertyKind r = resolvePropertyKind(m);
            if (r == null) {
                if (m.isStatic()) {
                    staticMethods.add(m);
                } else {
                    instanceMethods.add(m);
                }
            } else {

                PropertyDoc property0 = null;
                if (m.isStatic()) {

                    for (Iterator<PropertyDoc> it = staticProperties.iterator(); it.hasNext();) {
                        PropertyDoc property = it.next();
                        if (property.name.equals(r.name)) {
                            property0 = property;
                            break;
                        }
                    }
                    if (property0 == null) {
                        property0 = new PropertyDoc();
                        property0.name = r.name;
                        staticProperties.add(property0);
                    }
                    if (r.kind.equals("set")) {
                        property0.setter = m;
                    } else {
                        property0.getter = m;
                    }

                    for (Iterator<JDFieldDoc> it = staticFields.iterator(); it.hasNext();) {
                        JDFieldDoc field = it.next();
                        if (field.name().equals(r.name)) {
                            property0.field = field;
                            it.remove();
                            break;
                        }
                    }
                } else {
                    for (Iterator<PropertyDoc> it = instanceProperties.iterator(); it.hasNext();) {
                        PropertyDoc property = it.next();
                        if (property.name.equals(r.name)) {
                            property0 = property;
                            break;
                        }
                    }
                    if (property0 == null) {
                        property0 = new PropertyDoc();
                        property0.name = r.name;
                        instanceProperties.add(property0);
                    }
                    if (r.kind.equals("set")) {
                        property0.setter = m;
                    } else {
                        property0.getter = m;
                    }

                    for (Iterator<JDFieldDoc> it = instanceFields.iterator(); it.hasNext();) {
                        JDFieldDoc field = it.next();
                        if (field.name().equals(r.name)) {
                            property0.field = field;
                            it.remove();
                            break;
                        }
                    }
                }
            }
        }
        staticProperties.sort(new Comparator<PropertyDoc>() {
            @Override
            public int compare(PropertyDoc o1, PropertyDoc o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        instanceProperties.sort(new Comparator<PropertyDoc>() {
            @Override
            public int compare(PropertyDoc o1, PropertyDoc o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        consts.sort(new Comparator<JDFieldDoc>() {
            @Override
            public int compare(JDFieldDoc o1, JDFieldDoc o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        staticFields.sort(new Comparator<JDFieldDoc>() {
            @Override
            public int compare(JDFieldDoc o1, JDFieldDoc o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        instanceFields.sort(new Comparator<JDFieldDoc>() {
            @Override
            public int compare(JDFieldDoc o1, JDFieldDoc o2) {
                return o1.name().compareTo(o2.name());
            }
        });
        staticMethods.sort(new Comparator<JDMethodDoc>() {
            @Override
            public int compare(JDMethodDoc o1, JDMethodDoc o2) {
                int t = o1.name().compareTo(o2.name());
                if (t != 0) {
                    return t;
                }
                t = o1.parameters().length - o2.parameters().length;
                if (t != 0) {
                    return t;
                }
                for (int i = 0; i < o1.parameters().length; i++) {
                    t = o1.parameters()[i].name().compareTo(o2.parameters()[i].name());
                    if (t != 0) {
                        return t;
                    }
                }
                return t;
            }
        });
        instanceMethods.sort(new Comparator<JDMethodDoc>() {
            @Override
            public int compare(JDMethodDoc o1, JDMethodDoc o2) {
                int t = o1.name().compareTo(o2.name());
                if (t != 0) {
                    return t;
                }
                t = o1.parameters().length - o2.parameters().length;
                if (t != 0) {
                    return t;
                }
                for (int i = 0; i < o1.parameters().length; i++) {
                    t = o1.parameters()[i].name().compareTo(o2.parameters()[i].name());
                    if (t != 0) {
                        return t;
                    }
                }
                return t;
            }
        });
        constructors.sort(new Comparator<JDConstructorDoc>() {
            @Override
            public int compare(JDConstructorDoc o1, JDConstructorDoc o2) {
                int t = o1.name().compareTo(o2.name());
                if (t != 0) {
                    return t;
                }
                t = o1.parameters().length - o2.parameters().length;
                if (t != 0) {
                    return t;
                }
                for (int i = 0; i < o1.parameters().length; i++) {
                    t = o1.parameters()[i].name().compareTo(o2.parameters()[i].name());
                    if (t != 0) {
                        return t;
                    }
                }
                return t;
            }
        });
    }

    public PropertyDoc[] getStaticProperties() {
        return staticProperties.toArray(new PropertyDoc[0]);
    }

    public PropertyDoc[] getInstanceProperties() {
        return instanceProperties.toArray(new PropertyDoc[0]);
    }

    public JDFieldDoc[] getConsts() {
        return consts.toArray(new JDFieldDoc[0]);
    }

    public JDFieldDoc[] getStaticFields() {
        return staticFields.toArray(new JDFieldDoc[0]);
    }

    public JDFieldDoc[] getInstanceFields() {
        return instanceFields.toArray(new JDFieldDoc[0]);
    }

    public JDMethodDoc[] getStaticMethods() {
        return staticMethods.toArray(new JDMethodDoc[0]);
    }

    public JDMethodDoc[] getInstanceMethods() {
        return instanceMethods.toArray(new JDMethodDoc[0]);
    }

    public JDConstructorDoc[] getConstructors() {
        return constructors.toArray(new JDConstructorDoc[0]);
    }

    public PropertyKind resolvePropertyKind(JDMethodDoc n) {
        JDParameter[] parameters = n.parameters();
        if (n.name().startsWith("get")) {
            if (parameters.length == 0) {
                String a = n.name().substring(3);
                if (a.length() > 0) {
                    if (Character.isUpperCase(a.charAt(0))) {
                        return new PropertyKind("get", Character.toLowerCase(a.charAt(0)) + a.substring(1));
                    } else {
                        //fix me later
                        return new PropertyKind("get", Character.toLowerCase(a.charAt(0)) + a.substring(1));
                    }
                }
            }
        }
        if (n.name().startsWith("is")) {
            if (parameters.length == 0) {
                String a = n.name().substring(2);
                if (a.length() > 0) {
                    if (Character.isUpperCase(a.charAt(0))) {
                        return new PropertyKind("get", Character.toLowerCase(a.charAt(0)) + a.substring(1));
                    } else {
                        //fix me later
                        return new PropertyKind("get", Character.toLowerCase(a.charAt(0)) + a.substring(1));
                    }
                }
            }
        }
        if (n.name().startsWith("set")) {
            if (parameters.length == 1) {
                String a = n.name().substring(3);
                if (a.length() > 0) {
                    if (Character.isUpperCase(a.charAt(0))) {
                        return new PropertyKind("set", Character.toLowerCase(a.charAt(0)) + a.substring(1));
                    } else {
                        //fix me later
                        return new PropertyKind("set", Character.toLowerCase(a.charAt(0)) + a.substring(1));
                    }
                }
            }
        }
        return null;
    }

    public JDClassDoc getCls() {
        return cls;
    }
    
}
