package com.example.wille.tostiapp;

public class Order {
    private String name;
    private String uid;
    private int amount;
    private boolean withHam;
    private boolean withCheese;
    private boolean ready = false;
    private boolean received = false;

    public Order (String name, String uid, int amount, boolean withHam, boolean withCheese) {
        this.name = name;
        this.uid = uid;
        this.amount = amount;
        this.withHam = withHam;
        this.withCheese = withCheese;
        assert(withHam || withCheese);
    }

    public Order () {} // necessary for FireBase

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public int getAmount() {
        return amount;
    }

    public boolean getWithHam() {
        return withHam;
    }

    public boolean getWithCheese() {
        return withCheese;
    }

    public boolean getReady() {
        return ready;
    }

    public boolean getReceived() {
        return received;
    }
}
