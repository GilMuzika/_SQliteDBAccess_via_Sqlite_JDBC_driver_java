package org.example;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnectionInfrastructure {
    //This method must return a new Connection object every time called
    Connection getConnection() throws SQLException;
    String getConnectionKind();
}
