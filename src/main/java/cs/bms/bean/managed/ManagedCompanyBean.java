/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Company;
import cs.bms.model.CompanyGroup;
import cs.bms.model.DocumentNumbering;
import cs.bms.model.PaymentProof;
import cs.bms.model.WorkShift;
import cs.bms.service.interfac.ICompanyGroupService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDistrictService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IRegionService;
import cs.bms.service.interfac.ISubregionService;
import cs.bms.service.interfac.IWorkShiftService;
import gkfire.auditory.Auditory;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.hibernate.criterion.Disjunction;
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
public class ManagedCompanyBean extends AManagedBean<Company, ICompanyService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService mainService;
    @ManagedProperty(value = "#{companyGroupService}")
    protected ICompanyGroupService companyGroupService;
    @ManagedProperty(value = "#{districtService}")
    protected IDistrictService districtService;
    @ManagedProperty(value = "#{workShiftService}")
    protected IWorkShiftService workShiftService;
    @ManagedProperty(value = "#{regionService}")
    protected IRegionService regionService;
    @ManagedProperty(value = "#{documentNumberingService}")
    protected IDocumentNumberingService documentNumberingService;
    @ManagedProperty(value = "#{paymentProofService}")
    protected IPaymentProofService paymentProofService;
    @ManagedProperty(value = "#{subregionService}")
    protected ISubregionService subregionService;

    protected PaymentProofSearcher paymentProofSearcher;
    protected LocationSearcher locationSearcher;
    protected NumberingSearcher numberingSearcher;
    protected WorkShiftSearcher workShiftSearcher;
    protected CompanyGroupSearcher companyGroupSearcher;

    protected String name;
    protected String ruc;
    protected String description;
    protected Integer districtId;
    protected Integer companyGroupId;
    protected String city;
    protected String address;
    protected String phone1;
    protected String phone2;
    protected BigDecimal igvPercent;
    protected String title;
    protected String addressReference;
    protected Boolean buy;
    protected Boolean stored;
    protected Boolean sold;

    @PostConstruct
    public void init() {
        locationSearcher = new LocationSearcher();
        paymentProofSearcher = new PaymentProofSearcher();
        numberingSearcher = new NumberingSearcher();
        workShiftSearcher = new WorkShiftSearcher();
        companyGroupSearcher = new CompanyGroupSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        paymentProofSearcher.update();
        numberingSearcher.update();

    }

    @Override
    public boolean save() {
        boolean allow = super.save();
        if (allow) {
            workShiftSearcher.data.forEach(item -> {
                WorkShift workShift;
                if (item[0] != null) {
                    workShift = workShiftService.getById((Integer) item[0]);
                } else {
                    workShift = new WorkShift();
                }
                workShift.setCompany(selected);
                workShift.setName((String) item[1]);
                workShift.setTimeEntry((Date) item[2]);
                workShift.setTimeDeparture((Date) item[3]);
                workShift.setActive((Boolean) item[4]);
                Auditory.make(workShift, sessionBean.getCurrentUser());
                workShiftService.saveOrUpdate(workShift);
            });
            numberingSearcher.data.keySet().forEach(key -> {
                numberingSearcher.data.get(key).forEach(item -> {
                    DocumentNumbering documentNumbering = new DocumentNumbering();
                    documentNumbering.setId((Integer) item[0]);
                    documentNumbering.setPaymentProof(new PaymentProof(key));
                    documentNumbering.setRucCompany(selected.getRuc());
                    documentNumbering.setSerie((String) item[1]);
                    documentNumbering.setNumbering((Long) item[2]);
                    documentNumbering.setElectronic(true);
                    documentNumberingService.saveOrUpdate(documentNumbering);
                });
            });
            new PNotifyMessage("Datos guardados!!", "Se ha guardado los cambios", PNotifyMessage.Type.Success, "fa fa-save shaked animated", 3000L).execute();
        } else {
            new PNotifyMessage("ERROR AL GUARDAR", "Revise log del servidor para más detalles", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated", 3000L).execute();
        }
        return allow;
    }

    @Override
    protected void fillFields() {
        name = selected.getName();
        ruc = selected.getRuc();
        description = selected.getDescription();
        try {
            companyGroupId = selected.getCompanyGroup().getId();
        } catch (Exception e) {
            companyGroupId = null;
        }
        try {
            districtId = selected.getDistrict().getId();
            locationSearcher.subregionId = selected.getDistrict().getSubregion().getId();
            locationSearcher.regionId = selected.getDistrict().getSubregion().getRegion().getId();
        } catch (Exception e) {
            districtId = null;
            locationSearcher.subregionId = null;
            locationSearcher.regionId = null;
        }
        city = selected.getCity();
        address = selected.getAddress();
        phone1 = selected.getPhone1();
        phone2 = selected.getPhone2();
        igvPercent = selected.getIgvPercent();
        title = selected.getTitle();
        addressReference = selected.getAddressReference();
        buy = selected.getBuy();
        stored = selected.getStored();
        sold = selected.getSold();
        locationSearcher.updateRegions();
        locationSearcher.updateSubregions();
        locationSearcher.updateDistricts();
        workShiftSearcher.update();
        companyGroupSearcher.update();
        onLoad(true);
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setName(name.trim());
        selected.setRuc(ruc.trim());
        selected.setDescription(description.trim());
        try {
            selected.setDistrict(districtService.getById(districtId));
        } catch (Exception e) {
            selected.setDistrict(null);
        }
        if (companyGroupId != null) {
            selected.setCompanyGroup(new CompanyGroup(companyGroupId));
        } else {
            selected.setCompanyGroup(null);
        }
        selected.setCity(city.trim());
        selected.setAddress(address.trim());
        selected.setPhone1(phone1.trim());
        selected.setPhone2(phone2.trim());
        selected.setIgvPercent(igvPercent);
        selected.setTitle(title.trim());
        selected.setAddressReference(addressReference.trim());
        selected.setBuy(buy);
        selected.setStored(stored);
        selected.setSold(sold);
        Auditory.make(selected, sessionBean.getCurrentUser());
    }
    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">
    
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
     * @return the mainService
     */
    public ICompanyService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(ICompanyService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the districtService
     */
    public IDistrictService getDistrictService() {
        return districtService;
    }

    /**
     * @param districtService the districtService to set
     */
    public void setDistrictService(IDistrictService districtService) {
        this.districtService = districtService;
    }

    /**
     * @return the regionService
     */
    public IRegionService getRegionService() {
        return regionService;
    }

    /**
     * @param regionService the regionService to set
     */
    public void setRegionService(IRegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * @return the subregionService
     */
    public ISubregionService getSubregionService() {
        return subregionService;
    }

    /**
     * @param subregionService the subregionService to set
     */
    public void setSubregionService(ISubregionService subregionService) {
        this.subregionService = subregionService;
    }

    /**
     * @return the locationSearcher
     */
    public LocationSearcher getLocationSearcher() {
        return locationSearcher;
    }

    /**
     * @param locationSearcher the locationSearcher to set
     */
    public void setLocationSearcher(LocationSearcher locationSearcher) {
        this.locationSearcher = locationSearcher;
    }

    /**
     * @return the ruc
     */
    public String getRuc() {
        return ruc;
    }

    /**
     * @param ruc the ruc to set
     */
    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the districtId
     */
    public Integer getDistrictId() {
        return districtId;
    }

    /**
     * @param districtId the districtId to set
     */
    public void setDistrictId(Integer districtId) {
        this.districtId = districtId;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
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
     * @return the phone1
     */
    public String getPhone1() {
        return phone1;
    }

    /**
     * @param phone1 the phone1 to set
     */
    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    /**
     * @return the phone2
     */
    public String getPhone2() {
        return phone2;
    }

    /**
     * @param phone2 the phone2 to set
     */
    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     * @return the igvPercent
     */
    public BigDecimal getIgvPercent() {
        return igvPercent;
    }

    /**
     * @param igvPercent the igvPercent to set
     */
    public void setIgvPercent(BigDecimal igvPercent) {
        this.igvPercent = igvPercent;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the addressReference
     */
    public String getAddressReference() {
        return addressReference;
    }

    /**
     * @param addressReference the addressReference to set
     */
    public void setAddressReference(String addressReference) {
        this.addressReference = addressReference;
    }

    /**
     * @return the buy
     */
    public Boolean getBuy() {
        return buy;
    }

    /**
     * @param buy the buy to set
     */
    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    /**
     * @return the stored
     */
    public Boolean getStored() {
        return stored;
    }

    /**
     * @param stored the stored to set
     */
    public void setStored(Boolean stored) {
        this.stored = stored;
    }

    /**
     * @return the sold
     */
    public Boolean getSold() {
        return sold;
    }

    /**
     * @param sold the sold to set
     */
    public void setSold(Boolean sold) {
        this.sold = sold;
    }

    /**
     * @return the paymentProofService
     */
    public IPaymentProofService getPaymentProofService() {
        return paymentProofService;
    }

    /**
     * @param paymentProofService the paymentProofService to set
     */
    public void setPaymentProofService(IPaymentProofService paymentProofService) {
        this.paymentProofService = paymentProofService;
    }

    /**
     * @return the paymentProofSearcher
     */
    public PaymentProofSearcher getPaymentProofSearcher() {
        return paymentProofSearcher;
    }

    /**
     * @param paymentProofSearcher the paymentProofSearcher to set
     */
    public void setPaymentProofSearcher(PaymentProofSearcher paymentProofSearcher) {
        this.paymentProofSearcher = paymentProofSearcher;
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
     * @return the numberingSearcher
     */
    public NumberingSearcher getNumberingSearcher() {
        return numberingSearcher;
    }

    /**
     * @param numberingSearcher the numberingSearcher to set
     */
    public void setNumberingSearcher(NumberingSearcher numberingSearcher) {
        this.numberingSearcher = numberingSearcher;
    }

    /**
     * @return the workShiftService
     */
    public IWorkShiftService getWorkShiftService() {
        return workShiftService;
    }

    /**
     * @param workShiftService the workShiftService to set
     */
    public void setWorkShiftService(IWorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    /**
     * @return the workShiftSearcher
     */
    public WorkShiftSearcher getWorkShiftSearcher() {
        return workShiftSearcher;
    }

    /**
     * @param workShiftSearcher the workShiftSearcher to set
     */
    public void setWorkShiftSearcher(WorkShiftSearcher workShiftSearcher) {
        this.workShiftSearcher = workShiftSearcher;
    }

    /**
     * @return the companyGroupService
     */
    public ICompanyGroupService getCompanyGroupService() {
        return companyGroupService;
    }

    /**
     * @param companyGroupService the companyGroupService to set
     */
    public void setCompanyGroupService(ICompanyGroupService companyGroupService) {
        this.companyGroupService = companyGroupService;
    }

    /**
     * @return the companyGroupSearcher
     */
    public CompanyGroupSearcher getCompanyGroupSearcher() {
        return companyGroupSearcher;
    }

    /**
     * @param companyGroupSearcher the companyGroupSearcher to set
     */
    public void setCompanyGroupSearcher(CompanyGroupSearcher companyGroupSearcher) {
        this.companyGroupSearcher = companyGroupSearcher;
    }

    /**
     * @return the companyGroupId
     */
    public Integer getCompanyGroupId() {
        return companyGroupId;
    }

    /**
     * @param companyGroupId the companyGroupId to set
     */
    public void setCompanyGroupId(Integer companyGroupId) {
        this.companyGroupId = companyGroupId;
    }
    
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="PaymentProofSearcher">
    public class PaymentProofSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            if (getRuc() == null || getRuc().length() != 11) {
                data = Collections.EMPTY_LIST;
                return;
            }
            if (!getSold() && !getStored()) {
                data = Collections.EMPTY_LIST;
                return;
            }

            ProjectionList projection = Projections.projectionList()
                    .add(Projections.id())
                    .add(Projections.property("name"));
            CriterionList criterionList = new CriterionList();
            criterionList.add(Restrictions.eq("active", true));
            Disjunction d = Restrictions.or();
            if (getSold()) {
                d.add(Restrictions.eq("forSale", getSold()));
            }
            if (getStored()) {
                d.add(Restrictions.eq("forStored", getStored()));
            }
            criterionList.add(d);
            OrderList orderList = new OrderList();
            orderList.add(Order.asc("id"));
            data = getPaymentProofService().addRestrictionsVariant(projection, criterionList, orderList);
            numberingSearcher.update();
        }

        public List<Object[]> getData() {
            return data;
        }

    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="NumberingSearcher">
    public class NumberingSearcher implements java.io.Serializable {

        protected Map<Short, List<Object[]>> data;
        protected Short paymentProofId;
        protected int currentIndex;
        protected String serieCreated;
        protected DocumentNumbering documentNumbering;

        public void update() {
            data = new HashMap();
            if (getRuc() == null || getRuc().length() != 11) {
                return;
            }
            for (Object[] item : paymentProofSearcher.data) {
                List<Object[]> nData = getDocumentNumberingService().listHQL("SELECT "
                        + "dn.id,"
                        + "dn.serie,"
                        + "dn.numbering FROM DocumentNumbering dn WHERE dn.paymentProof.id = ?  and dn.rucCompany = ?", item[0], getRuc());
                data.put((Short) item[0], nData);
            }
        }

        public void edit(Short pp, int index) {
            currentIndex = index;
            Object[] item = data.get(pp).get(index);
            documentNumbering = new DocumentNumbering();
            documentNumbering.setId((Integer) item[0]);
            documentNumbering.setSerie((String) item[1]);
            documentNumbering.setPaymentProof(paymentProofService.getById(pp));
            documentNumbering.setNumbering((Long) item[2]);
        }

        public void save() {
            data.get(documentNumbering.getPaymentProof().getId()).set(currentIndex, new Object[]{
                documentNumbering.getId(),
                documentNumbering.getSerie(),
                documentNumbering.getNumbering()
            });
        }

        public void create() {
            List<Object[]> list = data.get(paymentProofId);
            for (Object[] item : list) {
                if (item[1].toString().equalsIgnoreCase(serieCreated)) {
                    new PNotifyMessage("ADVERTENCIA", "Nro de serie ya registrada", PNotifyMessage.Type.Warning, "fa fa-warning").execute();
                    return;
                }
            }

            data.get(paymentProofId).add(new Object[]{
                null,
                serieCreated,
                0L
            });
            serieCreated = "";
        }

        public Map<Short, List<Object[]>> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(Map<Short, List<Object[]>> data) {
            this.data = data;
        }

        /**
         * @return the serieCreated
         */
        public String getSerieCreated() {
            return serieCreated;
        }

        /**
         * @param serieCreated the serieCreated to set
         */
        public void setSerieCreated(String serieCreated) {
            this.serieCreated = serieCreated;
        }

        /**
         * @return the paymentProofId
         */
        public Short getPaymentProofId() {
            return paymentProofId;
        }

        /**
         * @param paymentProofId the paymentProofId to set
         */
        public void setPaymentProofId(Short paymentProofId) {
            this.paymentProofId = paymentProofId;
        }

        /**
         * @return the documentNumbering
         */
        public DocumentNumbering getDocumentNumbering() {
            return documentNumbering;
        }

        /**
         * @param documentNumbering the documentNumbering to set
         */
        public void setDocumentNumbering(DocumentNumbering documentNumbering) {
            this.documentNumbering = documentNumbering;
        }
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="WorkShiftSearcher">
    public class WorkShiftSearcher implements java.io.Serializable {

        protected List<Object[]> data;

        protected WorkShift workShift;

        public void update() {
            data = new ArrayList();
            if (selected.getId() != null) {
                data.addAll(workShiftService.listHQL("SELECT ws.id,ws.name,ws.timeEntry,ws.timeDeparture,ws.active FROM WorkShift ws WHERE ws.company = ? AND ws.active= true", selected));
            }
        }

        public void create() {
            workShift = new WorkShift();
        }

        public void edit(int index) {
            Object[] item = data.get(index);
            workShift = new WorkShift();
            workShift.setId((Integer) item[0]);
            workShift.setName((String) item[1]);
            workShift.setTimeEntry((Date) item[2]);
            workShift.setTimeDeparture((Date) item[3]);
            workShift.setActive((Boolean) item[4]);
        }

        public void delete(int index) {
            Object[] item = data.get(index);
            item[4] = false;
        }

        public void recovery(int index) {
            Object[] item = data.get(index);
            item[4] = true;
        }

        public void save() {
            if (workShift.getId() != null) {
                for (Object[] item : data) {
                    if ((Integer) item[0] == workShift.getId().intValue()) {
                        item[1] = workShift.getName();
                        item[2] = workShift.getTimeEntry();
                        item[3] = workShift.getTimeDeparture();
                        item[4] = workShift.getActive();
                    }
                }
            } else {
                data.add(new Object[]{
                    null,
                    workShift.getName(),
                    workShift.getTimeEntry(),
                    workShift.getTimeDeparture(),
                    true
                });
            }
        }

        /**
         * @return the data
         */
        public List<Object[]> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }

        /**
         * @return the workShift
         */
        public WorkShift getWorkShift() {
            return workShift;
        }

        /**
         * @param workShift the workShift to set
         */
        public void setWorkShift(WorkShift workShift) {
            this.workShift = workShift;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="LocationSearcher">
    public class LocationSearcher implements java.io.Serializable {

        private List<Object[]> regionData;
        private List<Object[]> subregionData;
        private List<Object[]> districtData;

        private Integer regionId;
        private Integer subregionId;

        public void updateRegions() {
            regionData = getRegionService().listHQL("SELECT r.id, r.name FROM Region r WHERE r.country.id = 1 ");
        }

        public void updateSubregions() {
            if (regionId == null) {
                subregionId = null;
                subregionData = Collections.EMPTY_LIST;
                return;
            }
            subregionData = getSubregionService().listHQL("SELECT sr.id, sr.name FROM Subregion sr WHERE sr.region.id = ? ", regionId);
        }

        public void updateDistricts() {
            if (subregionId == null) {
                setDistrictId(null);
                districtData = Collections.EMPTY_LIST;
                return;
            }
            districtData = getDistrictService().listHQL("SELECT d.id, d.name FROM District d WHERE d.subregion.id = ? ", subregionId);
        }

        /**
         * @return the regionData
         */
        public List<Object[]> getRegionData() {
            return regionData;
        }

        /**
         * @param regionData the regionData to set
         */
        public void setRegionData(List<Object[]> regionData) {
            this.regionData = regionData;
        }

        /**
         * @return the subregionData
         */
        public List<Object[]> getSubregionData() {
            return subregionData;
        }

        /**
         * @param subregionData the subregionData to set
         */
        public void setSubregionData(List<Object[]> subregionData) {
            this.subregionData = subregionData;
        }

        /**
         * @return the districtData
         */
        public List<Object[]> getDistrictData() {
            return districtData;
        }

        /**
         * @param districtData the districtData to set
         */
        public void setDistrictData(List<Object[]> districtData) {
            this.districtData = districtData;
        }

        /**
         * @return the regionId
         */
        public Integer getRegionId() {
            return regionId;
        }

        /**
         * @param regionId the regionId to set
         */
        public void setRegionId(Integer regionId) {
            this.regionId = regionId;
        }

        /**
         * @return the subregionId
         */
        public Integer getSubregionId() {
            return subregionId;
        }

        /**
         * @param subregionId the subregionId to set
         */
        public void setSubregionId(Integer subregionId) {
            this.subregionId = subregionId;
        }

    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="CompanyGroupSearcher">
    public class CompanyGroupSearcher implements java.io.Serializable {

        protected List<Object[]> data;

        public void update() {
            data = companyGroupService.getBasicData();
        }

        /**
         * @return the data
         */
        public List<Object[]> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }
        
    }
    
//</editor-fold>
}
