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
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IIdentityDocumentService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
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
public class ManagedSupplierBean extends AManagedBean<Actor, IActorService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService mainService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;

    protected IdentityDocumentSearcher identityDocumentSearcher;
    protected Short identityDocumentId;
    protected String address;
    protected String other;
    protected Boolean customer;
    protected Boolean supplier;
    protected Long points;
    protected String representative;
    protected Short length;
    protected String identityNumber;
    protected String name;

    protected Boolean permanentSupplier;
    protected Boolean permanentCustomer;
    protected Boolean allowedDocument;

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

    @Override
    public boolean save() {
        if (mainService.verifyIdentityNumber(identityNumber.trim(), selected.getId())) {
            PNotifyMessage.errorMessage("La identificacion \""+identityNumber.trim()+"\" ya esta registrado");
            saved = false;
        } else {
            try {
                String content = selected.getId() == null ? "Se ha creado un nuevo actor" : "Se ha actualizado los datos";
                saved = super.save(); //To change body of generated methods, choose Tools | Templates.
                PNotifyMessage.saveMessage(content);
            } catch (Exception e) {
                PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(e));
                saved = false;
            }
        }
        return saved;
    }

    public void create(boolean isCustomer) {
        create();
    }

    @Override
    protected void fillFields() {
        try {
            identityDocumentId = selected.getIdentityDocument().getId();
            length = selected.getIdentityDocument().getLength_();
        } catch (Exception e) {
            identityDocumentId = null;
            length = null;
        }
        identityNumber = selected.getIdentityNumber();
        name = selected.getName();
        other = selected.getOther();
        representative = selected.getRepresentative();
        address = selected.getAddress();
        customer = selected.getCustomer();
        supplier = selected.getSupplier();
        onLoad(true);
        identityDocumentSearcher.update();
        createAgain = selected.getId() == null;
        allowedDocument = selected.getId() != null;
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

        selected.setSupplier(true);
        try {
            selected.setAddress(address.trim());
        } catch (Exception e) {
            selected.setAddress(address);
        }
        try {
            selected.setRepresentative(representative.trim());
        } catch (Exception e) {
            selected.setRepresentative(representative);
        }
        selected.setIdentityNumber(identityNumber.trim());
        selected.setName(name.trim());
        if (other == null) {
            other = "";
        }
        selected.setOther(other.trim().length() == 0 ? null : other.trim());
        selected.setSupplier(supplier);
        selected.setCustomer(customer);
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

        Actor a = (Actor) mainService.getByHQL("FROM Actor a WHERE a.identityNumber LIKE ? AND a.identityDocument.id = ? AND a.id  <> ?", identityNumber, identityDocumentId, selected.getId() == null ? -1L : selected.getId());
//        if (a == null && identityNumber == selected.getIdentityNumber()) {
//            a = selected;
//        }
        if (a != null) {
            new PNotifyMessage("ERROR", "Ya existe alguien con ese documento", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated").execute();
            allowedDocument = false;
//            if (a.getSupplier() && a.getId().longValue() != selected.getId()) {
//                identityNumber = null;
//                new PNotifyMessage("ERROR", "Ya existe un cliente con ese documento", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated").execute();
//                return;
//            } else {
//                name = a.getName();
//                address = a.getAddress();
//                representative = a.getRepresentative();
//                selected.setId(a.getId());
//                points = selected.getPoints();
//                Object[] d = (Object[]) mainService.getByHQL("SELECT a.createUser,a.createDate FROM Actor a WHERE a.id = ?", selected.getId());
//                selected.setCreateUser((User) d[0]);
//                selected.setCreateDate((Date) d[1]);
//                selected.setCustomer(a.getCustomer());
//            }
//        } else {
//            name = "";
//            address = "";
//            representative = "";
//            selected.setId(null);
//            selected.setCustomer(false);
//        }
        }
        allowedDocument = true;
    }

//    public void nullify() {
//        identityNumber = null;
//    }
    /**
     * @return the customer
     */
    public Boolean getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(Boolean customer) {
        this.customer = customer;
    }

    /**
     * @return the supplier
     */
    public Boolean getSupplier() {
        return supplier;
    }

    /**
     * @param supplier the supplier to set
     */
    public void setSupplier(Boolean supplier) {
        this.supplier = supplier;
    }

    /**
     * @return the permanentSupplier
     */
    public Boolean getPermanentSupplier() {
        return permanentSupplier;
    }

    /**
     * @param permanentSupplier the permanentSupplier to set
     */
    public void setPermanentSupplier(Boolean permanentSupplier) {
        this.permanentSupplier = permanentSupplier;
    }

    /**
     * @return the permanentCustomer
     */
    public Boolean getPermanentCustomer() {
        return permanentCustomer;
    }

    /**
     * @param permanentCustomer the permanentCustomer to set
     */
    public void setPermanentCustomer(Boolean permanentCustomer) {
        this.permanentCustomer = permanentCustomer;
    }

    /**
     * @return the allowedDocument
     */
    public Boolean getAllowedDocument() {
        return allowedDocument;
    }

    /**
     * @param allowedDocument the allowedDocument to set
     */
    public void setAllowedDocument(Boolean allowedDocument) {
        this.allowedDocument = allowedDocument;
    }

    public class IdentityDocumentSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getIdentityDocumentService().listHQL("SELECT e.id,e.name,e.length_,e.abbr FROM IdentityDocument e WHERE e.active = true");
        }

        public void changeLength() {
            data.forEach(item -> {
                if ((Short) item[0] == getIdentityDocumentId().shortValue()) {
                    setLength((Short) item[2]);
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

    /**
     * @return the other
     */
    public String getOther() {
        return other;
    }

    /**
     * @param other the other to set
     */
    public void setOther(String other) {
        this.other = other;
    }

}
