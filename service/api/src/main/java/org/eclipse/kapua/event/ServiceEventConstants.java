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
package org.eclipse.kapua.event;

/**
 * Service Event constants
 *
 * This is a base class that contains the common constants that are used by all service event listeners
 *
 * Each service that listens to service events must extend this class and add a list of constants
 * defining the class names of the event source services.
 *
 * @since 1.0
 *
 */
public class ServiceEventConstants {

    protected ServiceEventConstants() {}

    /**
     * Service event operations.
     *
     * These are the names of the methods that were invoked and that generated the events. The actual source
     * service method names are used.
     *
     * Each service event object carries the name of the method that generated the event. This enables the event
     * handler to correctly react to each event based on the exact operation type that was performed.
     * It is imperative that the string literals are carefully aligned with the source service implementation.
     * Each mistake here will result in the service event handler silently ignoring the events.
     */
    public static final String OPERATION_CREATE = "create";
    public static final String OPERATION_UPDATE = "update";
    public static final String OPERATION_DELETE = "delete";

    /**
     * Service event sources.
     *
     * These are the class names of the services that generate the events. All the relevant source service names must
     * be listed.
     *
     * Each event object carries the class name of the generating service. This makes it possible to handle each
     * event based on the source service.
     * It is crucial that the constants defined below match the exact source service class names. A mistake in a class
     * name constant here will cause the handler to silently ignore the relevant service events.
     *
     * The event listening services must extend this class and list all the relevant event source service names.
     */

}
