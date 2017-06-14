/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.Sale;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.ISaleDetailService;
import cs.bms.service.interfac.ISaleService;
import cs.bms.util.DownloadUtil;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.hibernate.criterion.Criterion;
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
public class SaleBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{saleService}")
    protected ISaleService saleService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{saleDetailService}")
    protected ISaleDetailService saleDetailService;

    protected CompanySearcher companySearcher;

    protected Sale selected;
    protected Map<String, Object> otherData;

    protected Integer companyId;
    protected Date dateInit;
    protected Date dateEnd;
    protected State state;
    protected String documentNumber;
    protected String identityDocument;
    protected String customerName;
    protected BigDecimal totalInit;
    protected BigDecimal totalEnd;

    public void load() {
        selected = saleService.getById(id);
        List detail = saleDetailService.listHQL("FROM SaleDetail sd WHERE sd.sale = ?", selected);
        otherData = new HashMap<>();
        otherData.put("detail", detail);
    }

    public void copyPrint() {
        DownloadUtil.downloadJSONSale(id, sessionBean);
    }

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(saleService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("id"));
        companySearcher = new CompanySearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        companySearcher.update();
        refresh();
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

    public void send() {
        Long id = Long.parseLong(BeanUtil.getParameter("id"));
    }

    @Override
    public void search() {
        documentNumber = documentNumber.trim();
        identityDocument = identityDocument.trim();
        customerName = customerName.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id()) //0
                .add(Projections.property("dateIssue")) //1
                .add(Projections.property("serie")) //2
                .add(Projections.property("documentNumber"))//3
                .add(Projections.property("customerName")) //4
                .add(Projections.property("subtotal")) //5
                .add(Projections.property("igv")) //6
                .add(Projections.property("subtotalDiscount"))//7
                .add(Projections.property("electronic")) //8
                .add(Projections.property("sent")) //9
                .add(Projections.property("active")) //10
                .add(Projections.property("visa"))
                .add(Projections.property("verified"))
                .add(Projections.sqlProjection("(SELECT SUM(sp.quantity) FROM sale_payment sp WHERE sp.id_sale = {alias}.id) as payment", new String[]{"payment"}, new Type[]{BigDecimalType.INSTANCE}));      //11
        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("customer", "cs", JoinType.LEFT_OUTER_JOIN);
        aliasList.add("paymentProof", "pp");
        criterionList.add(Restrictions.eq("company", sessionBean.getCurrentCompany()));

        if (dateInit != null) {
            criterionList.add(Restrictions.ge("dateIssue", dateInit));
        }
        if (dateEnd != null) {
            criterionList.add(Restrictions.le("dateIssue", dateEnd));
        }
        if (state != null) {
            criterionList.add(state.getCriterion());
        }
        if (documentNumber.length() != 0) {
            criterionList.add(Restrictions.like("fullDocument", documentNumber, MatchMode.ANYWHERE));
        }
        if (identityDocument.length() != 0) {
            criterionList.add(Restrictions.like("cs.identityDocument", identityDocument, MatchMode.ANYWHERE));
        }
        if (customerName.length() != 0) {
            criterionList.add(Restrictions.like("customerName", customerName, MatchMode.ANYWHERE).ignoreCase());
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
     * @return the saleService
     */
    public ISaleService getSaleService() {
        return saleService;
    }

    /**
     * @param saleService the saleService to set
     */
    public void setSaleService(ISaleService saleService) {
        this.saleService = saleService;
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

    public State getState(boolean electronic, boolean sent, boolean active) {
        return State.getByParams(electronic, sent, active);
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

    public enum State {

        PENDING(
                Restrictions.and(
                        Restrictions.eq("sent", false),
                        Restrictions.eq("electronic", true),
                        Restrictions.eq("active", true)
                ), "Pendiente", "info"),
        SEND(
                Restrictions.and(
                        Restrictions.eq("sent", true),
                        Restrictions.eq("electronic", true),
                        Restrictions.eq("active", true)
                ), "Enviado", "sucess"),
        DROPPED_PENDING(
                Restrictions.and(
                        Restrictions.eq("sent", false),
                        Restrictions.eq("electronic", true),
                        Restrictions.eq("active", false)
                ), "Anulado Pendiente", "warning"),
        DROPPED_SEND(
                Restrictions.and(
                        Restrictions.eq("sent", true),
                        Restrictions.eq("electronic", true),
                        Restrictions.eq("active", false)
                ), "Anulado Enviado", "danger"),
        DROPPED(
                Restrictions.and(
                        Restrictions.eq("electronic", false),
                        Restrictions.eq("active", false)
                ), "Anulado", "alert"),
        PHYSICAL(Restrictions.and(
                Restrictions.eq("electronic", false),
                Restrictions.eq("active", true)
        ), "Físico", "system");

        private final Criterion criterion;
        private final String description;
        private final String styleType;

        private State(Criterion criterion, String description, String styleType) {
            this.criterion = criterion;
            this.description = description;
            this.styleType = styleType;
        }

        public Criterion getCriterion() {
            return criterion;
        }

        public String getStyleType() {
            return styleType;
        }

        public String getDescription() {
            return description;
        }

        public static State getByParams(boolean electronic, boolean sent, boolean active) {
            if (!active) {
                if (!electronic) {
                    return DROPPED;
                } else if (sent) {
                    return DROPPED_SEND;
                } else {
                    return DROPPED_PENDING;
                }
            } else {
                if (!electronic) {
                    return PHYSICAL;
                } else {
                    if (!sent) {
                        return PENDING;
                    }
                    return SEND;
                }
            }
        }
    }

    /**
     * @return the selected
     */
    public Sale getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(Sale selected) {
        this.selected = selected;
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

    /**
     * @return the saleDetailService
     */
    public ISaleDetailService getSaleDetailService() {
        return saleDetailService;
    }

    /**
     * @param saleDetailService the saleDetailService to set
     */
    public void setSaleDetailService(ISaleDetailService saleDetailService) {
        this.saleDetailService = saleDetailService;
    }
}
