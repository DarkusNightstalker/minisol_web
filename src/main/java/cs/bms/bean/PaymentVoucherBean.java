/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.PaymentVoucher;
import cs.bms.service.interfac.IPaymentVoucherService;
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
import org.hibernate.sql.JoinType;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class PaymentVoucherBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{paymentVoucherService}")
    protected IPaymentVoucherService paymentVoucherService;

    protected PaymentVoucher selected;
    protected String numeration;
    protected Integer value;

    @PostConstruct
    public void init() {
        pagination = new Pagination(paymentVoucherService);
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
        selected = paymentVoucherService.getById(id);
    }
    
    @Override
    protected void clearFields() {
        numeration = "";
        value = null;
    }
    
    @Override
    protected void initImport() {
    }

    @Override
    public void search() {
        numeration = numeration.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("numeration"))
                .add(Projections.property("value"))
                .add(Projections.property("dateExpiry"))
                .add(Projections.property("s.id"));
        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("sale", "s",JoinType.LEFT_OUTER_JOIN);
        if (numeration.length() != 0) {
            criterionList.add(Restrictions.like("numeration", numeration, MatchMode.ANYWHERE).ignoreCase());
        }
        if (value != null) {
            criterionList.add(Restrictions.eq("value", value));
        }
        pagination.search(1, projectionList, criterionList,aliasList, orderFactory.make());
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
     * @return the paymentVoucherService
     */
    public IPaymentVoucherService getPaymentVoucherService() {
        return paymentVoucherService;
    }

    /**
     * @param paymentVoucherService the paymentVoucherService to set
     */
    public void setPaymentVoucherService(IPaymentVoucherService paymentVoucherService) {
        this.paymentVoucherService = paymentVoucherService;
    }

    /**
     * @return the selected
     */
    public PaymentVoucher getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(PaymentVoucher selected) {
        this.selected = selected;
    }

    /**
     * @return the numeration
     */
    public String getNumeration() {
        return numeration;
    }

    /**
     * @param numeration the numeration to set
     */
    public void setNumeration(String numeration) {
        this.numeration = numeration;
    }

    /**
     * @return the value
     */
    public Integer getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Integer value) {
        this.value = value;
    }


}