/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar.commands;

import java.io.File;
import java.util.function.Predicate;

/**
 * <pre>
 * DiffCommandJar diff=new DiffCommandJar(jarfile1,jarfile2);
 * DiffResult result=diff.getResult();
 * for(DiffItem i:result){
 *   switch(i.getItemType()){
 *     case "java-class": {
 *          String className=((DiffItemJavaClass)i).getclassName();
 *          if(i.getDiffStatus()==DiffStatus.ADDED){
 *             System.out.println(className +" is a new class");
 *          }
 *          System.out.println(i);
 *          break;
 *     }
 *     default: {
 *          System.out.println(i);
 *          break;
 *     }
 *   }
 * }
 * </pre>
 *
 * @author thevpc
 */
public class DiffCommandJar extends DiffCommandZip {
    public static final DiffCommandJar INSTANCE = new DiffCommandJar();

    private static final Predicate<String> DEFAULT_PATH_FILTER_IMPL = x ->
            !x.equals(".netbeans_automatic_build");

    protected DiffCommandJar() {
        super("jar");
    }

    @Override
    protected boolean acceptEntry(String entryName) {
        return DEFAULT_PATH_FILTER_IMPL.test(entryName);
    }

    @Override
    public int acceptInput(Object input) {
//        if (input instanceof InputStream) {
//            return 1;
//        }
//        if (input instanceof byte[]) {
//            return 1;
//        }
        if (input instanceof File) {
            File f = ((File) input);
            String n = f.getName().toLowerCase();
            if (n.endsWith(".zip")) {
                return 50;
            }
            if (n.endsWith(".jar") || n.endsWith(".war") || n.endsWith(".ear")) {
                return 100;
            }
        }
        return -1;
    }

}
