###############################################################################
# Copyright (c) 2018 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech - initial API and implementation
###############################################################################
@serviceevents
Feature: Job service tests
    Basic workflow of Job creation and deletion. These test scenarios check for integration issues with other services.

    Background: Creation of account and user with credentials
        Create a set of accounts and users with all the required service configurations.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given Account
            | name      | scopeId |
            | account-a | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given I configure the scheduler service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        Given I configure the scheduler service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |

    @StartEventBroker
    Scenario: Start event broker for all scenarios

    Scenario: Create a number of jobs and schedules. Regular case.

        Given I select account "account-a"
        And A job named "test-job-a-1" in the current scope
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-a-1" in the current account
        And I create the schedule "test-trigger-a-1-2" for the job "test-job-a-1" in the current account
        And A job named "test-job-a-2" in the current scope
        And I create the schedule "test-trigger-a-2-1" for the job "test-job-a-2" in the current account
        And I create the schedule "test-trigger-a-2-2" for the job "test-job-a-2" in the current account
        Given I select account "account-b"
        And A job named "test-job-b-1" in the current scope
        And I create the schedule "test-trigger-b-1-1" for the job "test-job-b-1" in the current account
        And I create the schedule "test-trigger-b-1-2" for the job "test-job-b-1" in the current account
        And I create the schedule "test-trigger-b-1-3" for the job "test-job-b-1" in the current account
        And A job named "test-job-b-2" in the current scope
        And I create the schedule "test-trigger-b-2-1" for the job "test-job-b-2" in the current account
        And I create the schedule "test-trigger-b-2-2" for the job "test-job-b-2" in the current account
        And I create the schedule "test-trigger-b-2-3" for the job "test-job-b-2" in the current account
        When I count the schedules in account "account-a"
        Then There are exactly 4 schedules
        When I count the schedules in account "account-b"
        Then There are exactly 6 schedules

    @StopEventBroker
    Scenario: Stop event broker for all scenarios
