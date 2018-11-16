package com.slightsite.app.domain.params;

import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.params.ParamDao;

/**
 * This class is service locater for Params.
 *
 * @author Farid Efendi
 *
 */
public class ParamService {

    private ParamCatalog paramCatalog;
    private static ParamService instance = null;
    private static ParamDao paramDao = null;

    /**
     * Constructs Data Access Object of inventory.
     * @throws NoDaoSetException if DAO is not exist.
     */
    private ParamService() throws NoDaoSetException {
        if (!isDaoSet()) {
            throw new NoDaoSetException();
        }

        paramCatalog = new ParamCatalog(paramDao);
    }

    /**
     * Determines whether the DAO already set or not.
     * @return true if the DAO already set; otherwise false.
     */
    public static boolean isDaoSet() {
        return paramDao != null;
    }

    /**
     * Sets the database connector.
     * @param dao Data Access Object of inventory.
     */
    public static void setParamDao(ParamDao dao) {
        paramDao = dao;
    }

    /**
     * Returns param catalog using in this inventory.
     * @return param catalog using in this inventory.
     */
    public ParamCatalog getParamCatalog() {
        return paramCatalog;
    }

    /**
     * Returns the instance of this singleton class.
     * @return instance of this class.
     * @throws NoDaoSetException if DAO was not set.
     */
    public static ParamService getInstance() throws NoDaoSetException {
        if (instance == null)
            instance = new ParamService();
        return instance;
    }
}


