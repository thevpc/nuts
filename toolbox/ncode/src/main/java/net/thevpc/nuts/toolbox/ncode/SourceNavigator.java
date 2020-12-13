/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode;

/**
 * @author thevpc
 */
public class SourceNavigator {

//    public static void main(String[] args) {
//         navigate(SourceFactory.create(new File("/home/vpc/NetBeansProjects/IA3/")),new SourceProcessor() {
//
//            @Override
//            public boolean process(Source source) {
//                System.out.println(source.getClass().getSimpleName()+"   "+source.getExternalPath());
//                if(source instanceof JavaTypeSource){
//                    System.out.println("     "+((JavaTypeSource)source).getClassName());
//                }
//                return true;
//            }
//        });
//    }
    private static class ExitException extends RuntimeException {

    }

    public static void navigate(Source s, SourceFilter filter, SourceProcessor processor) {
        try {
            navigate0(s, filter, processor);
        } catch (ExitException ex) {
            //
        }
    }

    public static void navigate0(Source s, SourceFilter filter, SourceProcessor processor) {
        if (filter != null && !filter.accept(s)) {
            throw new ExitException();
        }
        processor.process(s);
        if (filter != null && !filter.lookInto(s)) {
            throw new ExitException();
        }
        for (Source children : s.getChildren()) {
            navigate0(children, filter, processor);
        }

    }
}
