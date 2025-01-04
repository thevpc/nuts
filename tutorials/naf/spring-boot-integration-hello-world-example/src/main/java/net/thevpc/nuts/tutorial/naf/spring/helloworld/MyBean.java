package net.thevpc.nuts.tutorial.naf.spring.helloworld;


import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NPrintStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is just an example to demonstrate how you can inject
 * NSession and other objects in your spring application
 */
@Component
public class MyBean {
    @Autowired
    private NSession session;
    @Autowired
    private NWorkspace workspace;
    @Autowired
    private NPrintStream out;

    public void hello() {
        out.println("##Hello##");
    }
}
