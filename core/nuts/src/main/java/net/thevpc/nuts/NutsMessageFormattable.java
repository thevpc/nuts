package net.thevpc.nuts;

/**
 * toString() should return {@code  formatMessage().toString()}.
 * it is recommended to implement {@code NutsMessageFormattableBase}.
 */
public interface NutsMessageFormattable {

    /**
     * return formatted message.
     *
     * @param session session, can be null
     * @return formatted message
     */
    NutsMessage formatMessage(NutsSession session);

    default NutsMessage formatMessage() {
        return formatMessage(null);
    }
}
