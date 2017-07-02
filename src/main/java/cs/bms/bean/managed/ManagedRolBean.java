/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Permission;
import cs.bms.model.PermissionCategory;
import cs.bms.model.Rol;
import cs.bms.model.User;
import cs.bms.service.interfac.IPermissionCategoryService;
import cs.bms.service.interfac.IPermissionService;
import cs.bms.service.interfac.IRolService;
import gkfire.auditory.Auditory;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedRolBean extends AManagedBean<Rol, IRolService> implements ILoadable {

    @ManagedProperty(value = "#{permissionCategoryService}")
    private IPermissionCategoryService permissionCategoryService;
    @ManagedProperty(value = "#{permissionService}")
    private IPermissionService permissionService;
    @ManagedProperty(value = "#{rolService}")
    private IRolService mainService;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;

    private PermissionSearcher permissionSearcher;

    private String name;
    private List<Permission> permissions;

    @PostConstruct
    public void init() {
        permissionSearcher = new PermissionSearcher();
    }

    @Override
    public void delete(Serializable id) {
        if (mainService.isActive((Integer) id)) {
            Rol rol = mainService.getById((Integer) id);
            Auditory.make(rol, sessionBean.getCurrentUser());
            try {
                getMainService().delete(rol);
            } catch (Exception e) {
                PNotifyMessage.systemError(e, sessionBean);
                return;
            }
            PNotifyMessage.infoMessage("Se ha desactivado un rol");
        } else {
            PNotifyMessage.errorMessage("Este rol ya fue desactivado!!");
        }
    }

    @Override
    public void recovery(Serializable id) {
        if (!mainService.isActive((Integer) id)) {
            Rol rol = mainService.getById((Integer) id);
            Auditory.make(rol, sessionBean.getCurrentUser());
            rol.setActive(true);
            try {
                getMainService().update(rol);
            } catch (Exception e) {
                PNotifyMessage.systemError(e, sessionBean);
                return;
            }
            PNotifyMessage.infoMessage("Se ha activado un rol");
        } else {
            PNotifyMessage.errorMessage("Este rol ya fue recuperado!!");
        }
    }

    @Override
    public boolean save() {
        try {
            String content = getSelected().getId() != null ? "Se ha actualizado un rol" : "Se ha creado un rol";
            saved = super.save(); //To change body of generated methods, choose Tools | Templates.
            PNotifyMessage.saveMessage(content);
        } catch (Exception e) {
            PNotifyMessage.systemError(e, sessionBean);
            saved = false;
        }
        return saved;
    }

    @Override
    public void onLoad(boolean allowAjax) {
    }

    @Override
    protected void fillFields() {
        if (selected.getId() != null) {
            AliasList aliasList = new AliasList();
            aliasList.add("rols", "r");
            CriterionList criterionList = new CriterionList();
            criterionList.add(Restrictions.eq("r.id", selected.getId()));
            permissions = permissionService.addRestrictionsVariant(Arrays.asList(aliasList, criterionList));
        } else {
            permissions = new ArrayList();
        }
        permissionSearcher.update();
        name = selected.getName();
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setName(name.trim());
        List<Permission> permissions = new ArrayList(permissionSearcher.selecteds.keySet());
        selected.setPermissions(new ArrayList());
        for (Permission item : permissions) {
            if (permissionSearcher.selecteds.get(item)) {
                selected.getPermissions().add(item);
            }
        }
    }

    /**
     * @return the permissionCategoryService
     */
    public IPermissionCategoryService getPermissionCategoryService() {
        return permissionCategoryService;
    }

    /**
     * @param permissionCategoryService the permissionCategoryService to set
     */
    public void setPermissionCategoryService(IPermissionCategoryService permissionCategoryService) {
        this.permissionCategoryService = permissionCategoryService;
    }

    /**
     * @return the permissionService
     */
    public IPermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the mainService
     */
    public IRolService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IRolService mainService) {
        this.mainService = mainService;
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
     * @return the navigationBean
     */
    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    /**
     * @param navigationBean the navigationBean to set
     */
    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }

    /**
     * @return the permissionSearcher
     */
    public PermissionSearcher getPermissionSearcher() {
        return permissionSearcher;
    }

    /**
     * @param permissionSearcher the permissionSearcher to set
     */
    public void setPermissionSearcher(PermissionSearcher permissionSearcher) {
        this.permissionSearcher = permissionSearcher;
    }

    /**
     * @return the permissions
     */
    public List<Permission> getPermissions() {
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
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

    public class PermissionSearcher implements java.io.Serializable {

        private List<PermissionCategory> categories;
        private Map<Integer, List<Permission>> permissions;
        private Map<Permission, Boolean> selecteds;
        private List<Permission> permissionNoCategory;

        public void update() {
            this.categories = permissionCategoryService.listHQL("FROM PermissionCategory p ORDER BY p.id");
            this.permissions = new HashMap();
            this.selecteds = new HashMap();
            for (PermissionCategory category : this.categories) {
                List<Permission> permissions = getPermissionService().getBy(category);
                for (Permission item1 : permissions) {
                    selecteds.put(item1, ManagedRolBean.this.getPermissions().contains(item1));
                }
                this.permissions.put(category.getId(), permissions);
            }
            permissionNoCategory = getPermissionService().getBy(null);
            for (Permission item1 : permissionNoCategory) {
                selecteds.put(item1, ManagedRolBean.this.getPermissions().contains(item1));
            }
        }

        /**
         * @return the categories
         */
        public List<PermissionCategory> getCategories() {
            return categories;
        }

        /**
         * @param categories the categories to set
         */
        public void setCategories(List<PermissionCategory> categories) {
            this.categories = categories;
        }

        /**
         * @return the permissions
         */
        public Map<Integer, List<Permission>> getPermissions() {
            return permissions;
        }

        /**
         * @param permissions the permissions to set
         */
        public void setPermissions(Map<Integer, List<Permission>> permissions) {
            this.permissions = permissions;
        }

        /**
         * @return the selecteds
         */
        public Map<Permission, Boolean> getSelecteds() {
            return selecteds;
        }

        /**
         * @param selecteds the selecteds to set
         */
        public void setSelecteds(Map<Permission, Boolean> selecteds) {
            this.selecteds = selecteds;
        }

        /**
         * @return the permissionNoCategory
         */
        public List<Permission> getPermissionNoCategory() {
            return permissionNoCategory;
        }

        /**
         * @param permissionNoCategory the permissionNoCategory to set
         */
        public void setPermissionNoCategory(List<Permission> permissionNoCategory) {
            this.permissionNoCategory = permissionNoCategory;
        }

    }
}
