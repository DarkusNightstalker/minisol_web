/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.enumerated.CashType;
import cs.bms.model.CashRegister;
import cs.bms.service.interfac.ICashRegisterDetailService;
import cs.bms.service.interfac.ICashRegisterService;
import gkfire.hibernate.AliasList;
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
public class CashRegisterBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{cashRegisterService}")
    protected ICashRegisterService cashRegisterService;
    @ManagedProperty(value = "#{cashRegisterDetailService}")
    protected ICashRegisterDetailService cashRegisterDetailService;

    protected CashRegister selected;
    protected Map<String,Object> aditionalData;
    
    protected Date dateInit;
    protected Date dateEnd;
    protected CashType[] types;

    @PostConstruct
    public void init() {
        aditionalData = new HashMap();
        pagination = new Pagination(cashRegisterService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("dateArcing"), Order.asc("ws.timeEntry"));
        types = CashType.values();
   }


    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    public void load() {
        selected = cashRegisterService.getById(id);
    }

    @Override
    protected void clearFields() {
        dateInit = null;
        dateEnd = null;
    }

    @Override
    protected void initImport() {
    }

    @Override
    public void search() {
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("ws.name"))
                .add(Projections.property("dateArcing"))
                .add(Projections.property("initialCash"))
                .add(Projections.property("realCash"))
                .add(Projections.property("id"));
        AliasList aliasList = new AliasList();
        aliasList.add("workShift", "ws");
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("company", sessionBean.getCurrentCompany()));
        if (dateEnd != null) {
            criterionList.add(Restrictions.le("dateArcing", dateEnd));
        }
        if (dateInit != null) {
            criterionList.add(Restrictions.ge("dateArcing", dateInit));
        }
        pagination.search(1, projectionList, criterionList, aliasList, orderFactory.make());
        pagination.getData().forEach(item -> {
            item[5] = cashRegisterDetailService.getQuantitiesAsMap((Long) item[0]);
        });
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
     * @return the cashRegisterServiceR
     */
    public ICashRegisterService getCashRegisterService() {
        return cashRegisterService;
    }

    /**
     * @param cashRegisterServiceR the cashRegisterServiceR to set
     */
    public void setCashRegisterService(ICashRegisterService cashRegisterServiceR) {
        this.cashRegisterService = cashRegisterServiceR;
    }

    /**
     * @return the selected
     */
    public CashRegister getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(CashRegister selected) {
        this.selected = selected;
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
     * @return the types
     */
    public CashType[] getTypes() {
        return types;
    }

    /**
     * @param types the types to set
     */
    public void setTypes(CashType[] types) {
        this.types = types;
    }

    /**
     * @return the cashRegisterDetailService
     */
    public ICashRegisterDetailService getCashRegisterDetailService() {
        return cashRegisterDetailService;
    }

    /**
     * @param cashRegisterDetailService the cashRegisterDetailService to set
     */
    public void setCashRegisterDetailService(ICashRegisterDetailService cashRegisterDetailService) {
        this.cashRegisterDetailService = cashRegisterDetailService;
    }
    
    /**
     * @return the aditionalData
     */
    public Map<String,Object> getAditionalData() {
        return aditionalData;
    }

    /**
     * @param aditionalData the aditionalData to set
     */
    public void setAditionalData(Map<String,Object> aditionalData) {
        this.aditionalData = aditionalData;
    }


}
