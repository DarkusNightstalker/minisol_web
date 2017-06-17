/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IUoMService;
import cs.bms.report.util.ReportExport;
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
public class ReportUoMBean  implements java.io.Serializable{
    
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{uomService}")
    protected IUoMService uomService;

    protected ReportExport uomExport;
    
    @PostConstruct
    public void init() {
        uomExport = new ReportExport("/1258488425132154132154214536/exp_uom.jasper", "Unidades de medida", sessionBean, new HashMap());
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
     * @return the uomService
     */
    public IUoMService getUomService() {
        return uomService;
    }

    /**
     * @param uomService the uomService to set
     */
    public void setUomService(IUoMService uomService) {
        this.uomService = uomService;
    }

    /**
     * @return the uomExport
     */
    public ReportExport getUomExport() {
        return uomExport;
    }

    /**
     * @param uomExport the uomExport to set
     */
    public void setUomExport(ReportExport uomExport) {
        this.uomExport = uomExport;
    }


    
}
