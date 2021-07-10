package net.thevpc.nuts.test.md;

import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.MdFactory;
import net.thevpc.nuts.lib.md.MdParser;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusMdParser;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

public class TestDocusaurusParser {
    @Test
    public void test(){
        String s="\n" +
                "## Where can I find Documentation about the Project\n" +
                "Mainly all of the documentation car be found in 2 places:\n" +
                "\n" +
                "* this website: it includes both user documentation and javadocs (code documentation)\n" +
                "* each command help option. when you type \n" +
                "  ```sh \n" +
                "  nuts --help\n" +
                "  ``` \n" +
                "  or \n" +
                "  ```sh \n" +
                "  nsh --help\n" +
                "  ``` \n" +
                "  you will get more details on nuts or on the tool (here nsh)\n"
;
        MdParser parser= MdFactory.createParser("docusaurus",new StringReader(s));
        MdElement e = parser.parse();
        System.out.println(e);
    }
    @Test
    public void test2(){
        String s="\n" +
                "## Where can I find Documentation about the Project\n" +
                "Mainly all of the documentation car be found in 2 places:\n" +
                "\n" +
                "* this website: it includes both user documentation and javadocs (code documentation)\n" +
                "* each command help option. when you type \n" +
                "  ```sh \n" +
                "  nuts --help\n" +
                "  ``` \n" +
                "  or \n" +
                "  ```sh \n" +
                "  nsh --help\n" +
                "  ``` \n" +
                "  you will get more details on nuts or on the tool (here nsh)\n"
;
        MdParser parser= new DocusaurusMdParser(new StringReader(s));
        MdElement e = parser.parse();
        System.out.println(e);
    }

    @Test
    public void test3(){
//        String s="* **config** : defines the" ;
//        String s="id: install-cmd" ;
//        String s="id_test: _install_" ;
//        String s="Or, you can also use NAF (**```nuts```** Application Framework) make your application full featured \"Nuts aware\" application." ;
        String s="* Create Options :\n" +
                "    * --skip-companions\n" +
                "    * --archetype\n" +
                "    * --store-strategy\n" +
                "    * --standalone\n" ;
        MdParser parser= new DocusaurusMdParser(new StringReader(s));
        MdElement e = parser.parse();
        System.out.println(e);
    }

    @Test
    public void test4(){
//        String file="/data/git/nuts/website/.dir-template/src/docs/intro/installation.md";
        String file="/data/git/nuts/website/.dir-template/src/docs/cmd/install.md";
        try(FileReader fr=new FileReader(file)) {
            MdParser parser = new DocusaurusMdParser(fr);
            MdElement e = parser.parse();
            System.out.println(e);
        }catch (IOException ex){
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test5(){
        String s="---\n" +
                "Here are all **```nuts```** requirements :"
;
        MdParser parser= new DocusaurusMdParser(new StringReader(s));
        MdElement e = parser.parse();
        System.out.println(e);
    }

    @Test
    public void test6(){
        String s="---\n" +
                "id: filesystem\n" +
                "title: File system\n" +
                "sidebar_label: File system\n" +
                "---\n" +
                "\n" +
                "```\n" +
                "     __        __    \n" +
                "  /\\ \\ \\ _  __/ /______\n" +
                " /  \\/ / / / / __/ ___/\n" +
                "/ /\\  / /_/ / /_(__  )\n" +
                "\\_\\ \\/\\__,_/\\__/____/    version v${apiVersion}\n" +
                "```\n" +
                "\n" +
                "**```nuts```** manages multiple workspaces. It has a default one located at ~/.config/nuts (~ is the user home directory). Each workspace handles a database and files related to the installed applications. The workspace has a specific layout to store different types of files relatives to your applications. **nuts** is largely inspired by [XDG Base Directory Specification](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html) and hence defines several  store locations for each file type. Such organization of folders is called Layout and is dependent on the current operating system, the layout strategy and any custom configuration.\n" +
                "\n" +
                "## Store Locations\n" +
                "Supported Store Locations are : \n" +
                "**```nuts```** File System defines the following folders :\n" +
                "* **config** : defines the base directory relative to which application specific configuration files should be stored.\n" +
                "* **apps** : defines the base directory relative to which application executable binaries should be stored \n" +
                "* **lib** : defines the base directory relative to which application non executable binaries should be stored \n" +
                "* **var** : defines the base directory relative to which application specific data files (other than config) should be stored\n" +
                "* **log** : defines the base directory relative to which application specific log and trace files should be stored\n" +
                "* **temp** : defines the base directory relative to which application specific temporary files should be stored\n" +
                "* **cache** : defines the base directory relative to which application non-essential data and binary files should be stored to optimize bandwidth or performance\n" +
                "* **run** : defines the base directory relative to which application-specific non-essential runtime files and other file objects (such as sockets, named pipes, ...) should be stored\n" +
                "\n" +
                "**```nuts```** defines such distinct folders (named Store Locations) for storing different types of application data according to your operating system.\n" +
                "\n" +
                "On Windows Systems the default locations are :\n" +
                "\n" +
                "        * apps     : \"$HOME/AppData/Roaming/nuts/apps\"\n" +
                "        * lib      : \"$HOME/AppData/Roaming/nuts/lib\"\n" +
                "        * config   : \"$HOME/AppData/Roaming/nuts/config\"\n" +
                "        * var      : \"$HOME/AppData/Roaming/nuts/var\"\n" +
                "        * log      : \"$HOME/AppData/Roaming/nuts/log\"\n" +
                "        * temp     : \"$HOME/AppData/Local/nuts/temp\"\n" +
                "        * cache    : \"$HOME/AppData/Local/nuts/cache\"\n" +
                "        * run      : \"$HOME/AppData/Local/nuts/run\"\n" +
                "\n" +
                "On Linux, Unix, MacOS and any POSIX System the default locations are :\n" +
                "\n" +
                "        * config   : \"$HOME/.config/nuts\"\n" +
                "        * apps     : \"$HOME/.local/share/nuts/apps\"\n" +
                "        * lib      : \"$HOME/.local/share/nuts/lib\"\n" +
                "        * var      : \"$HOME/.local/share/nuts/var\"\n" +
                "        * log      : \"$HOME/.local/log/nuts\"\n" +
                "        * cache    : \"$HOME/.cache/nuts\"\n" +
                "        * temp     : \"$java.io.tmpdir/$username/nuts\"\n" +
                "        * run      : \"/run/user/$USER_ID/nuts\"\n" +
                "\n" +
                "As an example, the configuration folder for the artifact net.vpc.app:netbeans-launcher#1.2.4 in the default workspace in a Linux environment is\n" +
                "```\n" +
                "home/me/.config/nuts/default-workspace/config/id/net/vpc/app/netbeans-launcher/1.2.4/\n" +
                "```\n" +
                "And the log file \"app.log\" for the same artifact in the workspace named \"personal\" in a Windows environment is located at\n" +
                "```\n" +
                "C:/Users/me/AppData/Roaming/nuts/log/nuts/personal/config/id/net/vpc/app/netbeans-launcher/1.2.4/app.log\n" +
                "```\n" +
                "\n" +
                "## Store Location Strategies\n" +
                "When you install any application using the **```nuts```** command a set of specific folders for the presented Store Locations are created. For that, \n" +
                "two strategies exist : **Exploded strategy** (the default) and **Standalone strategy**.  \n" +
                "\n" +
                "In **Exploded strategy**  **```nuts```** defines top level folders (in linux ~/.config for config Store Location etc), and then creates withing each top level Store Location a sub folder for the given application (or application version to be more specific). This helps putting all your config files in a SSD partition for instance and make **nuts** run faster. However if you are interested in the backup or roaming of your workspace, this may be not the best approach.\n" +
                "\n" +
                "The **Standalone strategy**   is indeed provided mainly for Roaming workspaces that can be shared, copied, moved to other locations. A single root folder will contain all of the Store Locations.\n" +
                "\n" +
                "As an example, in \"Standalone Strategy\", the configuration folder for the artifact net.vpc.app:netbeans-launcher#1.2.4 in the default workspace in a Linux environment is\n" +
                "```\n" +
                "home/me/.config/nuts/default-workspace/config/id/net/vpc/app/netbeans-launcher/1.2.4/\n" +
                "```\n" +
                "And the log file \"app.log\" for the same artifact in the workspace named \"personal\" in the same Linux environment is located at\n" +
                "```\n" +
                "/home/me/.config/nuts/default-workspace/log/id/net/vpc/app/netbeans-launcher/1.2.4/\n" +
                "```\n" +
                "You can see here that the following folder will contain ALL the data files of the workspace.\n" +
                "```\n" +
                "/home/me/.config/nuts/default-workspace\n" +
                "```\n" +
                "whereas in the **Exploded strategy** the Store Location are \"exploded\" into multiple root folders.\n" +
                "\n" +
                "## Custom Store Locations\n" +
                "Of course, your able to configure separately each Store Location to meet your needs.\n" +
                "\n" +
                "### Selecting strategies\n" +
                "The following command will create an exploded workspace\n" +
                "```\n" +
                "nuts -w my-workspace --exploded\n" +
                "```\n" +
                "\n" +
                "The following comman will create an standalone workspace\n" +
                "```\n" +
                "nuts -w my-workspace --standalone\n" +
                "```\n" +
                "### Finer Customization\n" +
                "The following command will create an exploded workspace and moves all config files to the SSD partition folder /myssd/myconfig\n" +
                "```\n" +
                "nuts -w my-workspace --system-config-home=/myssd/myconfig\n" +
                "```\n" +
                "You can type help for more details.\n" +
                "```\n" +
                "nuts help\n" +
                "```\n"
;
        MdParser parser= new DocusaurusMdParser(new StringReader(s));
        MdElement e = parser.parse();
        System.out.println(e);
    }
}
