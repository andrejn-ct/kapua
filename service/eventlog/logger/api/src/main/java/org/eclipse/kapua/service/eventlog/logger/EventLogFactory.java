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
package org.eclipse.kapua.service.eventlog.logger;

import org.eclipse.kapua.model.KapuaEntityFactory;
import org.eclipse.kapua.model.id.KapuaId;

/**
 * EventLog factory service definition.
 *
 * @since 1.0
 */
public interface EventLogFactory extends KapuaEntityFactory<EventLog, EventLogCreator, EventLogQuery, EventLogListResult> {

    /**
     * Creates a new {@link EventLogCreator} for the specified name
     *
     * @param scopedId
     * @return
     */
    EventLogCreator newCreator(KapuaId scopedId);

}
