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
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

class PrivateNutsUtilLauncher {
    private static boolean ndiAddFileLine(Path filePath, String commentLine, String goodLine, boolean force,
                                          String ensureHeader, String headerReplace, PrivateNutsBootLog bLog) {
        boolean found = false;
        boolean updatedFile = false;
        List<String> lines = new ArrayList<>();
        if (Files.isRegularFile(filePath)) {
            String fileContent = null;
            try {
                fileContent = new String(Files.readAllBytes(filePath));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            String[] fileRows = fileContent.split("\n");
            if (ensureHeader != null) {
                if (fileRows.length == 0 || !fileRows[0].trim().matches(ensureHeader)) {
                    lines.add(headerReplace);
                    updatedFile = true;
                }
            }
            for (int i = 0; i < fileRows.length; i++) {
                String row = fileRows[i];
                if (row.trim().equals("# " + (commentLine))) {
                    lines.add(row);
                    found = true;
                    i++;
                    if (i < fileRows.length) {
                        if (!fileRows[i].trim().equals(goodLine)) {
                            updatedFile = true;
                        }
                    }
                    lines.add(goodLine);
                    i++;
                    for (; i < fileRows.length; i++) {
                        lines.add(fileRows[i]);
                    }
                } else {
                    lines.add(row);
                }
            }
        }
        if (!found) {
            if (ensureHeader != null && headerReplace != null && lines.isEmpty()) {
                lines.add(headerReplace);
            }
            lines.add("# " + (commentLine));
            lines.add(goodLine);
            updatedFile = true;
        }
        if (force || updatedFile) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return updatedFile;
    }

    static boolean ndiRemoveFileCommented2Lines(Path filePath, String commentLine, boolean force, PrivateNutsBootLog bLog) {
        boolean found = false;
        boolean updatedFile = false;
        try {
            List<String> lines = new ArrayList<>();
            if (Files.isRegularFile(filePath)) {
                String fileContent = new String(Files.readAllBytes(filePath));
                String[] fileRows = fileContent.split("\n");
                for (int i = 0; i < fileRows.length; i++) {
                    String row = fileRows[i];
                    if (row.trim().equals("# " + (commentLine))) {
                        found = true;
                        i += 2;
                        for (; i < fileRows.length; i++) {
                            lines.add(fileRows[i]);
                        }
                    } else {
                        lines.add(row);
                    }
                }
            }
            if (found) {
                updatedFile = true;
            }
            if (force || updatedFile) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            }
            return updatedFile;
        } catch (IOException ex) {
            bLog.log(Level.WARNING, NutsMessage.jstyle("unable to update update " + filePath), ex);
            return false;
        }
    }

    static void ndiUndo(PrivateNutsBootLog bLog) {
        //need to unset settings configuration.
        //what is the safest way to do so?
        NutsOsFamily os = NutsOsFamily.getCurrent();
        //windows is ignored because it does not define a global nuts environment
        if (os == NutsOsFamily.LINUX || os == NutsOsFamily.MACOS) {
            String bashrc = os == NutsOsFamily.LINUX ? ".bashrc" : ".bash_profile";
            Path sysrcFile = Paths.get(System.getProperty("user.home")).resolve(bashrc);
            if (Files.exists(sysrcFile)) {

                //these two lines will remove older versions of nuts ( before 0.8.0)
                ndiRemoveFileCommented2Lines(sysrcFile, "net.vpc.app.nuts.toolbox.ndi configuration", true, bLog);
                ndiRemoveFileCommented2Lines(sysrcFile, "net.vpc.app.nuts configuration", true, bLog);

                //this line will remove 0.8.0+ versions of nuts
                ndiRemoveFileCommented2Lines(sysrcFile, "net.thevpc.nuts configuration", true, bLog);
            }

            // if we have deleted a non default workspace, we will fall back to the default one
            // and will consider the latest version of it.
            // this is helpful if we are playing with multiple workspaces. The default workspace will always be
            // accessible when deleting others
            String latestDefaultVersion = null;
            try {
                Path nbase = Paths.get(System.getProperty("user.home")).resolve(".local/share/nuts/apps/" + NutsConstants.Names.DEFAULT_WORKSPACE_NAME + "/id/net/thevpc/nuts/nuts");
                if (Files.isDirectory(nbase)) {
                    latestDefaultVersion = Files.list(nbase).filter(f -> Files.exists(f.resolve(".nuts-bashrc")))
                            .map(x -> sysrcFile.getFileName().toString()).min((o1, o2) -> NutsVersion.of(o2).get().compareTo(NutsVersion.of(o1).get()))
                            .orElse(null);
                }
                if (latestDefaultVersion != null) {
                    ndiAddFileLine(sysrcFile, "net.thevpc.nuts configuration",
                            "source " + nbase.resolve(latestDefaultVersion).resolve(".nuts-bashrc"),
                            true, "#!.*", "#!/bin/sh", bLog);
                }
            } catch (Exception e) {
                //ignore
                bLog.log(Level.FINEST, NutsLoggerVerb.FAIL, NutsMessage.jstyle("unable to undo NDI : {0}", e.toString()));
            }
        }
    }

}
