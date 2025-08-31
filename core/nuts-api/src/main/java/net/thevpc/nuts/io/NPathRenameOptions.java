package net.thevpc.nuts.io;

import java.util.Objects;

public class NPathRenameOptions {
    private final String template;
    private final String extension;
    private final NPathExtensionType type;

    private NPathRenameOptions(String template, String extension, NPathExtensionType type) {
        this.template = template;
        this.extension = extension;
        this.type = type;
    }

    public static NPathRenameOptions ofExtension(String ext) {
        return new NPathRenameOptions(null, ext, NPathExtensionType.SMART);
    }

    public static NPathRenameOptions ofTemplate(String template) {
        return new NPathRenameOptions(template, null, NPathExtensionType.SMART);
    }

    public NPathRenameOptions withSmartExtension() {
        return new NPathRenameOptions(this.template, this.extension, NPathExtensionType.SMART);
    }

    public NPathRenameOptions withLongExtension() {
        return new NPathRenameOptions(this.template, this.extension, NPathExtensionType.LONG);
    }

    public NPathRenameOptions withShortExtension() {
        return new NPathRenameOptions(this.template, this.extension, NPathExtensionType.SHORT);
    }

    public String template() { return template; }
    public String extension() { return extension; }
    public NPathExtensionType type() { return type; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NPathRenameOptions that = (NPathRenameOptions) o;
        return Objects.equals(template, that.template) && Objects.equals(extension, that.extension) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(template, extension, type);
    }

    @Override
    public String toString() {
        return "NPathRenameOptions{" +
                "template='" + template + '\'' +
                ", extension='" + extension + '\'' +
                ", type=" + type +
                '}';
    }
}
