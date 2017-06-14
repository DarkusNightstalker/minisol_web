/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Purchase;
import cs.bms.model.PurchasePayment;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.IPurchaseService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.math.BigDecimal;
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
public class ManagedPurchasePaymentBean extends AManagedBean<PurchasePayment, IPurchasePaymentService> implements ILoadable {
    
    @ManagedProperty(value = "#{purchasePaymentService}")
    protected IPurchasePaymentService mainService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{purchaseService}")
    protected IPurchaseService purchaseService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    
    protected Long purchaseId;
    protected String description;
    protected Date datePayment;
    protected BigDecimal quantity;
    protected Integer companyDisbursementId;
    
    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }
    
    @Override
    public boolean save() {
        String content = selected.getId() == null ? "Se ha registrado un nuevo gasto" : "Se han guardado los cambios en el gasto";
        try {
            saved = super.save();
            PNotifyMessage.saveMessage(content);
        } catch (Exception e) {
            saved = false;
            PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(e));
        }
        return saved;
    }
    
    @Override
    protected void fillFields() {
        description = selected.getDescription();
        purchaseId = (Long) mainService.getByHQL("SELECT pp.purchase.id FROM PurchasePayment pp WHERE pp.id  = ?", selected.getId() == null ? -1 : selected.getId());
        datePayment = selected.getDatePayment();
        quantity = selected.getQuantity();
        
        createAgain = selected.getId() == null;
        
    }
    
    @Override
    protected void clearFields() {
    }
    
    @Override
    protected void fillSelected() {
        if (purchaseId != null) {
            selected.setPurchase(new Purchase(purchaseId));
        } else {
            selected.setPurchase(null);
        }
        selected.setDatePayment(datePayment);
        selected.setCompanyDisbursement(sessionBean.getCurrentCompany());
        selected.setDescription(description);
        selected.setQuantity(quantity);
        
        Auditory.make(selected, sessionBean.getCurrentUser());
    }

    /**
     * @return the mainService
     */
    public IPurchasePaymentService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IPurchasePaymentService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the companyService
     */
    public ICompanyService getCompanyService() {
        return companyService;
    }

    /**
     * @param companyService the companyService to set
     */
    public void setCompanyService(ICompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * @return the purchaseService
     */
    public IPurchaseService getPurchaseService() {
        return purchaseService;
    }

    /**
     * @param purchaseService the purchaseService to set
     */
    public void setPurchaseService(IPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
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
     * @return the purchaseId
     */
    public Long getPurchaseId() {
        return purchaseId;
    }

    /**
     * @param purchaseId the purchaseId to set
     */
    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the datePayment
     */
    public Date getDatePayment() {
        return datePayment;
    }

    /**
     * @param datePayment the datePayment to set
     */
    public void setDatePayment(Date datePayment) {
        this.datePayment = datePayment;
    }

    /**
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the companyDisbursementId
     */
    public Integer getCompanyDisbursementId() {
        return companyDisbursementId;
    }

    /**
     * @param companyDisbursementId the companyDisbursementId to set
     */
    public void setCompanyDisbursementId(Integer companyDisbursementId) {
        this.companyDisbursementId = companyDisbursementId;
    }
    
}
