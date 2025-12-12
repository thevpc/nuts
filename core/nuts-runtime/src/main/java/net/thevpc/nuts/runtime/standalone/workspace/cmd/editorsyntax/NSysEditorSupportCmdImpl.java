package net.thevpc.nuts.runtime.standalone.workspace.cmd.editorsyntax;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NSysEditorFamily;
import net.thevpc.nuts.command.NSysEditorSupportCmd;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.io.NAsk;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NTrace;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;

public class NSysEditorSupportCmdImpl implements NSysEditorSupportCmd {
    private NPath source;
    private Set<NSysEditorFamily> editorKinds = new LinkedHashSet<>();
    private boolean force;
    private Info defaultInfo=new  Info();
    private Info forcedInfo=new  Info();

    public boolean isForce() {
        return force;
    }

    public NSysEditorSupportCmd setForce(boolean force) {
        this.force = force;
        return this;
    }

    public NPath getSource() {
        return source;
    }

    public NSysEditorSupportCmd setSource(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NSysEditorSupportCmd setLanguageId(String value) {
        forcedInfo.setLanguageId(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setDefaultLanguageId(String value) {
        defaultInfo.setLanguageId(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setLanguageName(String value) {
        forcedInfo.setLanguageName(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setDefaultLanguageName(String value) {
        defaultInfo.setLanguageName(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setLanguageGroupId(String value) {
        forcedInfo.setLanguageGroupId(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setDefaultLanguageGroupId(String value) {
        defaultInfo.setLanguageGroupId(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setLanguageVersion(String value) {
        forcedInfo.setLanguageVersion(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setDefaultLanguageVersion(String value) {
        defaultInfo.setLanguageVersion(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setFileExtension(String value) {
        forcedInfo.setFileExtension(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setDefaultFileExtension(String value) {
        defaultInfo.setFileExtension(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setFileName(String value) {
        forcedInfo.setFileName(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd setDefaultFileName(String value) {
        defaultInfo.setFileName(value);
        return this;
    }

    @Override
    public NSysEditorSupportCmd addEditorFamily(NSysEditorFamily family) {
        if (family != null) {
            editorKinds.add(family);
        }
        return this;
    }

    @Override
    public Set<NSysEditorFamily> getEditorFamilies() {
        return new LinkedHashSet<>(editorKinds);
    }

    @Override
    public NSysEditorSupportCmd addEditorFamilies(NSysEditorFamily... families) {
        for (NSysEditorFamily kind : families) {
            if (kind != null) {
                editorKinds.add(kind);
            }
        }
        return this;
    }

    @Override
    public NSysEditorSupportCmd removeEditorFamily(NSysEditorFamily family) {
        editorKinds.remove(family);
        return this;
    }

    @Override
    public NSysEditorSupportCmd removeEditorFamilies(NSysEditorFamily... families) {
        for (NSysEditorFamily kind : families) {
            if (kind != null) {
                editorKinds.remove(kind);
            }
        }
        return this;
    }

    @Override
    public NSysEditorSupportCmd run() {
        NAssert.requireNonBlank(source, "source");
        NAssert.requireNonEmpty(editorKinds, "editorKinds");
        Info info = prepareSource(source);
        for (NSysEditorFamily editorKind : editorKinds) {
            switch (editorKind) {
                case INTELLIJ: {
                    runActionInstallIdea(info);
                    break;
                }
                case JEDIT: {
                    runActionInstallJEdit(info);
                    break;
                }
                case NOTEPAD_PLUS_PLUS: {
                    runActionInstallNodepadPlusPlus(info);
                    break;
                }
                case VIM: {
                    runActionInstallVim(info);
                    break;
                }
                case VSCODE: {
                    runActionInstallVscode(info);
                    break;
                }
                case KATE: {
                    runActionInstallKate(info);
                    break;
                }
                case GEDIT: {
                    runActionInstallGedit(info);
                    break;
                }
            }
        }
        return this;
    }

    private Info prepareSource(NPath source) {
        if (!source.getName().toLowerCase().endsWith(".zip")) {
            Info info = new Info();
            info.repoFolder = source;
            NPath s = source.resolve("sys-editor-support.tson");
            if (s.isRegularFile()) {
                NObjectElement sObj = NElementParser.ofTson().parse(s).asObject().get();
                for (NElement child : sObj.children()) {
                    if (child.isNamedPair()) {
                        NPairElement p = child.asPair().get();
                        switch (NNameFormat.VAR_NAME.format(p.name().get())) {
                            case "languageId": {
                                info.languageId = NStringUtils.trimToNull(p.value().asStringValue().orNull());
                                break;
                            }
                            case "languageName": {
                                info.languageName = NStringUtils.trimToNull(p.value().asStringValue().orNull());
                                break;
                            }
                            case "languageVersion": {
                                info.languageVersion = NStringUtils.trimToNull(p.value().asStringValue().orNull());
                                break;
                            }
                            case "languageGroupId": {
                                info.languageGroupId = NStringUtils.trimToNull(p.value().asStringValue().orNull());
                                break;
                            }
                            case "fileExtension": {
                                info.fileExtension = NStringUtils.trimToNull(p.value().asStringValue().orNull());
                                if (info.fileExtension != null) {
                                    if (info.fileExtension.startsWith("*.")) {
                                        info.fileExtension = info.fileExtension.substring(2);
                                    } else if (info.fileExtension.startsWith(".")) {
                                        info.fileExtension = info.fileExtension.substring(1);
                                    }
                                }
                                break;
                            }
                            case "fileName": {
                                info.fileName = NStringUtils.trimToNull(p.value().asStringValue().orNull());
                                break;
                            }
                        }
                    }
                }
            }
            return validateInfo(info);
        } else {
            throw NExceptions.ofSafeAssertException(NMsg.ofC("invalid source %s", source));
        }
    }

    private Info validateInfo(Info info) {
        info = info.copy();
        String oldFileExtension = info.fileExtension;
        String oldFileName = info.fileName;
        if (oldFileExtension == null) {
            if (oldFileName != null) {
                if (oldFileName.startsWith("*.")) {
                    info.fileName = oldFileName.substring(2);
                } else if (info.fileName.startsWith(".")) {
                    info.fileName = oldFileName.substring(1);
                }
            }
        } else {
            if (oldFileExtension.startsWith("*.")) {
                info.fileExtension = oldFileExtension.substring(2);
            } else if (info.fileExtension.startsWith(".")) {
                info.fileExtension = oldFileExtension.substring(1);
            }
        }
        if (oldFileName == null) {
            if (oldFileExtension != null) {
                if (oldFileExtension.startsWith("*.")) {
                    info.fileName = oldFileExtension;
                } else if (oldFileExtension.startsWith(".")) {
                    info.fileName = "*" + oldFileExtension;
                } else {
                    info.fileName = oldFileExtension;
                }
            }
        } else {
            if (oldFileExtension != null) {
                if (oldFileExtension.startsWith("*.")) {
                    info.fileExtension = oldFileExtension.substring(2);
                } else if (info.fileExtension.startsWith(".")) {
                    info.fileExtension = oldFileExtension.substring(1);
                }
            }
        }
        if (defaultInfo != null) {
            if (info.fileExtension == null && defaultInfo.fileExtension != null) {
                info.fileExtension = defaultInfo.fileExtension;
            }
            if (info.fileName == null && defaultInfo.fileName != null) {
                info.fileName = defaultInfo.fileName;
            }
            if (info.languageId == null && defaultInfo.languageId != null) {
                info.languageId = defaultInfo.languageId;
            }
            if (info.languageGroupId == null && defaultInfo.languageGroupId != null) {
                info.languageGroupId = defaultInfo.languageGroupId;
            }
            if (info.languageVersion == null && defaultInfo.languageVersion != null) {
                info.languageVersion = defaultInfo.languageVersion;
            }
        }
        if (forcedInfo != null) {
            if (forcedInfo.fileExtension != null) {
                info.fileExtension = forcedInfo.fileExtension;
            }
            if (forcedInfo.fileName != null) {
                info.fileName = forcedInfo.fileName;
            }
            if (forcedInfo.languageId != null) {
                info.languageId = forcedInfo.languageId;
            }
            if (forcedInfo.languageGroupId != null) {
                info.languageGroupId = forcedInfo.languageGroupId;
            }
            if (forcedInfo.languageVersion != null) {
                info.languageVersion = forcedInfo.languageVersion;
            }
        }
        NAssert.requireNonBlank(info.languageId, "languageId");
        NAssert.requireNonBlank(info.languageVersion, "languageVersion");
        NAssert.requireNonBlank(info.languageGroupId, "languageGroupId");
        NAssert.requireNonBlank(info.fileExtension, "fileExtension");
        NAssert.requireNonBlank(info.fileName, "fileName");
        return info;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    @Override
    public int getScore(NScorableContext context) {
        return NScorable.DEFAULT_SCORE;
    }

    private void runActionInstallIdea(Info info) {
        NMsg styledLangId = NMsg.ofStyledKeyword(info.getLanguageId());
        NMsg app = NMsg.ofStyledDate("IntelliJIdea");
        NPath local = null;
        // ".config/JetBrains/IntelliJIdea2024.1/filetypes/
        NPath jb = NPath.ofUserHome().resolve(".config/JetBrains/");
        boolean doForce = false;
        if (jb.isDirectory()) {
            local = jb.list().stream().filter(x -> {
                if (!x.isDirectory()) {
                    return false;
                }
                if (!x.resolve("idea64.vmoptions").isRegularFile()) {
                    return false;
                }
                return true;
            }).sorted((a, b) -> {
                return -NVersion.of(resolveJetbrainsVersion(a.getName()))
                        .compareTo(resolveJetbrainsVersion(b.getName()));
            }).findFirst().orElse(null);
            if (local != null) {
                local = local.resolve("filetypes").resolve(info.getLanguageId() + ".xml");
            }
        }
        if (local == null) {
            NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting for %s is not supported. Idea does not seem to be installed", styledLangId, app, NWorkspace.of().getOsFamily()));
            return;
        } else if (!local.isRegularFile()) {

        } else {
            if (NAsk.of()
                    .setDefaultValue(true)
                    .setRememberMeKey("NSysEditorSupportCmd.forceInstall")
                    .forBoolean(NMsg.ofC("%s %s syntax highlighting is already configured in %s.\n Override it ?", styledLangId, app, local))
                    .getBooleanValue()) {
                doForce = true;
            } else {
                NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting is already configured in %s", styledLangId, app, local));
                return;
            }
        }
        NPath remote = info.getRepoFolder().resolve("/intellij/language.xml");
        remote.copyTo(local.mkParentDirs());
        if (doForce) {
            NTrace.println(NMsg.ofC("%s %s syntax highlighting re-installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
        } else {
            NTrace.println(NMsg.ofC("%s %s syntax highlighting installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
        }
    }

    private String resolveJetbrainsVersion(String name) {
        if (name.startsWith("IdeaIC")) {
            return name.substring("IdeaIC".length());
        }
        if (name.startsWith("IntelliJIdea")) {
            return name.substring("IntelliJIdea".length());
        }
        return "";
    }

    private void runActionInstallKate(Info info) {
        NMsg styledLangId = NMsg.ofStyledKeyword(info.getLanguageId());
        NMsg app = NMsg.ofStyledDate("kate");
        if (NWorkspace.of().getOsFamily().isPosix()) {
            NPath local = NPath.ofUserHome().resolve(".local/share/org.kde.syntax-highlighting/syntax/" + info.getLanguageId() + ".xml");
            boolean doForce = false;
            if (!local.isRegularFile()) {
            } else {
                if (NAsk.of()
                        .setDefaultValue(true)
                        .setRememberMeKey("NSysEditorSupportCmd.forceInstall")
                        .forBoolean(NMsg.ofC("%s %s syntax highlighting is already configured in %s.\n Override it ?", styledLangId, app, local))
                        .getBooleanValue()) {
                    doForce = true;
                } else {
                    NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting is already configured in %s", styledLangId, app, local));
                    return;
                }
            }
            NPath remote = info.getRepoFolder().resolve("/kate/language.xml");
            remote.copyTo(local.mkParentDirs());
            if (doForce) {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting re-installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            } else {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            }

        } else {
            NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting for %s is not supported", styledLangId, app, NWorkspace.of().getOsFamily()));
        }
    }

    private void runActionInstallVim(Info info) {
        NMsg styledLangId = NMsg.ofStyledKeyword(info.getLanguageId());
        NMsg app = NMsg.ofStyledDate("vim");
        if (NWorkspace.of().getOsFamily().isPosix()) {
            NPath local = NPath.ofUserHome().resolve(".vim/");
            boolean doForce = false;
            if (
                    !local.resolve("syntax/" + info.getLanguageId() + ".vim").isRegularFile()
                            && !local.resolve("ftdetect/" + info.getLanguageId() + ".vim").isRegularFile()
            ) {
                //
            } else {
                if (NAsk.of()
                        .setDefaultValue(true)
                        .setRememberMeKey("NSysEditorSupportCmd.forceInstall")
                        .forBoolean(NMsg.ofC("%s %s syntax highlighting is already configured in %s.\n Override it ?", styledLangId, app, local))
                        .getBooleanValue()) {
                    doForce = true;
                } else {
                    NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting is already configured in %s", styledLangId, app, local));
                    return;
                }
            }

            NPath remote = info.getRepoFolder().resolve("/vim/language.syntax.vim");
            remote.copyTo(local.resolve("syntax/" + info.getLanguageId() + ".vim").mkParentDirs());
            remote = info.getRepoFolder().resolve("/vim/language.ftdetect.vim");
            remote.copyTo(local.resolve("ftdetect/" + info.getLanguageId() + ".vim").mkParentDirs());

            if (doForce) {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting re-installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            } else {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            }

        } else {
            NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting for %s is not supported", styledLangId, app, NWorkspace.of().getOsFamily()));
        }
    }

    private void runActionInstallVscode(Info info) {
        NMsg styledLangId = NMsg.ofStyledKeyword(info.getLanguageId());
        NMsg app = NMsg.ofStyledDate("Visual Studio Code");
        String publisher = NStringUtils.firstNonBlank(info.getLanguageGroupId(), "thevpc");
        String langVersion = NStringUtils.firstNonBlank(info.getLanguageVersion(), "1.0.0");
        String pluginName = info.getLanguageId() + "-syntax";
        if (NWorkspace.of().getOsFamily().isPosix()) {
            NPath local = NPath.ofUserHome().resolve(".vscode/extensions/" + publisher + "." + pluginName + "-"+langVersion);
            boolean doForce = false;
            if (
                    !local.isDirectory()
                            || !local.resolve("package.json").isRegularFile()
            ) {
                //
            } else {
                if (NAsk.of()
                        .setDefaultValue(true)
                        .setRememberMeKey("NSysEditorSupportCmd.forceInstall")
                        .forBoolean(NMsg.ofC("%s %s syntax highlighting is already configured in %s.\n Override it ?", styledLangId, app, local))
                        .getBooleanValue()) {
                    doForce = true;
                } else {
                    NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting is already configured in %s", styledLangId, app, local));
                    return;
                }
            }

            local.resolve("package.json").mkParentDirs()
                    .writeString(generateVsCodePackageJson(publisher, langVersion, info));

            NPath remote;

            remote = info.getRepoFolder().resolve("/vscode/language-configuration.json");
            remote.copyTo(local.resolve("language-configuration.json").mkParentDirs());

            remote = info.getRepoFolder().resolve("/vscode/language.tmLanguage.json");
            remote.copyTo(local.resolve("syntaxes/" + info.getLanguageId() + ".tmLanguage.json").mkParentDirs());

            if (doForce) {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting re-installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            } else {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            }

        } else {
            NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting for %s is not supported", styledLangId, app, NWorkspace.of().getOsFamily()));
        }
    }

    private String generateVsCodePackageJson(String publisher, String langVersion, Info info) {
        String langName = NStringUtils.firstNonBlank(info.getLanguageName(), NNameFormat.CLASS_NAME.format(info.getLanguageId()));
        String vsCodeVersion = "1.80.0";
        return NMsg.ofV(
                "{\n" +
                        "  \"name\": \"${langId}-syntax\",\n" +
                        "  \"displayName\": \"${langName} Syntax\",\n" +
                        "  \"description\": \"Syntax highlighting for ${langName} files (.${extension})\",\n" +
                        "  \"publisher\": \"${publisher}\",\n" +
                        "  \"version\": \"${langVersion}\",\n" +
                        "  \"engines\": {\n" +
                        "    \"vscode\": \"^${vsCodeVersion}\"\n" +
                        "  },\n" +
                        "  \"categories\": [\"Programming Languages\"],\n" +
                        "  \"contributes\": {\n" +
                        "    \"languages\": [\n" +
                        "      {\n" +
                        "        \"id\": \"${langId}\",\n" +
                        "        \"aliases\": [\"${langName}\", \"${langId}\"],\n" +
                        "        \"extensions\": [\".${extension}\"],\n" +
                        "        \"configuration\": \"./language-configuration.json\"\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"grammars\": [\n" +
                        "      {\n" +
                        "        \"language\": \"${langId}\",\n" +
                        "        \"scopeName\": \"source.${langId}\",\n" +
                        "        \"path\": \"./syntaxes/${langId}.tmLanguage.json\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "}\n",
                v -> {
                    switch (v) {
                        case "vsCodeVersion":
                            return vsCodeVersion;
                        case "langId":
                            return info.getLanguageId();
                        case "langVersion":
                            return langVersion;
                        case "langName":
                            return langName;
                        case "publisher":
                            return publisher;
                        case "extension":
                            return info.getFileExtension();
                        case "fileName":
                            return info.getFileName();
                    }
                    return null;
                }
        ).toString();
    }

    private void runActionInstallGedit(Info info) {
        NMsg styledLangId = NMsg.ofStyledKeyword(info.getLanguageId());
        NMsg app = NMsg.ofStyledDate("gedit");
        if (NWorkspace.of().getOsFamily().isPosix()) {
            String latestGnomeVersion = "5";
            NPath local = NPath.ofUserHome().resolve(".local/share/").list().stream().filter(x -> x.startsWith("gtksourceview-"))
                    .sorted((a, b) -> NVersion.of(b.getName()).compareTo(a.getName()))
                    .findFirst().orElse(null);
            if (local == null) {
                local = NPath.ofUserHome().resolve(".local/share/gtksourceview-" + latestGnomeVersion + "/language-specs/" + info.getLanguageId() + ".lang");
            }
            boolean doForce = false;
            if (!local.isRegularFile()) {
                //
            } else {
                if (NAsk.of()
                        .setDefaultValue(true)
                        .setRememberMeKey("NSysEditorSupportCmd.forceInstall")
                        .forBoolean(NMsg.ofC("%s %s syntax highlighting is already configured in %s.\n Override it ?", styledLangId, app, local))
                        .getBooleanValue()) {
                    doForce = true;
                } else {
                    NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting is already configured in %s", styledLangId, app, local));
                    return;
                }
            }

            NPath remote = info.getRepoFolder().resolve("/gedit/language.lang");
            remote.copyTo(local.mkParentDirs());
            if (doForce) {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting re-installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            } else {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            }

        } else {
            NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting for %s is not supported", styledLangId, app, NWorkspace.of().getOsFamily()));
        }
    }

    private void runActionInstallNodepadPlusPlus(Info info) {
        NMsg styledLangId = NMsg.ofStyledKeyword(info.getLanguageId());
        NMsg app = NMsg.ofStyledDate("Nodepad++");
        if (NWorkspace.of().getOsFamily().isWindow()) {
            NPath local = NPath.ofUserHome().resolve(NWorkspace.of().getSysEnv("APPDATA") + "/Notepad++/userDefineLangs/" + info.getLanguageId() + ".xml");
            boolean doForce = false;
            if (!local.isRegularFile()) {
                //
            } else {
                if (NAsk.of()
                        .setDefaultValue(true)
                        .setRememberMeKey("NSysEditorSupportCmd.forceInstall")
                        .forBoolean(NMsg.ofC("%s %s syntax highlighting is already configured in %s.\n Override it ?", styledLangId, app, local))
                        .getBooleanValue()) {
                    doForce = true;
                } else {
                    NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting is already configured in %s", styledLangId, app, local));
                    return;
                }
            }

            NPath remote = info.getRepoFolder().resolve("/notepad-plus-plus/language.xml");
            remote.copyTo(local.mkParentDirs());
            if (doForce) {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting re-installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            } else {
                NTrace.println(NMsg.ofC("%s %s syntax highlighting installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
            }
        } else {
            NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting for %s is not supported", styledLangId, app, NWorkspace.of().getOsFamily()));
        }
    }

    private void runActionInstallJEdit(Info info) {
        NMsg styledLangId = NMsg.ofStyledKeyword(info.getLanguageId());
        NMsg app = NMsg.ofStyledDate("jEdit");
        NPath local = NPath.ofUserHome().resolve(".jedit/modes/" + info.getLanguageId() + ".xml");
        boolean doForce = false;
        if (!local.isRegularFile()) {
            //
        } else {
            if (NAsk.of()
                    .setDefaultValue(true)
                    .setRememberMeKey("NSysEditorSupportCmd.forceInstall")
                    .forBoolean(NMsg.ofC("%s %s syntax highlighting is already configured in %s.\n Override it ?", styledLangId, app, local))
                    .getBooleanValue()) {
                doForce = true;
            } else {
                NTrace.println(NMsg.ofC("Skipped installation : %s %s syntax highlighting is already configured in %s", styledLangId, app, local));
                return;
            }
        }

        NPath remote = info.getRepoFolder().resolve("/jedit/language.xml");
        remote.copyTo(local.mkParentDirs());

        // update catalog
        try {
            // Ensure parent directory exists
            NPath catalog = local.resolveSibling("catalog");

            Document doc;
            Element modesElement;
            boolean modeExists = false;
            if (catalog.isRegularFile()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringComments(true);
                factory.setIgnoringElementContentWhitespace(true);
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setValidating(false);

                DocumentBuilder builder = factory.newDocumentBuilder();
                // Ignore DTD resolution errors
                builder.setEntityResolver((publicId, systemId) -> new org.xml.sax.InputSource(new StringReader("")));

                doc = builder.parse(catalog.toFile().get());

                modesElement = doc.getDocumentElement();

                // Check if mode already exists
                modeExists = runActionInstallJEdit_modeExists(modesElement, info.getLanguageId());
            } else {
//                System.out.println("Creating new catalog file...");
                doc = runActionInstallJEdit_createNewCatalog();
                modesElement = doc.getDocumentElement();
            }

            if (!modeExists) {
                // Add the new mode
                runActionInstallJEdit_addMode(doc, modesElement, info.getLanguageId(), info.getLanguageId() + ".xml", "*." + info.getFileExtension());

                // Write back to file
                runActionInstallJEdit_writeCatalog(doc, catalog);
            }
            //System.out.println("Successfully updated catalog: " + catalogPath);
        } catch (TransformerException e) {
            throw new NIllegalArgumentException(NMsg.ofC("error : ", e), e);
        } catch (ParserConfigurationException e) {
            throw new NIllegalArgumentException(NMsg.ofC("error : ", e), e);
        } catch (IOException e) {
            throw new NIllegalArgumentException(NMsg.ofC("error : ", e), e);
        } catch (SAXException e) {
            throw new NIllegalArgumentException(NMsg.ofC("error : ", e), e);
        }

        if (doForce) {
            NTrace.println(NMsg.ofC("%s %s syntax highlighting re-installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
        } else {
            NTrace.println(NMsg.ofC("%s %s syntax highlighting installed successfully to %s. You might need to restart %s", styledLangId, app, local, app));
        }
    }

    /**
     * Creates a new catalog document from scratch.
     */
    private static Document runActionInstallJEdit_createNewCatalog() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Add DOCTYPE
        DOMImplementation domImpl = doc.getImplementation();
        DocumentType doctype = domImpl.createDocumentType("MODES",
                null, "catalog.dtd");

        // Create root element
        Element modesElement = doc.createElement("MODES");
        doc.appendChild(modesElement);

        return doc;
    }

    private static boolean runActionInstallJEdit_modeExists(Element modesElement, String modeName) {
        NodeList modeNodes = modesElement.getElementsByTagName("MODE");
        for (int i = 0; i < modeNodes.getLength(); i++) {
            Element modeElement = (Element) modeNodes.item(i);
            String name = modeElement.getAttribute("NAME");
            if (modeName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new MODE element to the catalog.
     */
    private static void runActionInstallJEdit_addMode(Document doc, Element modesElement,
                                                      String name, String file, String fileGlob) {
        Element modeElement = doc.createElement("MODE");
        modeElement.setAttribute("NAME", name);
        modeElement.setAttribute("FILE", file);
        modeElement.setAttribute("FILE_NAME_GLOB", fileGlob);

        // Add with proper indentation
        Text indent = doc.createTextNode("\n  ");
        modesElement.appendChild(indent);
        modesElement.appendChild(modeElement);

        // Add final newline before closing tag
        Text finalNewline = doc.createTextNode("\n");
        modesElement.appendChild(finalNewline);
    }

    /**
     * Writes the document back to the catalog file with proper formatting.
     */
    private static void runActionInstallJEdit_writeCatalog(Document doc, NPath catalogPath) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Set output properties for nice formatting
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "catalog.dtd");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(catalogPath.toFile().get());
        transformer.transform(source, result);
    }

    public static class Info implements NCopiable, Cloneable {
        private String languageGroupId;
        private String languageId;
        private String languageVersion;
        private String languageName;
        private String fileExtension;
        private String fileName;
        private NPath repoFolder;

        public String getFileName() {
            return fileName;
        }

        public Info setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public String getLanguageVersion() {
            return languageVersion;
        }

        public Info setLanguageVersion(String languageVersion) {
            this.languageVersion = languageVersion;
            return this;
        }

        public String getLanguageGroupId() {
            return languageGroupId;
        }

        public Info setLanguageGroupId(String languageGroupId) {
            this.languageGroupId = languageGroupId;
            return this;
        }

        public String getLanguageId() {
            return languageId;
        }

        public Info setLanguageId(String languageId) {
            this.languageId = languageId;
            return this;
        }

        public String getLanguageName() {
            return languageName;
        }

        public Info setLanguageName(String languageName) {
            this.languageName = languageName;
            return this;
        }

        public NPath getRepoFolder() {
            return repoFolder;
        }

        public Info setRepoFolder(NPath repoFolder) {
            this.repoFolder = repoFolder;
            return this;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        public Info setFileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
            return this;
        }

        @Override
        public Info copy() {
            return clone();
        }

        @Override
        public Info clone() {
            try {
                return (Info) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
