/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.OpeningBalance;
import cs.bms.service.interfac.IOpeningBalanceService;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.util.Calendar;
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
public class OpeningBalanceBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{openingBalanceService}")
    protected IOpeningBalanceService openingBalanceService;

    protected OpeningBalance selected;
    protected String name;
    protected String barcode;
    protected Integer year;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(openingBalanceService);
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

    public void load() {
        selected = openingBalanceService.getById(id);
    }

    @Override
    protected void clearFields() {
        name = "";
        barcode = "";
        year = Calendar.getInstance().get(Calendar.YEAR);
    }

    @Override
    protected void initImport() {
    }

    public void search() {
        name = name.trim();
        barcode = barcode.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("year"))
                .add(Projections.property("month"))
                .add(Projections.property("p.barcode"))
                .add(Projections.property("p.name"))
                .add(Projections.property("quantity"))
                .add(Projections.property("unitCost"))
                .add(Projections.property("totalCost"));
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("company", sessionBean.getCurrentCompany()));
        AliasList aliasList = new AliasList();
        aliasList.add("product", "p");
        if (name.length() != 0) {
            criterionList.add(Restrictions.like("p.name", name, MatchMode.ANYWHERE).ignoreCase());
        }
        if (barcode.length() != 0) {
            criterionList.add(Restrictions.like("p.barcode", barcode, MatchMode.ANYWHERE).ignoreCase());
        }
        if (year != null) {
            criterionList.add(Restrictions.eq("year", year));
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
     * @return the openingBalanceService
     */
    public IOpeningBalanceService getOpeningBalanceService() {
        return openingBalanceService;
    }

    /**
     * @param openingBalanceService the openingBalanceService to set
     */
    public void setOpeningBalanceService(IOpeningBalanceService openingBalanceService) {
        this.openingBalanceService = openingBalanceService;
    }

    /**
     * @return the selected
     */
    public OpeningBalance getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(OpeningBalance selected) {
        this.selected = selected;
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

    /**
     * @return the year
     */
    public Integer getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(Integer year) {
        this.year = year;
    }

}
