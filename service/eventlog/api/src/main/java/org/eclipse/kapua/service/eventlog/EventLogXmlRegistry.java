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

import org.eclipse.kapua.locator.KapuaLocator;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * {@link EventLog} xml factory class
 *
 * @since 1.0
 */
@XmlRegistry
public class EventLogXmlRegistry {

    private static final KapuaLocator LOCATOR = KapuaLocator.getInstance();
    private static final EventLogFactory EVENT_LOG_FACTORY = LOCATOR.getFactory(EventLogFactory.class);

    /**
     * Creates a new event log instance
     *
     * @return
     */
    public EventLog newEventLog() {
        return EVENT_LOG_FACTORY.newEntity(null);
    }

    /**
     * Creates a new event log creator instance
     *
     * @return
     */
    public EventLogCreator newEventLogCreator() {
        return EVENT_LOG_FACTORY.newCreator(null);
    }

    /**
     * Creates new event log list result
     *
     * @return
     */
    public EventLogListResult newEventLogListResult() {
        return EVENT_LOG_FACTORY.newListResult();
    }

    /**
     * Creates new event log query
     *
     * @return
     */
    public EventLogQuery newQuery() {
        return EVENT_LOG_FACTORY.newQuery(null);
    }
}
