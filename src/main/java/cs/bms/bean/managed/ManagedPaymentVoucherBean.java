/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.PaymentVoucher;
import cs.bms.service.interfac.IPaymentVoucherService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.util.Date;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedPaymentVoucherBean extends AManagedBean<PaymentVoucher, IPaymentVoucherService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{paymentVoucherService}")
    protected IPaymentVoucherService mainService;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;

    protected String prefix;
    protected Integer quantity;
    protected Integer value;
    protected String initNumeration;
    protected String endNumeration;
    protected Long startNumeration;
    protected Date dateExpiry;

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();

    }

    @Override
    public boolean save() {
        saved = false;
        try {
            for (long i = startNumeration; i < (startNumeration + quantity); i++) {
                PaymentVoucher paymentVoucher = new PaymentVoucher();
                paymentVoucher.setDateExpiry(dateExpiry);
                paymentVoucher.setNumeration(prefix + i);
                paymentVoucher.setValue(value);
                Auditory.make(paymentVoucher, sessionBean.getCurrentUser());
                mainService.saveOrUpdate(paymentVoucher);
            } 
            PNotifyMessage.saveMessage("Se han generado vales "+initNumeration+" - "+endNumeration);
            saved = true;
        } catch (Exception e) {
            saved = false;
            PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(e));
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        prefix = "";
        quantity = null;
        value = null;
        initNumeration = null;
        endNumeration = null;
        dateExpiry = null;
        startNumeration = null;
        createAgain = selected.getId() == null;
    }

    public void recalculated() {
        if (prefix.length() == 0 || quantity == null) {
            initNumeration = null;
            endNumeration = null;
            startNumeration = null;
        } else {
            startNumeration = mainService.getNextNumerationByPrefix(prefix);
            initNumeration = prefix + startNumeration;
            endNumeration = prefix + (startNumeration + quantity - 1);
        }
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
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
    public IPaymentVoucherService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IPaymentVoucherService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the value
     */
    public Integer getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * @return the initNumeration
     */
    public String getInitNumeration() {
        return initNumeration;
    }

    /**
     * @param initNumeration the initNumeration to set
     */
    public void setInitNumeration(String initNumeration) {
        this.initNumeration = initNumeration;
    }

    /**
     * @return the endNumeration
     */
    public String getEndNumeration() {
        return endNumeration;
    }

    /**
     * @param endNumeration the endNumeration to set
     */
    public void setEndNumeration(String endNumeration) {
        this.endNumeration = endNumeration;
    }

    /**
     * @return the startNumeration
     */
    public Long getStartNumeration() {
        return startNumeration;
    }

    /**
     * @param startNumeration the startNumeration to set
     */
    public void setStartNumeration(Long startNumeration) {
        this.startNumeration = startNumeration;
    }
    //</editor-fold>

    /**
     * @return the dateExpiry
     */
    public Date getDateExpiry() {
        return dateExpiry;
    }

    /**
     * @param dateExpiry the dateExpiry to set
     */
    public void setDateExpiry(Date dateExpiry) {
        this.dateExpiry = dateExpiry;
    }
}
