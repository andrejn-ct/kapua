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
package org.eclipse.kapua.service.endpoint.internal;

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.endpoint.EndpointInfoService;
import org.eclipse.kapua.service.endpoint.internal.setting.KapuaEndpointSetting;
import org.eclipse.kapua.service.endpoint.internal.setting.KapuaEndpointSettingKeys;

import javax.inject.Inject;
import java.util.List;

@KapuaProvider
public class EndpointInfoServiceModule extends ServiceEventModule {

    @Inject
    private EndpointInfoService endpointInfoService;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
        KapuaEndpointSetting kes = KapuaEndpointSetting.getInstance();
        List<ServiceEventClientConfiguration> selc = ServiceInspector.getEventBusClients(endpointInfoService, EndpointInfoService.class);
        return new ServiceEventModuleConfiguration(
                kes.getString(KapuaEndpointSettingKeys.ENDPOINT_KEY.ENDPOINT_EVENT_ADDRESS),
                EndpointEntityManagerFactory.getInstance(),
                selc.toArray(new ServiceEventClientConfiguration[0]));
    }
}
