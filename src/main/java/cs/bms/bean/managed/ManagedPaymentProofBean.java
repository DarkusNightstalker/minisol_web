/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.PaymentProof;
import cs.bms.service.interfac.IPaymentProofService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedPaymentProofBean extends AManagedBean<PaymentProof, IPaymentProofService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{paymentProofService}")
    private IPaymentProofService mainService;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;

    private String code;
    private Boolean forSale;
    private Boolean forPurchase;
    private Boolean forStored;
    private Boolean forReturn;
    private String abbr;
    private String name;

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();

    }

    @Override
    public boolean save() {
        if (mainService.existCode(code.trim(), selected.getId())) {
            PNotifyMessage.errorMessage("El codigo \"" + code + "\" ya esta registrado");
            saved = false;
        } else {
            try {
                String content = getSelected().getId() != null ? "Se ha actualizado una venta" : "Se ha registrado una venta";
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
        code = selected.getCode();
        abbr = selected.getAbbr();
        name = selected.getName();
        forSale = selected.getForSale();
        forPurchase = selected.getForPurchase();
        forReturn = selected.getForReturn();
        forStored = selected.getForStored();
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        code = code.trim();
        abbr = abbr.trim();
        name = name.trim();
        selected.setCode(code);
        selected.setAbbr(abbr.length() == 0 ? null : abbr);
        selected.setName(name);
        selected.setForSale(forSale);
        selected.setForPurchase(forPurchase);
        selected.setForReturn(forReturn);
        selected.setForStored(forStored);
        Auditory.make(selected, sessionBean.getCurrentUser());
    }
    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">

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
     * @return the mainService
     */
    public IPaymentProofService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IPaymentProofService mainService) {
        this.mainService = mainService;
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
     * @return the forSale
     */
    public Boolean getForSale() {
        return forSale;
    }

    /**
     * @param forSale the forSale to set
     */
    public void setForSale(Boolean forSale) {
        this.forSale = forSale;
    }
    //</editor-fold>

    /**
     * @return the forPurchase
     */
    public Boolean getForPurchase() {
        return forPurchase;
    }

    /**
     * @param forPurchase the forPurchase to set
     */
    public void setForPurchase(Boolean forPurchase) {
        this.forPurchase = forPurchase;
    }

    /**
     * @return the forStored
     */
    public Boolean getForStored() {
        return forStored;
    }

    /**
     * @param forStored the forStored to set
     */
    public void setForStored(Boolean forStored) {
        this.forStored = forStored;
    }

    /**
     * @return the forReturn
     */
    public Boolean getForReturn() {
        return forReturn;
    }

    /**
     * @param forReturn the forReturn to set
     */
    public void setForReturn(Boolean forReturn) {
        this.forReturn = forReturn;
    }
}
