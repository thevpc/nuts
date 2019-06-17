package net.vpc.app.nuts.core.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import net.vpc.app.nuts.core.DefaultNutsDependencyBuilder;

public class DefaultNutsDependencyFormat extends DefaultFormatBase<NutsDependencyFormat> implements NutsDependencyFormat {

    private boolean omitNamespace;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitEnv = true;
    private boolean omitFace = true;
    private boolean highlightImportedGroup;
    private boolean highlightScope;
    private boolean highlightOptional;
    private NutsDependency value;

    public DefaultNutsDependencyFormat(NutsWorkspace ws) {
        super(ws, "id-format");
    }

    @Override
    public boolean isOmitNamespace() {
        return omitNamespace;
    }

    @Override
    public NutsDependencyFormat setOmitNamespace(boolean omitNamespace) {
        this.omitNamespace = omitNamespace;
        return this;
    }

    @Override
    public boolean isOmitGroup() {
        return omitGroup;
    }

    @Override
    public NutsDependencyFormat setOmitGroup(boolean omitGroup) {
        this.omitGroup = omitGroup;
        return this;
    }

    @Override
    public boolean isOmitImportedGroup() {
        return omitImportedGroup;
    }

    @Override
    public NutsDependencyFormat setOmitImportedGroup(boolean omitImportedGroup) {
        this.omitImportedGroup = omitImportedGroup;
        return this;
    }

    @Override
    public boolean isOmitEnv() {
        return omitEnv;
    }

    @Override
    public NutsDependencyFormat setOmitEnv(boolean omitEnv) {
        this.omitEnv = omitEnv;
        return this;
    }

    @Override
    public boolean isOmitFace() {
        return omitFace;
    }

    @Override
    public NutsDependencyFormat setOmitFace(boolean omitFace) {
        this.omitFace = omitFace;
        return this;
    }

    @Override
    public boolean isHighlightImportedGroup() {
        return highlightImportedGroup;
    }

    @Override
    public NutsDependencyFormat setHighlightImportedGroup(boolean highlightImportedGroup) {
        this.highlightImportedGroup = highlightImportedGroup;
        return this;
    }

    @Override
    public boolean isHighlightScope() {
        return highlightScope;
    }

    @Override
    public NutsDependencyFormat setHighlightScope(boolean highlightScope) {
        this.highlightScope = highlightScope;
        return this;
    }

    @Override
    public boolean isHighlightOptional() {
        return highlightOptional;
    }

    @Override
    public NutsDependencyFormat setHighlightOptional(boolean highlightOptional) {
        this.highlightOptional = highlightOptional;
        return this;
    }

    @Override
    public String format() {
        return ws.format().id()
                .session(getSession())
                .setValue(value.getId())
                .setHighlightImportedGroup(isHighlightImportedGroup())
                .setHighlightOptional(isHighlightOptional())
                .setHighlightScope(isHighlightScope())
                .setOmitEnv(isOmitEnv())
                .setOmitFace(isOmitFace())
                .setOmitGroup(isOmitGroup())
                .setOmitImportedGroup(isOmitImportedGroup())
                .setOmitNamespace(isOmitNamespace())
                .format();
    }

    @Override
    public NutsDependency getValue() {
        return value;
    }

    @Override
    public NutsDependencyFormat set(NutsDependency id) {
        return setValue(id);
    }

    @Override
    public NutsDependencyFormat setValue(NutsDependency id) {
        this.value = id;
        return this;
    }

//    @Override
    public NutsDependency parseRequiredDependency(String nutFormat) {
        NutsDependency id = parse(nutFormat);
        if (id == null) {
            throw new NutsParseException(ws, "Invalid Id format : " + nutFormat);
        }
        return id;
    }

    @Override
    public NutsDependency parse(String dependency) {
        return CoreNutsUtils.parseNutsDependency(ws, dependency);
    }

    @Override
    public void print(Writer out) {
        try {
            out.write(format());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--omit-env": {
                setOmitEnv(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-face": {
                setOmitFace(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-group": {
                setOmitGroup(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-imported-group": {
                setOmitImportedGroup(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-namespace": {
                setOmitNamespace(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--highlight-imported-group": {
                setHighlightImportedGroup(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--highlight-optional": {
                setHighlightOptional(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--highlight-scope": {
                setHighlightScope(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
        }
        return false;
    }
}
