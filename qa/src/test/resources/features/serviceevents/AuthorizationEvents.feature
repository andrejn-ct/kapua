###############################################################################
# Copyright (c) 2018 Eurotech and/or its affiliates and others
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     Eurotech
###############################################################################
@serviceevents
Feature: Authorization Service

    Background:
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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |
        And I wait for 5 seconds

    Scenario: Start event broker for all scenarios

        Given Start Event Broker

    Scenario: Delete a user and all the access info items for this user have to be deleted too
    Provide a number of accounts and users. Create a number of access info items for the users in the accounts.
    After deleting a user, all the user access info items must be deleted too.
    Access info items for other users must remain.

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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |
        And I wait for 5 seconds

        Given I create an empty access info entity for user "kapua-a" in scope "account-a"
        And I create an empty access info entity for user "kapua-a" in scope "account-a"
        And I create an empty access info entity for user "kapua-a" in scope "account-a"
        And I create an empty access info entity for user "kapua-a" in scope "account-a"
        Given I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        Given I select account "account-a"
        Given A generic user
            | name      | displayName    | email             | phoneNumber     | status  | userType |
            | kapua-a-2 | Kapua User A 2 | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        Given I create an empty access info entity for user "kapua-a-2" in scope "account-a"
        Given I create an empty access info entity for user "kapua-a-2" in scope "account-a"
        Given I create an empty access info entity for user "kapua-a-2" in scope "account-a"
        When I query for the access info entities in scope "account-a"
        Then There are exactly 7 access info items
        When I query for the access info entities in scope "account-b"
        Then There are exactly 5 access info items
        When I query for the access info entities for the user "kapua-a"
        Then There are exactly 4 access info items
        When I query for the access info entities for the user "kapua-a-2"
        Then There are exactly 3 access info items
        When I query for the access info entities for the user "kapua-b"
        Then There are exactly 5 access info items
        # Up to now just the test setup
        Given I select user "kapua-a"
        When I try to delete user "kapua-a"
        Then I don't find user "kapua-a"
        And I wait for 10 seconds
        When I query for the access info entities for the last user
        Then I find no such access info item
        When I query for the access info entities for the user "kapua-a-2"
        Then There are exactly 3 access info items
        When I query for the access info entities for the user "kapua-b"
        Then There are exactly 5 access info items
        Given I select user "kapua-b"
        When I try to delete user "kapua-b"
        Then I don't find user "kapua-b"
        And I wait for 10 seconds
        When I query for the access info entities for the last user
        Then I find no such access info item
        When I query for the access info entities for the user "kapua-a-2"
        Then There are exactly 3 access info items
        Given I select user "kapua-a-2"
        When I try to delete user "kapua-a-2"
        Then I don't find user "kapua-a-2"
        And I wait for 10 seconds
        When I query for the access info entities for the last user
        Then I find no such access info item

    Scenario: Delete an account and all roles are deleted too
        Provide a number of accounts. Create a number of roles for each account. When an account
        is deleted, all the account roles must be deleted too.
        Roles for other accounts must remain.

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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |
        And I wait for 5 seconds

        Given The following roles in scope "account-a"
        |name      |actions              |
        |role-a-1  |write, read, connect |
        |role-a-2  |write, read, connect |
        |role-a-3  |write, read, connect |
        And The following roles in scope "account-b"
        |name      |actions              |
        |role-b-1  |write, read, connect |
        |role-b-2  |write, read, connect |
        |role-b-3  |write, read, connect |
        When I query for the role "role-a-1" in account "account-a"
        Then I find such a role
        When I query for the role "role-b-2" in account "account-b"
        Then I find such a role
        Given I select account "account-a"
        When I try to delete account "account-a"
        And I wait for 10 seconds
        When I query for the role "role-a-1" in the last account
        Then I find no such role
        When I query for the role "role-b-2" in account "account-b"
        Then I find such a role

    Scenario: Delete an account and all the account groups are deleted too
        Provide a number of accounts. Create a number of groups for each account. When an account
        is deleted, all the account groups must be deleted too.
        Groups for other accounts must remain.

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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |
        And I wait for 5 seconds

        Given I select account "account-a"
        And The following groups in scope "account-a"
        |name        |
        |group-a-1   |
        |group-a-2   |
        |group-a-3   |
        And The following groups in scope "account-b"
        |name        |
        |group-b-1   |
        |group-b-2   |
        |group-b-3   |
        When I query for the group "group-b-2" in account "account-b"
        Then I find such a group
        Given I select account "account-b"
        When I try to delete account "account-b"
        And I wait for 10 seconds
        When I query for the group "group-b-1" in the last account
        Then I find no such group
        When I query for the group "group-a-1" in account "account-a"
        Then I find such a group

    Scenario: Delete a group and all relevant devices will be removed from that group
    Provide a number of accounts, users, groups and devices. Assign the devices to the various groups. When
    a group is deleted, all the devices that are assigned to that group must be removed from that group.
    All other devices should remain unaffected.

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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |
        And I wait for 5 seconds

        Given The following groups in scope "account-a"
            |name        |
            |group-a-1   |
            |group-a-2   |
            |group-a-3   |
        When I query for the group "group-a-2" in account "account-a"
        Then I find such a group
        Given I select account "account-a"
        And I select user "kapua-a"
        Given A birth message from device "device-a-1"
        Given A birth message from device "device-a-2"
        Given A birth message from device "device-a-3"
        Given A birth message from device "device-b-1"
        Given A birth message from device "device-b-2"
        Given A birth message from device "device-b-3"
        Given A birth message from device "device-c-1"
        Given A birth message from device "device-c-2"
        Given A birth message from device "device-c-3"
        When I search for the device "device-a-1" in account "account-a"
        Then I find 1 device
        Given Device "device-a-1" is assigned to group "group-a-1"
        And Device "device-a-2" is assigned to group "group-a-1"
        And Device "device-a-3" is assigned to group "group-a-1"
        And Device "device-b-1" is assigned to group "group-a-2"
        And Device "device-b-2" is assigned to group "group-a-2"
        And Device "device-b-3" is assigned to group "group-a-2"
        And Device "device-c-1" is assigned to group "group-a-3"
        And Device "device-c-2" is assigned to group "group-a-3"
        And Device "device-c-3" is assigned to group "group-a-3"
        When I query for all devices in group "group-a-1"
        Then I find 3 devices
        When I query for all devices in group "group-a-2"
        Then I find 3 devices
        And Device "device-b-1" belongs to group "group-a-2"
        When I query for groups in account "account-a"
        Then There are exactly 3 groups
        When I delete the group "group-a-1"
        And I query for groups in account "account-a"
        Then There are exactly 2 groups
        Given I wait for 10 seconds
        Then Device "device-a-1" does not belong to any group
        And Device "device-a-2" does not belong to any group
        And Device "device-a-3" does not belong to any group
        And Device "device-b-1" belongs to group "group-a-2"

    Scenario: Delete an account and all the access info items for this account have to be deleted too
    Provide a number of accounts and users. Create a number of access info items for the users in the accounts.
    After deleting an account, all the account access info items must be deleted too.
    Access info items for other accounts must remain.

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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
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
        And I configure the role service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the group service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 5     |
        And I configure the device service
            | type    | name                   | value |
            | boolean | infiniteChildEntities  | true  |
            | integer | maxNumberChildEntities | 0     |
        And User B
            | name    | displayName  | email             | phoneNumber     | status  | userType |
            | kapua-b | Kapua User B | kapua_b@kapua.com | +386 31 323 444 | ENABLED | INTERNAL |
        And Credentials
            | name    | password          | enabled |
            | kapua-b | ToManySecrets123# | true    |
        And I wait for 5 seconds

        Given I create an empty access info entity for user "kapua-a" in scope "account-a"
        And I create an empty access info entity for user "kapua-a" in scope "account-a"
        And I create an empty access info entity for user "kapua-a" in scope "account-a"
        And I create an empty access info entity for user "kapua-a" in scope "account-a"
        Given I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        And I create an empty access info entity for user "kapua-b" in scope "account-b"
        When I query for the access info entities in scope "account-a"
        Then There are exactly 4 access info items
        When I query for the access info entities in scope "account-b"
        Then There are exactly 5 access info items
        Given I select account "account-a"
        When I try to delete account "account-a"
        And I wait for 10 seconds
        When I query for the access info entities in the last scope
        Then I find no such access info item
        When I query for the access info entities in scope "account-b"
        Then There are exactly 5 access info items
        Given I select account "account-b"
        When I try to delete account "account-b"
        And I wait for 10 seconds
        When I query for the access info entities in the last scope
        Then I find no such access info item

    Scenario: Stop event broker for all scenarios

        Given Stop Event Broker
