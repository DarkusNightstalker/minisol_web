/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.service.interfac.IUoMService;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
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
@ManagedBean(name = "uomBean")
@SessionScoped
public class UoMBean extends ABasicBean<Integer> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{uomService}")
    private IUoMService uomService;

    private String code;
    private String name;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(getUomService());
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("id"));
    }

    @Override
    protected void clearFields() {
        setCode("");
        setName("");
    }

    @Override
    protected void initImport() {
    }

    @Override
    public void search() {
        setName(getName().trim());
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("code"))
                .add(Projections.property("name"));
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("active", true));
        if (getName().length() != 0) {
            criterionList.add(Restrictions.like("name", getName(), MatchMode.ANYWHERE).ignoreCase());
        }
        if (getCode().length() != 0) {
            criterionList.add(Restrictions.like("code", getCode(), MatchMode.ANYWHERE).ignoreCase());
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
     * @return the uomService
     */
    public IUoMService getUomService() {
        return uomService;
    }

    /**
     * @param uomService the uomService to set
     */
    public void setUomService(IUoMService uomService) {
        this.uomService = uomService;
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
}
