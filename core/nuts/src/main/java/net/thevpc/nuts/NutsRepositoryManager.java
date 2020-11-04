package net.thevpc.nuts;

import java.nio.file.Path;

public interface NutsRepositoryManager {
    NutsRepositoryFilterManager filter();


    /**
     * add temporary repository
     *
     * @param repository temporary repository
     * @param session    session
     * @return repository
     */
    NutsRepository addRepository(NutsRepositoryModel repository, NutsSession session);

    NutsRepository addRepository(NutsRepositoryDefinition definition);

    NutsRepository addRepository(NutsAddRepositoryOptions options);

    NutsRepository addRepository(NutsRepositoryRef ref, NutsAddOptions options);

    /**
     * creates a new repository from the given {@code repositoryNamedUrl}.Accepted {@code repositoryNamedUrl} values are :
     * <ul>
     * <li>'local' : corresponds to a local updatable repository.
     * <p>
     * will be named
     * 'local'</li>
     * <li>'m2', '.m2', 'maven-local' : corresponds the local maven folder
     * repository. will be named 'local'</li>
     * <li>'maven-central': corresponds the remote maven central repository.
     * will be named 'local'</li>
     * <li>'maven-git', 'vpc-public-maven': corresponds the remote maven
     * vpc-public-maven git folder repository. will be named 'local'</li>
     * <li>'maven-git', 'vpc-public-nuts': corresponds the remote nuts
     * vpc-public-nuts git folder repository. will be named 'local'</li>
     * <li>name=uri-or-path : corresponds the given uri. will be named name.
     * Here are some examples:
     * <ul>
     * <li>myremote=http://192.168.6.3/folder</li>
     * <li>myremote=/folder/subfolder</li>
     * <li>myremote=c:/folder/subfolder</li>
     * </ul>
     * </li>
     * <li>uri-or-path : corresponds the given uri. will be named uri's last
     * path component name. Here are some examples:
     * <ul>
     * <li>http://192.168.6.3/folder : will be named 'folder'</li>
     * <li>myremote=/folder/subfolder : will be named 'folder'</li>
     * <li>myremote=c:/folder/subfolder : will be named 'folder'</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param repositoryNamedUrl repositoryNamedUrl
     * @param session            session
     * @return created repository
     */
    NutsRepository addRepository(String repositoryNamedUrl, NutsSession session);

    NutsRepository findRepositoryById(String repositoryIdOrName, NutsSession session);

    NutsRepository findRepositoryByName(String repositoryIdOrName, NutsSession session);

    /**
     * @param repositoryIdOrName repository id or name
     * @param session            session
     * @return null if not found
     */
    NutsRepository findRepository(String repositoryIdOrName, NutsSession session);

    NutsRepository getRepository(String repositoryIdOrName, NutsSession session) throws NutsRepositoryNotFoundException;

    NutsRepositoryManager removeRepository(String locationOrRepositoryId, NutsRemoveOptions options);

    NutsRepository[] getRepositories(NutsSession session);

///////////////

    NutsRepositoryRef[] getRepositoryRefs(NutsSession session);

    void removeAllRepositories(NutsRemoveOptions options);

    NutsRepository createRepository(NutsAddRepositoryOptions options, Path rootFolder, NutsRepository parentRepository);
}
