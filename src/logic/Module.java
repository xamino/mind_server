package logic;

import database.Data;
import database.objects.Task;

/**
 * Created by tamino on 2/19/14.
 */
public interface Module {

    public Data run(Task task, Data request);

}
