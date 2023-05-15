package org.example;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {


    static int maxConnectionsInPool = 5;
    static int numberOfThreads = 50;

    private static ArrayList<Thread> _allThreads = new ArrayList<>();

    public static String getDataBasePathFromResources(String fileName) throws URISyntaxException {
        URL fileUrl = Main.class.getResource("/" + fileName);
        return Paths.get(fileUrl.toURI()).toString();
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, URISyntaxException, InterruptedException {

        
        
        String absolutePathToTheDatabase  = getDataBasePathFromResources("mySql_database.db");
        SQLiteConnectionInfrastructure sQLiteConnectionInfrastructure = new SQLiteConnectionInfrastructure(absolutePathToTheDatabase);

        PostgersConnectionInfrastructure postgreSQLConnectionInfrastructure = new PostgersConnectionInfrastructure("127.0.0.1", "postgres_test_db", "1234", "postgres", "1978");

        Scanner scanner = new Scanner(System.in);
        System.out.println("If you want to operate with MySQL database, press 1,\n if you want to operate with PostgreSQL database, press 2");
        String userInput = "something";
        do {
            userInput = scanner.next();
            if(!userInput.equals("1") && !userInput.equals("2"))
                System.out.println("Press press either 1 or 2");
        }
        while (!userInput.equals("1") && !userInput.equals("2"));

        switch (userInput) {
            case "1":
                ConnectionPool.set_connectionInfrastructure(sQLiteConnectionInfrastructure);
                break;
            case "2":
                ConnectionPool.set_connectionInfrastructure(postgreSQLConnectionInfrastructure);
                break;
        }
        
        ConnectionPool.set_maxConnectionsNumber(maxConnectionsInPool);
        ConnectionPool pool = ConnectionPool.getInstance();






        System.out.println("Hello world!");
        CustomerGenerator cg = new CustomerGenerator();

        Runnable r = () -> {
            MySQLDBAccess<Customer> dba = null;
            try {
                Connection connection = pool.getConnection();
                dba = new MySQLDBAccess<>(connection, Customer.class);
                dba.insert(cg.getRandomCustomer());



            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (SQLException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            finally {
                pool.returnConnection(dba.returnConnection());
            }
        };

        for(int i = 0; i < numberOfThreads; i++) {
            Thread t = new Thread(r, "Thread " + (i+1));
            _allThreads.add(t);
            t.start();
        }
        _allThreads.forEach(x -> {
            try {
                x.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });




        MySQLDBAccess<Customer> dba = new MySQLDBAccess<>(pool.getConnection(), Customer.class);
        ArrayList<Customer> allCustomers = dba.getAll();

        for(Customer c : allCustomers) {
            System.out.println(c);
        }


    }
}