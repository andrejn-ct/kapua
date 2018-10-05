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
package org.eclipse.kapua.eventlog.housekeeper;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.service.KapuaService;

/**
 * EventLogManager exposes APIs to manage EventLog cleanup schedules.<br>
 * It includes APIs to purge Event Log entries.and to schedule and unschedule the purging task <br>
 *
 * @since 1.0
 */
public interface EventLogHouseKeeper extends KapuaService {

    /**
     * Schedules the Event Log purge action as a Kapua Job.<br>
     *
     * @param
     * @return
     * @throws KapuaException
     */
    void schedulePurge() throws KapuaException;

    /**
     * Unschedules the Event Log purge.<br>
     *
     * @param
     * @return
     * @throws KapuaException
     */
    void unschedulePurge() throws KapuaException;

    /**
     * Purge the obsolete Event Log entries.<br>
     *
     * @param
     * @return
     * @throws KapuaException
     */
    void purgeLogs() throws KapuaException;

}
