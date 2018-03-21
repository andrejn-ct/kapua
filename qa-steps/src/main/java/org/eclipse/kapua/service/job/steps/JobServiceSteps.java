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
package org.eclipse.kapua.service.job.steps;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicateImpl;
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
import org.eclipse.kapua.service.job.Job;
import org.eclipse.kapua.service.job.JobAttributes;
import org.eclipse.kapua.service.job.JobCreator;
import org.eclipse.kapua.service.job.JobFactory;
import org.eclipse.kapua.service.job.JobListResult;
import org.eclipse.kapua.service.job.JobQuery;
import org.eclipse.kapua.service.job.JobService;
import org.eclipse.kapua.service.user.steps.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

/**
 * Implementation of Gherkin steps used in TagService.feature scenarios.
 */

@ScenarioScoped
public class JobServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(JobServiceSteps.class);

    private static JobService jobService;
    private static JobFactory jobFactory;
    private static AccountService accountService;

    @Inject
    public JobServiceSteps(StepData stepData, DBHelper dbHelper) {

        this.database = dbHelper;
        this.stepData = stepData;
    }

/**
 * Setup and teardown functions
 */
    @Before
    public void beforeScenario(Scenario scenario) {

        this.database.setup();
        this.scenario = scenario;
        this.stepData.clear();

        KapuaLocator locator = KapuaLocator.getInstance();
        jobService = locator.getService(JobService.class);
        jobFactory = locator.getFactory(JobFactory.class);
        accountService = locator.getService(AccountService.class);

        XmlUtil.setContextProvider(new TestJAXBContextProvider());
    }

    @After
    public void afterScenario() {
        try {
            logger.info("Logging out in cleanup");
            SecurityUtils.getSubject().logout();
            KapuaSecurityUtils.clearSession();
        } catch (Exception e) {
            logger.error("Failed to log out in @After", e);
        }
    }

/**
 * Implementation of cucumber scenario steps
 */
    @When("^I configure the job service$")
    public void setJobServiceConfig(List<TestConfig> testConfigs)
            throws Exception {

        Account lastAcc = (Account) stepData.get("LastAccount");
        KapuaId scopeId = ROOT_SCOPE_ID;
        KapuaId parentId = ROOT_SCOPE_ID;
        if (lastAcc != null) {
            scopeId = lastAcc.getId();
            parentId = lastAcc.getScopeId();
        }
        Map<String, Object> valueMap = new HashMap<>();

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }
        try {
            primeException();
            jobService.setConfigValues(scopeId, parentId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Given("^A job named \"(.+)\" in the current scope$")
    public void createANamedJob(String jobName) throws Exception {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId tmpScope = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;
        JobCreator tmpCreator = prepareRegularJobCreator(tmpScope, jobName);
        Job tmpJob;

        try {
            primeException();
            stepData.remove("Job");
            tmpJob = jobService.create(tmpCreator);
            stepData.put("Job", tmpJob);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the job \"(.+)\" in account \"(.+)\"$")
    public void queryForNamedJobInAccount(String jobName, String accName) throws Exception{

        Account tmpAcc = accountService.findByName(accName);
        KapuaId tmpScopeId = tmpAcc.getId();
        JobQuery tmpQuery = jobFactory.newQuery(tmpScopeId);
        tmpQuery.setPredicate(new AttributePredicateImpl<>(JobAttributes.NAME, jobName));
        JobListResult tmpJobList;

        try {
            primeException();
            stepData.remove("Job");
            stepData.remove("JobList");
            tmpJobList = jobService.query(tmpQuery);
            stepData.put("JobList", tmpJobList);
            if (!tmpJobList.isEmpty()) {
                stepData.put("Job", tmpJobList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @When("^I query for the job \"(.+)\" in the last account$")
    public void queryForNamedJob(String jobName) throws Exception{

        Account tmpAcc = (Account) stepData.get("LastAccount");
        KapuaId tmpScopeId = (tmpAcc != null) ? tmpAcc.getId() : ROOT_SCOPE_ID;
        JobQuery tmpQuery = jobFactory.newQuery(tmpScopeId);
        tmpQuery.setPredicate(new AttributePredicateImpl<>(JobAttributes.NAME, jobName));
        JobListResult tmpJobList;

        try {
            primeException();
            stepData.remove("Job");
            stepData.remove("JobList");
            tmpJobList = jobService.query(tmpQuery);
            stepData.put("JobList", tmpJobList);
            if (!tmpJobList.isEmpty()) {
                stepData.put("Job", tmpJobList.getFirstItem());
            }
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    @Then("^There is no such job$")
    public void checkThatThereIsNoJob() {

        Assert.assertNull(stepData.get("Job"));
    }

    @Then("^There is such a job$")
    public void checkThatTheJobExists() {

        Assert.assertNotNull(stepData.get("Job"));
    }

/**
 * Private helper functions
 */
    JobCreator prepareRegularJobCreator(KapuaId scopeId, String jobName) {
        JobCreator tmpCreator = jobFactory.newCreator(scopeId);

        tmpCreator.setName(jobName);
        tmpCreator.setDescription("TestJobDescription");

        return tmpCreator;
    }

}
