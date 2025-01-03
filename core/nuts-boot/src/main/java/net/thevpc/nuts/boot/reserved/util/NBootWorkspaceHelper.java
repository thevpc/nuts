package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspaceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NBootWorkspaceHelper {

    public static void printDryCommand(String cmd, NBootOptionsInfo options, NBootLog bLog) {
        String f = NBootUtils.firstNonNull(options.getOutputFormat(), "PLAIN");
        if (NBootUtils.firstNonNull(options.getDry(), false)) {
            switch (NBootUtils.enumName(f)) {
                case "JSON": {
                    bLog.outln("{");
                    bLog.outln("  \"dryCommand\": \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case "TSON": {
                    bLog.outln("{");
                    bLog.outln("  dryCommand: \"%s\"", cmd);
                    bLog.outln("}");
                    return;
                }
                case "YAML": {
                    bLog.outln("dryCommand: %s", cmd);
                    return;
                }
                case "TREE": {
                    bLog.outln("- dryCommand: %s", cmd);
                    return;
                }
                case "TABLE": {
                    bLog.outln("dryCommand  %s", cmd);
                    return;
                }
                case "XML": {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<object>");
                    bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "dryCommand", cmd);
                    bLog.outln("</object>");
                    return;
                }
                case "PROPS": {
                    bLog.outln("dryCommand=%s", cmd);
                    return;
                }
            }
            bLog.outln("[Dry] %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
        }
    }

    public static void runCommandVersion(Supplier<String> digest, NBootOptionsInfo options, NBootLog bLog) {
        String f = NBootUtils.firstNonNull(options.getOutputFormat(), "PLAIN");
        if (NBootUtils.firstNonNull(options.getDry(), false)) {
            printDryCommand("version",options, bLog);
            return;
        }
        switch (NBootUtils.enumName(f)) {
            case "JSON": {
                bLog.outln("{");
                bLog.outln("  \"version\": \"%s\",", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    bLog.outln("  \"digest\": \"%s\"", digest.get());
                }
                bLog.outln("}");
                return;
            }
            case "TSON": {
                bLog.outln("{");
                bLog.outln("  version: \"%s\",", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    bLog.outln("  digest: \"%s\"", digest.get());
                }
                bLog.outln("}");
                return;
            }
            case "YAML": {
                bLog.outln("version: %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    bLog.outln("digest: %s", digest.get());
                }
                return;
            }
            case "TREE": {
                bLog.outln("- version: %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    bLog.outln("- digest: %s", digest.get());
                }
                return;
            }
            case "TABLE": {
                bLog.outln("version      %s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    bLog.outln("digest  %s", digest.get());
                }
                return;
            }
            case "XML": {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("<object>");
                bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "version", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    bLog.outln("  <string key=\"%s\" value=\"%s\"/>", "digest", digest.get());
                }
                bLog.outln("</object>");
                return;
            }
            case "PROPS": {
                bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bLog.outln("version=%s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
                if(digest!=null) {
                    bLog.outln("digest=%s", digest.get());
                }
                bLog.outln("</object>");
                return;
            }
        }
        bLog.outln("%s", NBootWorkspaceImpl.NUTS_BOOT_VERSION);
    }

    public static void addError(NBootMsg err, NBootOptionsInfo options) {
        List<String> showError = options.getErrors();
        if (showError == null) {
            showError = new ArrayList<>();
        }
        showError.add(err.toString());
    }

    public static void runCommandHelp(NBootOptionsInfo options, NBootLog bLog) {
        String f = NBootUtils.firstNonNull(options.getOutputFormat(), "PLAIN");
        if (NBootUtils.firstNonNull(options.getDry(), false)) {
            printDryCommand("help",options, bLog);
        } else {
            String msg = "nuts is an open source package manager mainly for java applications. Type 'nuts help' or visit https://github.com/thevpc/nuts for more help.";
            switch (NBootUtils.enumName(f)) {
                case "JSON": {
                    bLog.outln("{");
                    bLog.outln("  \"help\": \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case "TSON": {
                    bLog.outln("{");
                    bLog.outln("  help: \"%s\"", msg);
                    bLog.outln("}");
                    return;
                }
                case "YAML": {
                    bLog.outln("help: %s", msg);
                    return;
                }
                case "TREE": {
                    bLog.outln("- help: %s", msg);
                    return;
                }
                case "TABLE": {
                    bLog.outln("help  %s", msg);
                    return;
                }
                case "XML": {
                    bLog.outln("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                    bLog.outln("<string>");
                    bLog.outln(" %s", msg);
                    bLog.outln("</string>");
                    return;
                }
                case "PROPS": {
                    bLog.outln("help=%s", msg);
                    return;
                }
            }
            bLog.outln("%s", msg);
        }
    }
}
