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
import cs.bms.model.IdentityDocument;
import cs.bms.model.PaymentProof;
import cs.bms.model.Product;
import cs.bms.model.StockReduction;
import cs.bms.model.StockReductionDetail;
import cs.bms.model.Ubigeo;
import cs.bms.model.UoM;
import cs.bms.service.DistrictService;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IStockReductionDetailService;
import cs.bms.service.interfac.IStockReductionService;
import cs.bms.service.interfac.IStockService;
import cs.bms.service.interfac.IUoMService;
import cs.bms.util.ActorSearcher;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedStockReductionBean extends AManagedBean<StockReduction, IStockReductionService> implements ILoadable {

    @ManagedProperty(value = "#{stockReductionService}")
    protected IStockReductionService mainService;
    @ManagedProperty(value = "#{paymentProofService}")
    protected IPaymentProofService paymentProofService;
    @ManagedProperty(value = "#{stockReductionDetailService}")
    protected IStockReductionDetailService stockReductionDetailService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{stockService}")
    protected IStockService stockService;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{uomService}")
    protected IUoMService uoMService;
    @ManagedProperty(value = "#{productService}")
    protected IProductService productService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    @ManagedProperty(value = "#{managedSupplierBean}")
    protected ManagedSupplierBean managedSupplierBean;
    @ManagedProperty(value = "#{districtService}")
    protected DistrictService districtService;
    
    protected Short paymentProofId;
    protected String serie;
    protected String documentNumber;
    protected Date dateIssue;
    protected Long responsibleId;

    protected DetailSearcher detailSearcher;
    protected StockSearcher stockSearcher;
    protected ResponsibleSearcher responsibleSearcher;

    @Override
    public void delete(Serializable id) {
        StockReduction s = mainService.getById((Long) id);
        Auditory.make(s, sessionBean.getCurrentUser());
        ((List<Object[]>) stockReductionDetailService.listHQL(""
                + "SELECT "
                + "srd.quantity,"
                + "srd.product.id FROM StockReductionDetail srd "
                + "WHERE srd.stockReduction = ?", s)).forEach(item -> {
                    try {
                        stockService.updateHQL("UPDATE Stock SET quantity = (quantity + ?) WHERE product = ? AND company = ?", item[0], item[1], selected.getCompany());
                    } catch (Exception ex) {
                        Logger.getLogger(ManagedStockReductionBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
        mainService.delete(s);
    }

    @Override
    public void recovery(Serializable id) {
        StockReduction s = mainService.getById((Long) id);
        s.setActive(true);
        ((List<Object[]>) stockReductionDetailService.listHQL(""
                + "SELECT "
                + "srd.quantity,"
                + "srd.product.id FROM StockReductionDetail srd "
                + "WHERE srd.stockReduction = ?", s)).forEach(item -> {
                    try {
                        stockService.updateHQL("UPDATE Stock SET quantity = (quantity - ?) WHERE product = ? AND company = ?", item[0], item[1], selected.getCompany());
                    } catch (Exception ex) {
                        Logger.getLogger(ManagedStockReductionBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
        mainService.update(s);
    }

    @PostConstruct
    public void init() {
        stockSearcher = new StockSearcher();
        responsibleSearcher = new ResponsibleSearcher();
        detailSearcher = new DetailSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
    }

    @Override
    public boolean save() {
        boolean allow = super.save();
        if (allow) {
            detailSearcher.removed.forEach(item -> {
                StockReductionDetail detail = new StockReductionDetail();
                detail.setId((Long) item[0]);
                detail.setQuantity((BigDecimal) item[1]);
                detail.setProduct(new Product((Long) item[5]));
                stockReductionDetailService.delete(detail);
                try {
                    stockService.updateHQL("UPDATE Stock SET quantity = (quantity + ?) WHERE product = ? AND company = ?", detail.getQuantity(), detail.getProduct(), selected.getCompany());
                } catch (Exception ex) {
                    Logger.getLogger(ManagedSaleBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            detailSearcher.data.forEach(item -> {
                
//                        item[6], //ID
//                        item[5], //Cantidad
//                        item[8], //UOM ID
//                        item[4], //UOM ABBR
//                        item[2], //PRODUCT NAME
//                        item[7] //PRODUCT ID
                StockReductionDetail detail = new StockReductionDetail();
                detail.setId((Long) item[0]);
                detail.setQuantity((BigDecimal) item[1]);
                detail.setProduct(new Product((Long) item[5]));
                detail.setStockReduction(selected);
                detail.setProductName((String) item[4]);
                detail.setUom(new UoM((Integer) item[2]));
                detail.setUnitCost((BigDecimal) productService.getByHQL("SELECT p.basicCost FROM Product p WHERE p.id = ?", item[5]));

                if (detail.getId() != null) {
                    BigDecimal quantity = (BigDecimal) stockReductionDetailService.getByHQL("SELECT srd.quantity FROM StockReductionDetail srd WHERE srd.id = ?", detail.getId());
                    try {
                        stockService.updateHQL("UPDATE Stock SET quantity = (quantity + ?) WHERE product = ? AND company = ?", quantity, detail.getProduct(), selected.getCompany());
                    } catch (Exception ex) {
                        Logger.getLogger(ManagedSaleBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    stockService.updateHQL("UPDATE Stock SET quantity = (quantity - ?) WHERE product = ? AND company = ?", detail.getQuantity(), detail.getProduct(), selected.getCompany());
                } catch (Exception ex) {
                    Logger.getLogger(ManagedSaleBean.class.getName()).log(Level.SEVERE, null, ex);
                }
                stockReductionDetailService.saveOrUpdate(detail);
            });
        }
        new PNotifyMessage("Datos guardados!!", "Se ha creado el movimiento " + serie + "-" + documentNumber, PNotifyMessage.Type.Success, "fa fa-save shaked animated", 3000L).execute();
        //BeanUtil.exceuteJS("window.open('" + BeanUtil.getRequest().getContextPath() + "/service/print_ism.xhtml?id=" + selected.getId() + "')");
        return allow;
    }

    @Override
    protected void fillFields() {
        try {
            paymentProofId = selected.getPaymentProof().getId();
        } catch (Exception e) {
            paymentProofId = null;
        }
        serie = selected.getSerie();
        documentNumber = selected.getDocumentNumber();
        
        try {
            responsibleId = selected.getResponsible().getId();
        } catch (NullPointerException npe) {
            responsibleId = null;
        }
        
        if (responsibleId != null) {
            responsibleSearcher.setActor(selected.getResponsible());
            responsibleSearcher.setIdentityNumber(selected.getResponsible().getIdentityNumber());
        } else {
            responsibleSearcher.setActor(null);
            responsibleSearcher.setIdentityNumber(null);
        }
        try {
            responsibleId = selected.getResponsible().getId();
           
        } catch (Exception e) {
            responsibleId = null;
        }
        dateIssue = selected.getDateIssue();
        onLoad(true);
        detailSearcher.update();
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        try {
            selected.setPaymentProof((PaymentProof) paymentProofService.getByHQL("FROM PaymentProof pp WHERE pp.code LIKE ?", "00"));
        } catch (Exception e) {
            selected.setPaymentProof(null);
        }
        selected.setSerie(serie);
        selected.setDocumentNumber(String.format("%08d", Long.parseLong(documentNumber)));

        try {
            selected.setResponsible(actorService.getById(responsibleId));
        } catch (Exception e) {
            selected.setResponsible(null);
        }
        selected.setCompany(sessionBean.getCurrentCompany());
        selected.setDateIssue(dateIssue);
        Auditory.make(selected, sessionBean.getCurrentUser());

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
     * @return the stockService
     */
    public IStockService getStockService() {
        return stockService;
    }

    /**
     * @param stockService the stockService to set
     */
    public void setStockService(IStockService stockService) {
        this.stockService = stockService;
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
     * @return the serie
     */
    public String getSerie() {
        return serie;
    }

    /**
     * @param serie the serie to set
     */
    public void setSerie(String serie) {
        this.serie = serie;
    }

    /**
     * @return the documentNumber
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * @param documentNumber the documentNumber to set
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    /**
     * @return the stockSearcher
     */
    public StockSearcher getStockSearcher() {
        return stockSearcher;
    }

    /**
     * @param stockSearcher the stockSearcher to set
     */
    public void setStockSearcher(StockSearcher stockSearcher) {
        this.stockSearcher = stockSearcher;
    }

    /**
     * @return the responsibleCarrier
     */
    public ResponsibleSearcher getResponsibleSearcher() {
        return responsibleSearcher;
    }

    /**
     * @param responsibleCarrier the responsibleCarrier to set
     */
    public void setResponsibleSearcher(ResponsibleSearcher responsibleSearcher) {
        this.responsibleSearcher = responsibleSearcher;
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
     * @return the stockMovementDetailSearcher
     */
    public DetailSearcher getDetailSearcher() {
        return detailSearcher;
    }

    /**
     * @param detailSearcher the detailSearcher to set
     */
    public void setDetailSearcher(DetailSearcher detailSearcher) {
        this.detailSearcher = detailSearcher;
    }

    /**
     * @return the uoMService
     */
    public IUoMService getUoMService() {
        return uoMService;
    }

    /**
     * @param uoMService the uoMService to set
     */
    public void setUoMService(IUoMService uoMService) {
        this.uoMService = uoMService;
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
     * @return the mainService
     */
    public IStockReductionService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IStockReductionService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the dateIssue
     */
    public Date getDateIssue() {
        return dateIssue;
    }

    /**
     * @param dateIssue the dateIssue to set
     */
    public void setDateIssue(Date dateIssue) {
        this.dateIssue = dateIssue;
    }

    /**
     * @return the responsibleId
     */
    public Long getResponsibleId() {
        return responsibleId;
    }

    /**
     * @param responsibleId the responsibleId to set
     */
    public void setResponsibleId(Long responsibleId) {
        this.responsibleId = responsibleId;
    }

    //<editor-fold defaultstate="collapsed" desc="DetailSearcher">    
    public class DetailSearcher implements java.io.Serializable {

        public List<Object[]> data;
        public List<Object[]> removed;

        public DetailSearcher() {
        }

        public void update() {
            data = new ArrayList();
            removed = new ArrayList();
            if (selected.getId() != null) {
                data.addAll(getStockReductionDetailService().listHQL(""
                        + "SELECT "
                        + "srd.id,"
                        + "srd.quantity,"
                        + "srd.uom.id,"
                        + "srd.uom.abbr,"
                        + "srd.productName,"
                        + "srd.product.id FROM StockReductionDetail srd WHERE srd.stockReduction = ?", selected));
            }
        }

        public void addItem() {
            data = new ArrayList();
            getStockSearcher().getData().forEach(item -> {
                if (item[5] != null && ((BigDecimal) item[5]).doubleValue() != 0) {
                    Object[] newItem = new Object[]{
                        item[6], //ID
                        item[5], //Cantidad
                        item[8], //UOM ID
                        item[4], //UOM ABBR
                        item[2], //PRODUCT NAME
                        item[7] //PRODUCT ID
                    };
                    data.add(newItem);
                }
            });
        }

        public void removeItem(int index) {
            data.remove(index);
            getStockSearcher().update();
        }

        public List<Object[]> getData() {
            return data;
        }

        public void setData(List<Object[]> data) {
            this.data = data;
        }

    }

    //</editor-fold>  
    //<editor-fold defaultstate="collapsed" desc="StockSearcher">
    public class StockSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getStockService().listHQL("SELECT "
                    + "st.id,"
                    + "st.product.barcode,"
                    + "st.product.name,"
                    + "st.quantity,"
                    + "st.product.uom.abbr,"
                    + "cast(null as char),"
                    + "cast(null as char),"
                    + "st.product.id,"
                    + "st.product.uom.id"
                    + " FROM Stock st WHERE st.product.active = true AND st.company = ? AND st.quantity <> 0 ORDER BY st.product.name", getSessionBean().getCurrentCompany());
            data.forEach(item -> {
                getDetailSearcher().getData().forEach(detail -> {
                    if (((Long) item[7]) == ((Long) detail[5]).longValue()) {
                        item[2] = detail[4];
                        item[4] = detail[3];
                        item[5] = detail[1];
                        item[6] = detail[0];
                    }
                });
            });
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
    //<editor-fold defaultstate="collapsed" desc="ResponsibleSearcher">
    public class ResponsibleSearcher extends ActorSearcher {

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
            initManaged();
            managedSupplierBean.setIdentityNumber(identityNumber);
            managedSupplierBean.setIdentityDocumentId((Short) identityDocumentService.getByHQL("SELECT idd.id FROM IdentityDocument idd WHERE idd.abbr LIKE ?", "RUC"));
            managedSupplierBean.getIdentityDocumentSearcher().changeLength();
            BeanUtil.exceuteJS("open_create_responsible();");
        }

        @Override
        protected void saveOrUpdateByRUC(String[] data) {
            actor = searchActorInService();
            if (actor == null) {
                actor = new Actor();
                actor.setIdentityNumber(identityNumber);
                actor.setIdentityDocument((IdentityDocument) identityDocumentService.getByHQL("FROM IdentityDocument d WHERE d.length_ = ?", new Short(identityNumber.length() + "")));
            }
            actor.setName(data[0]);
            actor.setAddress(data[1]);
            Integer idUbigeo = (Integer) districtService.getByHQL("SELECT d.id FROM District d WHERE d.subregion.region.name LIKE ? AND s.subregion.name LIKE ? AND a.name LIKE ?", data[2], data[3], data[4]);
            actor.setUbigeo(new Ubigeo(idUbigeo));
            actor.setCustomer(true);
            actor.setOther(data[5]);
            Auditory.make(actor, sessionBean.getCurrentUser());
        }

        public String getExistCustomer() {
            return actor == null ? null : actor.toString();
        }

        public void setExistSupplier(String cust) {
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

        public void initManaged() {
            managedSupplierBean.create();
        }

        public void save() {
            managedSupplierBean.save();
            if (managedSupplierBean.isSaved()) {
                actor = getManagedSupplierBean().getSelected();
                identityNumber = actor.getIdentityNumber();
                responsibleId = managedSupplierBean.getSelected().getId();
            }
        }

    }
//</editor-fold>

    /**
     * @return the stockReductionDetailService
     */
    public IStockReductionDetailService getStockReductionDetailService() {
        return stockReductionDetailService;
    }

    /**
     * @param stockReductionDetailService the stockReductionDetailService to set
     */
    public void setStockReductionDetailService(IStockReductionDetailService stockReductionDetailService) {
        this.stockReductionDetailService = stockReductionDetailService;
    }


    /**
     * @return the productService
     */
    public IProductService getProductService() {
        return productService;
    }

    /**
     * @param productService the productService to set
     */
    public void setProductService(IProductService productService) {
        this.productService = productService;
    }

    /**
     * @return the managedSupplierBean
     */
    public ManagedSupplierBean getManagedSupplierBean() {
        return managedSupplierBean;
    }

    /**
     * @param managedSupplierBean the managedSupplierBean to set
     */
    public void setManagedSupplierBean(ManagedSupplierBean managedSupplierBean) {
        this.managedSupplierBean = managedSupplierBean;
    }

    /**
     * @return the districtService
     */
    public DistrictService getDistrictService() {
        return districtService;
    }

    /**
     * @param districtService the districtService to set
     */
    public void setDistrictService(DistrictService districtService) {
        this.districtService = districtService;
    }
}
