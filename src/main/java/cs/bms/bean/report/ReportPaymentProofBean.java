/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IPaymentProofService;
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
public class ReportPaymentProofBean  implements java.io.Serializable{
    
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{paymentProofService}")
    protected IPaymentProofService paymentProofService;

    protected ReportExport paymentProofExport;
    
    @PostConstruct
    public void init() {
        paymentProofExport = new ReportExport("/1258488425132154132154214536/exp_payment_proof.jasper", "Comprobantes de pago", sessionBean, new HashMap());
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
     * @return the paymentProofService
     */
    public IPaymentProofService getPaymentProofService() {
        return paymentProofService;
    }

    /**
     * @param paymentProofService the paymentProofService to set
     */
    public void setPaymentProofService(IPaymentProofService paymentProofService) {
        this.paymentProofService = paymentProofService;
    }

    /**
     * @return the paymentProofExport
     */
    public ReportExport getPaymentProofExport() {
        return paymentProofExport;
    }

    /**
     * @param paymentProofExport the paymentProofExport to set
     */
    public void setPaymentProofExport(ReportExport paymentProofExport) {
        this.paymentProofExport = paymentProofExport;
    }


    
}
