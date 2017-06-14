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
public class PurchaseReportBean implements Serializable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{purchaseService}")
    private IPurchaseService purchaseService;
    @ManagedProperty(value = "#{purchasePaymentService}")
    private IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;

    protected Actor actor;
    protected ReportExport supplierDebts;
    protected String identityNumber;

    @PostConstruct
    public void init() {
        Map<String, Object> map = new HashMap();
        map.put("date_end", null);
        map.put("date_start", null);
        map.put("id_company", sessionBean.getCurrentCompany().getId());
        map.put("id_supplier", null);
        supplierDebts = new ReportExport("/1258488425132154132154214536/supplier_debts.jasper", "Deudas a proveedor", sessionBean, map);
    }

    public void cleanReports() {
        supplierDebts.getParams().put("date_end", null);
        supplierDebts.getParams().put("date_start", null);
        supplierDebts.getParams().put("id_company", sessionBean.getCurrentCompany().getId());
        supplierDebts.getParams().put("id_supplier", null);
    }

    public void searchSupplier() {
        if (!identityNumber.trim().isEmpty()) {
            actor = (Actor) purchaseService.getByHQL("FROM Actor a WHERE a.identityNumber='" + identityNumber + "' AND a.supplier=true");
        }
    }

    public void toXlsx() {
        if (actor != null) {
            supplierDebts.getParams().put("id_supplier", actor.getId());
            supplierDebts.toXlsx();
        }
    }

    public void toPdf() {
        if (actor != null) {
            supplierDebts.getParams().put("id_supplier", actor.getId());
            supplierDebts.toPdf();
        }
    }

    public void toDocx() {
        if (actor != null) {
            supplierDebts.getParams().put("id_supplier", actor.getId());
            supplierDebts.toDocx();
        }
    }

    public void toHtml() {
        if (actor != null) {
            supplierDebts.getParams().put("id_supplier", actor.getId());
            supplierDebts.toHtml();
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

    public ReportExport getSupplierDebts() {
        return supplierDebts;
    }

    public void setSupplierDebts(ReportExport supplierDebts) {
        this.supplierDebts = supplierDebts;
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

}
