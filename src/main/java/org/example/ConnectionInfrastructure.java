package org.example;

public class ConnectionInfrastructure {
    public String getConnectionKind(){
        return this.getClass().getName();
    }
}
