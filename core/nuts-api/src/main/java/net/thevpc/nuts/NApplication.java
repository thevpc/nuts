/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 * Nuts Application is the Top Level interface to be managed by nuts. By default, NApplication classes :
 * <ul>
 * <li>have a nutsApplication=true in their descriptor file (in maven descriptor you should add a property nuts.application=true)</li>
 * <li>support inheritance of all workspace options (from caller nuts process)</li>
 * <li>enables auto-complete mode to help forecasting the next token in the command line</li>
 * <li>enables install mode to be executed when the jar is installed in nuts repos</li>
 * <li>enables uninstall mode to be executed when the jar is uninstalled from nuts repos</li>
 * <li>enables update mode to be executed when the a new version of the same jar has been installed</li>
 * <li>have many default options enabled (such as --help, --version, --json,--table, etc.) and thus support natively multi output channels</li>
 * <li>have a well defined storage layout (with temp, lib, config folders, etc...)</li>
 * </ul>
 * Typically, a Nuts Application follows this code pattern :
 * <pre>
 *   package org.example.test;
 *
 * import net.thevpc.nuts.NApplication;
 * import net.thevpc.nuts.NSession;
 * import net.thevpc.nuts.cmdline.NArg;
 * import net.thevpc.nuts.cmdline.NCmdLine;
 * import net.thevpc.nuts.cmdline.NCmdLineContext;
 * import net.thevpc.nuts.cmdline.NCmdLineRunner;
 *
 * import java.util.ArrayList;
 * import java.util.List;
 *
 * public class MyApplication1 implements NApplication {
 *     public static void main(String[] args) {
 *         // just create an instance and call runAndExit in the main method
 *         new MyApplication1().run(NAppRunOptions.ofExit(args));
 *     }
 *
 *     public void run() {
 *         session.runAppCmdLine(new NCmdLineRunner() {
 *             boolean noMoreOptions = false;
 *             boolean clean = false;
 *             List<String> params = new ArrayList<>();
 *
 *             public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
 *                 if (!noMoreOptions) {
 *                     return false;
 *                 }
 *                 switch (option.key()) {
 *                     case "-c":
 *                     case "--clean": {
 *                         NArg a = cmdLine.nextFlag().get();
 *                         if (a.isEnabled()) {
 *                             clean = a.getBooleanValue().get();
 *                         }
 *                         return true;
 *                     }
 *                 }
 *                 return false;
 *             }
 *
 *             public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
 *                 params.add(cmdLine.next().get().toString());
 *                 return true;
 *             }
 *
 *             public void onCmdExec(NCmdLine cmdLine, NCmdLineContext context) {
 *                 if(clean){
 *                     cmdLine.getSession().out().println("cleaned!");
 *                 }
 *             }
 *         });
 *     }
 * }
 * </pre>
 * another example of using this class is :
 * <pre>
 *     package org.example.test;
 *
 * import net.thevpc.nuts.*;
 * import net.thevpc.nuts.cmdline.NArg;
 * import net.thevpc.nuts.cmdline.NCmdLine;
 *
 * import java.util.ArrayList;
 * import java.util.List;
 *
 * public class MyApplication2 implements NApplication {
 *     public static void main(String[] args) {
 *         // just create an instance and call runAndExit in the main method
 *         new MyApplication2().run(NAppRunOptions.ofExit(args));
 *     }
 *
 *     // do the main staff in launch method
 *     public void run() {
 *         NCmdLine cmdLine = session.getCmdLine();
 *         boolean boolOption = false;
 *         String stringOption = null;
 *         List<String> others = new ArrayList<>();
 *         NArg a;
 *         while (cmdLine.hasNext()) {
 *             a = cmdLine.peek().get();
 *             if (a.isOption()) {
 *                 switch (a.key()) {
 *                     case "-o":
 *                     case "--option": {
 *                         a = cmdLine.nextFlag().get(session);
 *                         if (a.isEnabled()) {
 *                             boolOption = a.getBooleanValue().get(session);
 *                         }
 *                         break;
 *                     }
 *                     case "-n":
 *                     case "--name": {
 *                         a = cmdLine.nextEntry().get(session);
 *                         if (a.isEnabled()) {
 *                             stringOption = a.getStringValue().get(session);
 *                         }
 *                         break;
 *                     }
 *                     default: {
 *                         session.configureLast(cmdLine);
 *                     }
 *                 }
 *             } else {
 *                 others.add(cmdLine.next().get().toString());
 *             }
 *         }
 *         // test if application is running in exec mode
 *         // (and not in autoComplete mode)
 *         if (cmdLine.isExecMode()) {
 *             //do the good staff here
 *             NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * and yet another example of using this class is :
 * <pre>
 * package org.example.test;
 *
 * import net.thevpc.nuts.NApplication;
 * import net.thevpc.nuts.NSession;
 * import net.thevpc.nuts.util.NMsg;
 * import net.thevpc.nuts.NSession;
 * import net.thevpc.nuts.cmdline.NArg;
 * import net.thevpc.nuts.cmdline.NCmdLine;
 * import net.thevpc.nuts.util.NRef;
 *
 * import java.util.ArrayList;
 * import java.util.List;
 *
 * public class MyApplication3 implements NApplication {
 *     public static void main(String[] args) {
 *         // just create an instance and call runAndExit in the main method
 *         new MyApplication3().run(NAppRunOptions.ofExit(args));
 *     }
 *
 *     // do the main staff in launch method
 *     public void run() {
 *         NCmdLine cmdLine = session.getCmdLine();
 *         NRef<Boolean> boolOption = NRef.of(false);
 *         NRef<String> stringOption = NRef.ofNull();
 *         List<String> others = new ArrayList<>();
 *         NArg a;
 *         while (cmdLine.hasNext()) {
 *             a = cmdLine.peek().get();
 *             if (a.isOption()) {
 *                 switch (a.key()) {
 *                     case "-o":
 *                     case "--option": {
 *                         cmdLine.withNextFlag((v, e, s)->boolOption.set(v));
 *                         break;
 *                     }
 *                     case "-n":
 *                     case "--name": {
 *                         cmdLine.withNextEntry((v, e, s)->stringOption.set(v));
 *                         break;
 *                     }
 *                     default: {
 *                         session.configureLast(cmdLine);
 *                     }
 *                 }
 *             } else {
 *                 others.add(cmdLine.next().get().toString());
 *             }
 *         }
 *         // test if application is running in exec mode
 *         // (and not in autoComplete mode)
 *         if (cmdLine.isExecMode()) {
 *             //do the good staff here
 *             NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
 *         }
 *     }
 * }
 * </pre>
 * <p>
 * and yet another good way to use It's :
 * <pre>
 *     package org.example.test;
 *
 * import net.thevpc.nuts.NApplication;
 * import net.thevpc.nuts.util.NMsg;
 * import net.thevpc.nuts.NSession;
 * import net.thevpc.nuts.cmdline.NArg;
 * import net.thevpc.nuts.cmdline.NCmdLine;
 * import net.thevpc.nuts.util.NRef;
 *
 * import java.util.ArrayList;
 * import java.util.List;
 * @App.Info
 * public class MyApplication4 implements NApplication {
 *     public static void main(String[] args) {
 *         // just create a builer and run it
 *         NApp.builder(args).run();
 *     }
 *
 *     App.Main
 *     // do the main staff in launch method
 *     public void run() {
 *         NCmdLine cmdLine = session.getCmdLine();
 *         NRef<Boolean> boolOption = NRef.of(false);
 *         NRef<String> stringOption = NRef.ofNull();
 *         List<String> others = new ArrayList<>();
 *         while (cmdLine.hasNext()) {
 *             if(cmdLine.withNextFlag((v, a, s)->boolOption.set(v),"-o","--option")) {
 *
 *             }else if(cmdLine.withNextEntry((v, a, s)->stringOption.set(v),"-n","--name")){
 *
 *             }else if(cmdLine.hasNextOption()){
 *                 session.configureLast(cmdLine);
 *             }else{
 *                 others.add(cmdLine.nextString().get());
 *             }
 *         }
 *         // test if application is running in exec mode
 *         // (and not in autoComplete mode)
 *         if (cmdLine.isExecMode()) {
 *             //do the good staff here
 *             NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
 *         }
 *     }
 * }
 * </pre>
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
public interface NApplication {

    /**
     * this method should be overridden to perform specific business when
     * application is installed
     */
    default void onInstallApplication() {
    }

    /**
     * this method should be overridden to perform specific business when
     * application is updated
     */
    default void onUpdateApplication() {
    }

    /**
     * this method should be overridden to perform specific business when
     * application is uninstalled
     */
    default void onUninstallApplication() {
    }


    /**
     * run application within the given context
     */
    void run();

}
