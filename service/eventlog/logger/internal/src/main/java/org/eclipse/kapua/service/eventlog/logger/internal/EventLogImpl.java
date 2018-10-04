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

import org.eclipse.kapua.commons.model.AbstractKapuaEntity;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.eventlog.logger.EventLog;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * EventLog entity implementation.
 * 
 * @since 1.0
 *
 */
@Entity(name = "EventLog")
@Table(name = "evt_eventlog")
public class EventLogImpl extends AbstractKapuaEntity implements EventLog {

    private static final long serialVersionUID = 4029650117581681504L;

    @Basic
    @Column(name = "source_name", updatable = false, nullable = false)
    private String sourceName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "eid", column = @Column(name = "entity_scope_id", nullable = false, updatable = false))
    })
    private KapuaEid entityScopeId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "eid", column = @Column(name = "entity_id", nullable = false, updatable = false))
    })
    private KapuaEid entityId;

    @Basic
    @Column(name = "context_id", updatable = false, nullable = false)
    private String contextId;

    @Basic
    @Column(name = "event_operation", updatable = false, nullable = false)
    private String eventOperation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_sent_on", updatable = false, nullable = false)
    private Date eventSentOn;

    /**
     * Constructor
     */
    public EventLogImpl() {
        super();
    }

    /**
     * Constructor
     * 
     * @param scopeId
     */
    public EventLogImpl(KapuaId scopeId) {
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
        this.entityScopeId = KapuaEid.parseKapuaId(entityScopeId);
    }

    @Override
    public KapuaId getEntityId() {
        return entityId;
    }

    @Override
    public void setEntityId(KapuaId entityId) {
        this.entityId = KapuaEid.parseKapuaId(entityId);
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
