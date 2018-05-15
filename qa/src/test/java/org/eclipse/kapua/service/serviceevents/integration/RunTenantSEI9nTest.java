/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.serviceevents.integration;

import cucumber.api.CucumberOptions;
import org.eclipse.kapua.test.cucumber.CucumberWithProperties;
import org.junit.runner.RunWith;

@RunWith(CucumberWithProperties.class)
@CucumberOptions(
        features = "classpath:features/serviceevents/TenantEvents.feature",
        glue = {"org.eclipse.kapua.qa.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.device.steps",
                "org.eclipse.kapua.service.connection.steps",
                "org.eclipse.kapua.service.tag.steps",
                "org.eclipse.kapua.service.job.steps",
                "org.eclipse.kapua.service.authentication.steps"
               },
        plugin = {"pretty", 
                  "html:target/cucumber/TenantSEI9n",
                  "json:target/TenantSEI9n_cucumber.json"
                 },
        monochrome=true)
public class RunTenantSEI9nTest {}