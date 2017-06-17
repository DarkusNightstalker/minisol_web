/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.PermissionCategory;
import cs.bms.model.Rol;
import cs.bms.service.interfac.IRolService;
import cs.bms.report.util.ReportExport;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * @author CTIC
 */
@ManagedBean
@SessionScoped
public class RolBean extends ABasicBean<Integer> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{rolService}")
    protected IRolService rolService;
    protected String name;

    private Rol selected;
    private Map<String, Object> otherData;

    protected ReportExport reportRols;
    protected ReportExport reportPermissions;

    @PostConstruct
    public void init() {
        pagination = new Pagination(rolService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("name"));
        reportRols = new ReportExport("/1258488425132154132154214536/exp_rol.jasper", "Lista de Roles activos", sessionBean, new HashMap());
        reportPermissions = new ReportExport("/1258488425132154132154214536/exp_permission.jasper", "Lista de permisos activos", sessionBean, new HashMap());
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    public void load() {
        selected = rolService.getById(id);
        otherData = new HashMap();
        List<PermissionCategory> categories = rolService.listHQL("SELECT DISTINCT p.permissionCategory FROM Permission p join  p.rols r WHERE r.id = ? ", selected.getId());
        otherData.put("categories", categories);
        Map<Integer, List<Object>> m = new HashMap();
        categories.forEach(pc -> {
            m.put(pc.getId(), rolService.listHQL("SELECT p.name FROM Permission p join  p.rols r WHERE r.id = ? and p.permissionCategory = ?", selected.getId(), pc));
        });
        otherData.put("permission", m);
    }

    @Override
    public void refresh() {
        setName("");
        search();
    }

    @Override
    public void search() {
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("name"))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        if (name.length() != 0) {
            criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
        }
        pagination.search(1, projectionList, criterionList, orderFactory.make());
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
     * @return the rolService
     */
    public IRolService getRolService() {
        return rolService;
    }

    /**
     * @param rolService the rolService to set
     */
    public void setRolService(IRolService rolService) {
        this.rolService = rolService;
    }

    /**
     * @return the reportRols
     */
    public ReportExport getReportRols() {
        return reportRols;
    }

    /**
     * @param reportRols the reportRols to set
     */
    public void setReportRols(ReportExport reportRols) {
        this.reportRols = reportRols;
    }

    /**
     * @return the reportPermissions
     */
    public ReportExport getReportPermissions() {
        return reportPermissions;
    }

    /**
     * @param reportPermissions the reportPermissions to set
     */
    public void setReportPermissions(ReportExport reportPermissions) {
        this.reportPermissions = reportPermissions;
    }
}
