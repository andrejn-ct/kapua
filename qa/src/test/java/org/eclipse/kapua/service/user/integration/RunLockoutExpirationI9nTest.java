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
package org.eclipse.kapua.service.user.integration;

import cucumber.api.CucumberOptions;
import org.eclipse.kapua.test.cucumber.CucumberWithProperties;
import org.junit.runner.RunWith;

@RunWith(CucumberWithProperties.class)
@CucumberOptions(
        features = "classpath:features/user/LockoutExpirationI9n.feature",
        glue = {"org.eclipse.kapua.qa.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.service.authentication.steps"
               },
        plugin = {"pretty", 
                  "html:target/cucumber/LockoutExpirationI9n",
                  "json:target/LockoutExpirationI9n_cucumber.json"
                 },
        strict = true,
        monochrome=true)
public class RunLockoutExpirationI9nTest {}
