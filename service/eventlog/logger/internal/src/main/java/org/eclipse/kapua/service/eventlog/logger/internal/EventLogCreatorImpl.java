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

import org.eclipse.kapua.commons.model.AbstractKapuaEntityCreator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.eventlog.logger.EventLog;
import org.eclipse.kapua.service.eventlog.logger.EventLogCreator;

import java.util.Date;

/**
 * EventLog creator service implementation.
 *
 * @since 1.0
 */
public class EventLogCreatorImpl extends AbstractKapuaEntityCreator<EventLog> implements EventLogCreator {

    private static final long serialVersionUID = 4664940282892151010L;

    private String sourceName;
    private KapuaId entityScopeId;
    private KapuaId entityId;
    private String contextId;
    private String eventOperation;
    private Date eventSentOn;

    /**
     * Constructor
     *
     * @param scopeId
     */

    public EventLogCreatorImpl(KapuaId scopeId) {
        super(scopeId);
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public KapuaId getEntityScopeId() {
        return entityScopeId;
    }

    @Override
    public void setEntityScopeId(KapuaId entityScopeId) {
        this.entityScopeId = entityScopeId;
    }

    @Override
    public KapuaId getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(KapuaId entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    @Override
    public String getEventOperation() {
        return eventOperation;
    }

    @Override
    public void setEventOperation(String eventOperation) {
        this.eventOperation = eventOperation;
    }

    @Override
    public Date getEventSentOn() {
        return eventSentOn;
    }

    @Override
    public void setEventSentOn(Date eventSentOn) {
        this.eventSentOn = eventSentOn;
    }

}
