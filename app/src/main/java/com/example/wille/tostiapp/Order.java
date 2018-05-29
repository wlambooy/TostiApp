package com.example.wille.tostiapp;

public class Order {
    private String name;
    private String uid;
    private int amount;
    private boolean withHam;
    private boolean withCheese;

    public Order (String name, String uid, int amount, boolean withHam, boolean withCheese) {
        this.name = name;
        this.uid = uid;
        this.amount = amount;
        this.withHam = withHam;
        this.withCheese = withCheese;
        assert(withHam || withCheese);
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isWithHam() {
        return withHam;
    }

    public boolean isWithCheese() {
        return withCheese;
    }
}
