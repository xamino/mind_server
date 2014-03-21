package de.uulm.mi.mind.objects.messages;

/**
 * Created by Cassio on 21.02.14.
 */
public class Success extends Information {


    public Success(String name, String description) {
        super(name, description);
    }

    @Override
    public String toString() {
        return "Success{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}