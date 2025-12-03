package net.thevpc.nuts.runtime.standalone.descriptor.format;

import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NDescriptorStyle;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.text.NDescriptorFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextCode;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NUnsupportedOperationException;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;

public class DefaultNDescriptorFormat extends DefaultFormatBase<NDescriptorFormat> implements NDescriptorFormat {

    private boolean compact;
    private NDescriptor desc;
    private NDescriptorStyle descriptorStyle;

    public DefaultNDescriptorFormat() {
        super("descriptor-format");
    }

    public NDescriptorFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NDescriptorStyle getDescriptorStyle() {
        return descriptorStyle;
    }

    @Override
    public NDescriptorFormat setDescriptorStyle(NDescriptorStyle descriptorStyle) {
        this.descriptorStyle = descriptorStyle;
        return this;
    }


    @Override
    public NDescriptorFormat compact(boolean compact) {
        return setCompact(compact);
    }

    @Override
    public NDescriptorFormat compact() {
        return compact(true);
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NDescriptorFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NDescriptor getDescriptor() {
        return desc;
    }

    public NDescriptorFormat setDescriptor(NDescriptor desc) {
        this.desc = desc;
        return this;
    }

    public NDescriptorFormat setValue(NDescriptor desc) {
        return setDescriptor(desc);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    private void formatManifest(NDescriptor desc, NPrintStream out) {
        if (desc == null) {
            return;
        }

        // MANIFEST.MF requires line wrapping at 72 characters
        // Continuation lines start with a space
        ManifestWriter writer = new ManifestWriter(out);

        // Required manifest version
        writer.writeAttribute("Manifest-Version", "1.0");

        // Standard JAR manifest attributes
        NId id = desc.getId();
        if (id != null) {
            if (id.getVersion() != null && !id.getVersion().isBlank()) {
                writer.writeAttribute("Implementation-Version", id.getVersion().getValue());
            }
            if (!NBlankable.isBlank(id.getGroupId())) {
                writer.writeAttribute("Implementation-Vendor-Id", id.getGroupId());
            }
            if (!NBlankable.isBlank(id.getArtifactId())) {
                writer.writeAttribute("Implementation-Title", id.getArtifactId());
                // Also write as Automatic-Module-Name
                String moduleName = !NBlankable.isBlank(id.getGroupId())
                    ? id.getGroupId() + "." + id.getArtifactId()
                    : id.getArtifactId();
                writer.writeAttribute("Automatic-Module-Name", moduleName);
            }
        }

        // Implementation-Vendor-Title from name
        String name = NStringUtils.trimToNull(desc.getName());
        if (name != null) {
            writer.writeAttribute("Implementation-Vendor-Title", name);
        }

        // Main-Class from executor
        String mainClass = NStringUtils.trimToNull(extractMainClass(desc));
        if (mainClass != null) {
            writer.writeAttribute("Main-Class", mainClass);
        }

        // Nuts-specific attributes
        if (id != null) {
            writer.writeAttribute("Nuts-Id", id.toString());
        }

        if (name != null) {
            writer.writeAttribute("Nuts-Name", name);
        }

        String description = NStringUtils.trimToNull(desc.getDescription());
        if (description != null) {
            writer.writeAttribute("Nuts-Description", description);
        }

        String genericName = NStringUtils.trimToNull(desc.getGenericName());
        if (genericName != null) {
            writer.writeAttribute("Nuts-Generic-Name", genericName);
        }

        String packaging = NStringUtils.trimToNull(desc.getPackaging());
        if (packaging != null) {
            writer.writeAttribute("Nuts-Packaging", packaging);
        }

        // Flags
        if (desc.getFlags() != null && !desc.getFlags().isEmpty()) {
            String flags = desc.getFlags().stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.joining(" "));
            writer.writeAttribute("Nuts-Flags", flags);
        }

        // Categories
        if (desc.getCategories() != null && !desc.getCategories().isEmpty()) {
            String categories = String.join(" ", desc.getCategories());
            writer.writeAttribute("Nuts-Categories", categories);
        }

        // Icons
        if (desc.getIcons() != null && !desc.getIcons().isEmpty()) {
            String icons = String.join(" ", desc.getIcons());
            writer.writeAttribute("Nuts-Icons", icons);
        }

        // Dependencies (semicolon-separated)
        if (desc.getDependencies() != null && !desc.getDependencies().isEmpty()) {
            String dependencies = desc.getDependencies().stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.joining(";"));
            writer.writeAttribute("Nuts-Dependencies", dependencies);
        }

        // Properties
        if (desc.getProperties() != null) {
            for (net.thevpc.nuts.artifact.NDescriptorProperty prop : desc.getProperties()) {
                String propName = NStringUtils.trimToNull(prop.getName());
                if (propName != null) {
                    String value = prop.getValue() != null ? prop.getValue().asString().orElse("") : "";
                    writer.writeAttribute("Nuts-Property-" + propName, value);
                }
            }
        }

        // End with blank line
        out.println();
    }

    private String extractMainClass(NDescriptor desc) {
        if (desc.getExecutor() == null) {
            return null;
        }

        net.thevpc.nuts.artifact.NArtifactCall executor = desc.getExecutor();
        if (executor.getArguments() == null || executor.getArguments().isEmpty()) {
            return null;
        }

        // Arguments are stored as two separate elements: "--main-class=" and the class name
        // --main-class= followed by the actual class name
        java.util.List<String> args = executor.getArguments();
        for (int i = 0; i < args.size() - 1; i++) {
            String arg = args.get(i);
            if ("--main-class=".equals(arg)) {
                return args.get(i + 1);
            }
        }

        // checking  concatenated form "--main-class=ClassName" for compatibility
        for (String arg : args) {
            if (arg != null && arg.startsWith("--main-class=") && arg.length() > "--main-class=".length()) {
                return arg.substring("--main-class=".length());
            }
        }

        return null;
    }

    // Helper class to handle MANIFEST.MF line wrapping
    // JAR spec: lines must not exceed 72 bytes in UTF-8 encoding
    private static class ManifestWriter {
        private final NPrintStream out;
        private static final int MAX_LINE_BYTES = 72;

        public ManifestWriter(NPrintStream out) {
            this.out = out;
        }

        public void writeAttribute(String name, String value) {
            if (value == null || value.isEmpty()) {
                return;
            }

            String line = name + ": " + value;
            byte[] lineBytes = line.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            if (lineBytes.length <= MAX_LINE_BYTES) {
                out.println(line);
            } else {
                // Write first line (up to 72 bytes)
                int firstLineEnd = findSplitPosition(line, MAX_LINE_BYTES);
                out.println(line.substring(0, firstLineEnd));

                // Write continuation lines (starting with space, up to 72 bytes each)
                int pos = firstLineEnd;
                while (pos < line.length()) {
                    // -1 byte for leading space
                    int chunkEnd = findSplitPosition(line.substring(pos), MAX_LINE_BYTES - 1);
                    out.println(" " + line.substring(pos, pos + chunkEnd));
                    pos += chunkEnd;
                }
            }
        }

        /**
         * Find the position to split a string so it doesn't exceed maxBytes in UTF-8.
         * Avoids splitting in the middle of a multi-byte character.
         */
        private int findSplitPosition(String str, int maxBytes) {
            if (str.isEmpty()) {
                return 0;
            }

            byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            if (bytes.length <= maxBytes) {
                return str.length();
            }

            // Binary search for the right split position
            int left = 0;
            int right = str.length();
            int result = 0;

            while (left <= right) {
                int mid = (left + right) / 2;
                byte[] chunk = str.substring(0, mid).getBytes(java.nio.charset.StandardCharsets.UTF_8);

                if (chunk.length <= maxBytes) {
                    result = mid;
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }

            return result;
        }
    }


    @Override
    public void print(NPrintStream out) {
        NDescriptorStyle s = getDescriptorStyle();
        if (s == null) {
            s = NDescriptorStyle.NUTS;
        }
        switch (s) {
            case NUTS: {
                if (isNtf()) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    NElementWriter.ofJson().setNtf(true).setCompact(isCompact())
                            .write(desc, os);
                    NTextCode r = NText.ofCode("json", os.toString());
                    out.print(r);
                } else {
                    NElementWriter.ofJson().setCompact(isCompact())
                            .write(desc, out);
                }
                break;
            }
            case MANIFEST: {
                formatManifest(desc, out);
                break;
            }
            case MAVEN: {
                throw new NUnsupportedOperationException(NMsg.ofC("formatting descriptor in %s format is not yet implemented yet, your help is more than welcome", s));
            }
        }
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}
