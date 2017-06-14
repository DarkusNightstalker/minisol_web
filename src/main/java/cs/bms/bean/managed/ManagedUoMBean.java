/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.UoM;
import cs.bms.service.interfac.IUoMService;
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
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedUoMBean extends AManagedBean<UoM, IUoMService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;
    @ManagedProperty(value = "#{uomService}")
    private IUoMService mainService;

    private String name;
    private String abbr;
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
        if (mainService.existName(name.trim(), selected.getId())) {
            PNotifyMessage.errorMessage("El nombre \"" + name + "\" ya esta registrado");
            saved = false;
        } else if (mainService.existAbbr(abbr.trim(), selected.getId())) {
            PNotifyMessage.errorMessage("La abreviatura \"" + abbr + "\" ya esta registrado");
            saved = false;
        } else if (mainService.existCode(code.trim(), selected.getId())) {
            PNotifyMessage.errorMessage("El codigo \"" + code + "\" ya esta registrado");
            saved = false;
        } else {
            try {
                String content = getSelected().getId() != null ? "Se ha actualizado  datos" : "Se ha creado una unida de medida";
                saved = super.save(); //To change body of generated methods, choose Tools | Templates.
                PNotifyMessage.saveMessage(content);
            } catch (Exception e) {
                saved = false;
                PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(e));
            }
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        name = selected.getName();
        code = selected.getCode();
        abbr = selected.getAbbr();
        onLoad(true);
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setCode(code.trim());
        selected.setAbbr(abbr.trim());
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
     * @return the mainService
     */
    public IUoMService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IUoMService mainService) {
        this.mainService = mainService;
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
     * @return the abbr
     */
    public String getAbbr() {
        return abbr;
    }

    /**
     * @param abbr the abbr to set
     */
    public void setAbbr(String abbr) {
        this.abbr = abbr;
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

}
