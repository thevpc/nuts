package net.thevpc.nuts.spi;

import net.thevpc.nuts.core.NWorkspaceExtension;

/**
 * NExtensionLifeCycle interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExtensionLifeCycle extends NComponent {
    /**
     * On init extension.
     *
     * @param extension extension
     */
    default void onInitExtension(NWorkspaceExtension extension){

    }

    /**
     * On disable extension.
     *
     * @param extension extension
     */
    default void onDisableExtension(NWorkspaceExtension extension){

    }

    /**
     * On enable extension.
     *
     * @param extension extension
     */
    default void onEnableExtension(NWorkspaceExtension extension){

    }

    /**
     * On destroy extension.
     *
     * @param extension extension
     */
    default void onDestroyExtension(NWorkspaceExtension extension){

    }

    /**
     * On discover type.
     *
     * @param workspaceExtension workspace extension
     * @param discoveredType discovered type
     */
    default void onDiscoverType(NWorkspaceExtension workspaceExtension, Class<?> discoveredType){

    }
}
