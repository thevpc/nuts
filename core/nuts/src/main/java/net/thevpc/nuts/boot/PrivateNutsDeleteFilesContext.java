package net.thevpc.nuts.boot;

import java.io.File;

/**
 * @app.category Internal
 */
interface PrivateNutsDeleteFilesContext {

    boolean isForce();

    void setForce(boolean value);

    boolean accept(File directory);

    void ignore(File directory);
}
