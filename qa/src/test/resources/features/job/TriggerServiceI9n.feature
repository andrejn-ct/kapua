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
@jobschedules
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

    Scenario: Create a schedule with a duplicate name
        The schedule names must be unique in the scope. Creating a schedule with a duplicate name should
        throw an exception. A similarly named schedule in another account must be successfully created.

        Given I select account "account-a"
        And A job named "test-job-a-1" in the current scope
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-a-1" in the current account
        And A job named "test-job-a-2" in the current scope
        And I create the schedule "test-trigger-a-2-1" for the job "test-job-a-2" in the current account
        Given I expect the exception "KapuaDuplicateNameException" with the text "test-trigger-a-1-1 already exists"
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-a-2" in the current account
        Then An exception was thrown
        Given I select account "account-b"
        And A job named "test-job-b-1" in the current scope
        And I create the schedule "test-trigger-b-1-1" for the job "test-job-b-1" in the current account
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-b-1" in the current account

    Scenario: Delete an existing schedule
        It  must be possible to regularly delete an existing schedule entity.

        Given I select account "account-a"
        And A job named "test-job-a-1" in the current scope
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-a-1" in the current account
        And I create the schedule "test-trigger-a-1-2" for the job "test-job-a-1" in the current account
        Given I select account "account-b"
        And A job named "test-job-b-1" in the current scope
        And I create the schedule "test-trigger-b-1-1" for the job "test-job-b-1" in the current account
        When I count the schedules in account "account-a"
        Then There are exactly 2 schedules
        When I delete the schedule "test-trigger-a-1-2" in account "account-a"
        And I count the schedules in account "account-a"
        Then There is exactly 1 schedule

    Scenario: Delete a non existing schedule
        Try to delete the same schedule twice. When deleting the second time, an exception must be raised.

        Given I select account "account-a"
        And A job named "test-job-a-1" in the current scope
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-a-1" in the current account
        And I create the schedule "test-trigger-a-1-2" for the job "test-job-a-1" in the current account
        Given I select account "account-b"
        And A job named "test-job-b-1" in the current scope
        And I create the schedule "test-trigger-b-1-1" for the job "test-job-b-1" in the current account
        When I count the schedules in account "account-a"
        Then There are exactly 2 schedules
        When I select the schedule "test-trigger-a-1-2" in account "account-a"
        And I delete the selected schedule
        When I count the schedules in account "account-a"
        Then There is exactly 1 schedule
        Given I expect the exception "KapuaEntityNotFoundException" with the text "The entity of type schedule with id/name"
        When I delete the selected schedule
        Then An exception was thrown

    Scenario: Rename an existing schedule

        Given I select account "account-a"
        And A job named "test-job-a-1" in the current scope
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-a-1" in the current account
        Given I select the schedule "test-trigger-a-1-1" in the current account
        When I rename the selected schedule to "NewName"
        Then No exception was thrown
        When I search for the schedule "NewName" in the current account
        Then There is exactly 1 schedule
        When I search for the schedule "test-trigger-a-1-1" in the current account
        Then There is no such schedule

    Scenario: Rename an inexistent schedule
        An attempt to update an inexistent should result in an exception. For this purpose, a schedule is
        first created and deleted. Updating this deleted schedule should throw an exception.

        Given I select account "account-a"
        And A job named "test-job-a-1" in the current scope
        And I create the schedule "test-trigger-a-1-1" for the job "test-job-a-1" in the current account
        And I create the schedule "test-trigger-a-1-2" for the job "test-job-a-1" in the current account
        Given I select the schedule "test-trigger-a-1-2" in the current account
        Then I delete the selected schedule
        Given I expect the exception "KapuaEntityNotFoundException" with the text "The entity of type schedule with id/name"
        When I rename the selected schedule to "NewName"
        Then An exception was thrown
