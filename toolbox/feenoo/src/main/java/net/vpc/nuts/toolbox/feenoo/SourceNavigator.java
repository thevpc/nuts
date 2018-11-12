/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.nuts.toolbox.feenoo;

import net.vpc.nuts.toolbox.feenoo.sources.JavaTypeSource;
import net.vpc.nuts.toolbox.feenoo.sources.SourceFactory;

import java.io.File;

/**
 *
 * @author vpc
 */
public class SourceNavigator {

    public static void main(String[] args) {
         navigate(SourceFactory.create(new File("/home/vpc/NetBeansProjects/IA3/")),new SourceProcessor() {

            @Override
            public boolean process(Source source) {
                System.out.println(source.getClass().getSimpleName()+"   "+source.getExternalPath());
                if(source instanceof JavaTypeSource){
                    System.out.println("     "+((JavaTypeSource)source).getClassName());
                }
                return true;
            }
        });
    }
    
    private static class ExitException extends RuntimeException {

    }

    public static void navigate(Source s, SourceProcessor processor) {
        navigate0(s, processor);
    }

    public static void navigate0(Source s, SourceProcessor processor) {
        if (!processor.process(s)) {
            throw new ExitException();
        }
        for (Source children : s.getChildren()) {
            navigate0(children, processor);
        }
    }
}
