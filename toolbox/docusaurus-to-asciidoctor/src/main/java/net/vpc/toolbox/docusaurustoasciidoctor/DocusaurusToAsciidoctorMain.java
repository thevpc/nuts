package net.vpc.toolbox.docusaurustoasciidoctor;

import java.io.File;
import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.commons.md.convert.Docusaurus2Asciidoctor;

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
