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
package org.eclipse.kapua.service.job.internal;

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModule;
import org.eclipse.kapua.commons.event.ServiceEventModuleConfiguration;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.service.job.JobService;
import org.eclipse.kapua.service.job.internal.setting.KapuaJobSetting;
import org.eclipse.kapua.service.job.internal.setting.KapuaJobSettingKeys;

import javax.inject.Inject;
import java.util.List;

@KapuaProvider
public class JobServiceModule extends ServiceEventModule {

    @Inject
    private JobService jobService;

    @Override
    protected ServiceEventModuleConfiguration initializeConfiguration() {
        KapuaJobSetting kjs = KapuaJobSetting.getInstance();
        List<ServiceEventClientConfiguration> selc = ServiceInspector.getEventBusClients(jobService, JobService.class);
        return new ServiceEventModuleConfiguration(
                kjs.getString(KapuaJobSettingKeys.JOB_EVENT_ADDRESS),
                JobEntityManagerFactory.getInstance(),
                selc.toArray(new ServiceEventClientConfiguration[0]));
    }
}
