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
package org.eclipse.kapua.service.job.steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.job.JobCreator;
import org.eclipse.kapua.service.job.JobFactory;
import org.eclipse.kapua.service.job.JobQuery;
import org.eclipse.kapua.service.job.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

// ****************************************************************************************
// * Implementation of Gherkin steps used in JobService.feature scenarios.                *
// *                                                                                      *
// * MockedLocator is used for Location Service. Mockito is used to mock other            *
// * services that the Account services dependent on. Dependent services are:             *
// * - Authorization Service                                                              *
// ****************************************************************************************

@ScenarioScoped
public class JobServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceSteps.class);

    // Job service objects
    private JobFactory jobFactory;
    private JobService jobService;
    private AccountService accountService;

    // Default constructor
    @Inject
    public JobServiceSteps(StepData stepData, DBHelper dbHelper) {
        this.stepData = stepData;
        this.database = dbHelper;
    }

    // ************************************************************************************
    // ************************************************************************************
    // * Definition of Cucumber scenario steps                                            *
    // ************************************************************************************
    // ************************************************************************************

    // ************************************************************************************
    // * Setup and tear-down steps                                                        *
    // ************************************************************************************

    @Before
    public void beforeScenario(Scenario scenario)
            throws Exception {

        // Create User Service tables
        this.database.setup();

        // Services by default Locator
        KapuaLocator locator = KapuaLocator.getInstance();
        jobService = locator.getService(org.eclipse.kapua.service.job.JobService.class);
        jobFactory = locator.getFactory(org.eclipse.kapua.service.job.JobFactory.class);
        accountService = locator.getService(org.eclipse.kapua.service.account.AccountService.class);

        XmlUtil.setContextProvider(new TestJAXBContextProvider());

        this.scenario = scenario;
        this.stepData.clear();
    }

    @After
    public void afterScenario()
            throws Exception {

        try {
            logger.info("Logging out in cleanup");
            if (isIntegrationTest()) {
                SecurityUtils.getSubject().logout();
            }
            KapuaSecurityUtils.clearSession();
        } catch (Exception e) {
            logger.error("Failed to log out in @After", e);
        }
    }

    // ************************************************************************************
    // * Cucumber Test steps                                                              *
    // ************************************************************************************
    @Given("^I create (\\d+) job items$")
    public void createANumberOfJobs(int num)
            throws Exception {

        JobCreator tmpCreator;
        KapuaId tmpScope = (KapuaId) stepData.get("CurrentScopeId");
        try {
            primeException();
            for (int i = 0; i < num; i++) {
                tmpCreator = jobFactory.newCreator(tmpScope);
                tmpCreator.setName(String.format("TestJobNum%d", i));
                tmpCreator.setDescription("TestJobDescription");
                jobService.create(tmpCreator);
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for jobs in account \"(.+)\"$")
    public void countJobsInScope(String acc)
            throws Exception {

        Account tmpAcc = accountService.findByName(acc);
        JobQuery tmpQuery = jobFactory.newQuery(tmpAcc.getId());
        long tmpCount;

        try {
            primeException();
            tmpCount = jobService.query(tmpQuery).getSize();
            stepData.put("Count", tmpCount);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }
}
