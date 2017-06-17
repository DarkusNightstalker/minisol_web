/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IElectronicBookService;
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
public class ReportElectronicBookBean  implements java.io.Serializable{
    
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{electronicBookService}")
    protected IElectronicBookService electronicBookService;

    protected ReportExport electronicBookExport;
    
    @PostConstruct
    public void init() {
        electronicBookExport = new ReportExport("/1258488425132154132154214536/exp_financial_book.jasper", "Libros Electronicos", sessionBean, new HashMap());
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
     * @return the electronicBookService
     */
    public IElectronicBookService getElectronicBookService() {
        return electronicBookService;
    }

    /**
     * @param electronicBookService the electronicBookService to set
     */
    public void setElectronicBookService(IElectronicBookService electronicBookService) {
        this.electronicBookService = electronicBookService;
    }

    /**
     * @return the electronicBookExport
     */
    public ReportExport getElectronicBookExport() {
        return electronicBookExport;
    }

    /**
     * @param electronicBookExport the electronicBookExport to set
     */
    public void setElectronicBookExport(ReportExport electronicBookExport) {
        this.electronicBookExport = electronicBookExport;
    }
    

    
}
