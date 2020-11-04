package net.thevpc.toolbox.docusaurustoasciidoctor;

import java.io.File;
import net.thevpc.nuts.NutsApplication;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.commons.md.convert.Docusaurus2Asciidoctor;

public class DocusaurusToAsciidoctorMain extends NutsApplication {


    public static void main(String[] args) {
        new DocusaurusToAsciidoctorMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArguments();
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        do {
            if (appContext.configureFirst(cmdLine)) {
                //
            }else{
                cmdLine.setCommandName("docusaurus-to-asciidoctor").unexpectedArgument();
            }            
        } while (cmdLine.hasNext());
//        cmdLine.required();
        
        new Docusaurus2Asciidoctor(new File(".")).run();
        
    }

}
