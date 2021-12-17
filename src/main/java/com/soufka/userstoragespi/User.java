package com.soufka.userstoragespi;

public class User {
    private String accountUUID;
    private String employeeUUID;
    private String login;
    private String password;
    private String role;
    private String active;

    public User(){}

    public User(String name, String password) {
        this.password = password;
    }

    public User(String password, String id, String employeeId, String login, String role, String active) {
        this.password = password;
        this.accountUUID = id;
        this.employeeUUID = employeeId;
        this.login = login;
        this.role = role;
        this.active = active;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountUUID() {
        return accountUUID;
    }

    public void setAccountUUID(String accountUUID) {
        this.accountUUID = accountUUID;
    }

    public String getEmployeeUUID() {
        return employeeUUID;
    }

    public void setEmployeeUUID(String employeeUUID) {
        this.employeeUUID = employeeUUID;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
