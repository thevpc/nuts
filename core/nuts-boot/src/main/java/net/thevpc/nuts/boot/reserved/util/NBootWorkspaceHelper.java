package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspaceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NBootWorkspaceHelper {

    public static void printDryCommand(String cmd, NBootOptionsInfo options) {
        String f = NBootUtils.firstNonNull(options.getOutputFormat(), "PLAIN");
        NBootLog log = NBootContext.log();
        if (NBootUtils.firstNonNull(options.getDry(), false)) {
            switch (NBootUtils.enumName(f)) {
                case "JSON": {
                    log.outln("{");
                    log.outln("  \"dryCommand\": \"%s\"", cmd);
                    log.outln("}");
                    return;
                }
                case "TSON": {
                    log.outln("{");
                    log.outln("  dryCommand: \"%s\"", cmd);
                    log.outln("}");
                    return;
                }
                case "YAML": {
                    log.outln("dryCommand: %s", cmd);
                    return;
                }
                case "TREE": {
                    log.outln("- dryCommand: %s", cmd);
                    return;
                }
                case "TABLE": {
                    log.outln("dryCommand  %s", cmd);
                    return;
                }
                case "XML": {
                    log.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    log.outln("<object>");
                    log.outln("  <string key=\"%s\" value=\"%s\"/>", "dryCommand", cmd);
                    log.outln("</object>");
                    return;
                }
                case "PROPS": {
                    log.outln("dryCommand=%s", cmd);
                    return;
                }
            }
            log.outln("[Dry] %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
        }
    }

    public static void runCommandVersion(Supplier<String> digest, NBootOptionsInfo options) {
        String f = NBootUtils.firstNonNull(options.getOutputFormat(), "PLAIN");
        if (NBootUtils.firstNonNull(options.getDry(), false)) {
            printDryCommand("version",options);
            return;
        }
        NBootLog log = NBootContext.log();
        switch (NBootUtils.enumName(f)) {
            case "JSON": {
                log.outln("{");
                log.outln("  \"version\": \"%s\",", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    log.outln("  \"digest\": \"%s\"", digest.get());
                }
                log.outln("}");
                return;
            }
            case "TSON": {
                log.outln("{");
                log.outln("  version: \"%s\",", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    log.outln("  digest: \"%s\"", digest.get());
                }
                log.outln("}");
                return;
            }
            case "YAML": {
                log.outln("version: %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    log.outln("digest: %s", digest.get());
                }
                return;
            }
            case "TREE": {
                log.outln("- version: %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    log.outln("- digest: %s", digest.get());
                }
                return;
            }
            case "TABLE": {
                log.outln("version      %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    log.outln("digest  %s", digest.get());
                }
                return;
            }
            case "XML": {
                log.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                log.outln("<object>");
                log.outln("  <string key=\"%s\" value=\"%s\"/>", "version", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    log.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", digest.get());
                }
                log.outln("</object>");
                return;
            }
            case "PROPS": {
                log.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                log.outln("version=%s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    log.outln("digest=%s", digest.get());
                }
                log.outln("</object>");
                return;
            }
        }
        log.outln("%s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
    }

    public static void addError(NBootMsg err, NBootOptionsInfo options) {
        List<String> showError = options.getErrors();
        if (showError == null) {
            showError = new ArrayList<>();
        }
        showError.add(err.toString());
    }

    public static void runCommandHelp(NBootOptionsInfo options) {
        String f = NBootUtils.firstNonNull(options.getOutputFormat(), "PLAIN");
        NBootLog log = NBootContext.log();
        if (NBootUtils.firstNonNull(options.getDry(), false)) {
            printDryCommand("help",options);
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (NBootUtils.enumName(f)) {
                case "JSON": {
                    log.outln("{");
                    log.outln("  \"help\": \"%s\"", msg);
                    log.outln("}");
                    return;
                }
                case "TSON": {
                    log.outln("{");
                    log.outln("  help: \"%s\"", msg);
                    log.outln("}");
                    return;
                }
                case "YAML": {
                    log.outln("help: %s", msg);
                    return;
                }
                case "TREE": {
                    log.outln("- help: %s", msg);
                    return;
                }
                case "TABLE": {
                    log.outln("help  %s", msg);
                    return;
                }
                case "XML": {
                    log.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    log.outln("<string>");
                    log.outln(" %s", msg);
                    log.outln("</string>");
                    return;
                }
                case "PROPS": {
                    log.outln("help=%s", msg);
                    return;
                }
            }
            log.outln("%s", msg);
        }
    }
}
