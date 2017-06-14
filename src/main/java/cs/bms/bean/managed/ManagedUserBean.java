/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Actor;
import cs.bms.model.DocumentNumbering;
import cs.bms.model.IdentityDocument;
import cs.bms.model.Rol;
import cs.bms.model.SpecialPermission;
import cs.bms.model.User;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IRolService;
import cs.bms.service.interfac.ISpecialPermissionService;
import cs.bms.service.interfac.IUserService;
import cs.bms.util.AESKeys;
import cs.bms.util.ActorSearcher;
import gkfire.auditory.Auditory;
import gkfire.util.AES;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author CTIC
 */
@ManagedBean
@SessionScoped
public class ManagedUserBean extends AManagedBean<User, IUserService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{userService}")
    protected IUserService mainService;
    @ManagedProperty(value = "#{rolService}")
    protected IRolService rolService;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{specialPermissionService}")
    protected ISpecialPermissionService specialPermissionService;
    @ManagedProperty(value = "#{documentNumberingService}")
    protected IDocumentNumberingService documentNumberingService;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;

    protected RolSearcher rolSearcher;
    protected EmployeeSearcher employeeSearcher;
    protected SpecialPermissionSearcher specialPermissionSearcher;
    protected DocumentNumberingSearcher documentNumberingSearcher;
    protected Long idEmployee;
    protected Date lastLogin;
    protected String password;
    protected boolean staff;
    protected boolean superUser;
    protected String username;

    protected List<Integer> rols;
    protected List<Integer> documentNumberings;
    protected List<Long> specialPermissions;

    @PostConstruct
    public void init() {
        rolSearcher = new RolSearcher();
        specialPermissionSearcher = new SpecialPermissionSearcher();
        documentNumberingSearcher = new DocumentNumberingSearcher();
        employeeSearcher = new EmployeeSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();

    }

    @Override
    public boolean save() {
        if (getMainService().existUsername(username.trim(), selected.getId())) {
            PNotifyMessage.errorMessage("El nombre de usuario \"" + username + "\" ya esta registrado");
            saved = false;
        } else {
            try {
                String content = getSelected().getId() != null ? "Se ha actualizado un usuario" : "Se ha registrado un usuario";
                super.save();
                PNotifyMessage.saveMessage(content);
                saved = true;
            } catch (Exception e) {
                PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(e));
                saved = false;
            }
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        lastLogin = selected.getLastLogin();
        username = selected.getUsername();
        try {
            password = AES.decrypt(selected.getPassword(),AESKeys.USER_PASSWORD);
        } catch (Exception ex) {
            Logger.getLogger(ManagedUserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        superUser = selected.getSuperUser();
        try {
            idEmployee = selected.getEmployee().getId();
        } catch (Exception e) {
            idEmployee = null;
        }
        rols = rolService.listHQL("SELECT r.id FROM Rol r join r.users as u WHERE u.id = ?", selected.getId() == null ? -1 : selected.getId());
        specialPermissions = rolService.listHQL("SELECT sp.identifier FROM SpecialPermission sp join sp.users as u WHERE u.id = ? AND sp.entityName LIKE ?", selected.getId() == null ? -1 : selected.getId(), "Company");
        documentNumberings = documentNumberingService.listHQL("SELECT dn.id FROM DocumentNumbering dn join dn.users as u WHERE u.id = ?", selected.getId() == null ? -1 : selected.getId());
        rolSearcher.update();
        specialPermissionSearcher.update();
        documentNumberingSearcher.update();
        if (idEmployee != null) {
            employeeSearcher.setActor(selected.getEmployee());
            employeeSearcher.setIdentityNumber(selected.getEmployee().getIdentityNumber());
        } else {
            employeeSearcher.setActor(null);
            employeeSearcher.setIdentityNumber(null);
        }
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setLastLogin(getLastLogin());
        try {
            selected.setPassword(AES.encrypt(password, AESKeys.USER_PASSWORD));
        } catch (Exception ex) {
            Logger.getLogger(ManagedUserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        selected.setSuperUser(isSuperUser());
        selected.setUsername(getUsername());
        selected.setRols(new ArrayList());
        for (Object[] r : rolSearcher.getRols()) {
            if (rolSearcher.selecteds.get((Integer) r[0])) {
                selected.getRols().add(new Rol((Integer) r[0]));
            }
        }
        selected.setSpecialPermissions(new ArrayList());
        for (Object[] c : specialPermissionSearcher.getCompanies()) {
            if (specialPermissionSearcher.selecteds.get((Integer) c[0])) {
                Short idSP = (Short) mainService.getByHQL("SELECT sp.id FROM SpecialPermission sp WHERE sp.entityName LIKE ? AND sp.identifier = ?", "Company", ((Integer) c[0]).longValue());
                SpecialPermission sp = null;
                if (idSP == null) {
                    sp = new SpecialPermission();
                    sp.setEntityName("Company");
                    sp.setIdentifier(((Integer) c[0]).longValue());
                    specialPermissionService.saveOrUpdate(sp);
                } else {
                    sp = new SpecialPermission(idSP);
                }
                selected.getSpecialPermissions().add(sp);
            }
        }
        selected.setDocumentNumberings(new ArrayList());

        for (String ruc : documentNumberingSearcher.getRucs()) {
            for (Object[] item : documentNumberingSearcher.data.get(ruc)) {
                List<Object[]> series = (List<Object[]>) item[2];
                for (Object[] serie : series) {
                    if (documentNumberingSearcher.selecteds.get((Integer) serie[0])) {
                        selected.getDocumentNumberings().add(new DocumentNumbering((Integer) serie[0]));
                    }
                }
            }
        }
        selected.setEmployee(employeeSearcher.getActor());
        Auditory.make(selected, getSessionBean().getCurrentUser());
    }
    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">

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
     * @return the mainService
     */
    public IUserService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IUserService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the navigationBean
     */
    @Override
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
     * @return the lastLogin
     */
    public Date getLastLogin() {
        return lastLogin;
    }

    /**
     * @param lastLogin the lastLogin to set
     */
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the staff
     */
    public boolean isStaff() {
        return staff;
    }

    /**
     * @param staff the staff to set
     */
    public void setStaff(boolean staff) {
        this.staff = staff;
    }

    /**
     * @return the superUser
     */
    public boolean isSuperUser() {
        return superUser;
    }

    /**
     * @param superUser the superUser to set
     */
    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    //</editor-fold>

    /**
     * @return the rolSearcher
     */
    public RolSearcher getRolSearcher() {
        return rolSearcher;
    }

    /**
     * @param rolSearcher the rolSearcher to set
     */
    public void setRolSearcher(RolSearcher rolSearcher) {
        this.rolSearcher = rolSearcher;
    }

    public class RolSearcher implements java.io.Serializable {

        private Map<Integer, Boolean> selecteds;
        private List<Object[]> rols;

        public void update() {
            selecteds = new HashMap();
            rols = rolService.listHQL("SELECT r.id,r.name FROM Rol r WHERE r.active = true ORDER BY r.id");
            for (Object[] rol : rols) {
                selecteds.put((Integer) rol[0], ManagedUserBean.this.rols.contains(((Integer) rol[0])));
            }
        }

        /**
         * @return the selecteds
         */
        public Map<Integer, Boolean> getSelecteds() {
            return selecteds;
        }

        /**
         * @param selecteds the selecteds to set
         */
        public void setSelecteds(Map<Integer, Boolean> selecteds) {
            this.selecteds = selecteds;
        }

        /**
         * @return the rols
         */
        public List<Object[]> getRols() {
            return rols;
        }

        /**
         * @param rols the rols to set
         */
        public void setRols(List<Object[]> rols) {
            this.rols = rols;
        }

    }

    public class SpecialPermissionSearcher implements java.io.Serializable {

        protected Map<Integer, Boolean> selecteds;
        protected List<Object[]> companies;

        public void update() {
            selecteds = new HashMap();
            companies = companyService.listHQL("SELECT c.id,c.name,c.city,c.address FROM Company c WHERE c.active = true ORDER BY c.id");
            for (Object[] item : companies) {
                selecteds.put((Integer) item[0], ManagedUserBean.this.specialPermissions.contains(((Integer) item[0]).longValue()));
            }
        }

        /**
         * @return the selecteds
         */
        public Map<Integer, Boolean> getSelecteds() {
            return selecteds;
        }

        /**
         * @param selecteds the selecteds to set
         */
        public void setSelecteds(Map<Integer, Boolean> selecteds) {
            this.selecteds = selecteds;
        }

        /**
         * @return the companies
         */
        public List<Object[]> getCompanies() {
            return companies;
        }

        /**
         * @param companies the companies to set
         */
        public void setCompanies(List<Object[]> companies) {
            this.companies = companies;
        }

    }

    public class EmployeeSearcher extends ActorSearcher {

        @Override
        protected Actor searchActorInService() {
            return (Actor) actorService.getByHQL("FROM Actor a WHERE a.identityNumber LIKE ?", identityNumber);
        }

        @Override
        protected void saveOrUpdateByDNI(String name) {
            actor = searchActorInService();
            if (actor == null) {
                actor = new Actor();
                actor.setIdentityNumber(identityNumber);
                actor.setIdentityDocument((IdentityDocument) identityDocumentService.getByHQL("FROM IdentityDocument d WHERE d.length_ = ?", new Short(identityNumber.length() + "")));
            }
            actor.setName(name);
            Auditory.make(actor, sessionBean.getCurrentUser());
            actorService.saveOrUpdate(actor);
        }

        @Override
        protected void caseRUC() {
        }

        @Override
        protected void saveOrUpdateByRUC(String[] data) {
        }

        @Override
        public void search() {
            super.search();
            if (actor != null) {
                actor.setSupplier(Boolean.TRUE);
            }
        }

        public String getExist() {
            return actor == null ? null : actor.toString();
        }

        public void setExist(String cust) {
        }

        @Override
        public void searchByWeb() {
            try {
                super.searchByWeb();
            } catch (Exception ex) {
                PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(ex));
                webSearchValid = false;
                actor = null;
                captcha = "";
                identitySearch.init(identityNumber.length());
                ex.printStackTrace();
            }

        }

    }

    public class DocumentNumberingSearcher implements java.io.Serializable {

        protected Map<Integer, Boolean> selecteds;
        protected List<String> rucs;
        protected Map<String, List<Object[]>> data;

        public void update() {
            selecteds = new HashMap();
            data = new HashMap();
            rucs = documentNumberingService.listHQL("SELECT DISTINCT dn.rucCompany  FROM DocumentNumbering dn");
            rucs.forEach(ruc -> {
                List<Object[]> documents = documentNumberingService.listHQL("SELECT DISTINCT dn.paymentProof.id,dn.paymentProof.name,dn.paymentProof.id  FROM DocumentNumbering dn WHERE dn.rucCompany LIKE ?  AND (dn.paymentProof.forSale = true OR dn.paymentProof.forStored = true)", ruc);
                documents.forEach(d -> {
                    List<Object[]> series = documentNumberingService.listHQL("SELECT dn.id,dn.serie FROM DocumentNumbering dn WHERE dn.rucCompany LIKE ? AND dn.paymentProof.id = ?", ruc, d[0]);
                    series.forEach(serie -> {
                        selecteds.put((Integer) serie[0], ManagedUserBean.this.documentNumberings.contains(((Integer) serie[0])));
                    });
                    d[2] = series;
                });
                data.put(ruc, documents);
            });
        }

        /**
         * @return the selecteds
         */
        public Map<Integer, Boolean> getSelecteds() {
            return selecteds;
        }

        /**
         * @param selecteds the selecteds to set
         */
        public void setSelecteds(Map<Integer, Boolean> selecteds) {
            this.selecteds = selecteds;
        }

        /**
         * @return the data
         */
        public Map<String, List<Object[]>> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(Map<String, List<Object[]>> data) {
            this.data = data;
        }

        /**
         * @return the rucs
         */
        public List<String> getRucs() {
            return rucs;
        }

        /**
         * @param rucs the rucs to set
         */
        public void setRucs(List<String> rucs) {
            this.rucs = rucs;
        }

    }

    /**
     * @return the idEmployee
     */
    public Long getIdEmployee() {
        return idEmployee;
    }

    /**
     * @param idEmployee the idEmployee to set
     */
    public void setIdEmployee(Long idEmployee) {
        this.idEmployee = idEmployee;
    }

    /**
     * @return the rols
     */
    public List<Integer> getRols() {
        return rols;
    }

    /**
     * @param rols the rols to set
     */
    public void setRols(List<Integer> rols) {
        this.rols = rols;
    }

    /**
     * @return the actorService
     */
    public IActorService getActorService() {
        return actorService;
    }

    /**
     * @param actorService the actorService to set
     */
    public void setActorService(IActorService actorService) {
        this.actorService = actorService;
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

    /**
     * @return the specialPermissionSearcher
     */
    public SpecialPermissionSearcher getSpecialPermissionSearcher() {
        return specialPermissionSearcher;
    }

    /**
     * @param specialPermissionSearcher the specialPermissionSearcher to set
     */
    public void setSpecialPermissionSearcher(SpecialPermissionSearcher specialPermissionSearcher) {
        this.specialPermissionSearcher = specialPermissionSearcher;
    }

    /**
     * @return the specialPermissions
     */
    public List<Long> getSpecialPermissions() {
        return specialPermissions;
    }

    /**
     * @param specialPermissions the specialPermissions to set
     */
    public void setSpecialPermissions(List<Long> specialPermissions) {
        this.specialPermissions = specialPermissions;
    }

    /**
     * @return the specialPermissionService
     */
    public ISpecialPermissionService getSpecialPermissionService() {
        return specialPermissionService;
    }

    /**
     * @param specialPermissionService the specialPermissionService to set
     */
    public void setSpecialPermissionService(ISpecialPermissionService specialPermissionService) {
        this.specialPermissionService = specialPermissionService;
    }

    /**
     * @return the employeeSearcher
     */
    public EmployeeSearcher getEmployeeSearcher() {
        return employeeSearcher;
    }

    /**
     * @param employeeSearcher the employeeSearcher to set
     */
    public void setEmployeeSearcher(EmployeeSearcher employeeSearcher) {
        this.employeeSearcher = employeeSearcher;
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

    /**
     * @return the documentNumberingService
     */
    public IDocumentNumberingService getDocumentNumberingService() {
        return documentNumberingService;
    }

    /**
     * @param documentNumberingService the documentNumberingService to set
     */
    public void setDocumentNumberingService(IDocumentNumberingService documentNumberingService) {
        this.documentNumberingService = documentNumberingService;
    }

    /**
     * @return the documentNumberings
     */
    public List<Integer> getDocumentNumberings() {
        return documentNumberings;
    }

    /**
     * @param documentNumberings the documentNumberings to set
     */
    public void setDocumentNumberings(List<Integer> documentNumberings) {
        this.documentNumberings = documentNumberings;
    }

    /**
     * @return the documentNumberingSearcher
     */
    public DocumentNumberingSearcher getDocumentNumberingSearcher() {
        return documentNumberingSearcher;
    }

    /**
     * @param documentNumberingSearcher the documentNumberingSearcher to set
     */
    public void setDocumentNumberingSearcher(DocumentNumberingSearcher documentNumberingSearcher) {
        this.documentNumberingSearcher = documentNumberingSearcher;
    }
}
