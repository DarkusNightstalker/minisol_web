/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.bean.SaleBean;
import cs.bms.model.Actor;
import cs.bms.model.IdentityDocument;
import cs.bms.model.Product;
import cs.bms.model.Purchase;
import cs.bms.model.PurchaseDetail;
import cs.bms.model.PurchasePayment;
import cs.bms.model.Ubigeo;
import cs.bms.model.UoM;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDistrictService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IProductCostPriceService;
import cs.bms.service.interfac.IProductSalePriceService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IPurchaseDetailService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.IPurchaseService;
import cs.bms.service.interfac.ISaleDetailService;
import cs.bms.service.interfac.IStockService;
import cs.bms.util.ActorSearcher;
import gkfire.auditory.Auditory;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.CharacterType;
import org.hibernate.type.Type;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedPurchaseBean extends AManagedBean<Purchase, IPurchaseService> implements ILoadable {

    @ManagedProperty(value = "#{purchaseService}")
    protected IPurchaseService mainService;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{stockService}")
    protected IStockService stockService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{saleDetailService}")
    protected ISaleDetailService saleDetailService;
    @ManagedProperty(value = "#{paymentProofService}")
    protected IPaymentProofService paymentProofService;
    @ManagedProperty(value = "#{productSalePriceService}")
    protected IProductSalePriceService productSalePriceService;
    @ManagedProperty(value = "#{productCostPriceService}")
    protected IProductCostPriceService productCostPriceService;
    @ManagedProperty(value = "#{productService}")
    protected IProductService productService;
    @ManagedProperty(value = "#{purchaseDetailService}")
    protected IPurchaseDetailService purchaseDetailService;
    @ManagedProperty(value = "#{documentNumberingService}")
    protected IDocumentNumberingService documentNumberingService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{purchasePaymentService}")
    protected IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{districtService}")
    protected IDistrictService districtService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{saleBean}")
    protected SaleBean saleBean;
    @ManagedProperty(value = "#{managedSupplierBean}")
    protected ManagedSupplierBean managedSupplierBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    @ManagedProperty(value = "#{managedProductBean}")
    protected ManagedProductBean managedProductBean;

    protected PaymentProofSearcher paymentProofSearcher;
    protected SupplierSearcher supplierSearcher;

    protected Long points;
    protected Short paymentProofId;
    protected String serie;
    protected String documentNumber;
    protected boolean electronic;
    protected Long supplierId;
    protected String customerName;
    protected BigDecimal subtotal;
    protected boolean igv;
    protected BigDecimal constantIGV;

    protected boolean cash;
    protected BigDecimal igvDiscount;
    protected BigDecimal subtotalDiscount;
    protected Date dateIssue;
    protected Integer companyArrivalId;

    @PostConstruct
    public void init() {
        constantIGV = new BigDecimal(0.18).setScale(2, RoundingMode.HALF_UP);

        supplierSearcher = new SupplierSearcher();
        paymentProofSearcher = new PaymentProofSearcher();
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
        String content = selected.getId() == null ? "Se ha creado la compra " + serie + "-" + documentNumber : "Se han actualizado los datos";
        try {
            saved = super.save();
            if (saved) {
                if (selected.getSupplier() != null) {
                    actorService.saveOrUpdate(selected.getSupplier());
                }
            }
            PNotifyMessage.saveMessage(content);
        } catch (Exception e) {
            PNotifyMessage.systemError(e, sessionBean);
            saved = false;
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        
        try {
            paymentProofId = selected.getPaymentProof().getId();
        } catch (NullPointerException npe) {
            paymentProofId = paymentProofService.getIdByAbbr("FAC");
        }
        
        serie = selected.getSerie();
        documentNumber = selected.getDocumentNumber();
        electronic = selected.getElectronic();
        
        try {
            supplierId = selected.getSupplier().getId();
        } catch (NullPointerException npe) {
            supplierId = null;
        }
        
        if (supplierId != null) {
            supplierSearcher.setActor(selected.getSupplier());
            supplierSearcher.setIdentityNumber(selected.getSupplier().getIdentityNumber());
        } else {
            supplierSearcher.setActor(null);
            supplierSearcher.setIdentityNumber(null);
        }
        customerName = selected.getSupplierName();
        subtotal = selected.getSubtotal();
        igv = selected.getIgv().doubleValue() != 0;
        igvDiscount = selected.getIgvDiscount();
        subtotalDiscount = selected.getSubtotalDiscount();
        dateIssue = selected.getDateIssue();
        try {
            companyArrivalId = (Integer) mainService.getByHQL("SELECT p.companyArrival.id FROM Purchase p where p.id = ?", selected.getId());
        } catch (Exception npe) {
            companyArrivalId = sessionBean.getCurrentCompany().getId();
        }

        createAgain = selected.getId() == null;
        paymentProofSearcher.update();
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        try {
            selected.setPaymentProof(paymentProofService.getById(paymentProofId));
        } catch (Exception npe) {
            selected.setPaymentProof(null);
        }
        try {
            selected.setCompanyArrival(companyService.getById(companyArrivalId));
        } catch (Exception npe) {
            selected.setCompanyArrival(null);
        }

        selected.setSerie(serie.toUpperCase());
        documentNumber = String.format("%08d", Long.parseLong(this.documentNumber));
        selected.setDocumentNumber(documentNumber);

        selected.setElectronic(true);
        selected.setSupplier(supplierSearcher.getActor());
        selected.setSupplierName(selected.getSupplier() == null ? customerName : (selected.getSupplier().getOther() == null ? selected.getSupplier().getName() : selected.getSupplier().getOther()));
        selected.setSubtotal(subtotal);
        selected.setIgv(igv ? subtotal.multiply(constantIGV).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        selected.setIgvDiscount(igvDiscount);
        selected.setSubtotalDiscount(subtotalDiscount);
        selected.setDateIssue(dateIssue == null ? new Date() : dateIssue);
        Auditory.make(selected, sessionBean.getCurrentUser());
    }
    //<editor-fold defaultstate="collapsed" desc="Gets && Sets">

    /**
     * @return the mainService
     */
    public IPurchaseService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IPurchaseService mainService) {
        this.mainService = mainService;
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
     * @return the saleDetailService
     */
    public ISaleDetailService getSaleDetailService() {
        return saleDetailService;
    }

    /**
     * @param saleDetailService the saleDetailService to set
     */
    public void setSaleDetailService(ISaleDetailService saleDetailService) {
        this.saleDetailService = saleDetailService;
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
     * @return the productSalePriceService
     */
    public IProductSalePriceService getProductSalePriceService() {
        return productSalePriceService;
    }

    /**
     * @param productSalePriceService the productSalePriceService to set
     */
    public void setProductSalePriceService(IProductSalePriceService productSalePriceService) {
        this.productSalePriceService = productSalePriceService;
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
     * @return the supplierSearcher
     */
    public SupplierSearcher getSupplierSearcher() {
        return supplierSearcher;
    }

    /**
     * @param supplierSearcher the supplierSearcher to set
     */
    public void setSupplierSearcher(SupplierSearcher supplierSearcher) {
        this.supplierSearcher = supplierSearcher;
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
     * @return the electronic
     */
    public boolean isElectronic() {
        return electronic;
    }

    /**
     * @param electronic the electronic to set
     */
    public void setElectronic(boolean electronic) {
        this.electronic = electronic;
    }

    /**
     * @return the supplierId
     */
    public Long getSupplierId() {
        return supplierId;
    }

    /**
     * @param supplierId the supplierId to set
     */
    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    /**
     * @return the customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @param customerName the customerName to set
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * @return the subtotal
     */
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /**
     * @param subtotal the subtotal to set
     */
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * @return the igv
     */
    public boolean getIgv() {
        return igv;
    }

    /**
     * @param igv the igv to set
     */
    public void setIgv(boolean igv) {
        this.igv = igv;
    }

    /**
     * @return the igvDiscount
     */
    public BigDecimal getIgvDiscount() {
        return igvDiscount;
    }

    /**
     * @param igvDiscount the igvDiscount to set
     */
    public void setIgvDiscount(BigDecimal igvDiscount) {
        this.igvDiscount = igvDiscount;
    }

    /**
     * @return the subtotalDiscount
     */
    public BigDecimal getSubtotalDiscount() {
        return subtotalDiscount;
    }

    /**
     * @param subtotalDiscount the subtotalDiscount to set
     */
    public void setSubtotalDiscount(BigDecimal subtotalDiscount) {
        this.subtotalDiscount = subtotalDiscount;
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
     * @return the companyId
     */
    public Integer getCompanyArrivalId() {
        return companyArrivalId;
    }

    /**
     * @param companyId the companyId to set
     */
    public void setCompanyArrivalId(Integer companyArrivalId) {
        this.companyArrivalId = companyArrivalId;
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
     * @return the saleBean
     */
    public SaleBean getSaleBean() {
        return saleBean;
    }

    /**
     * @param saleBean the saleBean to set
     */
    public void setSaleBean(SaleBean saleBean) {
        this.saleBean = saleBean;
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
     * @return the purchaseDetailService
     */
    public IPurchaseDetailService getPurchaseDetailService() {
        return purchaseDetailService;
    }

    /**
     * @param purchaseDetailService the purchaseDetailService to set
     */
    public void setPurchaseDetailService(IPurchaseDetailService purchaseDetailService) {
        this.purchaseDetailService = purchaseDetailService;
    }

    /**
     * @return the managedProductBean
     */
    public ManagedProductBean getManagedProductBean() {
        return managedProductBean;
    }

    /**
     * @param managedProductBean the managedProductBean to set
     */
    public void setManagedProductBean(ManagedProductBean managedProductBean) {
        this.managedProductBean = managedProductBean;
    }


    /**
     * @return the igv
     */
    public boolean isIgv() {
        return igv;
    }

    /**
     * @return the cash
     */
    public boolean isCash() {
        return cash;
    }

    /**
     * @param cash the cash to set
     */
    public void setCash(boolean cash) {
        this.cash = cash;
    }

    /**
     * @return the constantIGV
     */
    public BigDecimal getConstantIGV() {
        return constantIGV;
    }

    /**
     * @param constantIGV the constantIGV to set
     */
    public void setConstantIGV(BigDecimal constantIGV) {
        this.constantIGV = constantIGV;
    }

    /**
     * @return the productCostPriceService
     */
    public IProductCostPriceService getProductCostPriceService() {
        return productCostPriceService;
    }

    /**
     * @param productCostPriceService the productCostPriceService to set
     */
    public void setProductCostPriceService(IProductCostPriceService productCostPriceService) {
        this.productCostPriceService = productCostPriceService;
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
     * @return the purchasePaymentService
     */
    public IPurchasePaymentService getPurchasePaymentService() {
        return purchasePaymentService;
    }

    /**
     * @param purchasePaymentService the purchasePaymentService to set
     */
    public void setPurchasePaymentService(IPurchasePaymentService purchasePaymentService) {
        this.purchasePaymentService = purchasePaymentService;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="PaymentProofSearcher">
    public class PaymentProofSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = paymentProofService.listHQL("SELECT pp.id, pp.name FROM PaymentProof pp WHERE pp.active = true AND pp.forPurchase = true");
        }

        public List<Object[]> getData() {
            return data;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="SupplierSearcher">
    public class SupplierSearcher extends ActorSearcher {

        @Override
        protected Actor searchActorInService() {
            return actorService.getByIdentityNumber(identityNumber);
        }

        @Override
        protected void saveOrUpdateByDNI(String name) {
            actor = searchActorInService();
            if (actor == null) {
                actor = new Actor();
                actor.setIdentityNumber(identityNumber);
                actor.setIdentityDocument(new IdentityDocument(identityDocumentService.getIdByLength(identityNumber.length())));
            }
            actor.setName(name);
            actor.setSupplier(true);
            Auditory.make(actor, sessionBean.getCurrentUser());
            actorService.saveOrUpdate(actor);
        }

        @Override
        protected void caseRUC() {
            if (actor == null || (actor.getOther() == null || actor.getOther().length() == 0)) {
                initManaged();
                managedSupplierBean.setIdentityNumber(identityNumber);
                managedSupplierBean.setIdentityDocumentId((Short) identityDocumentService.getByHQL("SELECT idd.id FROM IdentityDocument idd WHERE idd.abbr LIKE ?", "RUC"));
                managedSupplierBean.getIdentityDocumentSearcher().changeLength();
                BeanUtil.exceuteJS("open_create_supplier();");
            }
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
            actor.setSupplier(true);
            actor.setOther(data[5]);
            Auditory.make(actor, sessionBean.getCurrentUser());
        }

        @Override
        public void search() {
            super.search();
            if (actor != null) {
                actor.setSupplier(Boolean.TRUE);
            }
        }

        public String getExistSupplier() {
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
            managedSupplierBean.setSupplier(true);
        }

        public void save() {
            managedSupplierBean.save();
            if (managedSupplierBean.isSaved()) {
                actor = getManagedSupplierBean().getSelected();
                identityNumber = actor.getIdentityNumber();
                setSupplierId(getManagedSupplierBean().getSelected().getId());
            }
        }

        @Override
        protected void errorMessage(String message) {
            PNotifyMessage.errorMessage(message);
        }

    }

    //</editor-fold>
}
