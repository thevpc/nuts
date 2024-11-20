package net.thevpc.nuts.spi;

import net.thevpc.nuts.NWorkspaceExtension;

public interface NExtensionLifeCycle extends NComponent {
    default void onInitExtension(NWorkspaceExtension extension){

    }

    default void onDisableExtension(NWorkspaceExtension extension){

    }

    default void onEnableExtension(NWorkspaceExtension extension){

    }

    default void onDestroyExtension(NWorkspaceExtension extension){

    }
}
