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
import org.eclipse.kapua.commons.jpa.EntityManager;
import org.eclipse.kapua.commons.service.internal.ServiceDAO;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.eventlog.logger.EventLog;
import org.eclipse.kapua.service.eventlog.logger.EventLogCreator;
import org.eclipse.kapua.service.eventlog.logger.EventLogListResult;

/**
 * User DAO
 */
public class EventLogDAO extends ServiceDAO {

    /**
     * Creates and return new Event Log entry
     *
     * @param em
     * @param eventLogCreator
     * @return
     * @throws KapuaException
     */
    public static EventLog create(EntityManager em, EventLogCreator eventLogCreator)
            throws KapuaException {
        //
        // Create User
        EventLogImpl eventLogImpl = new EventLogImpl(eventLogCreator.getScopeId());

        eventLogImpl.setSourceName(eventLogCreator.getSourceName());
        eventLogImpl.setEntityScopeId(eventLogCreator.getEntityScopeId());
        eventLogImpl.setEntityId(eventLogCreator.getEntityId());
        eventLogImpl.setContextId(eventLogCreator.getContextId());
        eventLogImpl.setEventOperation(eventLogCreator.getEventOperation());
        eventLogImpl.setEventSentOn(eventLogCreator.getEventSentOn());

        return ServiceDAO.create(em, eventLogImpl);
    }

    /**
     * Deletes the event log item by event log identifier
     *
     * @param em
     * @param eventLogId
     * @throws KapuaEntityNotFoundException
     *             If {@link EventLog} is not found.
     */
    public static void delete(EntityManager em, KapuaId scopeId, KapuaId eventLogId)
            throws KapuaEntityNotFoundException {
        ServiceDAO.delete(em, EventLogImpl.class, scopeId, eventLogId);
    }

    /**
     * Finds the event log item by event log identifier
     *
     * @param em
     * @param eventLogId
     * @return
     */
    public static EventLog find(EntityManager em, KapuaId eventLogId) {
        return em.find(EventLogImpl.class, eventLogId);
    }

    /**
     * Returns the event log list matching the provided query
     *
     * @param em
     * @param eventLogQuery
     * @return
     * @throws KapuaException
     */
    public static EventLogListResult query(EntityManager em, KapuaQuery<EventLog> eventLogQuery)
            throws KapuaException {
        return ServiceDAO.query(em, EventLog.class, EventLogImpl.class, new EventLogListResultImpl(), eventLogQuery);
    }

    /**
     * Returns the event log count matching the provided query
     *
     * @param em
     * @param eventLogQuery
     * @return
     * @throws KapuaException
     */
    public static long count(EntityManager em, KapuaQuery<EventLog> eventLogQuery)
            throws KapuaException {
        return ServiceDAO.count(em, EventLog.class, EventLogImpl.class, eventLogQuery);
    }

}
