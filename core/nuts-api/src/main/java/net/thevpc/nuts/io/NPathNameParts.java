package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Objects;

public class NPathNameParts {
    private String baseName;
    private String extension;
    private String fullExtension;
    private NPathExtensionType type;

    public static NPathNameParts ofLong(String baseName, String extension, String fullExtension) {
        return new NPathNameParts(baseName, extension, fullExtension, NPathExtensionType.LONG);
    }

    public static NPathNameParts ofShort(String baseName, String extension, String fullExtension) {
        return new NPathNameParts(baseName, extension, fullExtension, NPathExtensionType.SHORT);
    }

    public static NPathNameParts ofSmart(String baseName, String extension, String fullExtension) {
        return new NPathNameParts(baseName, extension, fullExtension, NPathExtensionType.SMART);
    }

    public static NPathNameParts ofLong(String baseName, String extension) {
        return new NPathNameParts(baseName, extension, extension == null ? "" : ("." + extension), NPathExtensionType.LONG);
    }

    public static NPathNameParts ofShort(String baseName, String extension) {
        return new NPathNameParts(baseName, extension, extension == null ? "" : ("." + extension), NPathExtensionType.SHORT);
    }

    public static NPathNameParts ofSmart(String baseName, String extension) {
        return new NPathNameParts(baseName, extension, extension == null ? "" : ("." + extension), NPathExtensionType.SMART);
    }

    public NPathNameParts(String baseName, String extension, String fullExtension, NPathExtensionType type) {
        this.baseName = baseName == null ? "" : baseName;
        this.extension = extension == null ? "" : extension;
        this.fullExtension = fullExtension == null ? "" : fullExtension;
        this.type = type == null ? NPathExtensionType.SHORT : type;
    }

    public NPathExtensionType getType() {
        return type;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getExtension() {
        return extension;
    }

    public String getFullExtension() {
        return fullExtension;
    }

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
                    switch (NStringUtils.trim(s).toLowerCase()) {
                        case "name":
                            return toName();
                        case "base":
                        case "basename":
                        case "base-name":
                            return getBaseName();
                        case "ext":
                        case "extension":
                            return getExtension();
                        case "fullext":
                        case "full-ext":
                        case "full-extension":
                        case "fullextension":
                            return getFullExtension();
                    }
                    return null;
                }
        ).toString();
    }

    public String toNameWithExtension(String extension) {
        if (NBlankable.isBlank(extension)) {
            return baseName;
        }
        extension = extension.trim();
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return baseName + extension;
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
