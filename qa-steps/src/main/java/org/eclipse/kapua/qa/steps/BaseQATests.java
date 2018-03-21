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
package org.eclipse.kapua.qa.steps;

import cucumber.api.Scenario;

import java.math.BigInteger;

import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.account.Account;

public class BaseQATests {

    public static final KapuaEid ROOT_SCOPE_ID = new KapuaEid(BigInteger.ONE);

    /**
     * Inter step data scratchpad.
     */
    public StepData stepData;

    /**
     * Database setup and cleanup helper
     */
    public DBHelper database;

    /**
     * Current scenario scope
     */
    public Scenario scenario;

    public BaseQATests() {
    }

    public KapuaId getLastAccountId() {

        Account tmpAcc = (Account) stepData.get("LastAccount");
        return (tmpAcc == null) ? ROOT_SCOPE_ID : ((tmpAcc.getId() != null) ? tmpAcc.getId() : ROOT_SCOPE_ID);
    }

    public void primeException() {
        stepData.put("ExceptionCaught", false);
        stepData.remove("Exception");
    }

    /**
     * Check the exception that was caught. In case the exception was expected the type and message is shown in the cucumber logs.
     * Otherwise the exception is rethrown failing the test and dumping the stack trace to help resolving problems.
     */
    public void verifyException(Exception ex)
            throws Exception {

        boolean exceptionExpected = stepData.contains("ExceptionExpected") ? (boolean)stepData.get("ExceptionExpected") : false;
        String exceptionName = stepData.contains("ExceptionName") ? (String)stepData.get("ExceptionName") : "";
        String exceptionMessage = stepData.contains("ExceptionMessage") ? (String)stepData.get("ExceptionMessage") : "";

        if (!exceptionExpected ||
                (!exceptionName.isEmpty() && !ex.getClass().toGenericString().contains(exceptionName)) ||
                (!exceptionMessage.isEmpty() && !exceptionMessage.trim().contentEquals("*") && !ex.getMessage().contains(exceptionMessage))) {
            scenario.write("An unexpected exception was raised!");
            throw(ex);
        }

        scenario.write("Exception raised as expected: " + ex.getClass().getCanonicalName() + ", " + ex.getMessage());
        stepData.put("ExceptionCaught", true);
        stepData.put("Exception", ex);
    }
}
