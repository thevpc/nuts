/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndoc.doc;

import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.toolbox.ndoc.doc.java.JPRootDoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public class MdDoclet /*extends Doclet*/ {

    private static String _GLASSES_ICON = "\uD83D\uDC53";
    private static String _BEETLE_ICON = "\uD83D\uDC1E";
    private static String _CAFE_ICON = "\u2615";
    private static String _DRUM_ICON = "\uD83E\uDD41";
    private static String _PENSIL_ICON = "\u270F";
    private static String _PENSIL2_ICON = "\uD83D\uDD8A";
    private static String _CONTROLS_ICON = "\uD83C\uDF9B";
    private static String _ICECUBE_ICON = "\u2744";
    private static String _PAGE_ICON = "\uD83D\uDCC4";
    private static String _MEMO_ICON = "\uD83D\uDCDD";
    private static String _LOUDSPEAKER_ICON = "\uD83D\uDCE2";

    private static String BUG_ICON = _BEETLE_ICON;
    private static String STATIC_ICON = _LOUDSPEAKER_ICON;
    private static String READONLY_ICON = _PAGE_ICON;
    private static String READWRITE_ICON = _MEMO_ICON;
    private static String WRITEONLY_ICON = _PENSIL_ICON;

    private static String CLASS_ICON = _CAFE_ICON;
    private static String CONSTRUCTOR_ICON = "\uD83E\uDE84";
    private static String METHOD_ICON = "\u2699";
    private static String FIELD_ICON = "\uD83D\uDDD2";
    private static String PROPERTY_ICON = _CONTROLS_ICON;
    private static String STATIC_PROPERTY_ICON = STATIC_ICON + PROPERTY_ICON;
    private static String CONST_ICON = STATIC_ICON + _ICECUBE_ICON;
    private static String STATIC_FIELD_ICON = STATIC_ICON + FIELD_ICON;
    private static String STATIC_METHOD_ICON = STATIC_ICON + METHOD_ICON;
    private static String READ_ICON = _GLASSES_ICON;
    private static String WRITE_ICON = _PENSIL_ICON;

    //    private static String PROPERTY_ICON = "\uD83D\uDC41";
//    private static String STATIC_METHOD_ICON = "\uD83D\uDD27";
//    private static String STATIC_PROPERTY_ICON = "\uD83D\uDC40";
//    private static String STATIC_FIELD_ICON = "\uD83C\uDF8A";
//    private static String STATIC_METHOD_ICON = "\uD83E\uDDF2";
//    private static String CONST_ICON = "\uD83E\uDDCA";
//    public static void main(String[] args) {
//        System.out.println("Hello");
////        com.sun.tools.javadoc.Main.main(new String[]{
////            "-doclet", "net.thevpc.nuts.toolbox.ndoc.doc.MdDoclet",
//////            "-docletpath", "/data/public/git/docusaurus-docklet/target/docusaurus-docklet-0.1.jar:/usr/java/jdk1.8.0_211-amd64/lib/tools.jar",
////            "-sourcepath","/data/public/git/nuts/nuts/src/main/java",
////            "net.thevpc.nuts"
////        });
//    }
    public static String getCategory(JDClassDoc c) {
        JDDoc jd = c.comments();
        if (jd != null) {
            String t = jd.getTag("category");
            if (t != null) {
                t = t.trim();
                if (!t.isEmpty()) {
                    return t;
                }
            }
        }
        return "Other";
    }

    static MdElement printClass(JDClassDoc cls) {
        ToClassDoc tcd = new ToClassDoc(cls);
        List<MdElement> seq = new ArrayList<>();
        seq.add(MdFactory.title(2, CLASS_ICON + " " + tcd.getCls().name()));
        String classType = tcd.getCls().isClass() ? "class"
                : tcd.getCls().isAnnotation() ? "@interface"
                : tcd.getCls().isEnum() ? "enum"
                : tcd.getCls().isRecord() ? "record"
                : "class";
        seq.add(MdFactory.code("java", tcd.getCls().modifiers() + " " + classType + " " + tcd.getCls().qualifiedName()));
        if (tcd.getCls().comments() != null) {
            seq.add(tcd.getCls().comments().getDescription());
        }
        if (tcd.getConsts().length > 0) {

            seq.add(MdFactory.title(3, CONST_ICON + " Constant Fields"));
            seq.add(printFields(tcd.getConsts()));
        }

        if (tcd.getStaticFields().length > 0) {
            seq.add(MdFactory.title(3, STATIC_FIELD_ICON + " Static Fields"));
            printFields(tcd.getStaticFields());
        }

        if (tcd.getStaticProperties().length > 0) {
            seq.add(MdFactory.title(3, STATIC_PROPERTY_ICON + " Static Properties"));
            seq.add(printProperties(tcd.getStaticProperties()));
        }

        if (tcd.getStaticMethods().length > 0) {
            seq.add(MdFactory.title(3, STATIC_METHOD_ICON + " Static Methods"));
            seq.add(printMembers(tcd.getStaticMethods()));
        }

        if (tcd.getInstanceFields().length > 0) {
            seq.add(MdFactory.title(3, FIELD_ICON + " Instance Fields"));
            seq.add(printFields(tcd.getInstanceFields()));
        }

        if (tcd.getConstructors().length > 0) {
            seq.add(MdFactory.title(3, CONSTRUCTOR_ICON + " Constructors"));
            seq.add(printMembers(tcd.getConstructors()));
        }

        if (tcd.getInstanceProperties().length > 0) {
            seq.add(MdFactory.title(3, PROPERTY_ICON + " Instance Properties"));
            seq.add(printProperties(tcd.getInstanceProperties()));
        }

        if (tcd.getInstanceMethods().length > 0) {
            seq.add(MdFactory.title(3, METHOD_ICON + " Instance Methods"));
            seq.add(printMembers(tcd.getInstanceMethods()));
        }
        return MdFactory.seq(seq);
    }

    static MdElement printFields(JDFieldDoc[] mems) {
        Arrays.sort(mems, new Comparator<JDFieldDoc>() {
            @Override
            public int compare(JDFieldDoc o1, JDFieldDoc o2) {
                return o1.qualifiedName().compareTo(o2.qualifiedName());
            }
        });
        List<MdElement> ss = new ArrayList<>();
        for (int i = 0; i < mems.length; ++i) {
            JDFieldDoc mem = mems[i];
            ss.add(printField(mem));
        }
        return MdFactory.seq(ss);
    }

    static MdElement printProperties(PropertyDoc[] mems) {
        List<MdElement> ss = new ArrayList<>();
        for (int i = 0; i < mems.length; ++i) {
            PropertyDoc mem = mems[i];
            ss.add(printProperty(mem));
        }
        return MdFactory.seq(ss);
    }

    static MdElement printMembers(JDExecutableMemberDoc[] mems) {
        Arrays.sort(mems, new Comparator<JDExecutableMemberDoc>() {
            @Override
            public int compare(JDExecutableMemberDoc o1, JDExecutableMemberDoc o2) {
                return o1.qualifiedName().compareTo(o2.qualifiedName());
            }
        });
        List<MdElement> seq = new ArrayList<>();
        for (int i = 0; i < mems.length; ++i) {
            JDExecutableMemberDoc mem = mems[i];
            seq.add(printMember(mem));
        }
        return MdFactory.seq(seq);
    }

    static MdElement printMember(JDExecutableMemberDoc mem) {
        String icon = null;
        if (mem instanceof JDConstructorDoc) {
            icon = CONSTRUCTOR_ICON;
        } else {
            JDMethodDoc md = (JDMethodDoc) mem;
            if (md.isStatic()) {
                icon = STATIC_METHOD_ICON;
            } else {
                icon = METHOD_ICON;
            }
        }
        JDType returnType = null;
        if (mem instanceof JDMethodDoc) {
            JDMethodDoc dd = (JDMethodDoc) mem;
            returnType = dd.returnType();
        }
        List<MdElement> seq = new ArrayList<>();
        seq.add(MdFactory.title(4, icon + " " + sig(mem)));
        if (mem.commentText() != null) {
            seq.add(mem.commentText().getDescription());
        }
        seq.add(MdFactory.br());
        seq.add(MdFactory.code("java", sig(mem, true, true)));

        JDParameter[] params = mem.parameters();
        for (JDParameter param : params) {
            seq.add(MdFactory.ul(1, MdFactory.seqInline(
                    MdFactory.bold(
                            MdFactory.text(stype(param.type()) + " " + param.name())
                    ),
                    MdFactory.text(" : " + (param.getJavadocContent() == null ? "" : param.getJavadocContent()))
                    ))
            );
        }
        return MdFactory.seq(seq);
    }

    private static String resolveComment(String paramName, JDParamTag[] tags) {
        for (JDParamTag tag : tags) {
            if (tag.parameterName().equals(paramName)) {
                return tag.parameterComment();
            }
        }
        return "";
    }

    static MdElement printField(JDFieldDoc mem) {
        String e = mem.constantValueExpression();
        String icon = FIELD_ICON;
        if (mem.isStatic()) {
            if (mem.isFinal()) {
                icon = CONST_ICON;
            } else {
                icon = STATIC_FIELD_ICON;
            }
        }
        return MdFactory.seq(
                MdFactory.title(4, icon + " " + mem.name()),
                MdFactory.code("java", mem.modifiers() + " " + stype(mem.type()) + " " + mem.name() + (mem.constantValueExpression() == null ? "" : (" = " + mem.constantValueExpression())))
        );
    }

    static MdElement printProperty(PropertyDoc mem) {
        String icon = PROPERTY_ICON;
        if (mem.isStatic()) {
            icon = STATIC_PROPERTY_ICON;
        }
        if (mem.isRW()) {
            icon = READWRITE_ICON + icon;
        } else if (mem.isRO()) {
            icon = READONLY_ICON + icon;
        } else if (mem.isWO()) {
            icon = WRITEONLY_ICON + icon;
        }
        List<MdElement> seq = new ArrayList<>();
        seq.add(MdFactory.title(4, icon + " " + mem.name));
        if (mem.commentText() != null) {
            seq.add(mem.commentText().getDescription());
        }
        StringBuilder code = new StringBuilder();
        if (mem.isRW()) {
            code.append("[read-write] " + stype(mem.type()) + " " + mem.setter.modifiers() + " " + mem.name).append("\n");
            if (mem.field != null) {
                code.append(mem.field.modifiers() + " " + stype(mem.field.type()) + " " + mem.field.name() + (mem.field.constantValueExpression() == null ? "" : (" = " + mem.field.constantValueExpression()))).append("\n");
            }
            if (mem.getter != null) {
                code.append(mem.getter.modifiers() + " " + sig2(mem.getter)).append("\n");
            }
            if (mem.setter != null) {
                code.append(mem.setter.modifiers() + " " + sig2(mem.setter)).append("\n");
            }

        } else if (mem.isWO()) {
            code.append("[write-only] " + stype(mem.type()) + " " + mem.setter.modifiers() + " " + mem.name).append("\n");
            if (mem.field != null) {
                code.append(mem.field.modifiers() + " " + stype(mem.field.type()) + " " + mem.field.name() + (mem.field.constantValueExpression() == null ? "" : (" = " + mem.field.constantValueExpression()))).append("\n");
            }
            if (mem.getter != null) {
                code.append(mem.getter.modifiers() + " " + sig2(mem.getter)).append("\n");
            }
            if (mem.setter != null) {
                code.append(mem.setter.modifiers() + " " + sig2(mem.setter)).append("\n");
            }

        } else if (mem.isRO()) {
            code.append("[read-only] " + mem.getter.modifiers() + " " + stype(mem.type()) + " " + mem.name).append("\n");
            if (mem.field != null) {
                code.append(mem.field.modifiers() + " " + stype(mem.field.type()) + " " + mem.field.name() + (mem.field.constantValueExpression() == null ? "" : (" = " + mem.field.constantValueExpression()))).append("\n");
            }
            if (mem.getter != null) {
                code.append(mem.getter.modifiers() + " " + sig2(mem.getter)).append("\n");
            }
            if (mem.setter != null) {
                code.append(mem.setter.modifiers() + " " + sig2(mem.setter)).append("\n");
            }

        } else {
            throw new IllegalArgumentException("Should never happen");
        }
        seq.add(MdFactory.code("java", code.toString()));
        return MdFactory.seq(seq);
    }

    static String stype(JDType mem) {
        if (mem == null) {
            return "?";
        }
        if (mem.isPrimitive()) {
            return mem.toString();
        }
        String tn = mem.toString();
        if (tn.indexOf('.') > 0) {
            if (tn.indexOf('<') < 0) {
                return escape(tn.substring(tn.lastIndexOf('.') + 1));
            }
        }
        return escape(mem.toString());
    }

    static String sig2(JDExecutableMemberDoc mem) {
        String s = sig(mem);
        if (mem instanceof JDMethodDoc) {
            JDMethodDoc md = (JDMethodDoc) mem;
            if (md.returnType() != null) {
                return stype(md.returnType()) + " " + s;
            }
            return stype(md.returnType()) + " " + s;
        }
        return s;
    }

    static String sig(JDExecutableMemberDoc mem) {
        StringBuilder sb = new StringBuilder();
        sb.append(mem.name());
        sb.append("(");
        for (int i = 0; i < mem.parameters().length; i++) {
            JDParameter parameter = mem.parameters()[i];
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameter.name());
        }
        sb.append(")");
        return sb.toString();
    }

    static String sig(JDExecutableMemberDoc mem, boolean includeReturn, boolean includeTypes) {
        StringBuilder sb = new StringBuilder();
        if (includeReturn) {
            if (mem instanceof JDMethodDoc) {
                JDMethodDoc md = (JDMethodDoc) mem;
                if (md.returnType() != null) {
                    sb.append(stype(md.returnType()));
                    sb.append(" ");
                }
            }
        }
        sb.append(mem.name());
        sb.append("(");
        for (int i = 0; i < mem.parameters().length; i++) {
            JDParameter parameter = mem.parameters()[i];
            if (i > 0) {
                sb.append(", ");
            }
            if (includeTypes) {
                sb.append(stype(parameter.type()));
                sb.append(" ");
            }
            sb.append(parameter.name());
        }
        sb.append(")");
        return sb.toString();
    }

    static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '<':
                case '>':
                case '\'':
                case '{':
                case '}':
                case '@':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public boolean start(MdDocletConfig config) {
        JPRootDoc root = new JPRootDoc();
        String[] packages = config.getPackages();
        Predicate<String> packageFilter = packages.length == 0 ? (x -> true) : x -> {
            for (String p : packages) {
                if (p.equals("*")) {
                    return true;
                }
                if (p.equals(".*")) {
                    String sp = p.substring(0, p.length() - 2);
                    return x.equals(sp) || x.startsWith(sp + ".");
                }
                if (p.equals(".**")) {
                    String sp = p.substring(0, p.length() - 3);
                    return x.equals(sp) || x.startsWith(sp + ".");
                }
                return x.equals(p);
            }
            return false;
        };
        for (String s : config.getSources()) {
            root.parseSrcFolder(Paths.get(s), packageFilter);
        }

        JDClassDoc[] classes = root.classes();
        Arrays.sort(classes, new Comparator<JDClassDoc>() {
            @Override
            public int compare(JDClassDoc o1, JDClassDoc o2) {
                return o1.qualifiedName().compareTo(o2.qualifiedName());
            }
        });
        Map<String, List<JDClassDoc>> categories = new HashMap<>();
        for (int i = 0; i < classes.length; ++i) {
            String c = getCategory(classes[i]);
            List<JDClassDoc> li = (List<JDClassDoc>) categories.computeIfAbsent(c, new Function<String, List<JDClassDoc>>() {
                @Override
                public List<JDClassDoc> apply(String t) {
                    return new ArrayList<JDClassDoc>();
                }
            });
            li.add(classes[i]);
        }
        for (Map.Entry<String, List<JDClassDoc>> entry : categories.entrySet()) {
            String name = entry.getKey();
            String id = "javadoc_" + name.replace(' ', '_');
            File file = new File(config.getTarget(), entry.getKey() + ".md");
            try {
                System.out.println("generating " + file.getCanonicalPath());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            List<MdElement> doc=new ArrayList<>();
//            MdDocumentBuilder doc = MdFactory.document();
//            doc.setId(id);
//            doc.setTitle(name);
            for (JDClassDoc classDoc : entry.getValue()) {
                doc.add(printClass(classDoc));
            }
            try (MdWriter out = MdFactory.createWriter(config.getBackend(), new FileWriter(file))) {
                out.write(new MdDocument(
                        id,name,null,null,null,null,MdFactory.seq(doc)
                ));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return true;
    }
}
