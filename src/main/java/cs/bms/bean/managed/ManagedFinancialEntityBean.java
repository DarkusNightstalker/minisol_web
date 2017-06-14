/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.FinancialEntity;
import cs.bms.service.interfac.IFinancialEntityService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
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
public class ManagedFinancialEntityBean extends AManagedBean<FinancialEntity, IFinancialEntityService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;
    @ManagedProperty(value = "#{financialEntityService}")
    private IFinancialEntityService mainService;

    private String code;
    private String name;

    @PostConstruct
    public void init() {
    }

    @Override
    public void onLoad(boolean allowAjax) {
//        if (BeanUtil.isAjaxRequest() && !allowAjax) {
//            return;
//        }
    }

    @Override
    public boolean save() {
        boolean valid = mainService.verifyCode(code.trim(), selected.getId());
        if (!valid) {
            PNotifyMessage.errorMessage("El código\"" + code + "\" ya se encuentra en uso");
            saved = false;
        } else {
            try {
                String content = getSelected().getId() != null ? "Se ha actualizado  datos" : "Se ha creado una entidad financiera";
                saved = super.save();
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
        onLoad(true);
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setName(name.trim());
        selected.setCode(code.trim());
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
     * @return the mainService
     */
    public IFinancialEntityService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IFinancialEntityService mainService) {
        this.mainService = mainService;
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
