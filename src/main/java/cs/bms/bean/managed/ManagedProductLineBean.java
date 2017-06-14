/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.ProductLine;
import cs.bms.service.interfac.IProductLineService;
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
public class ManagedProductLineBean extends AManagedBean<ProductLine, IProductLineService> implements ILoadable {

    @ManagedProperty(value = "#{productLineService}")
    private IProductLineService mainService;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;

    private String name;

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
        boolean valid = mainService.getByHQL("SELECT 1 FROM ProductLine pl WHERE pl.name LIKE ? AND pl.id <> ?", name.trim().toUpperCase(), selected.getId() == null ? -1 : selected.getId()) == null;
        if (!valid) {
            new PNotifyMessage("ERROR", "El nombre ya ha sido registrado", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated", 3000L).execute();
            return false;
        } else {
            String content = getSelected().getId() != null ? "Se ha actualizado un rubro" : "Se ha creado un rubro";
            super.save(); //To change body of generated methods, choose Tools | Templates.
            new PNotifyMessage("Datos guardados!!", "Se ha guardado los datos del producto", PNotifyMessage.Type.Success, "fa fa-save shaked animated", 3000L).execute();
            return true;
        }
    }

    @Override
    protected void fillFields() {
        name = selected.getName();
        onLoad(true);
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setName(name.trim().toUpperCase());
        selected.setPath(name.trim().toUpperCase());
      
        Auditory.make(selected, sessionBean.getCurrentUser());
    }

    /**
     * @return the mainService
     */
    public IProductLineService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IProductLineService mainService) {
        this.mainService = mainService;
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
}
