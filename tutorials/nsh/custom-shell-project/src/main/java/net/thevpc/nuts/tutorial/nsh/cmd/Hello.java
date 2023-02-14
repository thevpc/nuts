package net.thevpc.nuts.tutorial.nsh.cmd;

import java.util.HashMap;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

/**
 * any command you want to support. The simplest way is to extend
 * SimpleJShellBuiltin. Don't you forget to add the fully qualified class name
 * to the file
 * <pre>
 * src/main/resources/META-INF/services/net.thevpc.nuts.toolbox.nsh.jshell.JShellBuildtin
 * </pre>
 *
 * @author vpc
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class Hello extends SimpleJShellBuiltin {

    /**
     * simple constructor, it defines mainly what is the command name (here
     * 'hello') and a supplier used create options instance (here an internal/private
     * class 'Options')
     */
    public Hello() {
        super("hello", Options::new);
    }

    /**
     * your special Options class. It can be private.
     */
    private static class Options {

        // an example of option that will be resolved to
        // the --who option
        String who;

        // another example of option which will be resolved as 'boolean'
        // it can be passed as '--complex' or '-c'
        boolean complex;
    }

    /**
     * this method is called multiple times to process all the command line
     * arguments. It just need to process the 'next'/'first' option if supported.
     *
     * @param cmdline cmdline to process partially
     * @param ctx     shell context
     * @return true if the option is processed
     */
    @Override
    protected boolean configureFirst(NCmdLine cmdline, JShellExecutionContext ctx) {
        //get an instance of the current options object we are filling.
        Options o = ctx.getOptions();
        //get the next option (without consuming it)
        // arguments can be options in the form --key=value or -k=value
        //if not an option, the key will be resolved to the hole argument string
        switch (cmdline.peek().get().key()) {
            case "--who": {
                //consume the next argument
                //which is of the form
                //        --who=me
                //        or (using spaces)
                //        --who me
                cmdline.withNextEntry((v, aa, session) -> o.who = v);
                //return true to say that the option was successfully processed
                return true;
            }
            case "-c":
            case "--complex": {
                //consume the next argument
                //which is of the form
                //        -c
                //        or
                //        --complex
                //        it can even be negated with '~' or '!'
                //        --!complex
                cmdline.withNextFlag((value, arg, session) -> o.complex = value);
                //return true to say that the option was successfully processed
                return true;
            }
        }
        //return false to say that the option is not recognized
        return false;
    }

    @Override
    protected void execBuiltin(NCmdLine cmdline, JShellExecutionContext ctx) {
        Options o = ctx.getOptions();
        NSession session = ctx.getSession();
        if (o.complex) {
            // print any object (it can be a simple string of course)
            // it will be formatted according to your "output format" (aka json, or any thing else)
            // you can try running your app with
            // nuts my-shell --json -c hello --complex --who=NoneOfYourBusiness
            session.out().print(new HashMap.SimpleEntry<String, String>(
                    "hello",
                    o.who == null ? System.getProperty("user.home") : o.who
            ));
        } else {
            // print a formatted string.
            // the name will be in blue!
            // nuts my-shell --json -c hello --~complex --who=NoneOfYourBusiness
            // '##' is used to enclose the text we want in blue!
            // '###' for another color
            // you can see NTF for more details on coloring
            // or just issue "nuts help --ntf" in your commandline
            session.out().println(NMsg.ofC("hello ##%s##", o.who == null ? System.getProperty("user.home") : o.who));
        }
    }

}
