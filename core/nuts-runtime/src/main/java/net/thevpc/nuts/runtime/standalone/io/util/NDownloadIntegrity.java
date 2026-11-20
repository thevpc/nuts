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

import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;

import java.util.regex.Pattern;

/**
 * Integrity verification for downloaded tool/JDK archives. Internal to the
 * runtime: no public API change.
 */
public final class NDownloadIntegrity {

    private static final Pattern HEX_256 = Pattern.compile("[0-9a-fA-F]{64}");
    private static final Pattern HEX_160 = Pattern.compile("[0-9a-fA-F]{40}");

    private NDownloadIntegrity() {
    }

    /**
     * Fail closed when the metadata provides no usable checksum or when the
     * downloaded bytes do not match it.
     */
    public static void verify(NPath file, String expectedChecksum) {
        if (NBlankable.isBlank(expectedChecksum)) {
            throw new NIOException(NMsg.ofC("missing integrity checksum for downloaded file : %s", file));
        }
        String value = expectedChecksum.trim();
        boolean sha1 = false;
        int colon = value.indexOf(':');
        if (colon > 0) {
            String alg = value.substring(0, colon);
            if ("sha256".equalsIgnoreCase(alg) || "sha-256".equalsIgnoreCase(alg)) {
                value = value.substring(colon + 1).trim();
            } else if ("sha1".equalsIgnoreCase(alg) || "sha-1".equalsIgnoreCase(alg)) {
                sha1 = true;
                value = value.substring(colon + 1).trim();
            }
        }
        if (HEX_160.matcher(value).matches()) {
            sha1 = true;
        } else if (!HEX_256.matcher(value).matches()) {
            throw new NIOException(NMsg.ofC("unsupported integrity checksum for downloaded file %s : %s",
                    file, value));
        }
        String actual = (sha1 ? NDigest.of().sha1() : NDigest.of().sha256())
                .source(file).computeString();
        if (!actual.equalsIgnoreCase(value)) {
            throw new NIOException(NMsg.ofC("integrity check failed for downloaded file %s : expected %s, got %s",
                    file, value, actual));
        }
    }
}
