package cs.bms.bean;

import cs.bms.model.Purchase;
import cs.bms.model.PurchasePayment;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.IPurchaseService;
import cs.bms.service.interfac.IStockReturnSupplierService;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.hibernate.sql.JoinType;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.Type;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class PurchaseBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{purchaseService}")
    private IPurchaseService purchaseService;
    @ManagedProperty(value = "#{purchasePaymentService}")
    private IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{stockReturnSupplierService}")
    private IStockReturnSupplierService stockReturnSupplierService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;

    private CompanySearcher companySearcher;
    private PurchasePaymentSearcher purchasePaymentSearcher;

    private Purchase selected;
    private Map<String, Object> otherData;

    private Integer companyId;
    private Date dateInit;
    private Date dateEnd;
    private State state;
    private String documentNumber;
    private String identityDocument;
    private String customerName;
    private BigDecimal totalInit;
    private BigDecimal totalEnd;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(purchaseService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("dateIssue"));
        companySearcher = new CompanySearcher();
        purchasePaymentSearcher = new PurchasePaymentSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        companySearcher.update();
        refresh();
    }

    public void load() {
        try {
            selected = purchaseService.getById(id);
            otherData = new HashMap();
            otherData.put("currentPay", purchasePaymentService.getCurrentPay(selected));
            List payments = new ArrayList();
            payments.addAll(purchasePaymentService.listHQL("FROM PurchasePayment pp WHERE pp.purchase  = ?", selected));
            otherData.put("payments",payments);
            otherData.put("repayment", stockReturnSupplierService.getSumRepayment(selected));
            otherData.put("devolutions", stockReturnSupplierService.getBasicDataByPurchase(selected));
            purchasePaymentSearcher.update();
        } catch (Exception e) {

        }
    }

    @Override
    protected void clearFields() {
        dateInit = null;
        dateEnd = null;
        state = null;
        documentNumber = "";
        identityDocument = "";
        customerName = "";
        totalInit = null;
        totalEnd = null;
    }

    @Override
    protected void initImport() {

    }

    public State[] getStates() {
        return State.values();
    }

    @Override
    public void search() {
        documentNumber = documentNumber.trim();
        identityDocument = identityDocument.trim();
        customerName = customerName.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("dateIssue"))
                .add(Projections.property("pp.abbr"))
                .add(Projections.property("fullDocument"))
                .add(Projections.property("supplierName"))
                .add(Projections.property("subtotal"))
                .add(Projections.property("igv"))
                .add(Projections.property("subtotalDiscount"))
                .add(Projections.property("electronic"))
                .add(Projections.sqlProjection("(SELECT SUM(pp.quantity) FROM purchase_payment pp WHERE pp.id_purchase = {alias}.id) as current_pay", new String[]{"current_pay"}, new Type[]{BigDecimalType.INSTANCE}))
                .add(Projections.sqlProjection("(SELECT SUM(srs.repayment+srs.igv) FROM stock_return_supplier srs WHERE srs.id_purchase = {alias}.id) as current_repayment", new String[]{"current_repayment"}, new Type[]{BigDecimalType.INSTANCE}))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("supplier", "s", JoinType.LEFT_OUTER_JOIN);
        aliasList.add("paymentProof", "pp");
        criterionList.add(Restrictions.eq("companyArrival", sessionBean.getCurrentCompany()));
        if (dateInit != null) {
            criterionList.add(Restrictions.ge("dateIssue", dateInit));
        }
        if (dateEnd != null) {
            criterionList.add(Restrictions.le("dateIssue", dateEnd));
        }
        if (state != null) {
            // criterionList.add(state.getCriterion());
        }
        if (documentNumber.length() != 0) {
            criterionList.add(Restrictions.like("fullDocument", documentNumber, MatchMode.ANYWHERE));
        }
        if (identityDocument.length() != 0) {
            criterionList.add(Restrictions.like("s.identityDocument", identityDocument, MatchMode.ANYWHERE));
        }
        if (customerName.length() != 0) {
            criterionList.add(Restrictions.like("supplierName", customerName, MatchMode.ANYWHERE).ignoreCase());
        }
        if (totalInit != null) {
            criterionList.add(Restrictions.ge("total", totalInit));
        }
        if (totalEnd != null) {
            criterionList.add(Restrictions.le("total", totalEnd));
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
     * @return the companySearcher
     */
    public CompanySearcher getCompanySearcher() {
        return companySearcher;
    }

    /**
     * @param companySearcher the companySearcher to set
     */
    public void setCompanySearcher(CompanySearcher companySearcher) {
        this.companySearcher = companySearcher;
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
     * @return the customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @param customerName the customerName to set
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
     * @return the purchaseService
     */
    public IPurchaseService getPurchaseService() {
        return purchaseService;
    }

    /**
     * @param purchaseService the purchaseService to set
     */
    public void setPurchaseService(IPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
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
     * @return the otherData
     */
    public Map<String, Object> getOtherData() {
        return otherData;
    }

    /**
     * @param otherData the otherData to set
     */
    public void setOtherData(Map<String, Object> otherData) {
        this.otherData = otherData;
    }

    public class CompanySearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getCompanyService().listHQL("SELECT c.id,c.name,c.city,c.address FROM Company c WHERE c.sold = true AND c.active=true");
        }

        public List<Object[]> getData() {
            return data;
        }
    }

    /**
     * @return the selected
     */
    public Purchase getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(Purchase selected) {
        this.selected = selected;
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

    public class PurchasePaymentSearcher implements java.io.Serializable {

        private BigDecimal quantity;
        private Date datePayment;

        void update() {
            quantity = null;
            datePayment = null;
        }

        public void add() {
            PurchasePayment pp = new PurchasePayment();
            pp.setCreateDate(new Date());
            pp.setCreateUser(getSessionBean().getCurrentUser());
            pp.setDatePayment(datePayment);
            pp.setPurchase(getSelected());
            pp.setQuantity(quantity);
            getPurchasePaymentService().saveOrUpdate(pp);
            ((List) getOtherData().get("payments")).add(pp);
            getOtherData().put("currentPay", ((BigDecimal) getOtherData().get("currentPay")).add(quantity));
            quantity = null;
            datePayment = null;
        }

        public void remove(int index) {
            PurchasePayment item = ((List<PurchasePayment>) getOtherData().get("payments")).remove(index);
            getOtherData().put("currentPay", ((BigDecimal) getOtherData().get("currentPay")).subtract(item.getQuantity()));
            getPurchasePaymentService().delete(item);
        }

        /**
         * @return the quantity
         */
        public BigDecimal getQuantity() {
            return quantity;
        }

        /**
         * @param quantity the quantity to set
         */
        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        /**
         * @return the datePayment
         */
        public Date getDatePayment() {
            return datePayment;
        }

        /**
         * @param datePayment the datePayment to set
         */
        public void setDatePayment(Date datePayment) {
            this.datePayment = datePayment;
        }

    }

    /**
     * @return the purchasePaymentSearcher
     */
    public PurchasePaymentSearcher getPurchasePaymentSearcher() {
        return purchasePaymentSearcher;
    }

    /**
     * @param purchasePaymentSearcher the purchasePaymentSearcher to set
     */
    public void setPurchasePaymentSearcher(PurchasePaymentSearcher purchasePaymentSearcher) {
        this.purchasePaymentSearcher = purchasePaymentSearcher;
    }

    public State getState(BigDecimal total, BigDecimal currentPay, Boolean active) {
        if (!active) {
            return State.CANCELED;
        } else {
            try {
                if (total.doubleValue() == currentPay.doubleValue()) {
                    return State.PAY;
                } else {
                    return State.IN_PAYMENT;
                }
            } catch (NullPointerException e) {
                return State.IN_PAYMENT;
            }
        }
    }

    public enum State {

        CANCELED("Anulado", "danger"),
        PAY("Pagado", "success"),
        IN_PAYMENT("En Pago", "alert");

        private final String description;
        private final String style;

        private State(String description, String style) {
            this.description = description;
            this.style = style;
        }

        public String getDescription() {
            return description;
        }

        public String getStyle() {
            return style;
        }

    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(State state) {
        this.state = state;
    }
}
