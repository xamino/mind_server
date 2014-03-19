package database.objects;

import database.Authenticated;
import database.Data;

/**
 * Public display user object.
 */
public class PublicDisplay implements Data, Authenticated {

    private String location;
    /**
     * Primary key of PublicDisplay!
     */
    private String identification;
    /**
     * Aka password.
     */
    private String token;
    private int coordinateX;
    private int coordinateY;

    public PublicDisplay(String location, String identification, String token, int coordinateX, int coordinateY) {
        this.location = location;
        this.identification = identification;
        this.token = token;
        this.coordinateX = coordinateX;
        this.coordinateY = coordinateY;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public int getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(int coordinateX) {
        this.coordinateX = coordinateX;
    }

    public int getCoordinateY() {

        return coordinateY;
    }

    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
    }

    @Override
    public String readIdentification() {
        return identification;
    }

    @Override
    public String readAuthentication() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "PublicDisplay{" +
                "location='" + location + '\'' +
                ", identification='" + identification + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
