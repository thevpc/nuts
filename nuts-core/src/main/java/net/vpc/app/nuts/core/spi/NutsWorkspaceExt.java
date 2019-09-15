/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.impl.def.repos.DefaultNutsInstalledRepository;

/**
 *
 * @author vpc
 */
public interface NutsWorkspaceExt {

    static NutsWorkspaceExt of(NutsWorkspace ws) {
        return (NutsWorkspaceExt) ws;
    }

    String getWelcomeText();

    String getHelpText();

    String getLicenseText();

    String resolveDefaultHelp(Class clazz);

    NutsId resolveEffectiveId(NutsDescriptor descriptor, NutsFetchCommand options);

    /**
     * return installed version
     *
     * @param id id
     * @param session session
     * @return installed version
     */
    String[] getInstalledVersions(NutsId id, NutsSession session);

    NutsIdType resolveNutsIdType(NutsId id);

    String[] getCompanionIds();

    NutsInstallerComponent getInstaller(NutsDefinition nutToInstall, NutsSession session);

    void installImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion);

    void updateImpl(NutsDefinition def, String[] args, NutsInstallerComponent installerComponent, NutsSession session, boolean updateDefaultVersion);

    /**
     * true when core extension is required for running this workspace. A
     * default implementation should be as follow, but developers may implements
     * this with other logic : core extension is required when there are no
     * extensions or when the
     * <code>NutsConstants.ENV_KEY_EXCLUDE_CORE_EXTENSION</code> is forced to
     * false
     *
     * @return true when core extension is required for running this workspace
     */
    boolean requiresCoreExtension();

    //    @Override
    NutsDescriptor resolveEffectiveDescriptor(NutsDescriptor descriptor, NutsSession session);

    DefaultNutsInstalledRepository getInstalledRepository();

    boolean isInstalled(NutsId id, boolean checkDependencies, NutsSession session);

    NutsExecutionContext createNutsExecutionContext(NutsDefinition nutToInstall, String[] args, String[] executorArgs, NutsSession session, boolean failFast, boolean temporary, NutsExecutionType executionType, String commandName);

    @Deprecated
    void deployBoot(NutsSession session, NutsId def,boolean withDependencies);

    /**
     * creates a zip file based on the folder. The folder should contain a
     * descriptor file at its root
     *
     * @param contentFolder folder to bundle
     * @param destFile created bundle file or null to create a file with the
     * very same name as the folder
     * @param session current session
     * @return bundled nuts file, the nuts is neither deployed nor installed!
     */
    //    @Derecated
    //    public NutsDefinition createBundle(Path contentFolder, Path destFile, NutsQueryOptions queryOptions, NutsSession session) {
    //        session = CoreNutsUtils.validateSession(session, this);
    //        if (Files.isDirectory(contentFolder)) {
    //            NutsDescriptor descriptor = null;
    //            Path ext = contentFolder.resolve(NutsConstants.NUTS_DESC_FILE_NAME);
    //            if (Files.exists(ext)) {
    //                descriptor = parse().parseDescriptor(ext);
    //                if (descriptor != null) {
    //                    if ("zip".equals(descriptor.getPackaging())) {
    //                        if (destFile == null) {
    //                            destFile = io().path(io().expandPath(contentFolder.getParent().resolve(descriptor.getId().getGroup() + "." + descriptor.getId().getName() + "." + descriptor.getId().getVersion() + ".zip")));
    //                        }
    //                        try {
    //                            ZipUtils.zip(contentFolder.toString(), new ZipOptions(), destFile.toString());
    //                        } catch (IOException ex) {
    //                            throw new UncheckedIOException(ex);
    //                        }
    //                        return new DefaultNutsDefinition(
    //                                this, null,
    //                                descriptor.getId(),
    //                                descriptor,
    //                                new NutsContent(destFile,
    //                                        true,
    //                                        false),
    //                                null,
    //                    false,false,false,false,null
    //                        );
    //                    } else {
    //                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
    //                    }
    //                }
    //            }
    //            throw new NutsIllegalArgumentException("Invalid Nut Folder source. unable to detect descriptor");
    //        } else {
    //            throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
    //        }
    //    }
    //    @Override
    //    public boolean isFetched(NutsId id, NutsSession session) {
    //        session = CoreNutsUtils.validateSession(session, this);
    //        NutsSession offlineSession = session.copy();
    //        try {
    //            NutsDefinition found = fetch().id(id).offline().setSession(offlineSession).setIncludeInstallInformation(false).setIncludeFile(true).getResultDefinition();
    //            return found != null;
    //        } catch (Exception e) {
    //            return false;
    //        }
    //    }
}
