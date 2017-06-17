package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.IPurchaseService;
import cs.bms.report.util.ReportExport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class ReportISMBean implements Serializable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{purchaseService}")
    private IPurchaseService purchaseService;
    @ManagedProperty(value = "#{purchasePaymentService}")
    private IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;

    protected ReportExport reportIsmSend;
    protected ReportExport reportIsmReception;

    @PostConstruct
    public void init() {
        Map<String, Object> map = new HashMap();
        map.put("date_end", null);
        map.put("date_start", null);
        map.put("id_company", sessionBean.getCurrentCompany().getId());
        reportIsmSend = new ReportExport("/1258488425132154132154214536/ism_source.jasper", "Movimientos-internos-enviados", sessionBean, map);
        reportIsmReception = new ReportExport("/1258488425132154132154214536/ism_target.jasper", "Movimientos-internos-recepciones", sessionBean, map);
    }

    public void cleanReports() {
        reportIsmSend.getParams().put("date_end", null);
        reportIsmSend.getParams().put("date_start", null);
        reportIsmSend.getParams().put("id_company", sessionBean.getCurrentCompany().getId());
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public IPurchaseService getPurchaseService() {
        return purchaseService;
    }

    public void setPurchaseService(IPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    public IPurchasePaymentService getPurchasePaymentService() {
        return purchasePaymentService;
    }

    public void setPurchasePaymentService(IPurchasePaymentService purchasePaymentService) {
        this.purchasePaymentService = purchasePaymentService;
    }

    public ICompanyService getCompanyService() {
        return companyService;
    }

    public void setCompanyService(ICompanyService companyService) {
        this.companyService = companyService;
    }

    public ReportExport getReportIsmSend() {
        return reportIsmSend;
    }

    public void setReportIsmSend(ReportExport reportIsmSend) {
        this.reportIsmSend = reportIsmSend;
    }

    public ReportExport getReportIsmReception() {
        return reportIsmReception;
    }

    public void setReportIsmReception(ReportExport reportIsmReception) {
        this.reportIsmReception = reportIsmReception;
    }

}
