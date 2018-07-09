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
package org.eclipse.kapua.service.device.registry;

import org.eclipse.kapua.event.ServiceEventConstants;

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
public final class RegistryServiceConstants extends ServiceEventConstants {

    private RegistryServiceConstants() {}

    // Service event source class names
    public static final String ACCOUNT_SERVICE_NAME = "org.eclipse.kapua.service.account.AccountService";
    public static final String TAG_SERVICE_NAME = "org.eclipse.kapua.service.tag.TagService";
    public static final String GROUP_SERVICE_NAME = "org.eclipse.kapua.service.authorization.group.GroupService";
}
