package net.thevpc.nuts.reflect;

import net.thevpc.nuts.core.NClassLoaderNode;
import net.thevpc.nuts.internal.rpi.NReflectRPI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.stream.Stream;

/**
 * Nuts mutable ClassLoader contract Interface.
 * Instances of this class must extend Java's Classloader.
 * <code>asClassLoader</code> is the way this instance is cast to ClassLoader
 */
public interface NClassLoader {
    static NClassLoader of(){
        return NReflectRPI.of().createClassLoader(null,null);
    }
    static NClassLoader of(String name,ClassLoader parent){
        return NReflectRPI.of().createClassLoader(name,parent);
    }

    default ClassLoader asClassLoader(){
        return (ClassLoader) this;
    }

    boolean contains(NClassLoaderNode node, boolean deep);

    NClassLoaderNode search(NClassLoaderNode node, boolean deep) ;

    boolean add(NClassLoaderNode node) ;

    Class<?> loadClass(String name) throws ClassNotFoundException ;
    URL getResource(String name) ;
    Enumeration<URL> getResources(String name) throws IOException;
    InputStream getResourceAsStream(String name);
    ClassLoader getParent();
}
