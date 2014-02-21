package logic;

import database.Data;

/**
 * Created by tamino on 2/19/14.
 */
public interface Module {

    public Data run(String task, Data request);

}
