package org.example;

import auxiliaryClasses.Function1;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class CustomerGenerator {

    private Random _rnd = new Random();
    private ArrayList<String> _names;
    private ArrayList<String> _addresses;
    private ArrayList<String> _emails;

    private  String getPathFromResources(String fileName) throws URISyntaxException {
        URL fileUrl = Main.class.getResource("/" + fileName);
        return Paths.get(fileUrl.toURI()).toString();
    }

    public CustomerGenerator() {
        try {
            _names = preload(getPathFromResources("names.txt"));
            _addresses = preload(getPathFromResources("addresses.txt"));
            _emails = preload(getPathFromResources("100,000 USA Email Address.TXT"));

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    private ArrayList<String> preload(String absolutePath) {
        ArrayList<String> list = new ArrayList<>();
        String path = null;
        Scanner sc = null;
        try {
            File namesFile = new File(absolutePath);
            sc = new Scanner(namesFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (sc.hasNextLine()) {
            list.add(sc.nextLine());
        }
        return list;
    }
    private Function1<ArrayList<String>,String> getRandomStringOfList = (list) -> list.get(_rnd.nextInt(0, list.size()-1));
    public Customer getRandomCustomer() {
        //public Customer(int id, String firstname, String lastname, Float weight, String address, String email){
        int id = _rnd.nextInt(1, 10000);
        String firstName = null;
        String lastName = null;
        try {
            firstName = getRandomStringOfList.func(_names);
            lastName = getRandomStringOfList.func(_names);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        Float weigh = _rnd.nextFloat(10f, 100f);
        String address = getRandomStringOfList.func(_addresses);
        String email = getRandomStringOfList.func(_emails);
        return new Customer(id, firstName, lastName, weigh, address, email);
    }

}
