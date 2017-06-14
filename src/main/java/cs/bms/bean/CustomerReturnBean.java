/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.StockReturnCustomer;
import cs.bms.service.interfac.IStockReturnCustomerDetailService;
import cs.bms.service.interfac.IStockReturnCustomerService;
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
public class CustomerReturnBean  extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{stockReturnCustomerService}")
    private IStockReturnCustomerService stockReturnCustomerService;
    @ManagedProperty(value = "#{stockReturnCustomerDetailService}")
    private IStockReturnCustomerDetailService stockReturnCustomerDetailService;

    private InfoDetail infoDetail;
    private StockReturnCustomer selected;
    private String documentNumber;
    private String documentNumberSale;
    private Date dateInit;
    private Date dateEnd;

    @PostConstruct
    public void init() {
        infoDetail = new InfoDetail();
        pagination = new Pagination<>(stockReturnCustomerService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("id"));
    }

    public void load() {
        selected = stockReturnCustomerService.getById(id);
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
        documentNumberSale = "";
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
                .add(Projections.property("spp.name"))
                .add(Projections.property("s.serie"))
                .add(Projections.property("s.documentNumber"))
                .add(Projections.property("pp.name"))
                .add(Projections.property("serie"))
                .add(Projections.property("documentNumber"))
                .add(Projections.property("repayment"))
                .add(Projections.property("dateIssue"))
                .add(Projections.property("active"))
                .add(Projections.property("send"));
        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("sale", "s");
        aliasList.add("s.paymentProof", "spp");
        aliasList.add("paymentProof", "pp");
        criterionList.add(Restrictions.eq("s.company", sessionBean.getCurrentCompany()));
        if (dateInit != null) {
            criterionList.add(Restrictions.ge("dateIssue", dateInit));
        }
        if (dateEnd != null) {
            criterionList.add(Restrictions.le("dateIssue", dateEnd));
        }
        if (documentNumber.length() != 0) {
            criterionList.add(Restrictions.like("fullDocument", documentNumber, MatchMode.ANYWHERE).ignoreCase());
        }
        if (documentNumberSale.length() != 0) {
            criterionList.add(Restrictions.like("s.fullDocument", documentNumberSale, MatchMode.ANYWHERE).ignoreCase());
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
            data = getStockReturnCustomerDetailService().listHQL(""
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
     * @return the stockReturnCustomerService
     */
    public IStockReturnCustomerService getStockReturnCustomerService() {
        return stockReturnCustomerService;
    }

    /**
     * @param stockReturnCustomerService the stockReturnCustomerService to set
     */
    public void setStockReturnCustomerService(IStockReturnCustomerService stockReturnCustomerService) {
        this.stockReturnCustomerService = stockReturnCustomerService;
    }

    /**
     * @return the stockReturnCustomerDetailService
     */
    public IStockReturnCustomerDetailService getStockReturnCustomerDetailService() {
        return stockReturnCustomerDetailService;
    }

    /**
     * @param stockReturnCustomerDetailService the stockReturnCustomerDetailService to set
     */
    public void setStockReturnCustomerDetailService(IStockReturnCustomerDetailService stockReturnCustomerDetailService) {
        this.stockReturnCustomerDetailService = stockReturnCustomerDetailService;
    }

    /**
     * @return the selected
     */
    public StockReturnCustomer getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(StockReturnCustomer selected) {
        this.selected = selected;
    }

    /**
     * @return the documentNumberSale
     */
    public String getDocumentNumberSale() {
        return documentNumberSale;
    }

    /**
     * @param documentNumberSale the documentNumberSale to set
     */
    public void setDocumentNumberSale(String documentNumberSale) {
        this.documentNumberSale = documentNumberSale;
    }

}
