package com.example.wille.tostiapp;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private boolean admin;
    private String email;
    private String uid;
    private String name;
    private double saldo;

    public User (boolean isAdmin, String email, String uid, String name, double saldo) {
        this.admin = isAdmin;
        this.email = email;
        this.uid = uid;
        this.name = name;
        this.saldo = saldo;
    }

    protected User(Parcel in) {
        admin = in.readByte() != 0;
        email = in.readString();
        uid = in.readString();
        name = in.readString();
        saldo = in.readDouble();
    }

    public User() { }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (admin ? 1 : 0));
        dest.writeString(email);
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeDouble(saldo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public double getSaldo() {
        return saldo;
    }

    public boolean getAdmin() {
        return admin;
    }
}
