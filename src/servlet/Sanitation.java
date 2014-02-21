package servlet;

/**
 * @author Tamino Hartmann
 */
public class Sanitation {
    private static Sanitation INSTANCE;

    public static Sanitation getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Sanitation();
        return INSTANCE;
    }

    private Sanitation() {
        
    }
}
