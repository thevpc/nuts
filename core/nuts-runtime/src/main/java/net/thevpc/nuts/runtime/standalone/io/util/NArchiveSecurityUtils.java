/**
 * ====================================================================
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared safety checks for archive extraction. Internal to the runtime, no API changes.
 */
public final class NArchiveSecurityUtils {

    public static final String PROP_MAX_TOTAL_BYTES = "nuts.archive.max-total-bytes";
    public static final String PROP_MAX_ENTRY_BYTES = "nuts.archive.max-entry-bytes";
    public static final String PROP_MAX_ENTRIES = "nuts.archive.max-entries";
    public static final String PROP_MAX_COMPRESSION_RATIO = "nuts.archive.max-compression-ratio";

    public static final long DEFAULT_MAX_TOTAL_BYTES = 1024L * 1024 * 1024;
    public static final long DEFAULT_MAX_ENTRY_BYTES = 256L * 1024 * 1024;
    public static final int DEFAULT_MAX_ENTRIES = 10000;
    public static final long DEFAULT_MAX_COMPRESSION_RATIO = 200L;

    private NArchiveSecurityUtils() {
    }

    /**
     * Validate and resolve an archive entry name against the extraction root.
     * Rejects absolute paths, parent traversal, Windows drive/UNC forms and
     * paths crossing an existing symbolic link.
     */
    public static Path resolveSafeEntry(Path root, String entryName) throws IOException {
        String validated = validateEntryName(entryName);
        Path rootPath = root.toAbsolutePath().normalize();
        Path resolved = rootPath;
        for (String segment : validated.split("/")) {
            if (segment.isEmpty() || segment.equals(".")) {
                continue;
            }
            resolved = resolved.resolve(segment);
        }
        resolved = resolved.normalize();
        if (resolved.equals(rootPath) || !resolved.startsWith(rootPath)) {
            throw new IOException("unsafe archive entry outside extraction root : " + entryName);
        }
        Path cursor = rootPath;
        Path relative = rootPath.relativize(resolved);
        for (int i = 0; i < relative.getNameCount(); i++) {
            cursor = cursor.resolve(relative.getName(i).toString());
            if (Files.isSymbolicLink(cursor)) {
                throw new IOException("unsafe archive entry crossing symbolic link : " + entryName);
            }
        }
        return resolved;
    }

    /**
     * Validate an entry name without resolving it against a root. Useful for
     * archive visitors that do not extract files themselves.
     *
     * @return the normalized relative name (using '/' separators)
     */
    public static String validateEntryName(String entryName) throws IOException {
        if (entryName == null || entryName.isEmpty()) {
            throw new IOException("empty archive entry name");
        }
        if (entryName.indexOf('\0') >= 0) {
            throw new IOException("invalid archive entry name : " + entryName);
        }
        String name = entryName.replace('\\', '/');
        if (name.startsWith("/")
                || (name.length() >= 2 && Character.isLetter(name.charAt(0)) && name.charAt(1) == ':')) {
            throw new IOException("absolute archive entry name : " + entryName);
        }
        for (String segment : name.split("/")) {
            if (segment.equals("..")) {
                throw new IOException("parent traversal in archive entry name : " + entryName);
            }
        }
        return name;
    }

    /**
     * Budgets applied while extracting an archive. Bounds are read from system
     * properties so they can be tuned without changing public API.
     */
    public static final class Limits {
        private final long maxTotalBytes;
        private final long maxEntryBytes;
        private final int maxEntries;
        private final long maxCompressionRatio;
        private long totalBytes;
        private long entryBytes;
        private int entryCount;

        public Limits(long maxTotalBytes, long maxEntryBytes, int maxEntries, long maxCompressionRatio) {
            this.maxTotalBytes = maxTotalBytes;
            this.maxEntryBytes = maxEntryBytes;
            this.maxEntries = maxEntries;
            this.maxCompressionRatio = maxCompressionRatio;
        }

        public static Limits fromSystemProperties() {
            return new Limits(
                    readLong(PROP_MAX_TOTAL_BYTES, DEFAULT_MAX_TOTAL_BYTES),
                    readLong(PROP_MAX_ENTRY_BYTES, DEFAULT_MAX_ENTRY_BYTES),
                    (int) readLong(PROP_MAX_ENTRIES, DEFAULT_MAX_ENTRIES),
                    readLong(PROP_MAX_COMPRESSION_RATIO, DEFAULT_MAX_COMPRESSION_RATIO)
            );
        }

        private static long readLong(String property, long defaultValue) {
            String v = System.getProperty(property);
            if (v == null || v.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                long parsed = Long.parseLong(v.trim());
                return parsed > 0 ? parsed : defaultValue;
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }

        public void startEntry(long declaredSize, long compressedSize) throws IOException {
            entryCount++;
            if (entryCount > maxEntries) {
                throw new IOException("archive entry count exceeds " + maxEntries);
            }
            entryBytes = 0;
            if (maxEntryBytes > 0 && declaredSize > maxEntryBytes) {
                throw new IOException("archive entry uncompressed size " + declaredSize
                        + " exceeds " + maxEntryBytes);
            }
            if (maxCompressionRatio > 0 && declaredSize > 0 && compressedSize > 0
                    && declaredSize > compressedSize * maxCompressionRatio) {
                throw new IOException("archive entry compression ratio exceeds " + maxCompressionRatio);
            }
        }

        public void addBytes(int count) throws IOException {
            if (count <= 0) {
                return;
            }
            entryBytes += count;
            if (maxEntryBytes > 0 && entryBytes > maxEntryBytes) {
                throw new IOException("archive entry uncompressed size exceeds " + maxEntryBytes);
            }
            totalBytes += count;
            if (maxTotalBytes > 0 && totalBytes > maxTotalBytes) {
                throw new IOException("archive total uncompressed size exceeds " + maxTotalBytes);
            }
        }

        public long totalBytes() {
            return totalBytes;
        }

        public int entryCount() {
            return entryCount;
        }
    }
}
