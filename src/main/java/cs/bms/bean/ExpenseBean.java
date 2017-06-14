/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.PurchasePayment;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.util.ReportExport;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ExpenseBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{purchasePaymentService}")
    protected IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;

    protected PurchasePayment selected;

    protected Integer companyId;
    protected String description;
    protected Date dateInit;
    protected Date dateEnd;

    protected ReportExport reportWorkShift;
    protected ReportExport reportMiddleDate;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(purchasePaymentService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("datePayment"));
        Map<String, Object> map = new HashMap();
        map.put("id_company", sessionBean.getCurrentCompany().getId());
        map.put("company", sessionBean.getCurrentCompany().getName()+" "+sessionBean.getCurrentCompany().getCity()+" ("+sessionBean.getCurrentCompany().getAddress()+")");
        map.put("date_init", null);
        map.put("date_end", null);
        reportMiddleDate = new ReportExport("/1258488425132154132154214536/expense_middle_date.jasper", "Lista de gastos entre fechas", sessionBean, map);
        reportWorkShift = new ReportExport("/1258488425132154132154214536/expense_workshift.jasper", "Lista de gastos por turno", sessionBean, new HashMap());
    }

    public void cleanReport() {
        reportMiddleDate.getParams().put("id_company", sessionBean.getCurrentCompany().getId());
        reportMiddleDate.getParams().put("company", sessionBean.getCurrentCompany().getName()+" "+sessionBean.getCurrentCompany().getCity()+" ("+sessionBean.getCurrentCompany().getAddress()+")");
        reportMiddleDate.getParams().put("date_init", null);
        reportMiddleDate.getParams().put("date_end", null);
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        reportMiddleDate.getParams().put("id_company", sessionBean.getCurrentCompany().getId());
        reportMiddleDate.getParams().put("company", sessionBean.getCurrentCompany().getName()+" "+sessionBean.getCurrentCompany().getCity()+" ("+sessionBean.getCurrentCompany().getAddress()+")");

        refresh();
    }

    public void load() {
        try {
            selected = purchasePaymentService.getById(id);
        } catch (Exception e) {

        }
    }

    @Override
    protected void clearFields() {
        dateInit = null;
        dateEnd = null;
        description = "";
    }

    @Override
    protected void initImport() {

    }

    @Override
    public void search() {
        description = description.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("datePayment"))
                .add(Projections.property("quantity"))
                .add(Projections.property("description"))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("companyDisbursement", sessionBean.getCurrentCompany()));
        if (dateInit != null) {
            criterionList.add(Restrictions.ge("dateIssue", dateInit));
        }
        if (dateEnd != null) {
            criterionList.add(Restrictions.le("dateIssue", dateEnd));
        }
        if (description.length() != 0) {
            criterionList.add(Restrictions.like("desription", description, MatchMode.ANYWHERE).ignoreCase());
        }
        pagination.search(1, projectionList, criterionList, orderFactory.make());
    }

    /**
     * @return the sessionBean
     */
    @Override
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

    /**
     * @return the companyId
     */
    public Integer getCompanyId() {
        return companyId;
    }

    /**
     * @param companyId the companyId to set
     */
    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    /**
     * @return the dateInit
     */
    public Date getDateInit() {
        return dateInit;
    }

    /**
     * @param dateInit the dateInit to set
     */
    public void setDateInit(Date dateInit) {
        this.dateInit = dateInit;
    }

    /**
     * @return the dateEnd
     */
    public Date getDateEnd() {
        return dateEnd;
    }

    /**
     * @param dateEnd the dateEnd to set
     */
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * @return the purchasePaymentService
     */
    public IPurchasePaymentService getPurchasePaymentService() {
        return purchasePaymentService;
    }

    /**
     * @param purchasePaymentService the purchasePaymentService to set
     */
    public void setPurchasePaymentService(IPurchasePaymentService purchasePaymentService) {
        this.purchasePaymentService = purchasePaymentService;
    }

    /**
     * @return the selected
     */
    public PurchasePayment getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(PurchasePayment selected) {
        this.selected = selected;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the reportWorkShift
     */
    public ReportExport getReportWorkShift() {
        return reportWorkShift;
    }

    /**
     * @param reportWorkShift the reportWorkShift to set
     */
    public void setReportWorkShift(ReportExport reportWorkShift) {
        this.reportWorkShift = reportWorkShift;
    }

    /**
     * @return the reportMiddleDate
     */
    public ReportExport getReportMiddleDate() {
        return reportMiddleDate;
    }

    /**
     * @param reportMiddleDate the reportMiddleDate to set
     */
    public void setReportMiddleDate(ReportExport reportMiddleDate) {
        this.reportMiddleDate = reportMiddleDate;
    }
    

}
