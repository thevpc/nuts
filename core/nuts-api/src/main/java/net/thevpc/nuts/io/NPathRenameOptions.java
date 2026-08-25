package net.thevpc.nuts.io;

import java.util.Objects;

/**
 * NPathRenameOptions class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPathRenameOptions {
    private final String template;
    private final String extension;
    private final NPathExtensionType type;

    /**
     * N path rename options.
     *
     * @param template template
     * @param extension extension
     * @param type type
     * @return n path rename options result
     */
    private NPathRenameOptions(String template, String extension, NPathExtensionType type) {
        this.template = template;
        this.extension = extension;
        this.type = type;
    }

    /**
     * Creates a new instance of extension.
     *
     * @param ext ext
     * @return of extension result
     */
    public static NPathRenameOptions ofExtension(String ext) {
        return new NPathRenameOptions(null, ext, NPathExtensionType.SMART);
    }

    /**
     * Creates a new instance of of template.
     *
     * @param template template
     * @return of template result
     */
    public static NPathRenameOptions ofTemplate(String template) {
        return new NPathRenameOptions(template, null, NPathExtensionType.SMART);
    }

    /**
     * With smart extension.
     *
     * @return with smart extension result
     */
    public NPathRenameOptions withSmartExtension() {
        return new NPathRenameOptions(this.template, this.extension, NPathExtensionType.SMART);
    }

    /**
     * With long extension.
     *
     * @return with long extension result
     */
    public NPathRenameOptions withLongExtension() {
        return new NPathRenameOptions(this.template, this.extension, NPathExtensionType.LONG);
    }

    /**
     * With short extension.
     *
     * @return with short extension result
     */
    public NPathRenameOptions withShortExtension() {
        return new NPathRenameOptions(this.template, this.extension, NPathExtensionType.SHORT);
    }

    /**
     * Template.
     *
     * @return template result
     */
    public String template() { return template; }
    /**
     * Extension.
     *
     * @return extension result
     */
    public String extension() { return extension; }
    /**
     * Type.
     *
     * @return type result
     */
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
