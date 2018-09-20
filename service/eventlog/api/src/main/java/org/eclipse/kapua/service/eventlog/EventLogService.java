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
package org.eclipse.kapua.service.eventlog;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.KapuaEntityService;
import org.eclipse.kapua.service.config.KapuaConfigurableService;

/**
 * EventLogService exposes APIs to manage EventLog objects.<br>
 * It includes APIs to create, find, list and delete Event Log entries.<br>
 * Instances of the EventLogService can be acquired through the ServiceLocator.
 *
 * @since 1.0
 */
public interface EventLogService extends KapuaEntityService<EventLog, EventLogCreator>,
        KapuaConfigurableService {

    /**
     * Creates a new event log entry as specified in the EventLogCreator.<br>
     *
     * @param eventLogCreator
     * @return created EventLog entry
     * @throws KapuaException
     */
    @Override
    EventLog create(EventLogCreator eventLogCreator) throws KapuaException;

    /**
     * Delete the supplied Event Log entry.
     *
     * @param scopeId
     * @param eventLogId
     * @throws KapuaException
     */
    @Override
    void delete(KapuaId scopeId, KapuaId eventLogId) throws KapuaException;

    /**
     * Returns the EventLog with the specified Id; returns null if the EventLog is not found.<br>
     * <b>The API does not perform any access control check and it is meant for internal use.</b>
     *
     * @param eventLogId
     * @return
     * @throws KapuaException
     */
    @Override
    EventLog find(KapuaId scopeId, KapuaId eventLogId) throws KapuaException;

    /**
     * Queries for EventLog entries
     *
     * @param query
     * @return list of event log entries
     * @throws KapuaException
     */
    @Override
    EventLogListResult query(KapuaQuery<EventLog> query) throws KapuaException;

    /**
     * Count EventLog entries
     *
     * @param query
     * @return number of matching event log entries
     * @throws KapuaException
     */
    @Override
    long count(KapuaQuery<EventLog> query) throws KapuaException;

    /**
     * Purge obsolete EventLog entries
     *
     * @param
     * @return
     * @throws KapuaException
     */
    void purge() throws KapuaException;
}
