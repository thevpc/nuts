package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an application complete entry point.
 * <p>
 * Methods annotated with {@code @NAppComplete} are intended to perform ...
 *
 * <pre>{@code
 * @NApp
 * public class AppMain {
 *
 *     public static void main(String[] args) {
 *         NApp.builder(args).run();
 *     }
 *
 *     // ... installer/updater/uninstaller unchanged ...
 *
 *     @NAppRun
 *     public void run() {
 *         AppOptions o = processCmdLine(NApplication.of().cmdLine());
 *         //.... do run the application
 *     }
 *
 *     @NAppComplete
 *     public void complete() {
 *         NCmdLine cmdLine = NApplication.of().cmdLine();
 *         processCmdLine(cmdLine);
 *         cmdLine.printCompleteResult();
 *     }
 *
 *      // Single walk, shared by exec and completion.
 *     private AppOptions processCmdLine(NCmdLine cmdLine) {
 *         AppOptions o = new AppOptions();
 *
 *         while (cmdLine.hasNext()) {
 *             if (cmdLine.next(cmdLine.createMatcher()
 *                     .whenOption("-i", "--interactive")
 *                     .as(NArgName.of("interactive"))
 *                     .matches()) != null) {
 *                 o.setInteractive(true);
 *                 continue;
 *             }
 *
 *             if (cmdLine.next(cmdLine.createMatcher()
 *                     .whenOption("-c", "--command")
 *                     .as(NArgName.of("command"))
 *                     .requireValue()
 *                     .matches()) != null) {
 *                 o.setCommand(cmdLine.nextString().get());
 *                 continue;
 *             }
 *
 *             if (cmdLine.isNextNonOption()) {
 *                 o.getScripts().add(cmdLine.nextString().get());
 *                 continue;
 *             }
 *
 *             cmdLine.throwUnexpectedArgument();
 *         }
 *
 *         return o;
 *     }
 * }
 *
 * }</pre>
 * @author thevpc
 * @app.category Annotation
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppComplete {

}
