/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.service.liquibase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.eclipse.kapua.test.junit.JUnitTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(JUnitTests.class)
public class KapuaLiquibaseClientTest {

    private Connection connection;

    @Before
    public void start() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "");
    }

    @After
    public void stop() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void shouldCreateTable() throws Exception {
        // Given
        System.setProperty("LIQUIBASE_ENABLED", "true");

        // When
        new KapuaLiquibaseClient("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "").update();

        // Then
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "");
        ResultSet sqlResults = connection.prepareStatement("SHOW TABLES").executeQuery();
        List<String> tables = new LinkedList<>();
        while (sqlResults.next()) {
            tables.add(sqlResults.getString(1));
        }
        Assertions.assertThat(tables).contains("tst_liquibase");
    }

    @Test
    public void shouldCreateTableOnlyOnce() throws Exception {
        // Given
        System.setProperty("LIQUIBASE_ENABLED", "true");

        // When
        new KapuaLiquibaseClient("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "").update();
        new KapuaLiquibaseClient("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "").update();

        // Then
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "");
        ResultSet sqlResults = connection.prepareStatement("SHOW TABLES").executeQuery();
        List<String> tables = new LinkedList<>();
        while (sqlResults.next()) {
            tables.add(sqlResults.getString(1));
        }
        Assertions.assertThat(tables).contains("tst_liquibase");
    }

    @Test
    public void shouldSkipDatabaseUpdate() throws Exception {
        // Given
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "");
        connection.prepareStatement("DROP TABLE IF EXISTS DATABASECHANGELOG").execute();
        System.setProperty("LIQUIBASE_ENABLED", "false");

        // When
        new KapuaLiquibaseClient("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "").update();

        // Then
        ResultSet sqlResults = connection.prepareStatement("SHOW TABLES").executeQuery();
        Assertions.assertThat(sqlResults.next()).isFalse();
    }

    @Test(expected = Exception.class)
    public void shouldCreateAttempToUseCustomSchema() throws Exception {
        // Given
        System.setProperty("LIQUIBASE_ENABLED", "true");

        // When
        try {
            new KapuaLiquibaseClient("jdbc:h2:mem:kapua;MODE=MySQL;DB_CLOSE_DELAY=-1", "", "", Optional.of("foo")).update();
        } catch (Exception e) {
            // Then
            Assertions.assertThat(e).hasMessageContaining("Schema \"FOO\" not found");
            throw e;
        }
    }

}
