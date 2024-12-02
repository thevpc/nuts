package net.thevpc.nuts.runtime.deprecated;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NBootOptionsBuilder;

import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.runtime.standalone.io.NCoreIOUtils;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NDeleteFileHelper {
    public static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";

    /**
     * @param includeRoot true if include root
     * @param locations   of type NutsStoreLocation, Path of File
     * @param readline
     */
    public static long deleteStoreLocations(NBootOptions lastBootOptions, NBootOptions o, boolean includeRoot,
                                            NLog bLog, Object[] locations, Supplier<String> readline) {
        if (lastBootOptions == null) {
            return 0;
        }
        NConfirmationMode confirm = o.getConfirm().orElse(NConfirmationMode.ASK);
        if (confirm == NConfirmationMode.ASK
                && o.getOutputFormat().orElse(NContentType.PLAIN) != NContentType.PLAIN) {
            throw new NExecutionException(
                    NMsg.ofPlain("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), NExecutionException.ERROR_255);
        }
        bLog.with().level(Level.FINEST).verb(NLogVerb.WARNING).log(NMsg.ofC("delete workspace location(s) at : %s",
                lastBootOptions.getWorkspace().orNull()
        ));
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
                bLog.with().level(Level.WARNING).verb(NLogVerb.WARNING).log(NMsg.ofPlain("reset cancelled (applied '--no' argument)"));
                throw new NNoSessionCancelException(NMsg.ofPlain("cancel delete folder"));
            }
        }
        List<Path> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(Paths.get(lastBootOptions.getWorkspace().get()));
        }
        for (Object ovalue : locations) {
            if (ovalue != null) {
                if (ovalue instanceof NStoreType) {
                    NStoreType value = (NStoreType) ovalue;
                    String p = NCoreIOUtils.getStoreLocationPath(lastBootOptions, value);
                    if (p != null) {
                        folders.add(Paths.get(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue));
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue).toPath());
                } else {
                    throw new NIllegalArgumentException(NMsg.ofC("unsupported path type : %s", ovalue));
                }
            }
        }
        NBootOptionsBuilder optionsCopy = o.builder();
        if (optionsCopy.getBot().orElse(false) || !NReservedLangUtils.isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.toArray(new Path[0]), force, DELETE_FOLDERS_HEADER, bLog, optionsCopy, readline);
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, String header,
                                           NLog bLog, NBootOptions bOptions, Supplier<String> readline) {
        return deleteAndConfirmAll(folders, force, new NReservedDeleteFilesContextImpl(), header, bLog, bOptions, readline);
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, NReservedDeleteFilesContext refForceAll,
                                           String header, NLog bLog, NBootOptions bOptions, Supplier<String> readline) {
        long count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            NSession session = NSession.get().orNull();
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
                                        bLog.with().level(Level.WARNING).verb(NLogVerb.WARNING).log(NMsg.ofC("%s", header));
                                    }
                                }
                            }
                        }
                    }
                    count += deleteAndConfirm(child, force, refForceAll, bLog, bOptions, readline);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(Path directory, boolean force, NReservedDeleteFilesContext refForceAll,
                                         NLog bLog, NBootOptions bOptions, Supplier<String> readline) {
        if (Files.exists(directory)) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line = null;
                NSession session = NSession.get().orNull();
                if (session != null) {
                    line = NAsk.of()
                            .forString(
                                    NMsg.ofC(
                                            "do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory
                                    )).getValue();
                } else {
                    if (bOptions.getBot().orElse(false)) {
                        if (bOptions.getConfirm().orElse(NConfirmationMode.ASK) == NConfirmationMode.YES) {
                            line = "y";
                        } else {
                            throw new NIllegalArgumentException(NMsg.ofPlain("failed to delete files in --bot mode without auto confirmation"));
                        }
                    } else {
                        if (bOptions.getGui().orElse(false)) {
                            line = NReservedLangUtils.inputString(
                                    NMsg.ofC("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).toString(),
                                    null, readline, bLog
                            );
                        } else {
                            NConfirmationMode cc = bOptions.getConfirm().orElse(NConfirmationMode.ASK);
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
                                    throw new NIllegalArgumentException(NMsg.ofPlain("error response"));
                                }
                                case ASK: {
                                    // Level.OFF is to force logging in all cases
                                    bLog.with().level(Level.OFF).verb(NLogVerb.WARNING).log(NMsg.ofC("do you confirm deleting %s [y/n/c/a] (default 'n') ? : ", directory));
                                    line = readline.get();
                                }
                            }
                        }
                    }
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NCancelException();
                } else if (!NLiteral.of(line).asBoolean().orElse(false)) {
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
                bLog.with().level(Level.FINEST).verb(NLogVerb.WARNING).log(NMsg.ofC("delete folder : %s (%s files/folders deleted)", directory, count[0]));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
    }
}
