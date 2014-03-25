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
    private String lastPosition;
    private boolean admin;
    private Date lastAccess;

    public User(String email) {
        this.email = email;
    }

    public User(String email, String name) {
        this(email);
        this.name = name;
    }

    public User(String email, String name, boolean admin) {
        this(email, name);
        this.admin = admin;
    }

    public String getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(String lastPosition) {
        this.lastPosition = lastPosition;
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
        User user = new User(this.email, this.name, this.admin);
        user.setPwdHash("");
        return user;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
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
