package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnectionInfrastructure extends ConnectionInfrastructure implements IConnectionInfrastructure {
    private String _pathToDb;


    public SQLiteConnectionInfrastructure(String pathToDb) {
        _pathToDb = pathToDb;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Connection getConnection() throws SQLException {
            return DriverManager.getConnection("jdbc:sqlite:" + _pathToDb);
    }

    @Override
    public String getConnectionKind() {
        return super.getConnectionKind();
    }


}
