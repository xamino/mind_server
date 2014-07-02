package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.objects.enums.Status;
import de.uulm.mi.mind.security.Authenticated;

import java.util.Date;

/**
 * Created by tamino on 2/19/14.
 * <p/>
 * zB placeholder user class
 */
public class User implements Sendable, Saveable, Authenticated {
    private String name;
    private String email;
    private String position;
    /**
     * Controls whether a user has administrative access.
     */
    private boolean admin;
    /**
     * Controls whether a user may be logged anonymously in the FileLog.
     */
    private boolean log;
    private Status status;
    private String pwdHash;
    private Date lastAccess;
    private String unique;

    private User() {
    }

    public User(String email) {
        this.email = email;
    }

    public User(String email, String name, boolean admin) {
        this.email = email;
        this.name = name;
        this.admin = admin;
    }

    public User(String email, String name) {
        this(email, name, false);
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    /* public String getPosition() {
        return position;
    }*/

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User safeClone() {
        User back = new User(this.email);
        back.setAccessDate(this.lastAccess);
        back.setPwdHash("");
        back.setAdmin(this.isAdmin());
        back.setLog(this.isLog());
        back.setName(this.name);
        back.setStatus(this.status);
        back.setUnique(this.unique);
        return back;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", position='" + position + '\'' +
                ", admin=" + admin +
                ", status=" + status +
                ", lastAccess=" + lastAccess +
                '}';
    }

    @Override
    public String readIdentification() {
        return this.email;
    }

    @Override
    public String readAuthentication() {
        return this.pwdHash;
    }

    @Override
    public Date getAccessDate() {
        return this.lastAccess;
    }

    @Override
    public void setAccessDate(Date accessDate) {
        this.lastAccess = accessDate;
    }

    @Override
    public String getKey() {
        return this.email;
    }

    @Override
    public Saveable deepClone() {
        User user = new User();
        user.setAccessDate(lastAccess);
        user.setPwdHash(pwdHash);
        user.setAdmin(admin);
        user.setLog(log);
        user.setEmail(email);
        user.setName(name);
        user.setPosition(position);
        user.setStatus(status);
        user.setUnique(unique);
        return user;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }
}
