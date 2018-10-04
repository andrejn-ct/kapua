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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.kapua.commons.jpa.AbstractEntityManagerFactory;
import org.eclipse.kapua.commons.jpa.EntityManager;
import org.eclipse.kapua.commons.jpa.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity manager factory for the event log module.
 * 
 * @since 1.0
 *
 */
public class EventLogEntityManagerFactory extends AbstractEntityManagerFactory implements EntityManagerFactory {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(EventLogEntityManagerFactory.class);

    private static final String PERSISTENCE_UNIT_NAME = "kapua-eventlog";
    private static final String DATASOURCE_NAME = "kapua-dbpool";
    private static final Map<String, String> UNIQUE_CONSTRAINTS = new HashMap<>();

    private static EventLogEntityManagerFactory instance = new EventLogEntityManagerFactory();

    /**
     * Constructs a new entity manager factory and configure it to use the event log persistence unit.
     */
    private EventLogEntityManagerFactory() {
        super(PERSISTENCE_UNIT_NAME, DATASOURCE_NAME, UNIQUE_CONSTRAINTS);
    }

    /**
     * Return the {@link EntityManager} singleton instance
     * 
     * @return
     */
    public static EventLogEntityManagerFactory getInstance() {
        return instance;
    }
}
