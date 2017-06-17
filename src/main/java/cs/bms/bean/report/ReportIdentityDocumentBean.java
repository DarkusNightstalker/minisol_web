/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IIdentityDocumentService;
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
public class ReportIdentityDocumentBean implements java.io.Serializable{
    
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;

    protected ReportExport identityDocumentExport;
    
    @PostConstruct
    public void init() {
       identityDocumentExport = new ReportExport("/1258488425132154132154214536/exp_identity_document.jasper", "Documentos de identidad", sessionBean, new HashMap());
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
     * @return the identityDocumentService
     */
    public IIdentityDocumentService getIdentityDocumentService() {
        return identityDocumentService;
    }

    /**
     * @param identityDocumentService the identityDocumentService to set
     */
    public void setIdentityDocumentService(IIdentityDocumentService identityDocumentService) {
        this.identityDocumentService = identityDocumentService;
    }

    /**
     * @return the identityDocumentExport
     */
    public ReportExport getIdentityDocumentExport() {
        return identityDocumentExport;
    }

    /**
     * @param identityDocumentExport the identityDocumentExport to set
     */
    public void setIdentityDocumentExport(ReportExport identityDocumentExport) {
        this.identityDocumentExport = identityDocumentExport;
    }

}
