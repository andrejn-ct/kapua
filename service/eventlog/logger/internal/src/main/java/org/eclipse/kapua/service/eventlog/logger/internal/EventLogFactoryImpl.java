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

import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.eventlog.logger.EventLog;
import org.eclipse.kapua.service.eventlog.logger.EventLogCreator;
import org.eclipse.kapua.service.eventlog.logger.EventLogFactory;
import org.eclipse.kapua.service.eventlog.logger.EventLogListResult;
import org.eclipse.kapua.service.eventlog.logger.EventLogQuery;

/**
 * EventLog factory service implementation.
 * 
 * @since 1.0
 *
 */
@KapuaProvider
public class EventLogFactoryImpl implements EventLogFactory {

    @Override
    public EventLog newEntity(KapuaId scopeId) {
        return new EventLogImpl(scopeId);
    }

    @Override
    public EventLogCreator newCreator(KapuaId scopeId) {
        return new EventLogCreatorImpl(scopeId);
    }

    @Override
    public EventLogQuery newQuery(KapuaId scopeId) {
        return new EventLogQueryImpl(scopeId);
    }

    @Override
    public EventLogListResult newListResult() {
        return new EventLogListResultImpl();
    }

}
