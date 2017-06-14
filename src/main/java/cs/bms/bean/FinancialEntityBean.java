/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.FinancialEntity;
import cs.bms.service.interfac.IFinancialEntityService;
import gkfire.auditory.Auditory;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.util.ImportUtils;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.AbstractImport;
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
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class FinancialEntityBean extends ABasicBean<Short> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{financialEntityService}")
    protected IFinancialEntityService financialEntityService;

    protected String code;
    protected String name;

    @PostConstruct
    public void init() {
        pagination = new Pagination(financialEntityService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("code"));
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
        code = "";
        name = "";
    }

    @Override
    public void search() {
        code = code.trim();
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("code"))
                .add(Projections.property("name"));
        CriterionList criterionList = new CriterionList();
        if (code.length() != 0) {
            criterionList.add(Restrictions.like("code", code, MatchMode.START));
        }
        if (name.length() != 0) {
            criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
        }
        pagination.search(1, projectionList, criterionList, orderFactory.make());
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
     * @return the path
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the path to set
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

    @Override
    protected void initImport() {
        import_ = new AbstractImport() {
            @Override
            public void beginThread(int rowBegin, int trama) {
                for (int i = rowBegin; i <= rowBegin + trama; i++) {
                    if (i > totalRecords) {
                        break;
                    }
                    Object[] o = null;
                    try {
                        o = ImportUtils.readRow(file, i, 5);
                    } catch (Exception e) {
                        addError(i, " Contenido no legible :  " + e.getMessage());
                        continue;
                    }
                    String code = null;
                    try {
                        try {
                            code = new Double(o[0].toString()).longValue() + "";
                        } catch (NumberFormatException e) {
                            code = o[0].toString().trim();
                        }
                    } catch (NullPointerException e) {
                        addError(i, "Código :  El campo no puede estar vacio");
                        continue;
                    }

                    FinancialEntity item = new FinancialEntity();
                    item.setCode(code);
                    Auditory.make(item, getSessionBean().getCurrentUser());

                    String name = null;
                    try {
                        if (o[2].toString().length() == 0) {
                            throw new NullPointerException();
                        }
                        name = o[2].toString();
                        item.setName(name);
                    } catch (NullPointerException npe) {
                        addError(i, "Nombre : El campo no puede estar vacio");
                        continue;
                    }

                    try {
                        getFinancialEntityService().saveOrUpdate(item);
                        addSaved(i, "Exito");
                    } catch (Exception e) {
                        if (e instanceof ConstraintViolationException) {
                            PSQLException psql = (PSQLException) ((ConstraintViolationException) e).getSQLException();
                            addError(i, " Error en guardado : " + psql.getMessage());
                        } else {
                            addError(i, " Error en guardado : " + e.getMessage());
                        }
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        };
    }

    /**
     * @return the financialEntityService
     */
    public IFinancialEntityService getFinancialEntityService() {
        return financialEntityService;
    }

    /**
     * @param financialEntityService the financialEntityService to set
     */
    public void setFinancialEntityService(IFinancialEntityService financialEntityService) {
        this.financialEntityService = financialEntityService;
    }

}
