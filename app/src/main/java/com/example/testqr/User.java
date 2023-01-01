package com.example.testqr;

public class User {
    public String name, id, email, confirm,hasused;

    public User(){
    }


    public User(String name, String id, String email, String confirm, String hasused) {
        this.name=name;
        this.id=id;
        this.email=email;
        this.confirm=confirm;
        this.hasused=hasused;

    }
}
