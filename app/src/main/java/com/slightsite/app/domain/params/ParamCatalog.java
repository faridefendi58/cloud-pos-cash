package com.slightsite.app.domain.params;

import java.util.List;

import com.slightsite.app.techicalservices.params.ParamDao;

/**
 *
 * @author Farid Efendi
 *
 */
public class ParamCatalog {

    private ParamDao paramDao;

    /**
     * Constructs Data Access Object of inventory in ParamCatalog.
     * @param paramDao DAO of param.
     */
    public ParamCatalog(ParamDao paramDao) {
        this.paramDao = paramDao;
    }

    /**
     *
     * @param name
     * @param value
     * @param type
     * @param description
     * @return
     */
    public boolean addParam(String name, String value, String type, String description) {
        Params param = new Params(name, value, type, description);
        int id = paramDao.addParam(param);
        return id != -1;
    }

    /**
     * Edit Parameter
     * @param param
     * @return
     */
    public boolean editParam(Params param) {
        boolean respond = paramDao.editParam(param);
        return respond;
    }

    /**
     * Geting parameter by name
     * @param name
     * @return
     */
    public Params getParamByName(String name) {
        return paramDao.getParamByName(name);
    }

    /**
     * Geting parameter by id
     * @param id
     * @return
     */
    public Params getParamById(int id) {
        return paramDao.getParamById(id);
    }

    /**
     * Geting all parameter
     * @return
     */
    public List<Params> getAllParams() {
        return paramDao.getAllParams();
    }

    /**
     * Searching for some parameter
     * @param search
     * @return
     */
    public List<Params> searchParam(String search) {
        return paramDao.searchParam(search);
    }

    /**
     * Clears ParamCatalog.
     */
    public void clearParamCatalog() {
        paramDao.clearParamCatalog();
    }

    /**
     * Suspend or dective param
     * @param param
     */
    public void suspendParam(Params param) {
        paramDao.suspendParam(param);
    }

}


