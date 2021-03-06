/*******************************************************************************
 * Copyright (c) 2016, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.test.user;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.locator.guice.TestService;
import org.eclipse.kapua.model.config.metatype.KapuaTocd;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserCreator;
import org.eclipse.kapua.service.user.UserListResult;
import org.eclipse.kapua.service.user.UserService;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@TestService
@KapuaProvider
public class UserServiceMock implements UserService {

    private final UserMock kapuaSysUser;
    private final Map<KapuaId, UserMock> users;

    public UserServiceMock() {
        kapuaSysUser = new UserMock(new KapuaEid(BigInteger.valueOf(1)), "kapua-sys");
        users = new HashMap<>();
        users.put(kapuaSysUser.getId(), kapuaSysUser);
    }

    @Override
    public long count(KapuaQuery<User> query) throws KapuaException {
        throw KapuaException.internalError("Not implemented");
    }

    @Override
    public User create(UserCreator userCreator) throws KapuaException {
        UserMock user = new UserMock(userCreator.getScopeId(), userCreator.getName());

        user.setDisplayName(userCreator.getDisplayName());
        user.setEmail(userCreator.getEmail());
        user.setPhoneNumber(userCreator.getPhoneNumber());

        users.put(user.getId(), user);

        return user;
    }

    @Override
    public User update(User user) throws KapuaException {
        if (!users.containsKey(user.getId())) {
            throw KapuaException.internalError("User not found");
        }

        UserMock userMock = users.get(user.getId());
        user.setDisplayName(user.getDisplayName());
        user.setEmail(user.getEmail());
        user.setPhoneNumber(user.getPhoneNumber());

        return userMock;
    }

    @Override
    public void delete(KapuaId scopeId, KapuaId userId) throws KapuaException {
        if (!users.containsKey(userId)) {
            throw KapuaException.internalError("User not found");
        }

        users.remove(userId);
    }

    @Override
    public void delete(User user) throws KapuaException {
        if (!users.containsKey(user.getId())) {
            throw KapuaException.internalError("User not found");
        }

        users.remove(user.getId());
    }

    @Override
    public User find(KapuaId accountId, KapuaId userId) throws KapuaException {
        if (!users.containsKey(userId)) {
            throw KapuaException.internalError("User not found");
        }

        return users.get(userId);
    }

    @Override
    public User findByName(String name) throws KapuaException {
        for (UserMock userMock : users.values()) {
            if (userMock.getName() != null && userMock.getName().equals(name)) {
                return userMock;
            }
        }

        throw KapuaException.internalError("User not found");
    }

    @Override
    public User findByExternalId(String externalId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserListResult query(KapuaQuery<User> query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KapuaTocd getConfigMetadata(KapuaId scopeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getConfigValues(KapuaId scopeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setConfigValues(KapuaId scopeId, KapuaId parentId, Map<String, Object> values) {
        throw new UnsupportedOperationException();
    }
}
