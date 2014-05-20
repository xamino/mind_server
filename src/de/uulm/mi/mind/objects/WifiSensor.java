package de.uulm.mi.mind.objects;

import de.uulm.mi.mind.objects.Interfaces.Saveable;
import de.uulm.mi.mind.objects.Interfaces.Sendable;
import de.uulm.mi.mind.security.Authenticated;

import java.util.Date;

/**
 * @author Tamino Hartmann
 *         This class implements the user object for the wifi-sniffing program. It authenticates the sensors.
 */
public class WifiSensor implements Authenticated, Sendable, Saveable {

    /**
     * Unique string that identifies this WifiSensor.
     */
    private String identification;
    /**
     * The Area ID of the area it is responsible for.
     */
    private String area;
    /**
     * The token with which the WifiSensor authenticates itself to the server.
     */
    private String tokenHash;
    /**
     * Last time the sensor logged in.
     */
    private Date lastAccess;

    private WifiSensor() {
    }

    public WifiSensor(String identification, String tokenHash, String area) {
        this.identification = identification;
        this.area = area;
        this.tokenHash = tokenHash;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    @Override
    public String readIdentification() {
        return this.identification;
    }

    @Override
    public String readAuthentication() {
        return this.tokenHash;
    }

    @Override
    public String toString() {
        return "WifiSensor{" +
                "identification='" + identification + '\'' +
                ", area='" + area + '\'' +
                ", lastAccess=" + lastAccess +
                '}';
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
        return this.identification;
    }
}
