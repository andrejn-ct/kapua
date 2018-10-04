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

import org.eclipse.kapua.model.KapuaEntityCreator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdAdapter;
import org.eclipse.kapua.model.xml.DateXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * EventLogCreator encapsulates all the information needed to create a new Event Log entry.
 *
 * @since 1.0
 */
@XmlRootElement(name = "eventLogCreator")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
            "sourceName",
            "entityScopeId",
            "entityId",
            "contextId",
            "eventOperation",
            "eventSentOn"
        },
        factoryClass = EventLogXmlRegistry.class,
        factoryMethod = "newEventLogCreator")
public interface EventLogCreator extends KapuaEntityCreator<EventLog> {

    /**
     * Return the event source service name
     *
     * @return
     */
    @XmlElement(name = "sourceName")
    String getSourceName();

    /**
     * Set the event source service name
     *
     * @param sourceName event source service name
     */
    void setSourceName(String sourceName);

    /**
     * Return the scope ID of the affected entity
     *
     * @return
     */
    @XmlElement(name = "entityScopeId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    KapuaId getEntityScopeId();

    /**
     * Set the scope ID of the affected entity
     *
     * @param entityScopeId
     */
    void setEntityScopeId(KapuaId entityScopeId);

    /**
     * Get the entity ID
     *
     * @return
     */
    @XmlElement(name = "entityId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    KapuaId getEntityId();

    /**
     * Set the entity ID
     *
     * @param entityId
     */
    void setEntityId(KapuaId entityId);

    /**
     * Get the event context ID
     *
     * @return
     */
    @XmlElement(name = "contextId")
    String getContextId();

    /**
     * Set the event context ID
     *
     * @param contextId
     */
    void setContextId(String contextId);

    /**
     * Get the event operation name
     *
     * @return
     */
    @XmlElement(name = "eventOperation")
    String getEventOperation();

    /**
     * Set the event operation name
     *
     * @param eventOperation
     */
    void setEventOperation(String eventOperation);

    /**
     * Get the event origination date
     *
     * @return
     */
    @XmlElement(name = "eventSentOn")
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    Date getEventSentOn();

    /**
     * Set the event origination date
     *
     * @param eventSentOn
     */
    void setEventSentOn(Date eventSentOn);
}
