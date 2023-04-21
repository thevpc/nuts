package net.thevpc.nuts.spi;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspaceExtension;

public interface NExtensionLifeCycle extends NComponent {
    default void onInitExtension(NWorkspaceExtension extension, NSession session){

    }

    default void onDisableExtension(NWorkspaceExtension extension, NSession session){

    }

    default void onEnableExtension(NWorkspaceExtension extension, NSession session){

    }

    default void onDestroyExtension(NWorkspaceExtension extension, NSession session){

    }
}
