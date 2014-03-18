package database.objects;

/**
 * Public display user object.
 */
public class PublicDisplay {

    private String location, identification;
    private int coordinateX;
    private int coordinateY;

    public PublicDisplay(String location, String identification, int coordinateX, int coordinateY) {
        this.location = location;
        this.identification = identification;
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
}
