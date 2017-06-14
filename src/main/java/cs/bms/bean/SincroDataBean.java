/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.service.interfac.ICompanyService;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@ApplicationScoped
public class SincroDataBean implements java.io.Serializable {

    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;
    private int activeSessions;
    private int completeSessions;
    private double waitTime = 1.5;

    public void addActiveSession() {
        activeSessions++;
    }
    public boolean allowedContinueOperating(){
        Number activeCompanies = (Number) companyService.getByHQL("SELECT COUNT(*) FROM Company c where c.active = true AND c.edit_date > current_timestamp - interval '"+waitTime+" minutes' ");
        return (activeCompanies.intValue() == activeSessions);
    }

    public void addCompleteSession() {
        completeSessions++;
        Number activeCompanies = (Number) companyService.getByHQL("SELECT COUNT(*) FROM Company c where c.active = true > current_timestamp - interval '"+waitTime+" minutes'");
        if (activeCompanies.intValue() == completeSessions) {
            completeSessions = 0;
            activeSessions = 0;
        }
    }

    /**
     * @return the activeSessions
     */
    public int getActiveSessions() {
        return activeSessions;
    }

    /**
     * @param activeSessions the activeSessions to set
     */
    public void setActiveSessions(int activeSessions) {
        this.activeSessions = activeSessions;
    }

    /**
     * @return the completeSessions
     */
    public int getCompleteSessions() {
        return completeSessions;
    }

    /**
     * @param completeSessions the completeSessions to set
     */
    public void setCompleteSessions(int completeSessions) {
        this.completeSessions = completeSessions;
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

}
