/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
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
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"classpath:features/job/JobServiceI9n.feature"
                   },
        glue = {"org.eclipse.kapua.qa.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.device.steps",
                "org.eclipse.kapua.service.job.steps"
               },
        plugin = {"pretty", 
                  "html:target/cucumber/JobServiceI9n",
                  "json:target/JobServiceI9n_cucumber.json"
                 },
        monochrome = true )

public class RunJobI9nTest {}
