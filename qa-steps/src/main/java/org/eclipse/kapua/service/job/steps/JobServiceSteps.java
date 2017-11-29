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
package org.eclipse.kapua.service.job.steps;

import cucumber.api.Scenario;

import cucumber.api.java.After;
import cucumber.api.java.Before;

import cucumber.runtime.java.guice.ScenarioScoped;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.job.JobFactory;
import org.eclipse.kapua.service.job.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import static org.junit.Assert.assertTrue;

/**
 * Implementation of Gherkin steps used in JobServiceI9n.feature scenarios.
 */
@ScenarioScoped
public class JobServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceSteps.class);

    // Job service
    private static JobService jobService;
    private static JobFactory jobFactory;

    // Inter step data scratchpad
    private StepData stepData;

    // Single point to database access
    private DBHelper dbHelper;

    @Inject
    public JobServiceSteps(StepData stepData, DBHelper dbHelper) {

        this.stepData = stepData;
        this.dbHelper = dbHelper;
    }

    // Scenario setup
    @Before
    public void beforeScenario(Scenario scenario) throws KapuaException {

        KapuaLocator locator = KapuaLocator.getInstance();
        jobService = locator.getService(JobService.class);
        jobFactory = locator.getFactory(JobFactory.class);

        XmlUtil.setContextProvider(new TestJAXBContextProvider());

        this.scenario = scenario;
        this.dbHelper.setup();
        this.stepData.clear();
    }

    // Scenario tear-down
    @After
    public void afterScenario() throws KapuaException {

        try {
            logger.info("Logging out in cleanup");
            SecurityUtils.getSubject().logout();
            KapuaSecurityUtils.clearSession();
        } catch (Exception e) {
            logger.error("Failed to log out in @After", e);
        }
    }

    // Cucumber test steps

    @Given("^A mock test step$")
    public void mockTestStep() {
        assertTrue(true);
    }

    @Then("^Nothing happens$")
    public void mockTestCheck() {
        assertTrue(true);
    }
}
