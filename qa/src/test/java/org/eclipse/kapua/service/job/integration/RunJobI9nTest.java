/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.service.job.integration;

import cucumber.api.CucumberOptions;
import org.eclipse.kapua.test.cucumber.CucumberProperty;
import org.eclipse.kapua.test.cucumber.CucumberWithProperties;
import org.junit.runner.RunWith;

@RunWith(CucumberWithProperties.class)
@CucumberOptions(
        features = {"classpath:features/job/TriggerServiceI9n.feature"
        },
        glue = {"org.eclipse.kapua.qa.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.job.steps",
                "org.eclipse.kapua.service.authentication.steps"
        },
        plugin = {"pretty",
                "html:target/cucumber/JobServiceI9n",
                "json:target/JobServiceI9n_cucumber.json"
        },
        strict = true,
        monochrome = true )
@CucumberProperty(key="org.eclipse.kapua.qa.broker.extraStartupDelay", value="5")
public class RunJobI9nTest {}

