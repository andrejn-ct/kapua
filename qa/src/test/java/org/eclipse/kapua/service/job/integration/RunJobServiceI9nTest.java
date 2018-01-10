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
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.service.job.integration;

import org.eclipse.kapua.test.cucumber.CucumberProperty;
import org.eclipse.kapua.test.cucumber.CucumberWithProperties;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;

@RunWith(CucumberWithProperties.class)
@CucumberOptions(
        features = "classpath:features/job/JobServiceI9n.feature",
        glue = {"org.eclipse.kapua.qa.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.job.steps"
               },
        plugin = {"pretty", 
                  "html:target/cucumber/JobServiceI9n",
                  "json:target/JobServiceI9n_cucumber.json"
                 },
        monochrome=true)
@CucumberProperty(key="datastore.client.class", value="org.eclipse.kapua.service.datastore.client.rest.RestDatastoreClient")
public class RunJobServiceI9nTest {}
