/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.InternalStockMovement;
import cs.bms.service.interfac.IInternalStockMovementDetailService;
import cs.bms.service.interfac.IInternalStockMovementService;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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
public class StockMovementBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{internalStockMovementService}")
    private IInternalStockMovementService internalStockMovementService;
    @ManagedProperty(value = "#{internalStockMovementDetailService}")
    private IInternalStockMovementDetailService internalStockMovementDetailService;

    private InternalStockMovement selected;
    private InfoDetail infoDetail;
    private Pagination<Object[]> paginationSource;
    private OrderFactory orderFactorySource;

    private Date dateInitSource;
    private Date dateEndSource;
    private Date dateInitTarget;
    private Date dateEndTarget;
    private String documentNumber;
    private BigDecimal totalInit;
    private BigDecimal totalEnd;

    @PostConstruct
    public void init() {
        infoDetail = new InfoDetail();
        pagination = new Pagination(internalStockMovementService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.asc("id"));

        paginationSource = new Pagination(internalStockMovementService);
        orderFactorySource = new OrderFactory(new OrderList());
        orderFactorySource.setDefaultOrder(Order.asc("id"));
      
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    public void load() {
        selected = internalStockMovementService.getById(id);
        infoDetail.update();
    }

    @Override
    protected void clearFields() {
        dateInitSource = null;
        dateEndSource = null;
        documentNumber = "";
        totalInit = null;
        totalEnd = null;
    }

    @Override
    protected void initImport() {
    }

    @Override
    public void search() {
        documentNumber = documentNumber.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("sc.name"))
                .add(Projections.property("sc.city"))
                .add(Projections.property("sc.address"))
                .add(Projections.property("serie"))
                .add(Projections.property("documentNumber"))
                .add(Projections.property("dateShipping"))
                .add(Projections.property("dateArrival"))
                .add(Projections.property("pp.name"))
                .add(Projections.property("sent"))
                .add(Projections.property("electronic"))
                .add(Projections.property("dateRealArrival"))
                .add(Projections.property("active"));

        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("paymentProof", "pp");
        aliasList.add("sourceCompany", "sc");
        criterionList.add(Restrictions.eq("targetCompany", sessionBean.getCurrentCompany()));

        if (dateInitTarget != null) {
            criterionList.add(Restrictions.ge("dateShipping", dateInitTarget));
        }
        if (dateEndTarget != null) {
            criterionList.add(Restrictions.le("dateShipping", dateEndTarget));
        }
        if (documentNumber.length() != 0) {
            criterionList.add(Restrictions.like("fullDocument", documentNumber, MatchMode.ANYWHERE));
        }
        pagination.search(1, projectionList, criterionList, aliasList, orderFactory.make());

        projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("st.name"))
                .add(Projections.property("st.city"))
                .add(Projections.property("st.address"))
                .add(Projections.property("serie"))
                .add(Projections.property("documentNumber"))
                .add(Projections.property("dateShipping"))
                .add(Projections.property("dateArrival"))
                .add(Projections.property("pp.name"))
                .add(Projections.property("sent"))
                .add(Projections.property("electronic"))
                .add(Projections.property("dateRealArrival"))
                .add(Projections.property("active"));

        criterionList = new CriterionList();
        aliasList = new AliasList();
        aliasList.add("targetCompany", "st");
        aliasList.add("paymentProof", "pp");
        criterionList.add(Restrictions.eq("sourceCompany", sessionBean.getCurrentCompany()));

        if (dateInitSource != null) {
            criterionList.add(Restrictions.ge("dateShipping", dateInitSource));
        }
        if (dateEndSource != null) {
            criterionList.add(Restrictions.le("dateShipping", dateEndSource));
        }
        if (documentNumber.length() != 0) {
            criterionList.add(Restrictions.like("fullDocument", documentNumber, MatchMode.ANYWHERE));
        }
        paginationSource.search(1, projectionList, criterionList, aliasList, orderFactorySource.make());

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
     * @return the internalStockMovementService
     */
    public IInternalStockMovementService getInternalStockMovementService() {
        return internalStockMovementService;
    }

    /**
     * @param internalStockMovementService the internalStockMovementService to
     * set
     */
    public void setInternalStockMovementService(IInternalStockMovementService internalStockMovementService) {
        this.internalStockMovementService = internalStockMovementService;
    }

    /**
     * @return the selected
     */
    public InternalStockMovement getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(InternalStockMovement selected) {
        this.selected = selected;
    }

    /**
     * @return the paginationSource
     */
    public Pagination<Object[]> getPaginationSource() {
        return paginationSource;
    }

    /**
     * @param paginationSource the paginationSource to set
     */
    public void setPaginationSource(Pagination<Object[]> paginationSource) {
        this.paginationSource = paginationSource;
    }

    /**
     * @return the orderFactorySource
     */
    public OrderFactory getOrderFactorySource() {
        return orderFactorySource;
    }

    /**
     * @param orderFactorySource the orderFactorySource to set
     */
    public void setOrderFactorySource(OrderFactory orderFactorySource) {
        this.orderFactorySource = orderFactorySource;
    }

    /**
     * @return the dateInitSource
     */
    public Date getDateInitSource() {
        return dateInitSource;
    }

    /**
     * @param dateInitSource the dateInitSource to set
     */
    public void setDateInitSource(Date dateInitSource) {
        this.dateInitSource = dateInitSource;
    }

    /**
     * @return the dateEndSource
     */
    public Date getDateEndSource() {
        return dateEndSource;
    }

    /**
     * @param dateEndSource the dateEndSource to set
     */
    public void setDateEndSource(Date dateEndSource) {
        this.dateEndSource = dateEndSource;
    }

    /**
     * @return the dateInitTarget
     */
    public Date getDateInitTarget() {
        return dateInitTarget;
    }

    /**
     * @param dateInitTarget the dateInitTarget to set
     */
    public void setDateInitTarget(Date dateInitTarget) {
        this.dateInitTarget = dateInitTarget;
    }

    /**
     * @return the dateEndTarget
     */
    public Date getDateEndTarget() {
        return dateEndTarget;
    }

    /**
     * @param dateEndTarget the dateEndTarget to set
     */
    public void setDateEndTarget(Date dateEndTarget) {
        this.dateEndTarget = dateEndTarget;
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

    public class InfoDetail implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getInternalStockMovementDetailService().listHQL(""
                    + "SELECT "
                    + "ismd.quantity,"
                    + "ismd.uom.abbr,"
                    + "ismd.productName,"
                    + "ismd.weight,"
                    + "wuom.abbr "
                    + "FROM InternalStockMovementDetail ismd left join ismd.weightUoM wuom WHERE ismd.internalStockMovement = ?", selected);
        }

        /**
         * @return the data
         */
        public List<Object[]> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }

    }

    /**
     * @return the internalStockMovementDetailService
     */
    public IInternalStockMovementDetailService getInternalStockMovementDetailService() {
        return internalStockMovementDetailService;
    }

    /**
     * @param internalStockMovementDetailService the
     * internalStockMovementDetailService to set
     */
    public void setInternalStockMovementDetailService(IInternalStockMovementDetailService internalStockMovementDetailService) {
        this.internalStockMovementDetailService = internalStockMovementDetailService;
    }

    /**
     * @return the infoDetail
     */
    public InfoDetail getInfoDetail() {
        return infoDetail;
    }

    /**
     * @param infoDetail the infoDetail to set
     */
    public void setInfoDetail(InfoDetail infoDetail) {
        this.infoDetail = infoDetail;
    }
}
