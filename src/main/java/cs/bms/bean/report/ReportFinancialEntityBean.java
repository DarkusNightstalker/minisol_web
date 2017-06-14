/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IFinancialEntityService;
import cs.bms.util.ReportExport;
import java.util.HashMap;
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
public class ReportFinancialEntityBean  implements java.io.Serializable{
    
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{financialEntityService}")
    protected IFinancialEntityService financialEntityService;

    protected ReportExport financialEntityExport;
    
    @PostConstruct
    public void init() {
        financialEntityExport = new ReportExport("/1258488425132154132154214536/exp_financial_entity.jasper", "Entidades Financieras", sessionBean, new HashMap());
    }
    
    public void clearReports(){
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
     * @return the financialEntityService
     */
    public IFinancialEntityService getFinancialEntityService() {
        return financialEntityService;
    }

    /**
     * @param financialEntityService the financialEntityService to set
     */
    public void setFinancialEntityService(IFinancialEntityService financialEntityService) {
        this.financialEntityService = financialEntityService;
    }

    /**
     * @return the financialEntityExport
     */
    public ReportExport getFinancialEntityExport() {
        return financialEntityExport;
    }

    /**
     * @param financialEntityExport the financialEntityExport to set
     */
    public void setFinancialEntityExport(ReportExport financialEntityExport) {
        this.financialEntityExport = financialEntityExport;
    }

    
}
