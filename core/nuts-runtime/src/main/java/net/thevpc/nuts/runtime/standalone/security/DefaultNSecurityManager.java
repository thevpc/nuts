/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.security;


import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.security.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNSecurityManager implements NSecurityManager {


    public DefaultNSecurityManager() {
    }

    private DefaultNWorkspaceSecurityModel securityModel() {
        return NWorkspaceExt.of().getModel().securityModel;
    }
    private DefaultNWorkspaceConfigModel configModel() {
        return NWorkspaceExt.of().getModel().configModel;
    }

    @Override
    public NSecurityManager login(final String username, final NSecureString password) {
        securityModel().login(username, password);
        return this;
    }

    @Override
    public boolean setSecureMode(boolean secure, NSecureString adminPassword) {
        return securityModel().setSecureMode(secure, adminPassword);
    }

    public boolean switchUnsecureMode(NSecureString adminPassword) {
        return securityModel().switchUnsecureMode(adminPassword);
    }

    public boolean switchSecureMode(NSecureString adminPassword) {
        return securityModel().switchSecureMode(adminPassword);
    }

    @Override
    public boolean isAdmin() {
        return securityModel().isAdmin();
    }

    @Override
    public boolean isAnonymous() {
        return securityModel().isAnonymous();
    }

    @Override
    public NSecurityManager logout() {
        securityModel().logout();
        return this;
    }

    @Override
    public NOptional<NUser> getUser(String username) {
        return securityModel().findUser(username);
    }

    @Override
    public List<NUser> users() {
        return securityModel().findUsers();
    }

    @Override
    public NSecurityManager checkAllowed(String permission, String operationName) {
        securityModel().checkAllowed(permission, operationName);
        return this;
    }

    @Override
    public NSecurityManager checkRepositoryAllowed(String repository, String permission, String operationName) {
        securityModel().checkRepositoryAllowed(repository, permission, operationName);
        return this;
    }

    @Override
    public boolean isAllowed(String permission) {
        return securityModel().isAllowed(permission);
    }

    public boolean isRepositoryAllowed(String permission, String repository) {
        return securityModel().isRepositoryAllowed(permission, repository);
    }

    @Override
    public List<String> currentLoginStack() {
        return securityModel().getCurrentLoginStack();
    }

    @Override
    public String currentUsername() {
        return securityModel().getCurrentUsername();
    }

    @Override
    public NSecurityManager authenticationAgent(String authenticationAgentId) {
        securityModel().setAuthenticationAgent(authenticationAgentId);
        return this;
    }

    @Override
    public boolean isSecureMode() {
        return securityModel().isSecure();
    }

    /// //////////

    @Override
    public void runWithSecret(NSecureToken id, NSecretRunner runner) {
        securityModel().agentMapper().runWithSecret(id, runner);
    }

    @Override
    public <T> T callWithSecret(NSecureToken id, NSecretCaller<T> caller) {
        return securityModel().agentMapper().callWithSecret(id, caller);
    }

    @Override
    public boolean verify(NSecureToken credentialsId, NSecureString candidate) {
        return securityModel().agentMapper().verify(credentialsId, candidate);
    }

    public boolean removeCredentials(NSecureToken credentialsId) {
        return securityModel().agentMapper().removeCredentials(credentialsId);
    }

    @Override
    public NSecureToken addSecret(NSecureString credentials) {
        return addSecret(credentials, null);
    }

    @Override
    public NSecureToken addSecret(NSecureString credentials, String agent) {
        return securityModel().agentMapper().storeSecret(credentials, agent);
    }

    @Override
    public NSecureToken updateSecret(NSecureToken old, NSecureString credentials, String agent) {
        return securityModel().agentMapper().updateSecret(old, credentials, agent);
    }

    @Override
    public NSecureToken addOneWayCredential(NSecureString password) {
        return addOneWayCredential(password, null);
    }

    @Override
    public NSecureToken addOneWayCredential(NSecureString password, String agent) {
        return securityModel().agentMapper().storeOneWay(password, agent);
    }

    @Override
    public NSecureToken updateOneWayCredential(NSecureToken old, NSecureString credentials, String agent) {
        return securityModel().agentMapper().updateOneWay(old, credentials, agent);
    }

    @Override
    public NNamedCredentialBuilder createNamedCredentialBuilder() {
        return new DefaultNNamedCredentialBuilder();
    }

    @Override
    public NSecurityManager addNamedCredential(NNamedCredential credential) {
        configModel().addNamedCredentials(credential);
        return this;
    }

    @Override
    public NSecurityManager removeNamedCredential(String name, String user) {
        configModel().removeNamedCredentials(name, user);
        return this;
    }

    @Override
    public NSecurityManager removeNamedCredential(String name) {
        return removeNamedCredential(name, null);
    }

    @Override
    public NOptional<NNamedCredential> getNamedCredential(String name, String user) {
        return configModel().findNamedCredential(name, user);
    }

    @Override
    public NOptional<NNamedCredential> getNamedCredential(String name) {
        return getNamedCredential(name, null);
    }

    @Override
    public List<NNamedCredential> namedCredentials() {
        return getNamedCredentials(null);
    }

    @Override
    public List<NNamedCredential> getNamedCredentials(String user) {
        return configModel().findNamedCredentials(user);
    }

    @Override
    public NSecurityManager addRepositoryPermissions(String user, String repository, String... permissions) {
        return withRepositoryUser(user, repository, u -> {
            Set<String> all = new HashSet<>();
            List<String> gg = u.permissions();
            if (gg != null) {
                for (String g : gg) {
                    if (!NBlankable.isBlank(g)) {
                        all.add(g);
                    }
                }
            }
            if (permissions != null) {
                for (String g : permissions) {
                    if (!NBlankable.isBlank(g)) {
                        all.add(g);
                    }
                }
            }
            u.permissions(new ArrayList<>(all));
        });
    }

    @Override
    public NSecurityManager removeRepositoryPermissions(String user, String repository, String... permissions) {
        return withRepositoryUser(user, repository, u -> {
            Set<String> all = new HashSet<>();
            List<String> gg = u.permissions();
            if (gg != null) {
                for (String g : gg) {
                    if (!NBlankable.isBlank(g)) {
                        all.add(g);
                    }
                }
            }
            if (permissions != null) {
                for (String g : permissions) {
                    if (!NBlankable.isBlank(g)) {
                        all.remove(g);
                    }
                }
            }
            u.permissions(new ArrayList<>(all));
        });
    }

    @Override
    public List<NRepositoryAccess> repositoryAccessList() {
        NWorkspaceExt wse = NWorkspaceExt.of();
        List<NRepositoryAccess> all = new ArrayList<>();
        for (NRepository repository : wse.getRepositoryModel().getRepositories()) {
            all.addAll(getRepositoryAccessListByRepository(repository.uuid()));
        }
        return all;
    }

    @Override
    public List<NRepositoryAccess> getRepositoryAccessListByRepository(String repository) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NRepository repository1 = wse.getRepositoryModel().getRepository(repository).get();
        return users().stream().flatMap(x -> getRepositoryAccess(x.username(), repository1.name()).stream().stream()).collect(Collectors.toList());
    }

    @Override
    public List<NRepositoryAccess> getRepositoryAccessListByUser(String user) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NUser user1 = securityModel().findUser(user).get();
        return Arrays.asList(wse.getRepositoryModel().getRepositories()).stream()
                .flatMap(x -> getRepositoryAccess(user1.username(), x.uuid()).stream().stream()).collect(Collectors.toList());
    }

    @Override
    public NSecurityManager updateRepositoryAccess(NRepositoryAccessSpec repositoryAccess) {
        withRepositoryUser(repositoryAccess.userName(), repositoryAccess.repository(), c -> {
            if (repositoryAccess.permissions() != null) {
                c.permissions(repositoryAccess.permissions());
            }
            if (repositoryAccess.remoteUserName() != null) {
                c.remoteUserName(NStringUtils.trimToNull(repositoryAccess.remoteUserName()));
            }
            if (repositoryAccess.remoteAuthType() != null) {
                c.remoteAuthType(NStringUtils.trimToNull(repositoryAccess.remoteAuthType()));
            }
            c.remoteCredential(repositoryAccess.remoteCredential() == null ? null : repositoryAccess.remoteCredential().toString());
        });
        return this;
    }

    private NSecurityManager withUser(String user, Consumer<NUserConfig> consumer) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NUser user1 = securityModel().findUser(user).get();
        NUserConfig r = wse.getConfigModel().getUser(user1.username());
        if (r == null) {
            NUserConfig ru = new NUserConfig();
            ru.userName(user1.username());
            consumer.accept(ru);
            wse.getConfigModel().addOrUpdateUser(ru);
        } else {
            consumer.accept(r);
            wse.getConfigModel().addOrUpdateUser(r);
        }
        return this;
    }

    public NOptional<NRepositoryAccess> getRepositoryAccess(String user, String repository) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NOptional<NUserConfig> userConfigNOptional = wse.getConfigModel().resolveAsValidUserConfig(user);
        if (userConfigNOptional.isPresent()) {
            String finalUser = userConfigNOptional.get().userName();
            if (wse.getModel().securityModel.isAdminOrUser(finalUser)) {
                NOptional<NRepository> repository1 = wse.getRepositoryModel().getRepository(repository);
                if (repository1.isPresent()) {
                    NRepositoryAccessConfig r = getRepositoryUserConfig(user, repository);
                    return NOptional.of(new DefaultNRepositoryAccess(
                            r.userName(),
                            repository1.get().uuid(),
                            repository1.get().name(),
                            r.remoteUserName(),
                            NBlankable.isBlank(r.remoteCredential()) ? null : NSecureToken.parse(r.remoteCredential()),
                            r.remoteAuthType(),
                            r.permissions()
                    ));
                }
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("access for user %s and repo %s"));
    }

    private NRepositoryAccessConfig getRepositoryUserConfig(String user, String repository) {
        NRef<NRepositoryAccessConfig> r = NRef.of();
        withRepositoryUser(user, repository, r::set);
        return r.get();
    }

    private NSecurityManager withRepositoryUser(String user, String repository, Consumer<NRepositoryAccessConfig> consumer) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NUser user1 = securityModel().findUser(user).get();
        NRepository repository1 = wse.getRepositoryModel().getRepository(repository).get();
        NOptional<NRepositoryAccessConfig> r = wse.getConfigModel().getRepositoryUser(repository1.uuid(), user1.username());
        if (!r.isPresent()) {
            NRepositoryAccessConfig ru = new NRepositoryAccessConfig();
            ru.userName(user1.username());
            ru.repository(repository1.uuid());
            consumer.accept(ru);
            wse.getConfigModel().addRepositoryUser(ru);
        } else {
            consumer.accept(r.get());
            wse.getConfigModel().addRepositoryUser(r.get());
        }
        return this;
    }

    @Override
    public NSecurityManager addUser(NUserSpec query) {
        NAssert.requireNamedNonNull(query, "add user query");
        NAssert.requireNamedNonNull(query.userName(), "add user query");
        if (!query.userName().matches("[a-zA-Z]+[a-zA-Z0-9_-]*")) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid username %s", query.userName()));
        }
        DefaultNWorkspaceConfigModel c = NWorkspaceExt.of().getConfigModel();
        NUserConfig u = c.getUser(query.userName());
        if (u != null) {
            throw new NSecurityException(NMsg.ofC("user already exists : %s", query.userName()));
        }
        NSecureString credential = query.credential();
        NSecureToken owCredential = null;
        if (credential != null) {
            owCredential = addOneWayCredential(credential);
        }
        NUserConfig uc = new NUserConfig(query.userName(), owCredential == null ? null : owCredential.toString(), query.groups(), query.permissions());
        c.addOrUpdateUser(uc);
        return this;
    }

    @Override
    public NSecurityManager updateUser(NUserSpec query) {
        NAssert.requireNamedNonNull(query, "query user query");
        NAssert.requireNamedNonNull(query.userName(), "add user query");
        if (!query.userName().matches("[a-zA-Z]+[a-zA-Z0-9_-]*")) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid username %s", query.userName()));
        }
        DefaultNWorkspaceConfigModel c = NWorkspaceExt.of().getConfigModel();
        NUserConfig u = c.getUser(query.userName());
        if (u == null) {
            throw new NSecurityException(NMsg.ofC("user not found : %s", query.userName()));
        }
        u = u.copy();
        NSecureString credential = query.credential();
        if (credential != null) {
            String oldOwString = u.credential();
            NSecureToken oldOw=NBlankable.isBlank(oldOwString)?null:NSecureToken.parse(oldOwString);
            NSecureToken owCredential;
            if(oldOw!=null) {
                owCredential = updateOneWayCredential(oldOw,credential,null);
            }else{
                owCredential = addOneWayCredential(credential);
            }
            u.credential(owCredential.toString());
        }
        if (query.groups() != null) {
            u.groups(query.groups());
        }
        if (query.permissions() != null) {
            u.permissions(query.permissions());
        }
        c.addOrUpdateUser(u);
        return this;
    }

    @Override
    public NUserSpec createUserUpdateQuery(String username) {
        return new DefaultNUserSpec(username);
    }

    @Override
    public NRepositoryAccessSpec createRepositoryAccessSpec(String userName, String repository) {
        return new DefaultNRepositoryAccessSpec(userName, repository);
    }

    @Override
    public NSecureString createEmptySecureString() {
        return NUndestroyableString.EMPTY;
    }

    @Override
    public NSecureString createSecureString(char[] value) {
        NAssert.requireNamedNonNull(value, "value");
        try {
            return new NDestroyableString(Arrays.copyOf(value, value.length));
        } finally {
            Arrays.fill(value, '\0');
        }
    }

    @Override
    public NSecureString createUnsecureString(String value) {
        NAssert.requireNamedNonNull(value, "value");
        return new NUndestroyableString(value.toCharArray());
    }
}
