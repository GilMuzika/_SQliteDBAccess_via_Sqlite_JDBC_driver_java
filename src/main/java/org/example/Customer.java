package org.example;

public class Customer {
    //@DBFieldTableName(table_name =  "customer")
    //public static String TABLE_NAME = "customer";

    public Customer() {
    }

    public Customer(int id, String firstname, String lastname, Float weight, String address, String email){
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.weight = weight;
        this.address = address;
        this.email = email;
    }


    @DBField(column_name = "id", isPrimaryKey = true, type = int.class)
    public int id;

    @DBField(column_name = "firstname", type = String.class)
    public String firstname;

    //@DBField(column_name = "lastname", type = String.class)
    public String lastname;

    @DBField(column_name = "weight", type = Float.class)
    public Float weight;

    //@DBField(column_name = "address", type = String.class)
    public String address;

    @DBField(column_name = "email", type = String.class)
    public String email;

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", weight=" + weight +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
