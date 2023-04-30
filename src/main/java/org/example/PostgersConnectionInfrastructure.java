package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgersConnectionInfrastructure extends ConnectionInfrastructure implements IConnectionInfrastructure {

    private String _url;
    private Properties _props;


    public PostgersConnectionInfrastructure(String host, String database, String port, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        _url = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        _props = new Properties();
        _props.setProperty("user", user);
        _props.setProperty("password", password);


    }
    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(_url, _props);
    }

    @Override
    public String getConnectionKind() {
        return super.getConnectionKind();
    }


}
