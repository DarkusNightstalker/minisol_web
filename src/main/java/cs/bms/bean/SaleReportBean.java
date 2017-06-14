package cs.bms.bean;

import cs.bms.model.Actor;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.IPurchaseService;
import cs.bms.util.ReportExport;
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
public class SaleReportBean implements Serializable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{purchaseService}")
    private IPurchaseService purchaseService;
    @ManagedProperty(value = "#{purchasePaymentService}")
    private IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;

    protected Actor actor;
    protected ReportExport customerDebts;
    protected ReportExport salesReport;
    protected String identityNumber;

    @PostConstruct
    public void init() {
        Map<String, Object> map = new HashMap();
        map.put("date_end", null);
        map.put("date_start", null);
        map.put("id_company", sessionBean.getCurrentCompany().getId());
        map.put("id_customer", null);
        customerDebts = new ReportExport("/1258488425132154132154214536/customer_debts.jasper", "Deudas de clientes", sessionBean, map);
        map.put("date_end", null);
        map.put("date_start", null);
        salesReport = new ReportExport("/1258488425132154132154214536/exp_sales.jasper", "Lista de ventas", sessionBean, map);
    }

    public void cleanReports() {
        customerDebts.getParams().put("date_end", null);
        customerDebts.getParams().put("date_start", null);
        customerDebts.getParams().put("id_company", sessionBean.getCurrentCompany().getId());
        customerDebts.getParams().put("id_customer", null);
    }

    public void searchCustomer() {
        if (!identityNumber.trim().isEmpty()) {
            actor = (Actor) purchaseService.getByHQL("FROM Actor a WHERE a.identityNumber='" + identityNumber + "' AND a.customer=true");
            System.out.println("jolaaa + " + actor);
        }
    }

    public void toXlsx() {
        if (actor != null) {
            customerDebts.getParams().put("id_customer", actor.getId());
            customerDebts.toXlsx();
        }
    }

    public void toPdf() {
        if (actor != null) {
            customerDebts.getParams().put("id_customer", actor.getId());
            customerDebts.toPdf();
        }
    }

    public void toDocx() {
        if (actor != null) {
            customerDebts.getParams().put("id_customer", actor.getId());
            customerDebts.toDocx();
        }
    }

    public void toHtml() {
        if (actor != null) {
            customerDebts.getParams().put("id_customer", actor.getId());
            customerDebts.toHtml();
        }
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

    public ReportExport getCustomerDebts() {
        return customerDebts;
    }

    public void setCustomerDebts(ReportExport customerDebts) {
        this.customerDebts = customerDebts;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
    }

    /**
     * @return the salesReport
     */
    public ReportExport getSalesReport() {
        return salesReport;
    }

    /**
     * @param salesReport the salesReport to set
     */
    public void setSalesReport(ReportExport salesReport) {
        this.salesReport = salesReport;
    }

}
