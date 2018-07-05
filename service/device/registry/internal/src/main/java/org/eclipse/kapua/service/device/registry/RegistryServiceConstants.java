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

public final class RegistryServiceConstants extends ServiceEventConstants {

    private RegistryServiceConstants() {}

    // Service event source class names
    public static final String ACCOUNT_SERVICE_NAME = "org.eclipse.kapua.service.account.AccountService";
    public static final String TAG_SERVICE_NAME = "org.eclipse.kapua.service.tag.TagService";
    public static final String GROUP_SERVICE_NAME = "org.eclipse.kapua.service.authorization.group.GroupService";
}
