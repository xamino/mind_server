package de.uulm.mi.mind.objects;

import java.util.Date;

/**
 * Created by tamino on 2/19/14.
 * <p/>
 * zB placeholder user class
 */
public class User implements Data, Authenticated {
    private String name;
    private String pwdHash;
    private String email;
    private boolean admin;
    private Date lastAccess;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                // ", pwdHash='" + pwdHash + '\'' +
                ", email='" + email + '\'' +
                ", admin=" + admin +
                '}';
    }

    /**
     * Method for getting a copy of this user object without the sensitive information.
     *
     * @return Copy of this object with empty password string.
     */
    public User safeClone() {
        User user = new User(this.name, this.email);
        user.setAdmin(this.admin);
        user.setPwdHash("");
        return user;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    @Override
    public String readIdentification() {
        return email;
    }

    @Override
    public String readAuthentication() {
        return pwdHash;
    }

    @Override
    public String getKey() {
        return email;
    }
}
