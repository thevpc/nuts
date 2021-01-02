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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
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
 * Nuts Application is the Top Level class to be handled by nuts as rich console
 * application. By default NutsApplication classes :
 * <ul>
 * <li>have a nutsApplication=true in their descriptor file</li>
 * <li>support inheritance of all workspace options (from caller nuts
 * process)</li>
 * <li>enables auto-complete mode to help forecasting the next token in the
 * command line</li>
 * <li>enables install mode to be executed when the jar is installed in nuts
 * repos</li>
 * <li>enables uninstall mode to be executed when the jar is uninstaleld from
 * nuts repos</li>
 * <li>enables update mode to be executed when the a new version of the same jar
 * has been installed</li>
 * <li>have many default options enabled (such as --help, --version, --json,
 * --table, etc.) and thus support natively multi output channels</li>
 * </ul>
 * Typically a Nuts Application follows this code pattern :
 * <pre>
 *   public class MyApplication extends NutsApplication{
 *     public static void main(String[] args) {
 *         // just create an instance and call runAndExit in the main method
 *         new MyApplication().runAndExit(args);
 *     }
 *     // do the main staff in launch method
 *     public void run(NutsApplicationContext appContext) {
 *         boolean myBooleanOption=false;
 *         NutsCommandLine cmdLine=appContext.getCommandLine()
 *         boolean boolOption=false;
 *         String stringOption=null;
 *         Argument a;
 *         while(cmdLine.hasNext()){
 *             if(appContext.configureFirst(cmdLine)){
 *                 //do nothing
 *             }else {
 *                  a=cmdLine.peek();
 *                  switch(a.getStringKey())[
 *                      case "-o": case "--option":{
 *                          boolOption=cmdLine.nextBoolean().getBooleanValue();
 *                          break;
 *                      }
 *                      case "-n": case "--name":{
 *                          stringOption=cmdLine.nextString().getStringValue();
 *                          break;
 *                      }
 *                      default:{
 *                          cmdLine.unexpectedArgument();
 *                      }
 *                  }
 *             }
 *         }
 *         // test if application is running in exec mode
 *         // (and not in autoComplete mode)
 *         if(cmdLine.isExecMode()){
 *              //do the good staff here
 *         }
 *     }
 *   }
 * </pre>
 * another example of using this class is :
 * <pre>
 *     public class HLMain extends NutsApplication {
 *         public static void main(String[] args) {
 *            // just create an instance and call runAndExit in the main method
 *            new HLMain().runAndExit(args);
 *         }
 *
 *         &#64;Override
 *         public void run(NutsApplicationContext applicationContext) {
 *             applicationContext.processCommandLine(new NutsCommandLineProcessor() {
 *                 HLCWithOptions hl = new HL().withOptions();
 *                 boolean noMoreOptions=false;
 *                 &#64;Override
 *                 public boolean processOption(NutsArgument argument, NutsCommandLine cmdLine) {
 *                     if(!noMoreOptions){
 *                         return false;
 *                     }
 *                     switch (argument.getStringKey()) {
 *                         case "--clean": {
 *                             hl.clean(cmdLine.nextBoolean().getBooleanValue());
 *                             return true;
 *                         }
 *                         case "-i":
 *                         case "--incremental":{
 *                             hl.setIncremental(cmdLine.nextBoolean().getBooleanValue());
 *                             return true;
 *                         }
 *                         case "-r":
 *                         case "--root":{
 *                             hl.setProjectRoot(cmdLine.nextString().getStringValue());
 *                             return true;
 *                         }
 *                     }
 *                     return false;
 *                 }
 *
 *                 &#64;Override
 *                 public boolean processNonOption(NutsArgument argument, NutsCommandLine cmdLine) {
 *                     String s = argument.getString();
 *                     if(isURL(s)){
 *                         hl.includeFileURL(s);
 *                     }else{
 *                         hl.includeFile(s);
 *                     }
 *                     noMoreOptions=true;
 *                     return true;
 *                 }
 *
 *                 private boolean isURL(String s) {
 *                     return
 *                             s.startsWith("file:")
 *                             ||s.startsWith("http:")
 *                             ||s.startsWith("https:")
 *                             ;
 *                 }
 *
 *                 &#64;Override
 *                 public void exec() {
 *                     hl.compile();
 *                 }
 *             });
 *         }
 *     }
 * </pre>
 *
 * @since 0.5.5
 * @category Application
 */
public abstract class NutsApplication {

    /**
     * creates an instance of {@code appType} and calls runAndExit.
     * 
     * This method is intended be called in main methods of NutsApplication
     * classes.
     * 
     * @param <T> application type
     * @param appType application type
     * @param args main arguments
     * @since 0.7.1
     */
    public static <T extends NutsApplication> void main(Class<T> appType,String[] args) {
        T newInstance;
        try {
            newInstance = appType.newInstance();
        } catch (InstantiationException ex) {
            Throwable c = ex.getCause();
            if(c instanceof RuntimeException){
                throw (RuntimeException)c;
            }
            if(c instanceof Error){
                throw (Error)c;
            }
            throw new NutsBootException("Unable to instantiate "+appType.getName(),ex);
        } catch (IllegalAccessException ex) {
            throw new NutsBootException("Illegal access to default constructor for "+appType.getName(),ex);
        }
        newInstance.runAndExit(args);
    }
    
    /**
     * run the application and <strong>EXIT</strong> process
     *
     * @param args arguments
     */
    public void runAndExit(String[] args) {
        try {
            run(args);
        } catch (Exception ex) {
            System.exit(NutsApplications.processThrowable(ex, args, null));
            return;
        }
        System.exit(0);
    }

    /**
     * run the application with the given arguments. If the first arguments is
     * in the form of --nuts-exec-mode=... the argument will be removed and the
     * corresponding mode is activated.
     *
     * @param args application arguments. should not be null or contain nulls
     */
    public void run(String[] args) {
        run((NutsSession) null, args);
    }

    /**
     * run the application with the given arguments against the given workspace
     * If the first arguments is in the form of --nuts-exec-mode=... the
     * argument will be removed and the corresponding mode is activated.
     *
     * @param session session (can be null)
     * @param args application arguments. should not be null or contain nulls
     * @since 0.6.0, first parameter changed from NutsWorkspace to NutsSession to enable passing session options
     */
    public void run(NutsSession session, String[] args) {
        NutsApplications.runApplication(args, session, getClass(), new NutsApplicationLifeCycleImpl(this));
    }

    /**
     * this method should be overridden to perform specific business when
     * application is installed
     *
     * @param applicationContext context
     */
    protected void onInstallApplication(NutsApplicationContext applicationContext) {
    }

    /**
     * this method should be overridden to perform specific business when
     * application is updated
     *
     * @param applicationContext context
     */
    protected void onUpdateApplication(NutsApplicationContext applicationContext) {
    }

    /**
     * this method should be overridden to perform specific business when
     * application is uninstalled
     *
     * @param applicationContext context
     */
    protected void onUninstallApplication(NutsApplicationContext applicationContext) {
    }

    /**
     * create application context or return null for default
     * @param ws workspace
     * @param args arguments
     * @param startTimeMillis start time
     * @return new instance of NutsApplicationContext or null
     */
    protected NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
        return null;
    }

    /**
     * run application within the given context
     * @param applicationContext app context
     */
    public abstract void run(NutsApplicationContext applicationContext);

    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Default NutsApplicationLifeCycle implementation based on NutsApplication class.
     * @category Application
     */
    private static class NutsApplicationLifeCycleImpl implements NutsApplicationLifeCycle {
        /**
         * application
         */
        private final NutsApplication app;

        /**
         * application
         * @param app application
         */
        NutsApplicationLifeCycleImpl(NutsApplication app) {
            this.app = app;
        }

        @Override
        public void onRunApplication(NutsApplicationContext applicationContext) {
            app.run(applicationContext);
        }

        @Override
        public void onInstallApplication(NutsApplicationContext applicationContext) {
            app.onInstallApplication(applicationContext);
        }

        @Override
        public void onUpdateApplication(NutsApplicationContext applicationContext) {
            app.onUpdateApplication(applicationContext);
        }

        @Override
        public void onUninstallApplication(NutsApplicationContext applicationContext) {
            app.onUninstallApplication(applicationContext);
        }

        @Override
        public NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
            return app.createApplicationContext(ws, args, startTimeMillis);
        }

        @Override
        public String toString() {
            return app.toString();
        }
    }

}
