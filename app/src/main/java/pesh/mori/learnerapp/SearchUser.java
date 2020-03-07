package pesh.mori.learnerapp;

/**
 * Created by Nick Otto on 10/04/2019.
 */

public class SearchUser {
    private String name,email;

    public SearchUser() {
    }

    public SearchUser(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
