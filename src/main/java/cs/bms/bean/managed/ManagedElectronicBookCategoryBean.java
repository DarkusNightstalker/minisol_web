/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.ElectronicBookCategory;
import cs.bms.service.interfac.IElectronicBookCategoryService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.util.List;
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
public class ManagedElectronicBookCategoryBean extends AManagedBean<ElectronicBookCategory, IElectronicBookCategoryService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{electronicBookCategoryService}")
    private IElectronicBookCategoryService mainService;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;

    private ParentSearcher parentSearcher;
    
    private Short parentId;
    private String code;
    private String name;
    
    @PostConstruct
    public void init(){
        parentSearcher = new ParentSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        parentSearcher.update();
    }

    @Override
    public boolean save() {
        if (mainService.existCode(code.trim(), selected.getId())) {
            new PNotifyMessage("ERROR", "El codigo \"" + code + "\" ya esta registrado", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated", 3000L).execute();
            return false;
        } else {
            try {
                String content = getSelected().getId() != null ? "Se ha actualizado una venta" : "Se ha registrado una venta";
                super.save(); //To change body of generated methods, choose Tools | Templates.
                new PNotifyMessage("Datos guardadados", content, PNotifyMessage.Type.Success, "fa fa-save shake animated", 3000L).execute();
                return true;
            } catch (Exception ex) {
                PNotifyMessage.systemError(ex,sessionBean);
                return false;
            }
        }
    }

    @Override
    protected void fillFields() {
        code = selected.getCode();
        name = selected.getName();
        try {
            parentId = selected.getParent().getId();
        } catch (Exception e) {
            parentId = null;
        }
        onLoad(true);
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        code = code.trim();
        name = name.trim();
        selected.setCode(code);
        selected.setName(name);
        try {
            selected.setParent(mainService.getById(parentId));
        } catch (Exception e) {
            selected.setParent(null);
        }        
        Auditory.make(selected, sessionBean.getCurrentUser());
    }
    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">

    /**
     * @return the parentSearcher
     */
    public ParentSearcher getParentSearcher() {
        return parentSearcher;
    }

    /**
     * @param parentSearcher the parentSearcher to set
     */
    public void setParentSearcher(ParentSearcher parentSearcher) {
        this.parentSearcher = parentSearcher;
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
     * @return the parentId
     */
    public Short getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(Short parentId) {
        this.parentId = parentId;
    }

    /**
     * @return the mainService
     */
    public IElectronicBookCategoryService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IElectronicBookCategoryService mainService) {
        this.mainService = mainService;
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="ParentSearcher">
    public class ParentSearcher implements java.io.Serializable{
        private List<Object[]> data;
        
        public void update(){
            Short id = selected.getId() == null ? -1 : selected.getId();
            data = getMainService().listHQL("SELECT ebc.id,ebc.code,ebc.name FROM ElectronicBookCategory ebc WHERE ebc.active = true and ebc.id <> ? and ebc.parent.id <> ?",id,id);
        }

        public List<Object[]> getData() {
            return data;
        }
    }
    
    //</editor-fold>
}