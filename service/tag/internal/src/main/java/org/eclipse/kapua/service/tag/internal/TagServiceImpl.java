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
package org.eclipse.kapua.service.tag.internal;

import org.eclipse.kapua.KapuaDuplicateNameException;
import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableResourceLimitedService;
import org.eclipse.kapua.commons.model.query.predicate.AndPredicateImpl;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.event.ListenServiceEvent;
import org.eclipse.kapua.event.RaiseServiceEvent;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.AttributePredicate.Operator;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.tag.Tag;
import org.eclipse.kapua.service.tag.TagCreator;
import org.eclipse.kapua.service.tag.TagFactory;
import org.eclipse.kapua.service.tag.TagListResult;
import org.eclipse.kapua.service.tag.TagPredicates;
import org.eclipse.kapua.service.tag.TagQuery;
import org.eclipse.kapua.service.tag.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TagService} implementation.
 *
 * @since 1.0.0
 */
@KapuaProvider
public class TagServiceImpl extends AbstractKapuaConfigurableResourceLimitedService<Tag, TagCreator, TagService, TagListResult, TagQuery, TagFactory> implements TagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagServiceImpl.class);

    private final KapuaLocator locator = KapuaLocator.getInstance();

    private final AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
    private final PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);


    public TagServiceImpl() {
        super(TagService.class.getName(), TAG_DOMAIN, TagEntityManagerFactory.getInstance(), TagService.class, TagFactory.class);
    }

    @Override
    public Tag create(TagCreator tagCreator) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(tagCreator, "tagCreator");
        ArgumentValidator.notNull(tagCreator.getScopeId(), "tagCreator.scopeId");
        ArgumentValidator.notEmptyOrNull(tagCreator.getName(), "tagCreator.name");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(TAG_DOMAIN,Actions.write, tagCreator.getScopeId()));

        //
        // Check limit
        if (allowedChildEntities(tagCreator.getScopeId()) <= 0) {
            throw new KapuaIllegalArgumentException("scopeId", "max tags reached");
        }

        //
        // Check duplicate name
        TagQuery query = new TagQueryImpl(tagCreator.getScopeId());
        query.setPredicate(new AttributePredicateImpl<>(TagPredicates.NAME, tagCreator.getName()));

        if (count(query) > 0) {
            throw new KapuaDuplicateNameException(tagCreator.getName());
        }

        //
        // Do create
        return entityManagerSession.onTransactedInsert(em -> TagDAO.create(em, tagCreator));
    }

    @Override
    public Tag update(Tag tag) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(tag, "tag");
        ArgumentValidator.notNull(tag.getScopeId(), "tag.scopeId");
        ArgumentValidator.notNull(tag.getId(), "tag.id");
        ArgumentValidator.notEmptyOrNull(tag.getName(), "tag.name");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(TAG_DOMAIN,Actions.write, tag.getScopeId()));

        //
        // Check existence
        if (find(tag.getScopeId(), tag.getId()) == null) {
            throw new KapuaEntityNotFoundException(Tag.TYPE, tag.getId());
        }

        //
        // Check duplicate name
        TagQuery query = new TagQueryImpl(tag.getScopeId());
        query.setPredicate(
                new AndPredicateImpl(
                        new AttributePredicateImpl<>(TagPredicates.NAME, tag.getName()),
                        new AttributePredicateImpl<>(TagPredicates.ENTITY_ID, tag.getId(), Operator.NOT_EQUAL)
                ));

        if (count(query) > 0) {
            throw new KapuaDuplicateNameException(tag.getName());
        }

        //
        // Do Update
        return entityManagerSession.onTransactedInsert(em -> TagDAO.update(em, tag));
    }

    @Override
    @RaiseServiceEvent
    public void delete(KapuaId scopeId, KapuaId tagId) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(tagId, "tagId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(TAG_DOMAIN,Actions.delete, scopeId));

        //
        // Check existence
        if (find(scopeId, tagId) == null) {
            throw new KapuaEntityNotFoundException(Tag.TYPE, tagId);
        }

        //
        //
        entityManagerSession.onTransactedAction(em -> TagDAO.delete(em, tagId));
    }

    @Override
    public Tag find(KapuaId scopeId, KapuaId tagId) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(tagId, "tagId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(TAG_DOMAIN,Actions.read, scopeId));

        //
        // Do find
        return entityManagerSession.onResult(em -> TagDAO.find(em, tagId));
    }

    @Override
    public TagListResult query(KapuaQuery<Tag> query) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(TAG_DOMAIN,Actions.read, query.getScopeId()));

        //
        // Do query
        return entityManagerSession.onResult(em -> TagDAO.query(em, query));
    }

    @Override
    public long count(KapuaQuery<Tag> query) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(TAG_DOMAIN,Actions.read, query.getScopeId()));

        //
        // Do count
        return entityManagerSession.onResult(em -> TagDAO.count(em, query));
    }

    @ListenServiceEvent(fromAddress = "account")
    public void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException {
        if (kapuaEvent == null) {
            LOGGER.warn("TagService: Service bus error. Received null ServiceEvent");
        }
        LOGGER.info("TagService: received kapua event from {}, operation {}", kapuaEvent.getService(), kapuaEvent.getOperation());
        if ("org.eclipse.kapua.service.account.AccountService".equals(kapuaEvent.getService()) && "delete".equals(kapuaEvent.getOperation())) {
            deleteTagByAccountId(kapuaEvent.getScopeId(), kapuaEvent.getEntityId());
        }
    }

    // -----------------------------------------------------------------------------------------
    //
    // Private Methods
    //
    // -----------------------------------------------------------------------------------------

    private void deleteTagByAccountId(KapuaId scopeId, KapuaId accountId) throws KapuaException {

        TagFactory tagFactory = KapuaLocator.getInstance().getFactory(TagFactory.class);
        TagQuery query = tagFactory.newQuery(accountId);

        KapuaSecurityUtils.doPrivileged(()-> {
            TagListResult tagsToDelete = query(query);
            for (Tag tag : tagsToDelete.getItems()) {
                delete(tag.getScopeId(), tag.getId());
            }
        });
    }
}
