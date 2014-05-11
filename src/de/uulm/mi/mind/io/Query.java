package de.uulm.mi.mind.io;

import de.uulm.mi.mind.objects.Data;
import de.uulm.mi.mind.objects.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Cassio on 08.05.2014.
 */
public class Query {

    private String table;
    private HashMap<Object, Object> conditionMap = new HashMap<>();

    public Query() {

    }

    public Query descendConstrain(Object o, Object c) {
        if (c == null || c == 0 || c == 0.0)
            return this; // Removes condition allowing all matching results in these cases TODO does this wrong?
        conditionMap.put(o, c);
        return this;
    }

    public <E extends Data> List<E> execute() {
        List<E> list = new ArrayList<>();
        try {
            Connection connection = DatabaseControllerSQL.getInstance().createConnection();

            String query = "SELECT * FROM " + table;

            if (conditionMap.size() > 0) {
                HashMap<Object, Object> conditionMap = new HashMap<>();

                query += " WHERE ";
                for (Map.Entry<Object, Object> objectObjectEntry : conditionMap.entrySet()) {
                    query += objectObjectEntry.getKey() + " = " + objectObjectEntry.getValue() + " AND ";
                }
                query += "is TRUE";
            }

            System.out.println(query);

            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                if (table.equals("User")) {
                    String email = rs.getString("email");
                    String name = rs.getString("name");
                    String password = rs.getString("pwdHash");
                    boolean admin = rs.getBoolean("admin");
                    //Enum status
                    // lastaccess
                    User u = new User(email, name, admin);
                    u.setPwdHash(password);
                    list.add((E) u); //TODO problem?
                }
            }
            rs.close();
            connection.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public void constrain(Class<? extends Data> aClass) {
        table = aClass.getSimpleName();
    }
}
