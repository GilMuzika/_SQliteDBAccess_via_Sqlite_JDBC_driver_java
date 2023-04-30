package org.example;

import java.sql.Connection;

public interface IConnectionPool {

    Connection getConnection() throws InterruptedException;

    void returnConnection(Connection aaa);

    void close();

}
