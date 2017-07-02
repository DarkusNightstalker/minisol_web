/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.StockReturnSupplier;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.IStockReturnSupplierService;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.math.BigDecimal;
import java.util.Date;
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
 * @author Darkus Nightmare
 */
@ManagedBean
@SessionScoped
public class SupplierReturnBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{stockReturnSupplierService}")
    private IStockReturnSupplierService stockReturnSupplierService;
    @ManagedProperty(value = "#{purchasePaymentService}")
    private IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;

    private StockReturnSupplier selected;

    private Integer companyId;
    private Date dateInit;
    private Date dateEnd;
    private String documentNumber;
    private String identityDocument;
    private String supplierName;
    private BigDecimal totalInit;
    private BigDecimal totalEnd;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(stockReturnSupplierService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("dateIssue"));
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    public void load() {
        try {
            selected = stockReturnSupplierService.getById(id);
        } catch (Exception e) {

        }
    }

    @Override
    protected void clearFields() {
        dateInit = null;
        dateEnd = null;
        documentNumber = "";
        identityDocument = "";
        supplierName = "";
        totalInit = null;
        totalEnd = null;
    }

    @Override
    protected void initImport() {

    }


    @Override
    public void search() {
        documentNumber = documentNumber.trim();
        identityDocument = identityDocument.trim();
        supplierName = supplierName.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("dateIssue"))
                .add(Projections.property("ppp.abbr"))
                .add(Projections.property("p.fullDocument"))
                .add(Projections.property("pp.abbr"))
                .add(Projections.property("fullDocument"))
                .add(Projections.property("p.supplierName"))
                .add(Projections.property("repayment"))
                .add(Projections.property("igv"))
                .add(Projections.property("electronic"))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("purchase", "p");
        aliasList.add("p.paymentProof", "ppp");
        aliasList.add("p.supplier", "s");
        aliasList.add("paymentProof", "pp");
        criterionList.add(Restrictions.eq("p.companyArrival", sessionBean.getCurrentCompany()));
        if (dateInit != null) {
            criterionList.add(Restrictions.ge("dateIssue", dateInit));
        }
        if (dateEnd != null) {
            criterionList.add(Restrictions.le("dateIssue", dateEnd));
        }
        if (documentNumber.length() != 0) {
            criterionList.add(Restrictions.like("p.fullDocument", documentNumber, MatchMode.ANYWHERE));
        }
        if (identityDocument.length() != 0) {
            criterionList.add(Restrictions.like("s.identityDocument", identityDocument, MatchMode.ANYWHERE));
        }
        if (supplierName.length() != 0) {
            criterionList.add(Restrictions.like("supplierName", supplierName, MatchMode.ANYWHERE).ignoreCase());
        }
        if (totalInit != null) {
            criterionList.add(Restrictions.ge("repayment", totalInit));
        }
        if (totalEnd != null) {
            criterionList.add(Restrictions.le("repayment", totalEnd));
        }
        pagination.search(1, projectionList, criterionList, aliasList, orderFactory.make());
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
     * @return the documentNumber
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * @param documentNumber the documentNumber to set
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * @return the identityDocument
     */
    public String getIdentityDocument() {
        return identityDocument;
    }

    /**
     * @param identityDocument the identityDocument to set
     */
    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }

    /**
     * @return the supplierName
     */
    public String getSupplierName() {
        return supplierName;
    }

    /**
     * @param supplierName the supplierName to set
     */
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    /**
     * @return the totalInit
     */
    public BigDecimal getTotalInit() {
        return totalInit;
    }

    /**
     * @param totalInit the totalInit to set
     */
    public void setTotalInit(BigDecimal totalInit) {
        this.totalInit = totalInit;
    }

    /**
     * @return the totalEnd
     */
    public BigDecimal getTotalEnd() {
        return totalEnd;
    }

    /**
     * @param totalEnd the totalEnd to set
     */
    public void setTotalEnd(BigDecimal totalEnd) {
        this.totalEnd = totalEnd;
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
     * @return the stockReturnSupplierService
     */
    public IStockReturnSupplierService getStockReturnSupplierService() {
        return stockReturnSupplierService;
    }

    /**
     * @param stockReturnSupplierService the stockReturnSupplierService to set
     */
    public void setStockReturnSupplierService(IStockReturnSupplierService stockReturnSupplierService) {
        this.stockReturnSupplierService = stockReturnSupplierService;
    }

    /**
     * @return the selected
     */
    public StockReturnSupplier getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(StockReturnSupplier selected) {
        this.selected = selected;
    }

}
