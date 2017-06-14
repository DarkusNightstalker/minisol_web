/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.StockType;
import cs.bms.service.interfac.IStockTypeService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedStockTypeBean extends AManagedBean<StockType, IStockTypeService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;
    @ManagedProperty(value = "#{stockTypeService}")
    private IStockTypeService mainService;

    private String name;
    private String code;


    @PostConstruct
    public void init() {
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
    }

    @Override
    public boolean save() {
        boolean valid = mainService.getByHQL("SELECT 1 FROM StockType st WHERE st.code LIKE ? and st.id <> ?",code.trim(),selected.getId() == null ? -1 : selected.getId()) == null;
        if (! valid) {
            new PNotifyMessage("ERROR", "El codigo \"" + code + "\" ya esta registrado", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated", 3000L).execute();
            return false;
        } else {
            //String content = getSelected().getId() != null ? "Se ha actualizado un rubro" : "Se ha creado un rubro";
            super.save(); //To change body of generated methods, choose Tools | Templates.
            new PNotifyMessage("Datos guardados!!", "Se ha guardado los datos de la unidad de medida", PNotifyMessage.Type.Success, "fa fa-save shaked animated", 3000L).execute();
            return true;
        }
    }

    @Override
    protected void fillFields() {
        name = selected.getName();
        code = selected.getCode();
        onLoad(true);
        createAgain = selected.getId() == null;
    }


    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setCode(code.trim());
        selected.setName(name.trim());
        Auditory.make(selected, sessionBean.getCurrentUser());
    }

    /**
     * @return the sessionBean
     */
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    /**
     * @return the navigationBean
     */
    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    /**
     * @param navigationBean the navigationBean to set
     */
    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }



    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the mainService
     */
    public IStockTypeService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IStockTypeService mainService) {
        this.mainService = mainService;
    }

}
