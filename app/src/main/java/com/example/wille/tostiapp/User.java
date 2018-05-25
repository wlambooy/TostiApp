package com.example.wille.tostiapp;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private boolean isAdmin;
    private String email;
    private String UID;
    private String name;
    private double saldo;

    public User (boolean isAdmin, String email, String UID, String name, double saldo) {
        this.isAdmin = isAdmin;
        this.email = email;
        this.UID = UID;
        this.name = name;
        this.saldo = saldo;
    }

    protected User(Parcel in) {
        isAdmin = in.readByte() != 0;
        email = in.readString();
        UID = in.readString();
        name = in.readString();
        saldo = in.readDouble();
    }

    public User() { }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isAdmin ? 1 : 0));
        dest.writeString(email);
        dest.writeString(UID);
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

    public String getUID() {
        return UID;
    }

    public double getSaldo() {
        return saldo;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
