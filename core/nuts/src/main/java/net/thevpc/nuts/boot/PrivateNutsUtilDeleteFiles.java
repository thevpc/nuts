package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

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

    static String getStoreLocationPath(PrivateNutsWorkspaceInitInformation workspaceInformation, NutsStoreLocation value) {
        Map<NutsStoreLocation, String> storeLocations = workspaceInformation.getStoreLocations();
        if (storeLocations != null) {
            return storeLocations.get(value);
        }
        return null;
    }

    /**
     * @param includeRoot true if include root
     * @param locations   of type NutsStoreLocation, Path of File
     */
    static long deleteStoreLocations(PrivateNutsWorkspaceInitInformation lastWorkspaceInformation, NutsWorkspaceOptions o, boolean includeRoot, PrivateNutsLog LOG, Object[] locations) {
        if (lastWorkspaceInformation == null) {
            return 0;
        }
        NutsConfirmationMode confirm = o.getConfirm() == null ? NutsConfirmationMode.ASK : o.getConfirm();
        if (confirm == NutsConfirmationMode.ASK
                && o.getOutputFormat() != null
                && o.getOutputFormat() != NutsContentType.PLAIN) {
            throw new NutsBootException(
                    NutsMessage.cstyle("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), 243);
        }
        LOG.log(Level.FINE, NutsLogVerb.WARNING, "delete workspace location(s) at : {0}", new Object[]{lastWorkspaceInformation.getWorkspaceLocation()});
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
                LOG.log(Level.WARNING, NutsLogVerb.WARNING, "reset cancelled (applied '--no' argument)");
                throw new PrivateNutsBootCancelException(NutsMessage.plain("cancel delete folder"));
            }
        }
        NutsWorkspaceConfigManager conf = null;
        List<File> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(new File(lastWorkspaceInformation.getWorkspaceLocation()));
        }
        for (Object ovalue : locations) {
            if (ovalue != null) {
                if (ovalue instanceof NutsStoreLocation) {
                    NutsStoreLocation value = (NutsStoreLocation) ovalue;
                    String p = getStoreLocationPath(lastWorkspaceInformation, value);
                    if (p != null) {
                        folders.add(new File(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue).toFile());
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue));
                } else {
                    throw new NutsBootException(NutsMessage.cstyle("unsupported path type : %s", ovalue));
                }
            }
        }
        NutsWorkspaceOptionsBuilder optionsCopy = o.builder();
        if (optionsCopy.isBot() || !PrivateNutsUtilGui.isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.toArray(new File[0]), force, DELETE_FOLDERS_HEADER, null, LOG, optionsCopy.build());
    }

    public static long deleteAndConfirmAll(File[] folders, boolean force, String header, NutsSession session, PrivateNutsLog LOG, NutsWorkspaceOptions woptions) {
        return deleteAndConfirmAll(folders, force, new PrivateNutsDeleteFilesContextImpl(), header, session, LOG, woptions);
    }

    private static long deleteAndConfirmAll(File[] folders, boolean force, PrivateNutsDeleteFilesContext refForceAll, String header, NutsSession session, PrivateNutsLog LOG, NutsWorkspaceOptions woptions) {
        long count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (File child : folders) {
                if (child.exists()) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll.isForce()) {
                            if (header != null) {
                                if (!woptions.isBot()) {
                                    if (session != null) {
                                        session.err().println(header);
                                    } else {
                                        LOG.log(Level.WARNING, NutsLogVerb.WARNING, "{0}", header);
                                    }
                                }
                            }
                        }
                    }
                    count += deleteAndConfirm(child, force, refForceAll, session, LOG, woptions);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(File directory, boolean force, PrivateNutsDeleteFilesContext refForceAll, NutsSession session, PrivateNutsLog log, NutsWorkspaceOptions woptions) {
        if (directory.exists()) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line = null;
                if (session != null) {
                    line = session.getTerminal().ask()
                            .resetLine()
                            .forString("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).setSession(session).getValue();
                } else {
                    if (woptions.isBot()) {
                        if (woptions.getConfirm() == NutsConfirmationMode.YES) {
                            line = "y";
                        } else {
                            throw new NutsBootException(NutsMessage.plain("failed to delete files in --bot mode without auto confirmation"));
                        }
                    } else {
                        if (woptions.isGui()) {
                            line = PrivateNutsUtilGui.inputString(
                                    NutsMessage.cstyle("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).toString(),
                                    null,()->log.readLine(),log.err()
                            );
                        } else {
                            NutsConfirmationMode cc = woptions.getConfirm();
                            if (cc == null) {
                                cc = NutsConfirmationMode.ASK;
                            }
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
                                    log.log(Level.WARNING, NutsLogVerb.WARNING, "do you confirm deleting {0} [y/n/c/a] (default 'n') ? : ", directory);
                                    line = log.readLine();
                                }
                            }
                        }
                    }
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NutsUserCancelException(session);
                } else if (!NutsUtilStrings.parseBoolean(line, false, false)) {
                    refForceAll.ignore(directory);
                    return 0;
                }
            }
            Path directoryPath = Paths.get(directory.getPath());
            long[] count = new long[1];
            try {
                Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
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
                log.log(Level.FINEST, NutsLogVerb.WARNING, "delete folder : {0} ({1} files/folders deleted)", new Object[]{directory, count[0]});
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
    }
}
