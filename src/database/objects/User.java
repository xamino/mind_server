package database.objects;

import database.Data;

/**
 * Created by tamino on 2/19/14.
 * <p/>
 * zB placeholder user class
 */
public class User implements Data {
    private String name;
    private String pwdHash;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }
}
