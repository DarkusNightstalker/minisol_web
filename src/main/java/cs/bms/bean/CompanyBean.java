/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.bean.util.PNotifyMessage;
import cs.bms.service.interfac.ICompanyService;
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
public class CompanyBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;

    private String city;
    private String name;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(companyService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("id"));
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
        city = "";
        name = "";
    }

    @Override
    protected void initImport() {

    }

    @Override
    public void search() {
        city = city.trim();
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("ruc"))
                .add(Projections.property("name"))
                .add(Projections.property("city"))
                .add(Projections.property("address"))
                .add(Projections.property("sold"))
                .add(Projections.property("buy"))
                .add(Projections.property("stored"))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("active", true));
        try {
            if (city.length() != 0) {
                criterionList.add(Restrictions.like("city", city, MatchMode.ANYWHERE).ignoreCase());
            }
            if (name.length() != 0) {
                criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
            }
            pagination.search(1, projectionList, criterionList, orderFactory.make());
        } catch (Exception e) {
            PNotifyMessage.systemError(e, sessionBean);
        }
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
     * @return the identityNumber
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the identityNumber to set
     */
    public void setCity(String city) {
        this.city = city;
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
}
