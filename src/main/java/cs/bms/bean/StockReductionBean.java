/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.StockReduction;
import cs.bms.service.interfac.IStockReductionDetailService;
import cs.bms.service.interfac.IStockReductionService;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
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
public class StockReductionBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{stockReductionService}")
    private IStockReductionService stockReductionService;
    @ManagedProperty(value = "#{stockReductionDetailService}")
    private IStockReductionDetailService stockReductionDetailService;

    private InfoDetail infoDetail;
    private StockReduction selected;
    private String documentNumber;
    private Date dateInit;
    private Date dateEnd;

    @PostConstruct
    public void init() {
        infoDetail = new InfoDetail();
        pagination = new Pagination<>(stockReductionService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("id"));
    }

    public void load() {
        selected = stockReductionService.getById(id);
        infoDetail.update();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    @Override
    protected void clearFields() {
        documentNumber = "";
        dateEnd = null;
        dateInit = null;
    }

    @Override
    protected void initImport() {
    }

    @Override
    public void search() {
        documentNumber = documentNumber.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("pp.name"))
                .add(Projections.property("serie"))
                .add(Projections.property("documentNumber"))
                .add(Projections.property("r.name"))
                .add(Projections.property("dateIssue"))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("paymentProof", "pp");
        aliasList.add("responsible", "r");
        criterionList.add(Restrictions.eq("company", sessionBean.getCurrentCompany()));
        if (dateInit != null) {
            criterionList.add(Restrictions.ge("dateIssue", dateInit));
        }
        if (dateEnd != null) {
            criterionList.add(Restrictions.le("dateIssue", dateEnd));
        }
        if (documentNumber.length() != 0) {
            criterionList.add(Restrictions.like("fullDocument", documentNumber, MatchMode.ANYWHERE).ignoreCase());
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
     * @return the stockReductionService
     */
    public IStockReductionService getStockReductionService() {
        return stockReductionService;
    }

    /**
     * @param stockReductionService the stockReductionService to set
     */
    public void setStockReductionService(IStockReductionService stockReductionService) {
        this.stockReductionService = stockReductionService;
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

    public class InfoDetail implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getStockReductionDetailService().listHQL(""
                    + "SELECT "
                    + "srd.quantity,"
                    + "srd.uom.abbr,"
                    + "srd.productName "
                    + "FROM StockReductionDetail srd WHERE srd.stockReduction = ?", getSelected());
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
     * @return the stockReductionDetailService
     */
    public IStockReductionDetailService getStockReductionDetailService() {
        return stockReductionDetailService;
    }

    /**
     * @param stockReductionDetailService the stockReductionDetailService to set
     */
    public void setStockReductionDetailService(IStockReductionDetailService stockReductionDetailService) {
        this.stockReductionDetailService = stockReductionDetailService;
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

    /**
     * @return the selected
     */
    public StockReduction getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(StockReduction selected) {
        this.selected = selected;
    }
}
