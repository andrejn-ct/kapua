/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.eventlog.logger.internal;

import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.AbstractKapuaConfigurableService;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.setting.system.SystemSetting;
import org.eclipse.kapua.commons.setting.system.SystemSettingKey;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.event.ListenServiceEvent;
import org.eclipse.kapua.event.ServiceEvent;
import org.eclipse.kapua.event.ServiceEventBusException;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.eventlog.logger.EventLog;
import org.eclipse.kapua.service.eventlog.logger.EventLogCreator;
import org.eclipse.kapua.service.eventlog.logger.EventLogDomains;
import org.eclipse.kapua.service.eventlog.logger.EventLogListResult;
import org.eclipse.kapua.service.eventlog.logger.EventLogService;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * {@link EventLogService} implementation.
 */
@KapuaProvider
public class EventLogServiceImpl extends AbstractKapuaConfigurableService implements EventLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogServiceImpl.class);

    private final KapuaLocator locator = KapuaLocator.getInstance();

    private final AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
    private final PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
    private final UserService userService = locator.getService(UserService.class);

    /**
     * Constructor
     */
    public EventLogServiceImpl() {
        super(EventLogService.class.getName(), EventLogDomains.EVENT_LOG_DOMAIN, EventLogEntityManagerFactory.getInstance());
    }

    @Override
    public EventLog create(EventLogCreator eventLogCreator) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(eventLogCreator.getScopeId(), "eventLogCreator.scopeId");
        ArgumentValidator.notEmptyOrNull(eventLogCreator.getSourceName(), "eventLogCreator.sourceName");
        ArgumentValidator.notEmptyOrNull(eventLogCreator.getContextId(), "eventLogCreator.contextId");
        ArgumentValidator.notEmptyOrNull(eventLogCreator.getEventOperation(), "eventLogCreator.eventOperation");
        ArgumentValidator.notNull(eventLogCreator.getEntityScopeId(), "eventLogCreator.entityScopeId");
        ArgumentValidator.notNull(eventLogCreator.getEntityId(), "eventLogCreator.entityId");
        ArgumentValidator.notNull(eventLogCreator.getEventSentOn(), "eventLogCreator.eventSentOn");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(EventLogDomains.EVENT_LOG_DOMAIN, Actions.write, eventLogCreator.getScopeId()));

        //
        // Do create
        return KapuaSecurityUtils.doPrivileged(() ->
                entityManagerSession.onTransactedInsert(em -> EventLogDAO.create(em, eventLogCreator)));
    }

    @Override
    public void delete(KapuaId scopeId, KapuaId eventLogId) throws KapuaException {
        //
        // Argument validation
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(eventLogId, "eventLogId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(EventLogDomains.EVENT_LOG_DOMAIN, Actions.delete, scopeId));

        //
        // Check existence
        EventLog eventLog = find(scopeId, eventLogId);
        if (eventLog == null) {
            throw new KapuaEntityNotFoundException(EventLog.TYPE, eventLogId);
        }

        //
        // Do  delete
        KapuaSecurityUtils.doPrivileged(() ->
                entityManagerSession.onTransactedAction(em -> EventLogDAO.delete(em, scopeId, eventLogId)));
    }

    @Override
    public EventLog find(KapuaId scopeId, KapuaId eventLogId) throws KapuaException {
        // Validation of the fields
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(eventLogId, "eventLogId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(EventLogDomains.EVENT_LOG_DOMAIN, Actions.read, scopeId));

        //
        // Do the find
        return entityManagerSession.onResult(em -> EventLogDAO.find(em, scopeId, eventLogId));
    }

    @Override
    public EventLogListResult query(KapuaQuery<EventLog> query) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(EventLogDomains.EVENT_LOG_DOMAIN, Actions.read, query.getScopeId()));

        //
        // Do query
        return entityManagerSession.onResult(em -> EventLogDAO.query(em, query));
    }

    @Override
    public long count(KapuaQuery<EventLog> query) throws KapuaException {
        //
        // Argument Validator
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        authorizationService.checkPermission(permissionFactory.newPermission(EventLogDomains.EVENT_LOG_DOMAIN, Actions.read, query.getScopeId()));

        //
        // Do count
        return entityManagerSession.onResult(em -> EventLogDAO.count(em, query));
    }

    @ListenServiceEvent(fromAddress = "account")
    @ListenServiceEvent(fromAddress = "user")
    @ListenServiceEvent(fromAddress = "tag")
    @ListenServiceEvent(fromAddress = "job")
    @ListenServiceEvent(fromAddress = "group")
    public void onKapuaEvent(ServiceEvent kapuaEvent) throws KapuaException {
        if (kapuaEvent == null) {
            LOGGER.warn("EventLogService: Service bus error. Received null ServiceEvent");
            throw new ServiceEventBusException("Service bus error. Received null ServiceEvent.");
        }
        LOGGER.info("EventLogService: received kapua event from {}, operation {}", kapuaEvent.getService(), kapuaEvent.getOperation());
        logReceivedEvent(kapuaEvent);
    }

    // -----------------------------------------------------------------------------------------
    //
    // Private Methods
    //
    // -----------------------------------------------------------------------------------------

    private User getSystemAdministrator() throws KapuaException {

        return KapuaSecurityUtils.doPrivileged(() -> {
            //
            // Retrieve the administrator (for the account id)
            String adminUsername = SystemSetting.getInstance().getString(SystemSettingKey.SYS_ADMIN_USERNAME);
            User adminUser = userService.findByName(adminUsername);
            return adminUser;
        });
    }

    private void logReceivedEvent(ServiceEvent kapuaEvent) throws KapuaException {

        KapuaSecurityUtils.doPrivileged(() -> {
                //
                // Retrieve the administrator (for the account id)
                User adminUser = getSystemAdministrator();

                //
                // Check whether event logging is active
                Map<String, Object> eventLoggerConfig = getConfigValues(adminUser.getScopeId());
                boolean logEnabled = (boolean) eventLoggerConfig.get("eventLoggingEnabled");
                // If logging is disabled just return without doing anything
                if (!logEnabled) {
                    return;
                }

                //
                // Prepare the event creator
                EventLogCreator eventCreator = new EventLogCreatorImpl(adminUser.getScopeId());
                eventCreator.setSourceName(kapuaEvent.getService());
                eventCreator.setEventOperation(kapuaEvent.getOperation());
                eventCreator.setContextId(kapuaEvent.getContextId());
                eventCreator.setEntityScopeId(kapuaEvent.getScopeId());
                eventCreator.setEntityId(kapuaEvent.getEntityId());
                eventCreator.setEventSentOn(kapuaEvent.getTimestamp());

                //
                // Do insert the event log entry
                create(eventCreator);
        });
    }
}
