/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IStockService;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
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
public class StockBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{stockService}")
    protected IStockService stockService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;

    protected String productName;
    protected String barcode;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(stockService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("p.name"));
    }

    @Override
    protected void initImport() {
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
        productName = "";
        barcode = "";
    }

    @Override
    public void search() {
        productName = productName.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("p.barcode"))
                .add(Projections.property("p.name"))
                .add(Projections.property("uom.abbr"))
                .add(Projections.property("quantity"));
        AliasList aliasList = new AliasList();
        aliasList.add("product", "p");
        aliasList.add("p.uom", "uom");
        CriterionList criterionList = new CriterionList();
        if (productName.length() != 0) {
            criterionList.add(Restrictions.like("p.name", productName, MatchMode.ANYWHERE).ignoreCase());
        }
        if (barcode.length() != 0) {
            criterionList.add(Restrictions.like("p.barcode", barcode, MatchMode.ANYWHERE).ignoreCase());
        }
        criterionList.add(Restrictions.eq("p.active", true));
        criterionList.add(Restrictions.eq("company", sessionBean.getCurrentCompany()));

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
     * @return the stockService
     */
    public IStockService getStockService() {
        return stockService;
    }

    /**
     * @param stockService the stockService to set
     */
    public void setStockService(IStockService stockService) {
        this.stockService = stockService;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName the productName to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
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
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * @param barcode the barcode to set
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

}
