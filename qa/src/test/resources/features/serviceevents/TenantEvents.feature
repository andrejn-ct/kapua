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
Feature: Tenant service with Service Events
    Basic workflow of Account and User creation and deletion, where Service Events are
    being triggered on create, update and delete action on Account and User service.

    Scenario: Start event broker for all scenarios
        Given Start Event Broker

    Scenario: Account is deleted, user has to be deleted too
        Remove account and eventually user has to be deleted to by user service
        listening on account delete event.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
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
        And User A
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-a | Kapua User A | kapua_a@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-a | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
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
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |

        Given I select account "account-a"
        And I select user "kapua-a"
        When I try to delete account "account-a"
        And I wait 5 seconds
        Then I don't find user "kapua-a"
        And I logout

    Scenario: Account is deleted, users, tags and devices have to be deleted too
        Remove account and eventually all users, tags and devices belonging to this account have
        to be deleted by the user and device services listening on account delete events.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User A
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-a | Kapua User A | kapua_a@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-a | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |

        Given I select account "account-a"
        And I select user "kapua-a"
        Given A birth message from device "device-a"
        When I search for the device "device-a" in account "account-a"
        Then I find 1 device
        Given A new tag with name "test-tag-1" in the last account
        Then I tag device "device-a" with the existing tag "test-tag-1"
        When I try to delete account "account-a"
        And I wait 5 seconds
        Then I don't find user "kapua-a"
        When I search for the device "device-a" in the last account
        Then I find no device
        When I search for the tag "test-tag-1" in the last account
        Then I find no such tag
        And I logout

    Scenario: Account is deleted, only this account users and devices have to be deleted
        Remove account and eventually all users and devices belonging to this account have
        to be deleted by the user and device services listening on account delete events. All other
        users and devices must remain.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User A
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-a | Kapua User A | kapua_a@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-a | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |

        Given I select account "account-a"
        Given I select user "kapua-a"
        And The following device connections
            | scope     | clientId | user    | userCouplingMode |
            | account-a | device-a | kapua-a | LOOSE            |
        Given A birth message from device "device-a"
        When I search for the device "device-a" in account "account-a"
        Then I find 1 device
        Given A new tag with name "test-tag-a-1" in the last account
        Then I tag device "device-a" with the existing tag "test-tag-a-1"
        When I search for device connections for the last account
        Then I find 1 connection
        Given I select account "account-b"
        And The following device connections
            | scope     | clientId | user    | userCouplingMode |
            | account-b | device-b | kapua-b | LOOSE            |
        Given A birth message from device "device-b"
        When I search for the device "device-b" in account "account-b"
        Then I find 1 device
        Given A new tag with name "test-tag-b" in the last account
        Then I tag device "device-b" with the existing tag "test-tag-b"
        Given I select account "account-a"
        Given I select user "kapua-a"
        When I try to delete account "account-a"
        And I wait 10 seconds
        Then I don't find user "kapua-a"
        When I search for the device "device-a" in the last account
        Then I find no device
        When I search for device connections for the last account
        Then I find no connection
        When I search for the tag "test-tag-a-1" in the last account
        Then I find no such tag
        Then I find user "kapua-b"
        When I search for the device "device-b" in account "account-b"
        Then I find 1 device
        When I search for device connections for account "account-b"
        Then I find 1 connection
        When I search for the tag "test-tag-b" in account "account-b"
        Then I find such a tag
        And I logout

    Scenario: Tag is deleted, the device tag should be deleted too
        Remove an existing tag. This tag should be stripped from all the devices. All
        other tags must remain.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User A
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-a | Kapua User A | kapua_a@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-a | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |

        Given I select account "account-a"
        Given I select user "kapua-a"
        And The following device connections
            | scope     | clientId | user    | userCouplingMode |
            | account-a | device-a | kapua-a | LOOSE            |
            | account-a | device-b | kapua-a | LOOSE            |
            | account-a | device-c | kapua-a | LOOSE            |
            | account-a | device-d | kapua-a | LOOSE            |
        Given A birth message from device "device-a"
        Given A birth message from device "device-b"
        Given A birth message from device "device-c"
        Given A birth message from device "device-d"
        When I search for the device "device-a" in account "account-a"
        Then I find 1 device
        Given A new tag with name "test-tag-a-1" in the last account
        Then I tag device "device-a" with the existing tag "test-tag-a-1"
        Given A new tag with name "test-tag-a-2" in the last account
        Then I tag device "device-a" with the existing tag "test-tag-a-2"
        Then I tag device "device-b" with the existing tag "test-tag-a-2"
        Then I tag device "device-c" with the existing tag "test-tag-a-2"
        Then I tag device "device-d" with the existing tag "test-tag-a-2"
        Given A new tag with name "test-tag-a-3" in the last account
        Then I tag device "device-a" with the existing tag "test-tag-a-3"
        Then I tag device "device-b" with the existing tag "test-tag-a-3"
        Then I tag device "device-c" with the existing tag "test-tag-a-3"
        When I search for the devices with the tag "test-tag-a-2"
        Then I find 4 devices
        When I search for the devices with the tag "test-tag-a-3"
        Then I find 3 devices
        Then I search for the tag "test-tag-a-2" in account "account-a"
        When I delete the tag "test-tag-a-2"
        And I wait 10 seconds
        When I search for the devices that are tagged with the last tag
        Then I find 0 devices
        When I search for the tag "test-tag-a-2" in account "account-a"
        Then I find no such tag
        Then I search for the tag "test-tag-a-1" in account "account-a"
        When I search for the devices that are tagged with the last tag
        Then I find 1 device
        Then I search for the tag "test-tag-a-3" in account "account-a"
        When I search for the devices that are tagged with the last tag
        Then I find 3 devices
        And Device "device-a" has the tag "test-tag-a-1"
        And Device "device-a" has the tag "test-tag-a-3"
        Then The device "device-a" has 2 tags

    Scenario: Account is deleted, jobs for this account are deleted too
        Remove an account. Eventually all the defined jobs have to be deleted too. Jobs in other
        accounts must be left intact.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User A
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-a | Kapua User A | kapua_a@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-a | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |

        Given I select account "account-a"
        Given A job named "test-job-a-1" in the current scope
        And A job named "test-job-a-2" in the current scope
        Given I select account "account-b"
        Given A job named "test-job-b-1" in the current scope
        And A job named "test-job-b-2" in the current scope
        When I query for the job "test-job-a-1" in account "account-a"
        Then There is such a job
        When I query for the job "test-job-b-1" in account "account-b"
        Then There is such a job
        Given I select account "account-a"
        When I try to delete account "account-a"
        And I wait 10 seconds
        And I query for the job "test-job-a-2" in the last account
        Then There is no such job
        When I query for the job "test-job-b-1" in account "account-b"
        Then There is such a job

    Scenario: User is deleted, credentials have to be deleted too
        Remove user and his credentials has to be deleted by Security service
        listening on user service events.

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User A
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-a | Kapua User A | kapua_a@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-a | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |

        Given I select account "account-a"
        Given I select user "kapua-a"
        When I try to delete user "kapua-a"
        And I wait 1 second
        Then I don't find user credentials
        And I logout

    Scenario: Job is deleted, the job schedules must be deleted too

        Given I login as user with name "kapua-sys" and password "kapua-password"
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
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
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User A
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-a | Kapua User A | kapua_a@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-a | ToManySecrets123# | true    |
        Given Account
            | name      | scopeId |
            | account-b | 1       |
        And I configure the account service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the user service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the tag service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And I configure the job service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |

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
