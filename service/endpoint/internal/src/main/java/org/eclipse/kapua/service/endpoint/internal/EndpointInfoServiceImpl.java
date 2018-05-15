/*******************************************************************************
 * Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.endpoint.internal;

import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaEntityUniquenessException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableResourceLimitedService;
import org.eclipse.kapua.commons.model.query.predicate.AndPredicateImpl;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.commons.util.CommonsValidationRegex;
import org.eclipse.kapua.event.ListenServiceEvent;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.AndPredicate;
import org.eclipse.kapua.model.query.predicate.AttributePredicate.Operator;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.endpoint.EndpointInfo;
import org.eclipse.kapua.service.endpoint.EndpointInfoCreator;
import org.eclipse.kapua.service.endpoint.EndpointInfoFactory;
import org.eclipse.kapua.service.endpoint.EndpointInfoListResult;
import org.eclipse.kapua.service.endpoint.EndpointInfoPredicates;
import org.eclipse.kapua.service.endpoint.EndpointInfoQuery;
import org.eclipse.kapua.service.endpoint.EndpointInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link EndpointInfoService} implementation.
 *
 * @since 1.0.0
 */
@KapuaProvider
public class EndpointInfoServiceImpl
        extends AbstractKapuaConfigurableResourceLimitedService<EndpointInfo, EndpointInfoCreator, EndpointInfoService, EndpointInfoListResult, EndpointInfoQuery, EndpointInfoFactory>
        implements EndpointInfoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointInfoServiceImpl.class);

    private final KapuaLocator locator = KapuaLocator.getInstance();

    private final AccountService accountService = locator.getService(AccountService.class);

    private final AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
    private final PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);

    public EndpointInfoServiceImpl() {
        super(EndpointInfoService.class.getName(), ENDPOINT_INFO_DOMAIN, EndpointEntityManagerFactory.getInstance(), EndpointInfoService.class, EndpointInfoFactory.class);
    }

    @Override
    public EndpointInfo create(EndpointInfoCreator endpointInfoCreator)
            throws KapuaException {
        ArgumentValidator.notNull(endpointInfoCreator, "endpointInfoCreator");
        ArgumentValidator.notNull(endpointInfoCreator.getScopeId(), "endpointInfoCreator.scopeId");

        ArgumentValidator.notEmptyOrNull(endpointInfoCreator.getSchema(), "endpointInfoCreator.schema");
        ArgumentValidator.match(endpointInfoCreator.getSchema(), CommonsValidationRegex.URI_SCHEME, "endpointInfoCreator.schema");
        ArgumentValidator.lengthRange(endpointInfoCreator.getSchema(), 1L, 64L, "endpointInfoCreator.schema");

        ArgumentValidator.notEmptyOrNull(endpointInfoCreator.getDns(), "endpointInfoCreator.dns");
        ArgumentValidator.match(endpointInfoCreator.getDns(), CommonsValidationRegex.URI_DNS, "endpointInfoCreator.dns");
        ArgumentValidator.lengthRange(endpointInfoCreator.getDns(), 3L, 1024L, "endpointInfoCreator.dns");

        ArgumentValidator.notNegative(endpointInfoCreator.getPort(), "endpointInfoCreator.port");
        ArgumentValidator.numRange(endpointInfoCreator.getPort(), 1, 65535, "endpointInfoCreator.port");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(ENDPOINT_INFO_DOMAIN, Actions.write, null));

        //
        // Check duplicate endpoint
        checkDuplicateEndpointInfo(
                endpointInfoCreator.getScopeId(),
                null,
                endpointInfoCreator.getSchema(),
                endpointInfoCreator.getDns(),
                endpointInfoCreator.getPort());

        //
        // Do create
        return entityManagerSession.onTransactedInsert(em -> EndpointInfoDAO.create(em, endpointInfoCreator));
    }

    @Override
    public EndpointInfo update(EndpointInfo endpointInfo) throws KapuaException {
        ArgumentValidator.notNull(endpointInfo, "endpointInfo");
        ArgumentValidator.notNull(endpointInfo.getScopeId(), "endpointInfo.scopeId");

        ArgumentValidator.notEmptyOrNull(endpointInfo.getSchema(), "endpointInfo.schema");
        ArgumentValidator.match(endpointInfo.getSchema(), CommonsValidationRegex.URI_SCHEME, "endpointInfo.schema");
        ArgumentValidator.lengthRange(endpointInfo.getSchema(), 1L, 64L, "endpointInfo.schema");

        ArgumentValidator.notEmptyOrNull(endpointInfo.getDns(), "endpointInfo.dns");
        ArgumentValidator.match(endpointInfo.getDns(), CommonsValidationRegex.URI_DNS, "endpointInfo.dns");
        ArgumentValidator.lengthRange(endpointInfo.getDns(), 3L, 1024L, "endpointInfo.dns");

        ArgumentValidator.notNegative(endpointInfo.getPort(), "endpointInfo.port");
        ArgumentValidator.numRange(endpointInfo.getPort(), 1, 65535, "endpointInfo.port");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(ENDPOINT_INFO_DOMAIN, Actions.write, null));

        //
        // Check duplicate endpoint
        //
        // Check duplicate endpoint
        checkDuplicateEndpointInfo(
                endpointInfo.getScopeId(),
                endpointInfo.getId(),
                endpointInfo.getSchema(),
                endpointInfo.getDns(),
                endpointInfo.getPort());
        //
        // Do update
        return entityManagerSession.onTransactedInsert(em -> EndpointInfoDAO.update(em, endpointInfo));
    }

    @Override
    public void delete(KapuaId scopeId, KapuaId endpointInfoId) throws KapuaException {
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(endpointInfoId, "endpointInfoId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(ENDPOINT_INFO_DOMAIN, Actions.delete, null));

        //
        // Do delete
        entityManagerSession.onTransactedAction(em -> EndpointInfoDAO.delete(em, endpointInfoId));
    }

    @Override
    public EndpointInfo find(KapuaId scopeId, KapuaId endpointInfoId)
            throws KapuaException {
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(endpointInfoId, "endpointInfoId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(ENDPOINT_INFO_DOMAIN, Actions.read, scopeId));

        //
        // Do find
        return entityManagerSession.onResult(em -> EndpointInfoDAO.find(em, endpointInfoId));
    }

    @Override
    public EndpointInfoListResult query(KapuaQuery<EndpointInfo> query)
            throws KapuaException {
        ArgumentValidator.notNull(query, "query");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(ENDPOINT_INFO_DOMAIN, Actions.read, query.getScopeId()));

        //
        // Do Query
        return entityManagerSession.onResult(em -> {
            EndpointInfoListResult endpointInfoListResult = EndpointInfoDAO.query(em, query);

            if (endpointInfoListResult.isEmpty() && query.getScopeId() != null) {

                KapuaId originalScopeId = query.getScopeId();

                do {
                    Account account = KapuaSecurityUtils.doPrivileged(() -> accountService.find(query.getScopeId()));

                    if (account == null) {
                        throw new KapuaEntityNotFoundException(Account.TYPE, query.getScopeId());
                    }

                    query.setScopeId(account.getScopeId());
                    endpointInfoListResult = EndpointInfoDAO.query(em, query);
                }
                while (query.getScopeId() != null && endpointInfoListResult.isEmpty());

                query.setScopeId(originalScopeId);
            }

            return endpointInfoListResult;
        });
    }

    @Override
    public long count(KapuaQuery<EndpointInfo> query)
            throws KapuaException {
        ArgumentValidator.notNull(query, "query");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(ENDPOINT_INFO_DOMAIN, Actions.read, query.getScopeId()));

        //
        // Do count
        return entityManagerSession.onResult(em -> {
            long endpointInfoCount = EndpointInfoDAO.count(em, query);

            if (endpointInfoCount == 0 && query.getScopeId() != null) {

                KapuaId originalScopeId = query.getScopeId();

                do {
                    Account account = KapuaSecurityUtils.doPrivileged(() -> accountService.find(query.getScopeId()));

                    if (account == null) {
                        throw new KapuaEntityNotFoundException(Account.TYPE, query.getScopeId());
                    }

                    query.setScopeId(account.getScopeId());
                    endpointInfoCount = EndpointInfoDAO.count(em, query);
                }
                while (query.getScopeId() != null && endpointInfoCount == 0);

                query.setScopeId(originalScopeId);
            }

            return endpointInfoCount;
        });
    }

    @ListenServiceEvent(fromAddress = "account")
    public void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException {
        if (kapuaEvent == null) {
            LOGGER.warn("EndpointInfoService: received null kapua event from account");
        }
        LOGGER.info("EndpointInfoService: received kapua event from {}, operation {}", kapuaEvent.getService(), kapuaEvent.getOperation());
        if ("org.eclipse.kapua.service.account.AccountService".equals(kapuaEvent.getService()) && "delete".equals(kapuaEvent.getOperation())) {
            deleteEndpointsByAccountId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        }
    }

    //
    // Private methods
    //

    /**
     * Checks whether or not another {@link EndpointInfo} already exists with the given values.
     *
     * @param scopeId  The ScopeId of the {@link EndpointInfo}
     * @param entityId The entity id, if exists. On update you need to exclude the same entity.
     * @param schema   The {@link EndpointInfo#getSchema()}  value.
     * @param dns      The {@link EndpointInfo#getDns()}  value.
     * @param port     The {@link EndpointInfo#getPort()} value.
     * @throws KapuaException if the values provided matches another {@link EndpointInfo}
     * @since 1.0.0
     */
    private void checkDuplicateEndpointInfo(KapuaId scopeId, KapuaId entityId, String schema, String dns, int port) throws KapuaException {

        AndPredicate andPredicate = new AndPredicateImpl(
                new AttributePredicateImpl<>(EndpointInfoPredicates.SCHEMA, schema),
                new AttributePredicateImpl<>(EndpointInfoPredicates.DNS, dns),
                new AttributePredicateImpl<>(EndpointInfoPredicates.PORT, port)
        );

        if (entityId != null) {
            andPredicate.and(new AttributePredicateImpl<>(EndpointInfoPredicates.ENTITY_ID, entityId, Operator.NOT_EQUAL));
        }

        EndpointInfoQuery query = new EndpointInfoQueryImpl(scopeId);
        query.setPredicate(andPredicate);

        if (count(query) > 0) {
            List<Map.Entry<String, Object>> uniquesFieldValues = new ArrayList<>();
            uniquesFieldValues.add(new AbstractMap.SimpleEntry<>(EndpointInfoPredicates.SCOPE_ID, scopeId));
            uniquesFieldValues.add(new AbstractMap.SimpleEntry<>(EndpointInfoPredicates.SCHEMA, schema));
            uniquesFieldValues.add(new AbstractMap.SimpleEntry<>(EndpointInfoPredicates.DNS, dns));
            uniquesFieldValues.add(new AbstractMap.SimpleEntry<>(EndpointInfoPredicates.PORT, port));

            throw new KapuaEntityUniquenessException(EndpointInfo.TYPE, uniquesFieldValues);
        }
    }

    private void deleteEndpointsByAccountId(KapuaId scope, KapuaId id) throws KapuaException {

        EndpointInfoQuery query = new EndpointInfoQueryImpl(id);
        EndpointInfoListResult toDelete = query(query);

        for(EndpointInfo epi : toDelete.getItems()) {
            delete(epi.getScopeId(), epi.getId());
        }
    }
}
