package com.irlix.Server.entities;

import javax.persistence.*;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String login;
    @Column
    private String password;
    @Column
    private String email;
    @Column
    private String fio;

    public User(){}

    public User(String login, String password, String email, String fio){
        this.login = login;
        this.password = password;
        this.email = email;
        this.fio = fio;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getFio() {
        return fio;
    }


}
