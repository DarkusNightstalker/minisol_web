/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IActorService;
import cs.bms.report.util.ReportExport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class ReportActorBean implements java.io.Serializable{

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;

    protected ReportExport customerExport;
    protected ReportExport supplierExport;

    @PostConstruct
    public void init() {
        Map<String, Object> map = new HashMap();
        map.put("id_identity_documents", Collections.EMPTY_LIST);
        customerExport = new ReportExport("/1258488425132154132154214536/exp_customer.jasper", "Clientes minisol", sessionBean, map);
        map = new HashMap();
        map.put("id_identity_documents", Collections.EMPTY_LIST);
        supplierExport = new ReportExport("/1258488425132154132154214536/exp_supplier.jasper", "Proveedores Minisol", sessionBean, map);

    }

    public void clearReports() {
        customerExport.getParams().put("id_identity_documents", Collections.EMPTY_LIST);
        supplierExport.getParams().put("id_identity_documents", Collections.EMPTY_LIST);
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
     * @return the actorService
     */
    public IActorService getActorService() {
        return actorService;
    }

    /**
     * @param actorService the actorService to set
     */
    public void setActorService(IActorService actorService) {
        this.actorService = actorService;
    }

    /**
     * @return the customerExport
     */
    public ReportExport getCustomerExport() {
        return customerExport;
    }

    /**
     * @param customerExport the customerExport to set
     */
    public void setCustomerExport(ReportExport customerExport) {
        this.customerExport = customerExport;
    }

    /**
     * @return the supplierExport
     */
    public ReportExport getSupplierExport() {
        return supplierExport;
    }

    /**
     * @param supplierExport the supplierExport to set
     */
    public void setSupplierExport(ReportExport supplierExport) {
        this.supplierExport = supplierExport;
    }
    
}
