/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.StockType;
import cs.bms.service.interfac.IStockTypeService;
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
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class StockTypeBean extends ABasicBean<Short> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{stockTypeService}")
    private IStockTypeService stockTypeService;

    private StockType selected;
    private String code;
    private String name;

    @PostConstruct
    public void init() {
        pagination = new Pagination(stockTypeService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.asc("id"));
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }
    
    public void load(){
        selected = stockTypeService.getById(id);
    }
    
    @Override
    protected void clearFields() {
        code = "";
        name = "";
    }
    
    @Override
    protected void initImport() {
    }

    public void search() {
        code = code.trim();
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("code"))
                .add(Projections.property("name"))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("active", true));
        if (code.length() != 0) {
            criterionList.add(Restrictions.like("code", code, MatchMode.ANYWHERE));
        }
        if (name.length() != 0) {
            criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
        }
        pagination.search(1, projectionList, criterionList, orderFactory.make());
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
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the stockTypeService
     */
    public IStockTypeService getStockTypeService() {
        return stockTypeService;
    }

    /**
     * @param stockTypeService the stockTypeService to set
     */
    public void setStockTypeService(IStockTypeService stockTypeService) {
        this.stockTypeService = stockTypeService;
    }

    /**
     * @return the selected
     */
    public StockType getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(StockType selected) {
        this.selected = selected;
    }


}
