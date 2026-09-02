package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSince;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;

/**
 * NPathNameParts class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPathNameParts {
    private String baseName;
    private String extension;
    private String fullExtension;
    private NPathExtensionType type;

    /**
     * Creates a new instance of of long.
     *
     * @param baseName base name
     * @param extension extension
     * @param fullExtension full extension
     * @return of long result
     */
    public static NPathNameParts ofLong(String baseName, String extension, String fullExtension) {
        return new NPathNameParts(baseName, extension, fullExtension, NPathExtensionType.LONG);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param baseName base name
     * @param extension extension
     * @param fullExtension full extension
     * @return of short result
     */
    public static NPathNameParts ofShort(String baseName, String extension, String fullExtension) {
        return new NPathNameParts(baseName, extension, fullExtension, NPathExtensionType.SHORT);
    }

    /**
     * Creates a new instance of of smart.
     *
     * @param baseName base name
     * @param extension extension
     * @param fullExtension full extension
     * @return of smart result
     */
    public static NPathNameParts ofSmart(String baseName, String extension, String fullExtension) {
        return new NPathNameParts(baseName, extension, fullExtension, NPathExtensionType.SMART);
    }

    /**
     * Creates a new instance of of long.
     *
     * @param baseName base name
     * @param extension extension
     * @return of long result
     */
    public static NPathNameParts ofLong(String baseName, String extension) {
        return new NPathNameParts(baseName, extension, extension == null ? "" : ("." + extension), NPathExtensionType.LONG);
    }

    /**
     * Creates a new instance of of short.
     *
     * @param baseName base name
     * @param extension extension
     * @return of short result
     */
    public static NPathNameParts ofShort(String baseName, String extension) {
        return new NPathNameParts(baseName, extension, extension == null ? "" : ("." + extension), NPathExtensionType.SHORT);
    }

    /**
     * Creates a new instance of of smart.
     *
     * @param baseName base name
     * @param extension extension
     * @return of smart result
     */
    public static NPathNameParts ofSmart(String baseName, String extension) {
        return new NPathNameParts(baseName, extension, extension == null ? "" : ("." + extension), NPathExtensionType.SMART);
    }

    /**
     * N path name parts.
     *
     * @param baseName base name
     * @param extension extension
     * @param fullExtension full extension
     * @param type type
     * @return n path name parts result
     */
    public NPathNameParts(String baseName, String extension, String fullExtension, NPathExtensionType type) {
        this.baseName = baseName == null ? "" : baseName;
        this.extension = extension == null ? "" : extension;
        this.fullExtension = fullExtension == null ? "" : fullExtension;
        this.type = type == null ? NPathExtensionType.SHORT : type;
    }

    /**
     * Type.
     *
     * @return type result
     */
    @NGetter
    public NPathExtensionType type() {
        return type;
    }

    /**
     * Base name.
     *
     * @return base name result
     */
    @NGetter
    public String baseName() {
        return baseName;
    }

    /**
     * Extension.
     *
     * @return extension result
     */
    @NGetter
    public String extension() {
        return extension;
    }

    /**
     * Full extension.
     *
     * @return full extension result
     */
    @NGetter
    public String fullExtension() {
        return fullExtension;
    }

    /**
     * Converts to name.
     *
     * @return to name result
     */
    public String toName() {
        return baseName + fullExtension;
    }

    /**
     * build file name based on variables.
     * Example : "${name}-02.${extension}"
     * build a new file name by appending "-02" the the base name
     * acceptable vars :
     * <ul>
     *     <li>name</li>
     *     <li>basename</li>
     *     <li>extension</li>
     *     <li>fullextension</li>
     * </ul>
     *
     * @param template vars template
     * @return file name based on variables
     */
    public String toName(String template) {
        return NMsg.ofV(template,
                s -> {
                    switch (NStringUtils.strip(s).toLowerCase()) {
                        case "name":
                            /**
                             * Converts to name.
                             *
                             * @return to name result
                             */
                            return toName();
                        case "base":
                        case "basename":
                        case "base-name":
                            /**
                             * Base name.
                             *
                             * @return base name result
                             */
                            return baseName();
                        case "ext":
                        case "extension":
                            /**
                             * Extension.
                             *
                             * @return extension result
                             */
                            return extension();
                        case "fullext":
                        case "full-ext":
                        case "full-extension":
                        case "fullextension":
                            /**
                             * Full extension.
                             *
                             * @return full extension result
                             */
                            return fullExtension();
                    }
                    return null;
                }
        ).toString();
    }

    /**
     * Converts to name with extension.
     *
     * @param extension extension
     * @return to name with extension result
     */
    public String toNameWithExtension(String extension) {
        if (NBlankable.isBlank(extension)) {
            return baseName;
        }
        extension = NStringUtils.strip(extension);
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return baseName + extension;
    }

    @NSince("1.0.0")
    public boolean hasDot() {
        return !fullExtension.isEmpty();
    }

    @NSince("1.0.0")
    public boolean hasExtension() {
        return !extension.isEmpty();
    }

    @Override
    public String toString() {
        return "NPathNameParts{" +
                "baseName='" + baseName + '\'' +
                ", extension='" + extension + '\'' +
                ", fullExtension='" + fullExtension + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NPathNameParts that = (NPathNameParts) o;
        return Objects.equals(baseName, that.baseName)
                && Objects.equals(extension, that.extension)
                && Objects.equals(fullExtension, that.fullExtension)
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseName, extension, fullExtension, type);
    }
}
