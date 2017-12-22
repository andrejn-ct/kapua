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
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.service.account.steps;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.apache.shiro.SecurityUtils;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.metatype.KapuaMetatypeFactoryImpl;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.commons.security.KapuaSession;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.config.metatype.KapuaMetatypeFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.qa.steps.BaseQATests;
import org.eclipse.kapua.qa.steps.DBHelper;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.TestJAXBContextProvider;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountCreator;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.account.internal.AccountEntityManagerFactory;
import org.eclipse.kapua.service.account.internal.AccountFactoryImpl;
import org.eclipse.kapua.service.account.internal.AccountServiceImpl;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.common.TestConfig;
import org.eclipse.kapua.test.MockedLocator;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Gherkin steps used in User Service feature scenarios.
 */
@ScenarioScoped
public class AccountServiceSteps extends BaseQATests {

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceSteps.class);

    /**
     * Account service by locator.
     */
    private AccountService accountService;
    private AccountFactory accountFactory;

    @Inject
    public AccountServiceSteps(StepData stepData, DBHelper dbHelper) {

        this.stepData = stepData;
        this.database = dbHelper;
    }

    @Before
    public void beforeScenario(Scenario scenario) {

        this.database.setup();

        // Services by default Locator
        locator = KapuaLocator.getInstance();
        accountService = locator.getService(AccountService.class);

        XmlUtil.setContextProvider(new TestJAXBContextProvider());

        this.scenario = scenario;
        this.stepData.clear();

        if (isUnitTest()) {
            try {
                setupMockLocatorForAccount();
            } catch (Exception ex) {
                logger.error("Failed to set up mock locator in @Before", ex);
            }
        }
    }

    @After
    public void afterScenario() {

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

    @Given("^Account$")
    public void givenAccount(List<TestAccount> accountList) throws Exception {
        TestAccount testAccount = accountList.get(0);
        // If accountId is not set in account list, use last created Account for scope id
        if (testAccount.getScopeId() == null) {
            testAccount.setScopeId(((Account) stepData.get("LastAccount")).getId().getId());
        }

        stepData.put("LastAccount", createAccount(testAccount));
    }

    @When("^I select account \"(.*)\"$")
    public void selectAccount(String accountName) throws KapuaException{

        Account tmpAccount;
        tmpAccount = accountService.findByName(accountName);
        if (tmpAccount != null) {
            stepData.put("LastAccount", tmpAccount);
        } else {
            stepData.remove("LastAccount");
        }
    }

    @When("^I configure account service$")
    public void setAccountServiceConfig(List<TestConfig> testConfigs)
            throws Exception {
        Map<String, Object> valueMap = new HashMap<>();
        KapuaId accId;
        KapuaId scopeId;

        for (TestConfig config : testConfigs) {
            config.addConfigToMap(valueMap);
        }

        primeException();
        try {
            Account tmpAccount = (Account) stepData.get("LastAccount");
            if (tmpAccount != null) {
                accId = tmpAccount.getId();
                scopeId = new KapuaEid(BigInteger.ONE);
            } else {
                accId = new KapuaEid(BigInteger.ONE);
                scopeId = new KapuaEid(BigInteger.ONE);
            }
            accountService.setConfigValues(accId, scopeId, valueMap);
        } catch (KapuaException ex) {
            verifyException(ex);
        }
    }

    /**
     * Create account in privileged mode as kapua-sys user.
     * Account is created in scope specified by scopeId in testAccount parameter.
     * This is not accountId, but account under which it is created. AccountId itself
     * is created automatically.
     *
     * @param testAccount
     *            basic data about account
     * @return Kapua Account object
     */
    private Account createAccount(TestAccount testAccount) throws Exception {
        List<Account> accountList = new ArrayList<>();
        KapuaSecurityUtils.doPrivileged(() -> {
            primeException();
            try {
                Account account = accountService.create(accountCreatorCreator(testAccount.getName(),
                        testAccount.getScopeId()));
                accountList.add(account);
            } catch (KapuaException ke) {
                verifyException(ke);
            }

            return null;
        });

        return accountList.size() == 1 ? accountList.get(0) : null;
    }

    /**
     * Create account creator.
     *
     * @param name
     *            account name
     * @param scopeId
     *            acount scope id
     * @return
     */
    private AccountCreator accountCreatorCreator(String name, BigInteger scopeId) {
        AccountCreator accountCreator;

//        accountCreator = new AccountFactoryImpl().newCreator(new KapuaEid(scopeId), name);
        accountCreator = accountFactory.newCreator(new KapuaEid(scopeId), name);
        accountCreator.setOrganizationName("ACME Inc.");
        accountCreator.setOrganizationEmail("some@one.com");

        return accountCreator;
    }

    /**
     * Set up the preconditions for unit tests. This includes filling the mock locator with the correct
     * mocked services and the actual service implementation under test.
     * Also, all the unit tests will be run with the kapua-sys user.
     *
     * @throws Exception
     */
    private void setupMockLocatorForAccount() throws Exception {
        MockedLocator mockedLocator = (MockedLocator) KapuaLocator.getInstance();

        AbstractModule module = new AbstractModule() {

            @Override
            protected void configure() {

                // Inject mocked Authorization Service method checkPermission
                AuthorizationService mockedAuthorization = Mockito.mock(AuthorizationService.class);
                try {
                    Mockito.doNothing().when(mockedAuthorization).checkPermission(Matchers.any(Permission.class));
                } catch (KapuaException e) {
                    // skip
                }
                bind(AuthorizationService.class).toInstance(mockedAuthorization);
                // Inject mocked Permission Factory
                PermissionFactory mockedPermissionFactory = Mockito.mock(PermissionFactory.class);
                bind(PermissionFactory.class).toInstance(mockedPermissionFactory);
                // Set KapuaMetatypeFactory for Metatype configuration
                KapuaMetatypeFactory metaFactory = new KapuaMetatypeFactoryImpl();
                bind(KapuaMetatypeFactory.class).toInstance(metaFactory);

                // Inject actual implementation of AccountService
                AccountEntityManagerFactory accountEntityManagerFactory = (AccountEntityManagerFactory) AccountEntityManagerFactory.getInstance();
                bind(AccountEntityManagerFactory.class).toInstance(accountEntityManagerFactory);
                AccountService accountService = new AccountServiceImpl();
                bind(AccountService.class).toInstance(accountService);
                AccountFactory accountFactory = new AccountFactoryImpl();
                bind(AccountFactory.class).toInstance(accountFactory);
            }
        };

        Injector injector = Guice.createInjector(module);
        mockedLocator.setInjector(injector);

        accountService = KapuaLocator.getInstance().getService(AccountService.class);
        accountFactory = KapuaLocator.getInstance().getFactory(AccountFactory.class);

        // Create KapuaSession using KapuaSecurtiyUtils and kapua-sys user as logged in user.
        // All operations on database are performed using system user.
        KapuaSession kapuaSession = new KapuaSession(null, kapuaSys, kapuaSys);
        KapuaSecurityUtils.setSession(kapuaSession);
    }
}
