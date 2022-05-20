package net.thevpc.nuts.tutorial.naf.spring.helloworld;


import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.io.NutsPrintStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is just an example to demonstrate how you can inject
 * NutsSession and other objects in your spring application
 */
@Component
public class MyBean {
    @Autowired
    private NutsSession session;
    @Autowired
    private NutsWorkspace workspace;
    @Autowired
    private NutsApplicationContext applicationContext;
    @Autowired
    private NutsPrintStream out;

    public void hello() {
        out.println("##Hello##");
    }
}
