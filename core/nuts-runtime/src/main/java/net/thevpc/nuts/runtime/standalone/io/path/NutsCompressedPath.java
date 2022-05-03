package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class NutsCompressedPath extends NutsPathBase {

    private final String compressedForm;
    private final NutsString formattedCompressedForm;
    private final NutsPath base;
    private final NutsCompressedPathHelper compressedPathHelper;

    public NutsCompressedPath(NutsPath base,NutsCompressedPathHelper compressedPathHelper) {
        super(base.getSession());
        this.base = base;
        this.formattedCompressedForm = compressedPathHelper.toCompressedString(base,base.getSession());
        this.compressedForm = this.formattedCompressedForm.filteredText();
        this.compressedPathHelper = compressedPathHelper;
    }

    @Override
    public NutsPath copy() {
        return new NutsCompressedPath(
                base,compressedPathHelper
        ).copyExtraFrom(this);
    }

    @Override
    public String getContentEncoding() {
        return base.getContentEncoding();
    }

    @Override
    public String getContentType() {
        return base.getContentType();
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public String getLocation() {
        return base.getLocation();
    }

    @Override
    public NutsPath resolve(String other) {
        return base.resolve(other).toCompressedForm();
    }

    @Override
    public NutsPath resolve(NutsPath other) {
        return base.resolve(other).toCompressedForm();
    }

    @Override
    public NutsPath resolveSibling(String other) {
        return base.resolveSibling(other).toCompressedForm();
    }

    @Override
    public NutsPath resolveSibling(NutsPath other) {
        return base.resolveSibling(other).toCompressedForm();
    }

    @Override
    public byte[] readAllBytes() {
        return base.readAllBytes();
    }

    @Override
    public NutsPath writeBytes(byte[] bytes) {
        return base.writeBytes(bytes);
    }

    @Override
    public String getProtocol() {
        return base.getProtocol();
    }

    @Override
    public NutsPath toCompressedForm() {
        return this;
    }

    @Override
    public URL toURL() {
        return base.toURL();
    }

    @Override
    public Path toFile() {
        return base.toFile();
    }

    @Override
    public NutsStream<NutsPath> list() {
        return base.list();
    }

    @Override
    public InputStream getInputStream() {
        return (InputStream) NutsIO.of(getSession()).createInputSource(base.getInputStream(),getInputMetaData());
    }

    @Override
    public OutputStream getOutputStream() {
        return (OutputStream) NutsIO.of(getSession()).createOutputTarget(base.getOutputStream(),getOutputMetaData());
    }

    @Override
    public NutsPath deleteTree() {
        return base.deleteTree();
    }

    @Override
    public NutsPath delete(boolean recurse) {
        return base.delete(recurse);
    }

    @Override
    public NutsPath mkdir(boolean parents) {
        return base.mkdir(parents);
    }

    @Override
    public NutsPath mkdirs() {
        return base.mkdirs();
    }

    @Override
    public NutsPath mkdir() {
        return base.mkdir();
    }

    @Override
    public NutsPath expandPath(Function<String, String> resolver) {
        return base.expandPath(resolver).toCompressedForm();
    }

    @Override
    public NutsPath mkParentDirs() {
        return base.mkParentDirs();
    }

    @Override
    public boolean isOther() {
        return base.isOther();
    }

    @Override
    public boolean isSymbolicLink() {
        return base.isSymbolicLink();
    }

    @Override
    public boolean isDirectory() {
        return base.isDirectory();
    }

    @Override
    public boolean isRegularFile() {
        return base.isRegularFile();
    }

    @Override
    public boolean isRemote() {
        return base.isRemote();
    }

    @Override
    public boolean isLocal() {
        return base.isLocal();
    }

    @Override
    public boolean exists() {
        return base.exists();
    }

    @Override
    public long getContentLength() {
        return base.getContentLength();
    }

    @Override
    public Instant getLastModifiedInstant() {
        return base.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant() {
        return base.getLastAccessInstant();
    }

    @Override
    public Instant getCreationInstant() {
        return base.getCreationInstant();
    }

    @Override
    public NutsPath getParent() {
        return base.getParent();
    }

    @Override
    public boolean isAbsolute() {
        return base.isAbsolute();
    }

    @Override
    public NutsPath normalize() {
        return base.normalize();
    }

    @Override
    public NutsPath toAbsolute() {
        return toAbsolute((NutsPath) null);
    }

    @Override
    public NutsPath toAbsolute(String basePath) {
        return base.toAbsolute(basePath).toCompressedForm();
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath) {
        if (base.isAbsolute()) {
            return this;
        }
        return basePath.toAbsolute(basePath).toCompressedForm();
    }

    @Override
    public NutsPath toRelativePath(NutsPath basePath) {
        return base.toRelativePath(basePath);
    }

    @Override
    public String owner() {
        return base.owner();
    }

    @Override
    public String group() {
        return base.group();
    }

    @Override
    public Set<NutsPathPermission> getPermissions() {
        return base.getPermissions();
    }

    @Override
    public NutsPath setPermissions(NutsPathPermission... permissions) {
        base.setPermissions(permissions);
        return this;
    }

    @Override
    public NutsPath addPermissions(NutsPathPermission... permissions) {
        base.addPermissions(permissions);
        return this;
    }

    @Override
    public NutsPath removePermissions(NutsPathPermission... permissions) {
        base.removePermissions(permissions);
        return this;
    }

    @Override
    public boolean isName() {
        return base.isName();
    }

    @Override
    public int getPathCount() {
        return base.getPathCount();
    }

    @Override
    public boolean isRoot() {
        return base.isRoot();
    }

    @Override
    public NutsStream<NutsPath> walk(int maxDepth, NutsPathOption[] options) {
        return base.walk(maxDepth, options);
    }

    @Override
    public NutsPath subpath(int beginIndex, int endIndex) {
        return base.subpath(beginIndex, endIndex).toCompressedForm();
    }

    @Override
    public String getItem(int index) {
        return base.getItem(index);
    }

    @Override
    public List<String> getItems() {
        return base.getItems();
    }

    @Override
    public void moveTo(NutsPath other, NutsPathOption... options) {
        base.moveTo(other);
    }

    @Override
    public void copyTo(NutsPath other, NutsPathOption... options) {
        base.copyTo(other);
    }

    @Override
    public NutsPath getRoot() {
        return base.getRoot();
    }

    @Override
    public NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor, NutsPathOption... options) {
        base.walkDfs(visitor, options);
        return this;
    }

    @Override
    public NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {
        base.walkDfs(visitor, maxDepth, options);
        return this;
    }

    @Override
    public NutsStream<NutsPath> walkGlob(NutsPathOption... options) {
        return base.walkGlob(options);
    }

    @Override
    public String toString() {
        return String.valueOf(compressedForm);
    }

    @Override
    public NutsFormat formatter(NutsSession session) {
        return new MyPathFormat(this)
                .setSession(getSession());
    }

    private static class MyPathFormat extends DefaultFormatBase<NutsFormat> {

        private final NutsCompressedPath p;

        public MyPathFormat(NutsCompressedPath p) {
            super(p.getSession(), "path");
            this.p = p;
        }

        public NutsString asFormattedString() {
            return p.formattedCompressedForm;
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }

        @Override
        public int getSupportLevel(NutsSupportLevelContext context) {
            return DEFAULT_SUPPORT;
        }
    }


}
