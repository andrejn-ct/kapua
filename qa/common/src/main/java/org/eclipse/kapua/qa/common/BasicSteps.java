/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.qa.common;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Duration;

@ScenarioScoped
public class BasicSteps extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(BasicSteps.class);

    private static final double WAIT_MULTIPLIER = Double.parseDouble(System.getProperty("org.eclipse.kapua.qa.waitMultiplier", "1.0"));

    /**
     * Scenario scoped step data.
     */
    private StepData stepData;

    @Inject
    public BasicSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Before
    public void checkWaitMultipier() {
        if (WAIT_MULTIPLIER != 1.0d) {
            logger.info("Wait multiplier active: {}", WAIT_MULTIPLIER);
        }
    }

    @Given("^Scope with ID (\\d+)$")
    public void setSpecificScopeId(Integer id) {

        stepData.put("LastAccountId", getKapuaId(id));
    }

    @Given("^The KAPUA-SYS scope$")
    public void setRootScope() {

        stepData.put("LastAccountId", SYS_SCOPE_ID);
    }

    @Given("^A null scope$")
    public void setNullScope() {

        stepData.put("LastAccountId", null);
    }

    @Given("^The User ID (\\d+)$")
    public void setSpecificUserId(Integer id) {

        stepData.put("LastUserId", getKapuaId(id));
    }

    @Given("^The KAPUA-SYS user$")
    public void setRootUserId() {

        stepData.put("LastUserId", SYS_USER_ID);
    }

    @Given("^A null user")
    public void setNullUser() {

        stepData.put("LastUserId", null);
    }

    @Given("^I expect the exception \"(.+)\" with the text \"(.+)\"$")
    public void setExpectedExceptionDetails(String name, String text) {
        stepData.put("ExceptionExpected", true);
        stepData.put("ExceptionName", name);
        stepData.put("ExceptionMessage", text);
    }

    @When("I wait (\\d+) seconds?.*")
    public void waitSeconds(int seconds) throws InterruptedException {
        double effectiveSeconds = ((double) seconds) * WAIT_MULTIPLIER;
        Thread.sleep(Duration.ofSeconds((long) Math.ceil(effectiveSeconds)).toMillis());
    }

    @When("(\\d+) seconds?.* passed")
    public void secondsPassed(int seconds) throws InterruptedException {
        waitSeconds(seconds);
    }

    @Then("^An exception was thrown$")
    public void exceptionCaught() {
        String exName = stepData.contains("ExceptionName") ? (String)stepData.get("ExceptionName") : "Unknown";
        boolean exCaught = stepData.contains("ExceptionCaught") ? (boolean) stepData.get("ExceptionCaught") : false;
        assertTrue(String.format("Exception %s was expected but was not raised.", exName), exCaught);
    }

    @Then("^No exception was thrown$")
    public void noExceptionCaught() {
        boolean exCaught = stepData.contains("ExceptionCaught") ? (boolean) stepData.get("ExceptionCaught") : false;
        assertFalse("An unexpected exception was raised!", exCaught);
    }

    @Then("^I count (\\d+)$")
    public void checkCountResult(Long num) {
        assertEquals(num, stepData.get("Count"));
    }

    @Then("^I get the integer (\\d+)$")
    public void checkIntResult(int num) {
        assertEquals(num, (int) stepData.get("IntValue"));
    }

    @Then("^I get the boolean \"(.+)\"$")
    public void checkBoolResult(String val) {
        assertEquals(Boolean.valueOf(val).booleanValue(), stepData.get("BoolValue"));
    }

    @Then("^I get the text \"(.+)\"$")
    public void checkStringResult(String text) {
        assertEquals(text, stepData.get("Text"));
    }

    @Given("^System property \"(.*)\" with value \"(.*)\"$")
    public void setSystemProperty(String key, String value) {
        if ("null".equalsIgnoreCase(value)) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }
}
