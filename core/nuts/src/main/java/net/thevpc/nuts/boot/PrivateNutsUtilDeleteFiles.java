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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

class PrivateNutsUtilDeleteFiles {
    private static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";

    PrivateNutsUtilDeleteFiles() {
    }

    static String getStoreLocationPath(NutsWorkspaceBootOptions bOptions, NutsStoreLocation value) {
        Map<NutsStoreLocation, String> storeLocations = bOptions.getStoreLocations().orNull();
        if (storeLocations != null) {
            return storeLocations.get(value);
        }
        return null;
    }

    /**
     * @param includeRoot true if include root
     * @param locations   of type NutsStoreLocation, Path of File
     */
    static long deleteStoreLocations(NutsWorkspaceBootOptions lastBootOptions, NutsWorkspaceBootOptions o, boolean includeRoot,
                                     PrivateNutsBootLog bLog, Object[] locations) {
        if (lastBootOptions == null) {
            return 0;
        }
        NutsConfirmationMode confirm = o.getConfirm().orElse(NutsConfirmationMode.ASK);
        if (confirm == NutsConfirmationMode.ASK
                && o.getOutputFormat().orElse(NutsContentType.PLAIN) != NutsContentType.PLAIN) {
            throw new NutsBootException(
                    NutsMessage.cstyle("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), 243);
        }
        bLog.log(Level.FINE, NutsLoggerVerb.WARNING, NutsMessage.jstyle("delete workspace location(s) at : {0}", lastBootOptions.getWorkspace()));
        boolean force = false;
        switch (confirm) {
            case ASK: {
                break;
            }
            case YES: {
                force = true;
                break;
            }
            case NO:
            case ERROR: {
                bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.jstyle("reset cancelled (applied '--no' argument)"));
                throw new PrivateNutsBootCancelException(NutsMessage.plain("cancel delete folder"));
            }
        }
        NutsWorkspaceConfigManager conf = null;
        List<Path> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(Paths.get(lastBootOptions.getWorkspace().get()));
        }
        for (Object ovalue : locations) {
            if (ovalue != null) {
                if (ovalue instanceof NutsStoreLocation) {
                    NutsStoreLocation value = (NutsStoreLocation) ovalue;
                    String p = getStoreLocationPath(lastBootOptions, value);
                    if (p != null) {
                        folders.add(Paths.get(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue));
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue).toPath());
                } else {
                    throw new NutsBootException(NutsMessage.cstyle("unsupported path type : %s", ovalue));
                }
            }
        }
        NutsWorkspaceBootOptionsBuilder optionsCopy = o.builder();
        if (optionsCopy.getBot().orElse(false) || !PrivateNutsUtilGui.isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.toArray(new Path[0]), force, DELETE_FOLDERS_HEADER, null, bLog, optionsCopy);
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, String header, NutsSession session,
                                           PrivateNutsBootLog bLog, NutsWorkspaceBootOptions bOptions) {
        return deleteAndConfirmAll(folders, force, new PrivateNutsDeleteFilesContextImpl(), header, session, bLog, bOptions);
    }

    private static long deleteAndConfirmAll(Path[] folders, boolean force, PrivateNutsDeleteFilesContext refForceAll,
                                            String header, NutsSession session, PrivateNutsBootLog bLog, NutsWorkspaceBootOptions bOptions) {
        long count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (Path child : folders) {
                if (Files.exists(child)) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll.isForce()) {
                            if (header != null) {
                                if (!bOptions.getBot().orElse(false)) {
                                    if (session != null) {
                                        session.err().println(header);
                                    } else {
                                        bLog.log(Level.WARNING, NutsLoggerVerb.WARNING, NutsMessage.jstyle("{0}", header));
                                    }
                                }
                            }
                        }
                    }
                    count += deleteAndConfirm(child, force, refForceAll, session, bLog, bOptions);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(Path directory, boolean force, PrivateNutsDeleteFilesContext refForceAll,
                                         NutsSession session, PrivateNutsBootLog bLog, NutsWorkspaceBootOptions bOptions) {
        if (Files.exists(directory)) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line = null;
                if (session != null) {
                    line = session.getTerminal().ask()
                            .resetLine()
                            .forString("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).setSession(session).getValue();
                } else {
                    if (bOptions.getBot().orElse(false)) {
                        if (bOptions.getConfirm().orElse(NutsConfirmationMode.ASK) == NutsConfirmationMode.YES) {
                            line = "y";
                        } else {
                            throw new NutsBootException(NutsMessage.plain("failed to delete files in --bot mode without auto confirmation"));
                        }
                    } else {
                        if (bOptions.getGui().orElse(false)) {
                            line = PrivateNutsUtilGui.inputString(
                                    NutsMessage.cstyle("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).toString(),
                                    null, () -> bLog.readLine(), bLog.err()
                            );
                        } else {
                            NutsConfirmationMode cc = bOptions.getConfirm().orElse(NutsConfirmationMode.ASK);
                            switch (cc) {
                                case YES: {
                                    line = "y";
                                    break;
                                }
                                case NO: {
                                    line = "n";
                                    break;
                                }
                                case ERROR: {
                                    throw new NutsBootException(NutsMessage.plain("error response"));
                                }
                                case ASK: {
                                    // Level.OFF is to force logging in all cases
                                    bLog.log(Level.OFF, NutsLoggerVerb.WARNING, NutsMessage.jstyle("do you confirm deleting {0} [y/n/c/a] (default 'n') ? : ", directory));
                                    line = bLog.readLine();
                                }
                            }
                        }
                    }
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NutsUserCancelException(session);
                } else if (!NutsValue.of(line).asBoolean().orElse(false)) {
                    refForceAll.ignore(directory);
                    return 0;
                }
            }
            long[] count = new long[1];
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        count[0]++;
//                        LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete file   : {0}", file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        count[0]++;
                        boolean deleted = false;
                        for (int i = 0; i < 2; i++) {
                            try {
                                Files.delete(dir);
                                deleted = true;
                                break;
                            } catch (DirectoryNotEmptyException e) {
                                // sometimes, on Windows OS, the Filesystem hasn't yet finished deleting
                                // the children (asynchronous)
                                //try three times and then exit!
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (!deleted) {
                            //do not catch, last time the exception is thrown
                            Files.delete(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                count[0]++;
                bLog.log(Level.FINEST, NutsLoggerVerb.WARNING, NutsMessage.jstyle("delete folder : {0} ({1} files/folders deleted)", directory, count[0]));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
    }
}
