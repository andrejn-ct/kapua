/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.endpoint.internal.setting;

import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

/**
 * Class that offers access to endpoint settings
 *
 * 
 * @since 1.0
 *
 */
public class KapuaEndpointSetting extends AbstractKapuaSetting<KapuaEndpointSettingKeys> {

    /**
     * Resource file from which source properties.
     *
     */
    private static final String ENDPOINT_SETTING_RESOURCE = "kapua-endpoint-setting.properties";

    /**
     * Singleton instance of this {@link Class}.
     *
     */
    private static final KapuaEndpointSetting INSTANCE = new KapuaEndpointSetting();

    /**
     * Initialize the {@link AbstractKapuaSetting} with the {@link KapuaEndpointSettingKeys#ENDPOINT_KEY} value.
     *
     */
    private KapuaEndpointSetting() {
        super(ENDPOINT_SETTING_RESOURCE);
    }

    /**
     * Gets a singleton instance of {@link KapuaEndpointSetting}.
     * 
     * @return A singleton instance of JmsClientSetting.
     */
    public static KapuaEndpointSetting getInstance() {
        return INSTANCE;
    }
}
