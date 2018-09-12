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

import org.eclipse.kapua.model.KapuaEntityAttributes;

public class EventLogAttributes extends KapuaEntityAttributes {

    public static final String SOURCE_NAME = "sourceName";
    public static final String ENTITY_SCOPE_ID = "entityScopeId";
    public static final String ENTITY_ID = "entityId";
    public static final String CONTEXT_ID = "contextId";
    public static final String EVENT_OPERATION = "eventOperation";
    public static final String EVENT_SENT_ON = "eventSentOn";
}
