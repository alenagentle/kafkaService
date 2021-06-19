package com.irlix.Server.models;

public class UserRegistrationModel {
    private String login;
    private String password;
    private String email;
    private String fio;

    public UserRegistrationModel() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFio() { return fio; }

    public void setFio(String fio) { this.fio = fio; }
}
