/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.User;
import cs.bms.service.interfac.IUserService;
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
 * @author CTIC
 */
@ManagedBean
@SessionScoped
public class UserBean extends ABasicBean<Integer> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{userService}")
    protected IUserService userService;

    protected User selected;
    
    protected String username;
    protected String name;
   
    public void load(){
        selected = userService.getById(id);
    }
    
    
    @PostConstruct
    public void init() {
        setPagination((Pagination<Object[]>) new Pagination(getUserService()));
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("username"));

    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    public void refresh() {
        setName("");
        setUsername("");
        search();
    }

    public void search() {
        name = name.trim();
        username = username.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("username"))
                .add(Projections.property("e.name"))
                .add(Projections.property("lastLogin"))
                .add(Projections.property("active"));
        AliasList aliasList = new AliasList();
        aliasList.add("employee", "e",JoinType.LEFT_OUTER_JOIN);
        CriterionList criterionList = new CriterionList();
        if (name.length() != 0) {
            criterionList.add(Restrictions.like("e.name", name, MatchMode.ANYWHERE).ignoreCase());
        }
        if (username.length() != 0) {
            criterionList.add(Restrictions.like("username", username, MatchMode.ANYWHERE).ignoreCase());
        }
        pagination.search(1, projectionList, criterionList, aliasList, orderFactory.make());
    }

    /**
     * @param sessionBean the sessionBean to set
     */
    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    /**
     * @return the name
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the name to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the userService
     */
    public IUserService getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(IUserService userService) {
        this.userService = userService;
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
     * @return the sessionBean
     */
    public SessionBean getSessionBean() {
        return sessionBean;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void initImport() {
    }

    /**
     * @return the selected
     */
    public User getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(User selected) {
        this.selected = selected;
    }

}
