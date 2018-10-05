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
package org.eclipse.kapua.service.eventlog.manager.internal;

import org.eclipse.kapua.eventlog.housekeeper.EventLogHouseKeeper;
import org.eclipse.kapua.service.eventlog.manager.EventLogManager;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (@link EventLogManager) implementation.
 */
@KapuaProvider
public class EventLogManagerImpl implements EventLogManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventLogManagerImpl.class);

    private static KapuaLocator locator = KapuaLocator.getInstance();
    private static EventLogHouseKeeper eventLogHouseKeeper = locator.getService(EventLogHouseKeeper.class);

    public EventLogManagerImpl() {}

    @Override
    public void schedulePurge() throws KapuaException {
        eventLogHouseKeeper.schedulePurge();
    }

    @Override
    public void unschedulePurge() throws KapuaException {
        eventLogHouseKeeper.unschedulePurge();
    }

    @Override
    public void purgeLogs() throws KapuaException {
        eventLogHouseKeeper.purgeLogs();
    }

}
