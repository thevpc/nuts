package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a class whose public no-argument methods define a Nuts application
 * lifecycle.
 *
 * <p>The runtime adapts an annotated instance to {@link NApplicationHandler} and
 * discovers methods marked with {@link NAppRun}, {@link NAppComplete},
 * {@link NAppInstall}, {@link NAppUpdate}, and {@link NAppUninstall}. The optional
 * {@link #id()} is application metadata; an empty value leaves its resolution to the
 * runtime and descriptor metadata.</p>
 *
 * @since 0.8.7
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NApp {
    String id() default "";
}
