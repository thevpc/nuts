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
    public NOptional<NUser> findUser(String username) {
        return securityModel().findUser(username);
    }

    @Override
    public List<NUser> findUsers() {
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
    public String[] getCurrentLoginStack() {
        return securityModel().getCurrentLoginStack();
    }

    @Override
    public String getCurrentUsername() {
        return securityModel().getCurrentUsername();
    }


    @Override
    public NAuthenticationAgent getAuthenticationAgent(String authenticationAgentId) {
        return securityModel().getAuthenticationAgent(authenticationAgentId);
    }

    @Override
    public NSecurityManager setAuthenticationAgent(String authenticationAgentId) {
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
    public NOptional<NNamedCredential> findNamedCredential(String name, String user) {
        return configModel().findNamedCredential(name, user);
    }

    @Override
    public NOptional<NNamedCredential> findNamedCredential(String name) {
        return findNamedCredential(name, null);
    }

    @Override
    public List<NNamedCredential> findNamedCredentials() {
        return findNamedCredentials(null);
    }

    @Override
    public List<NNamedCredential> findNamedCredentials(String user) {
        return configModel().findNamedCredentials(user);
    }

    @Override
    public NSecurityManager addRepositoryPermissions(String user, String repository, String... permissions) {
        return withRepositoryUser(user, repository, u -> {
            Set<String> all = new HashSet<>();
            List<String> gg = u.getPermissions();
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
            u.setPermissions(new ArrayList<>(all));
        });
    }

    @Override
    public NSecurityManager removeRepositoryPermissions(String user, String repository, String... permissions) {
        return withRepositoryUser(user, repository, u -> {
            Set<String> all = new HashSet<>();
            List<String> gg = u.getPermissions();
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
            u.setPermissions(new ArrayList<>(all));
        });
    }

    @Override
    public List<NRepositoryAccess> findRepositoryAccess() {
        NWorkspaceExt wse = NWorkspaceExt.of();
        List<NRepositoryAccess> all = new ArrayList<>();
        for (NRepository repository : wse.getRepositoryModel().getRepositories()) {
            all.addAll(findRepositoryAccessByRepository(repository.getUuid()));
        }
        return all;
    }

    @Override
    public List<NRepositoryAccess> findRepositoryAccessByRepository(String repository) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NRepository repository1 = wse.getRepositoryModel().findRepository(repository).get();
        return findUsers().stream().flatMap(x -> findRepositoryAccess(x.getUsername(), repository1.getName()).stream().stream()).collect(Collectors.toList());
    }

    @Override
    public List<NRepositoryAccess> findRepositoryAccessByUser(String user) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NUser user1 = securityModel().findUser(user).get();
        return Arrays.asList(wse.getRepositoryModel().getRepositories()).stream()
                .flatMap(x -> findRepositoryAccess(user1.getUsername(), x.getUuid()).stream().stream()).collect(Collectors.toList());
    }

    @Override
    public NSecurityManager updateRepositoryAccess(NRepositoryAccessSpec repositoryAccess) {
        withRepositoryUser(repositoryAccess.getUserName(), repositoryAccess.getRepository(), c -> {
            if (repositoryAccess.getPermissions() != null) {
                c.setPermissions(repositoryAccess.getPermissions());
            }
            if (repositoryAccess.getRemoteUserName() != null) {
                c.setRemoteUserName(NStringUtils.trimToNull(repositoryAccess.getRemoteUserName()));
            }
            if (repositoryAccess.getRemoteAuthType() != null) {
                c.setRemoteAuthType(NStringUtils.trimToNull(repositoryAccess.getRemoteAuthType()));
            }
            c.setRemoteCredential(repositoryAccess.getRemoteCredential() == null ? null : repositoryAccess.getRemoteCredential().toString());
        });
        return this;
    }

    private NSecurityManager withUser(String user, Consumer<NUserConfig> consumer) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NUser user1 = securityModel().findUser(user).get();
        NUserConfig r = wse.getConfigModel().getUser(user1.getUsername());
        if (r == null) {
            NUserConfig ru = new NUserConfig();
            ru.setUserName(user1.getUsername());
            consumer.accept(ru);
            wse.getConfigModel().addOrUpdateUser(ru);
        } else {
            consumer.accept(r);
            wse.getConfigModel().addOrUpdateUser(r);
        }
        return this;
    }

    public NOptional<NRepositoryAccess> findRepositoryAccess(String user, String repository) {
        NWorkspaceExt wse = NWorkspaceExt.of();
        NOptional<NUserConfig> userConfigNOptional = wse.getConfigModel().resolveAsValidUserConfig(user);
        if (userConfigNOptional.isPresent()) {
            String finalUser = userConfigNOptional.get().getUserName();
            if (wse.getModel().securityModel.isAdminOrUser(finalUser)) {
                NOptional<NRepository> repository1 = wse.getRepositoryModel().findRepository(repository);
                if (repository1.isPresent()) {
                    NRepositoryAccessConfig r = getRepositoryUserConfig(user, repository);
                    return NOptional.of(new DefaultNRepositoryAccess(
                            r.getUserName(),
                            repository1.get().getUuid(),
                            repository1.get().getName(),
                            r.getRemoteUserName(),
                            NBlankable.isBlank(r.getRemoteCredential()) ? null : NSecureToken.parse(r.getRemoteCredential()),
                            r.getRemoteAuthType(),
                            r.getPermissions()
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
        NRepository repository1 = wse.getRepositoryModel().findRepository(repository).get();
        NOptional<NRepositoryAccessConfig> r = wse.getConfigModel().getRepositoryUser(repository1.getUuid(), user1.getUsername());
        if (!r.isPresent()) {
            NRepositoryAccessConfig ru = new NRepositoryAccessConfig();
            ru.setUserName(user1.getUsername());
            ru.setRepository(repository1.getUuid());
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
        NAssert.requireNamedNonNull(query.getUserName(), "add user query");
        if (!query.getUserName().matches("[a-zA-Z]+[a-zA-Z0-9_-]*")) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid username %s", query.getUserName()));
        }
        DefaultNWorkspaceConfigModel c = NWorkspaceExt.of().getConfigModel();
        NUserConfig u = c.getUser(query.getUserName());
        if (u != null) {
            throw new NSecurityException(NMsg.ofC("user already exists : %s", query.getUserName()));
        }
        NSecureString credential = query.getCredential();
        NSecureToken owCredential = null;
        if (credential != null) {
            owCredential = addOneWayCredential(credential);
        }
        NUserConfig uc = new NUserConfig(query.getUserName(), owCredential == null ? null : owCredential.toString(), query.getGroups(), query.getPermissions());
        c.addOrUpdateUser(uc);
        return this;
    }

    @Override
    public NSecurityManager updateUser(NUserSpec query) {
        NAssert.requireNamedNonNull(query, "query user query");
        NAssert.requireNamedNonNull(query.getUserName(), "add user query");
        if (!query.getUserName().matches("[a-zA-Z]+[a-zA-Z0-9_-]*")) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid username %s", query.getUserName()));
        }
        DefaultNWorkspaceConfigModel c = NWorkspaceExt.of().getConfigModel();
        NUserConfig u = c.getUser(query.getUserName());
        if (u == null) {
            throw new NSecurityException(NMsg.ofC("user not found : %s", query.getUserName()));
        }
        u = u.copy();
        NSecureString credential = query.getCredential();
        if (credential != null) {
            String oldOwString = u.getCredential();
            NSecureToken oldOw=NBlankable.isBlank(oldOwString)?null:NSecureToken.parse(oldOwString);
            NSecureToken owCredential;
            if(oldOw!=null) {
                owCredential = updateOneWayCredential(oldOw,credential,null);
            }else{
                owCredential = addOneWayCredential(credential);
            }
            u.setCredential(owCredential.toString());
        }
        if (query.getGroups() != null) {
            u.setGroups(query.getGroups());
        }
        if (query.getPermissions() != null) {
            u.setPermissions(query.getPermissions());
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
