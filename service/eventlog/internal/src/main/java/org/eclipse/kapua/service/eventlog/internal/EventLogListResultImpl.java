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
package org.eclipse.kapua.service.eventlog.internal;

import org.eclipse.kapua.commons.model.query.KapuaListResultImpl;
import org.eclipse.kapua.service.eventlog.EventLog;
import org.eclipse.kapua.service.eventlog.EventLogListResult;

/**
 * EventLog list result implementation.
 * 
 * @since 1.0
 * 
 */
public class EventLogListResultImpl extends KapuaListResultImpl<EventLog> implements EventLogListResult {

    private static final long serialVersionUID = 2231053707705207565L;
}
