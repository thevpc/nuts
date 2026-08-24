/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.text.NMsg;

/**
 * Exception Thrown when a locked object is invoked.
 *
 * @author thevpc
 * @app.category Exceptions
 * @since 0.8.7
 */
public class NInterruptedException extends NException {
    /**
     * N interrupted exception.
     *
     * @param throwable throwable
     * @return n interrupted exception result
     */
    public NInterruptedException(Throwable throwable) {
      /**
       * Super.
       *
       * @param NException.getErrorMessage(throwable)) n exception.get error message(throwable))
       * @param throwable throwable
       */
        super(NMsg.ofC("%s", NException.getErrorMessage(throwable)), throwable);
    }

    /**
     * N interrupted exception.
     *
     * @param message message
     * @return n interrupted exception result
     */
    public NInterruptedException(NMsg message) {
      /**
       * Super.
       *
       * @param message message
       */
        super(message);
    }

    /**
     * N interrupted exception.
     *
     * @return n interrupted exception result
     */
    public NInterruptedException() {
      /**
       * Super.
       *
       * @param NMsg.ofC("interrupted") n msg.of c("interrupted")
       */
        super(NMsg.ofC("interrupted"));
    }

    /**
     * N interrupted exception.
     *
     * @param message message
     * @param cause cause
     * @return n interrupted exception result
     */
    public NInterruptedException(NMsg message, Throwable cause) {
      /**
       * Super.
       *
       * @param message message
       * @param cause cause
       */
        super(message, cause);
    }

    /**
     * N interrupted exception.
     *
     * @param message message
     * @param cause cause
     * @param enableSuppression enable suppression
     * @param writableStackTrace writable stack trace
     * @return n interrupted exception result
     */
    public NInterruptedException(NMsg message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      /**
       * Super.
       *
       * @param message message
       * @param cause cause
       * @param enableSuppression enable suppression
       * @param writableStackTrace writable stack trace
       */
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
