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
import cs.bms.model.User;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IIdentityDocumentService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedCustomerBean extends AManagedBean<Actor, IActorService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;
    @ManagedProperty(value = "#{actorService}")
    private IActorService mainService;
    @ManagedProperty(value = "#{identityDocumentService}")
    private IIdentityDocumentService identityDocumentService;

    private IdentityDocumentSearcher identityDocumentSearcher;
    private Short identityDocumentId;
    private String address;
    private Long points;
    private String representative;
    private Short length;
    private String identityNumber;
    private String name;

    @PostConstruct
    public void init() {
        identityDocumentSearcher = new IdentityDocumentSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        identityDocumentSearcher.update();
    }

    public void create(boolean isCustomer) {
        create();
    }

    @Override
    protected void fillFields() {
        try {
            identityDocumentId = selected.getIdentityDocument().getId();
        } catch (Exception e) {
            identityDocumentId = null;
            length = null;
        }
        identityNumber = selected.getIdentityNumber();
        name = selected.getName();
        representative = selected.getRepresentative();
        address = selected.getAddress();
        onLoad(true);
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        try {
            selected.setIdentityDocument(identityDocumentService.getById(identityDocumentId));
        } catch (Exception e) {
            selected.setIdentityDocument(null);
        }
        selected.setCustomer(true);
        selected.setAddress(address.trim());
        selected.setRepresentative(representative.trim());
        selected.setIdentityNumber(identityNumber);
        selected.setName(name);
        Auditory.make(selected, sessionBean.getCurrentUser());
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
     * @return the mainService
     */
    public IActorService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IActorService mainService) {
        this.mainService = mainService;
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
     * @return the identityDocumentSearcher
     */
    public IdentityDocumentSearcher getIdentityDocumentSearcher() {
        return identityDocumentSearcher;
    }

    /**
     * @param identityDocumentSearcher the identityDocumentSearcher to set
     */
    public void setIdentityDocumentSearcher(IdentityDocumentSearcher identityDocumentSearcher) {
        this.identityDocumentSearcher = identityDocumentSearcher;
    }

    /**
     * @return the identityNumber
     */
    public String getIdentityNumber() {
        return identityNumber;
    }

    /**
     * @param identityNumber the identityNumber to set
     */
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
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
     * @return the identityDocumentId
     */
    public Short getIdentityDocumentId() {
        return identityDocumentId;
    }

    /**
     * @param identityDocumentId the identityDocumentId to set
     */
    public void setIdentityDocumentId(Short identityDocumentId) {
        this.identityDocumentId = identityDocumentId;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the points
     */
    public Long getPoints() {
        return points;
    }

    /**
     * @param points the points to set
     */
    public void setPoints(Long points) {
        this.points = points;
    }

    /**
     * @return the representative
     */
    public String getRepresentative() {
        return representative;
    }

    /**
     * @param representative the representative to set
     */
    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    /**
     * @return the length
     */
    public Short getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(Short length) {
        this.length = length;
    }

    public void search() {
        Actor a = (Actor) mainService.getByHQL("FROM Actor a WHERE a.identityNumber LIKE ? AND a.identityDocument.id = ?", identityNumber, identityDocumentId);
        if (a != null) {
            if (a.getCustomer()) {
                identityNumber = null;
                new PNotifyMessage("ERROR", "Ya existe un cliente con ese documento", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated").execute();
                return;
            } else {
                name = a.getName();
                address = a.getAddress();
                representative = a.getRepresentative();
                selected.setId(a.getId());
                Object[] d = (Object[]) mainService.getByHQL("SELECT a.createUser,a.createDate FROM Actor a WHERE a.id = ?",selected.getId());
                selected.setCreateUser((User) d[0]);
                selected.setCreateDate((Date) d[1]);
                selected.setSupplier(a.getSupplier());
            }
        } else {
            new PNotifyMessage("Avertencia", "Este documento no se ha encontrado", PNotifyMessage.Type.Warning, "fa fa-warning shaked animated").execute();
            name = "";
            address = "";
            representative = "";
            selected.setId(null);
            selected.setSupplier(false);
        }
    }

    public void nullify() {
        name = null;
        identityNumber = null;
        address = null;
        representative = null;
        selected.setId(null);
        selected.setSupplier(false);
    }

    public class IdentityDocumentSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getIdentityDocumentService().listHQL("SELECT e.id,e.name,e.length_ FROM IdentityDocument e WHERE e.active = true");
        }

        public void changeLength() {
            data.forEach(item -> {
                if ((Short) item[0] == identityDocumentId.shortValue()) {
                    length = (Short) item[2];
                    return;
                }
            });
        }

        /**
         * @return the identityDocuments
         */
        public List<Object[]> getData() {
            return data;
        }
    }

}
