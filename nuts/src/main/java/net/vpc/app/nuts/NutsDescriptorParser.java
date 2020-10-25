package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public interface NutsDescriptorParser {

    /**
     * parse descriptor.
     *
     * @param url URL to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(URL url);

    /**
     * parse descriptor.
     *
     * @param bytes value to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(byte[] bytes);

    /**
     * parse descriptor.
     *
     * @param path path to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(Path path);

    /**
     * parse descriptor.
     *
     * @param file file to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(File file);

    /**
     * parse descriptor.
     *
     * @param stream stream to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(InputStream stream);

    /**
     * parse descriptor.
     *
     * @param descriptorString string to parse
     * @return parsed Descriptor
     * @since 0.5.6
     */
    NutsDescriptor parse(String descriptorString);

    NutsDescriptorParser setLenient(boolean lenient);

    boolean isLenient();
}
