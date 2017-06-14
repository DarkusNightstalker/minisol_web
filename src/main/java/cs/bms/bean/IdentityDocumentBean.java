/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.IdentityDocument;
import cs.bms.service.interfac.IIdentityDocumentService;
import gkfire.util.ImportUtils;
import gkfire.auditory.Auditory;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
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
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class IdentityDocumentBean extends ABasicBean<Short> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;
    
    protected String code;
    protected String name;

    @PostConstruct
    public void init() {
        pagination = new Pagination(identityDocumentService);
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

    public void search() {
        code = code.trim();
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("code"))
                .add(Projections.property("abbr"))
                .add(Projections.property("name"));
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("active", true));
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

    /**
     * @return the identityDocumentService
     */
    public IIdentityDocumentService getIdentityDocumentService() {
        return identityDocumentService;
    }

    /**
     * @param identityDocumentService the identityDocumentService to set
     */
    public void setIdentityDocumentService(IIdentityDocumentService identityDocumentService) {
        this.identityDocumentService = identityDocumentService;
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

                    IdentityDocument item = new IdentityDocument();
                    item.setCode(code);
                    Auditory.make(item, getSessionBean().getCurrentUser());

                    String sysmedCode = null;
                    try {
                        sysmedCode = String.format("%02d", new Double(o[1].toString()).intValue());
                        if (sysmedCode.length() > 2) {
                            throw new NumberFormatException();
                        }
                    } catch (NullPointerException npe) {
                        addError(i, "Codigo sysmed : El campo no puede estar vacio");
                        continue;
                    } catch (NumberFormatException npe) {
                        addError(i, "Codigo sysmed : El campo debe ser un numero de 2 digitos como maximo");
                        continue;
                    }

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
                    String abbr = null;
                    try {
                        if (o[3].toString().length() == 0) {
                            throw new NullPointerException();
                        }
                        abbr = o[3].toString();
                        item.setAbbr(abbr);
                    } catch (NullPointerException npe) {
                        addError(i, "Abreviatura : El campo no puede estar vacio");
                        continue;
                    }
                    Short lenght = null;
                    try {
                        lenght = new Double(o[4].toString()).shortValue();
                        item.setLength_(lenght);
                    } catch (NullPointerException npe) {
                        addError(i, "Digitos maximos : El campo no puede estar vacio");
                        continue;
                    } catch (NumberFormatException npe) {
                        addError(i, "Digitos maximos : El campo debe ser un numero");
                        continue;
                    }
                    try {
                        getIdentityDocumentService().saveOrUpdate(item);
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

}
