/*******************************************************************************
 * Copyright (c) 2017, 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.scheduler.trigger.quartz;

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.scheduler.trigger.TriggerService;
import org.eclipse.kapua.service.scheduler.quartz.setting.KapuaSchedulerSetting;
import org.eclipse.kapua.service.scheduler.quartz.setting.KapuaSchedulerSettingKeys;

import javax.inject.Inject;
import java.util.List;

@KapuaProvider
public class TriggerServiceModule extends ServiceEventModule {

    @Inject
    private TriggerService triggerService;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
        KapuaSchedulerSetting kas = KapuaSchedulerSetting.getInstance();
        List<ServiceEventClientConfiguration> selc = ServiceInspector.getEventBusClients(triggerService, TriggerService.class);
        return new ServiceEventModuleConfiguration(
                kas.getString(KapuaSchedulerSettingKeys.SCHEDULER_EVENT_ADDRESS),
                SchedulerEntityManagerFactory.getInstance(),
                selc.toArray(new ServiceEventClientConfiguration[0]));
    }
}
