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
Feature: Job service with Service Events
    Basic workflow of Job creation and deletion, where Service Events are triggered on
    delete action on Job service.

    Scenario: Start event broker for all scenarios

        Given Start Event Broker

    Scenario: Job is deleted, the job schedules must be deleted too

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

        Given I select account "account-a"
        And A job named "test-job-a-1" in the current scope
        And A job named "test-job-a-2" in the current scope
        When I create the schedule "test-trigger-1" for the job "test-job-a-1" in the current account
        When I create the schedule "test-trigger-2" for the job "test-job-a-1" in the current account
        When I create the schedule "test-trigger-3" for the job "test-job-a-2" in the current account
        And I search for the schedule "test-trigger-1" in the current account
        Then There is such a schedule
        When I count the schedules in the current account
        Then There are exactly 3 schedules
        When I delete the job "test-job-a-1" in the current account
        And I wait 5 seconds
        When I search for the schedule "test-trigger-1" in the current account
        Then There is no such schedule
        When I count the schedules in the current account
        Then There is exactly 1 schedule

    Scenario: Stop event broker for all scenarios

        Given Stop Event Broker
