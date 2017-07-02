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

    protected DetailSearcher detailSearcher;

    protected PaymentProofSearcher paymentProofSearcher;
    protected SupplierSearcher supplierSearcher;
    protected ProductSearcher productSearcher;

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
        detailSearcher = new DetailSearcher();
        productSearcher = new ProductSearcher();
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
        if (detailSearcher.data.isEmpty()) {
            PNotifyMessage.errorMessage("Compra vacia");
            saved = false;
        } else {
            String content = selected.getId() == null ? "Se ha creado la compra " + serie + "-" + documentNumber : "Se han actualizado los datos";
            try {
                saved = super.save();
                if (saved) {
                    if (selected.getSupplier() != null) {
                        actorService.saveOrUpdate(selected.getSupplier());
                    }
                    Double initialStock, detailStock, finalStock;
                    Double initialCost, detailCost, finalCost;
                    for (Object[] item : detailSearcher.removed) {
                        PurchaseDetail detail = new PurchaseDetail();
                        detail.setId((Long) item[0]);
                        detail.setQuantity((BigDecimal) item[3]);
                        detail.setProduct(new Product((Long) item[6]));
                        //<editor-fold defaultstate="collapsed" desc="Formula de Costo Inicial">
                        finalStock = stockService.getGroupQuantityByCompanyProduct(selected.getCompanyArrival(), detail.getProduct()).doubleValue();
                        finalCost = productCostPriceService.getCostByCompanyProduct(selected.getCompanyArrival(), detail.getProduct()).doubleValue();
                        detailCost = purchaseDetailService.getUnitCostById(detail.getId()).doubleValue();
                        detailStock = purchaseDetailService.getQuantityById(detail.getId()).doubleValue();

                        initialStock = finalStock - detailStock;
                        initialCost = ((finalCost * finalStock) - (detailStock * detailCost)) / initialStock;
                        //</editor-fold>

                        stockService.substractQuantity(
                                new BigDecimal(detailStock),
                                detail.getProduct(),
                                selected.getCompanyArrival(),
                                sessionBean.getCurrentUser()
                        );
                        productCostPriceService.updateGroupCostByCompanyProduct(new BigDecimal(initialCost).setScale(4, RoundingMode.HALF_UP), selected.getCompanyArrival(), detail.getProduct(), sessionBean.getCurrentUser());

                        purchaseDetailService.delete(detail);
                    }

                    for (Object[] item : detailSearcher.data) {
                        PurchaseDetail detail = new PurchaseDetail();
                        detail.setId((Long) item[0]);
                        detail.setQuantity((BigDecimal) item[3]);
                        detail.setProduct(new Product((Long) item[6]));
                        detail.setPurchase(selected);
                        detail.setProductName((String) item[2]);
                        detail.setUnitPrice((BigDecimal) item[4]);
                        detail.setUom(new UoM((Integer) item[7]));
                        detail.setSubtotal((BigDecimal) item[5]);

                        if (item[9] != null && ((Number) item[9]).doubleValue() != 0) {
                            BigDecimal basicPrice = productSalePriceService.getBasicPrice(sessionBean.getCurrentCompany(), detail.getProduct());
                            if (basicPrice == null || ((Number) item[9]).doubleValue() != basicPrice.doubleValue()) {
                                productSalePriceService.deleteByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct());
                                productSalePriceService.saveForGroup((BigDecimal) item[9], 1, sessionBean.getCurrentCompany(), detail.getProduct(), sessionBean.getCurrentUser());
                            }
                        }

                        if (detail.getId() != null) {
                            //<editor-fold defaultstate="collapsed" desc="Formula de Costo Inicial">
                            finalStock = stockService.getGroupQuantityByCompanyProduct(selected.getCompanyArrival(), detail.getProduct()).doubleValue();
                            finalCost = productCostPriceService.getCostByCompanyProduct(selected.getCompanyArrival(), detail.getProduct()).doubleValue();
                            detailCost = purchaseDetailService.getUnitCostById(detail.getId()).doubleValue();
                            detailStock = purchaseDetailService.getQuantityById(detail.getId()).doubleValue();

                            initialStock = finalStock - detailStock;
                            initialCost = ((finalCost * finalStock) - (detailStock * detailCost)) / initialStock;
                            //</editor-fold>
                            //<editor-fold defaultstate="collapsed" desc="Formula de Costo final">
                            detailCost = detail.getUnitPrice().doubleValue();
                            detailStock = detail.getQuantity().doubleValue();
                            finalStock = initialStock + detailStock;

                            finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;

                            //</editor-fold>
                            stockService.substractQuantity(
                                    purchaseDetailService.getQuantityById(detail.getId()),
                                    detail.getProduct(),
                                    selected.getCompanyArrival(), sessionBean.getCurrentUser()
                            );
                        } else {
                            //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">
                            detailCost = detail.getUnitPrice().doubleValue();
                            detailStock = detail.getQuantity().doubleValue();
                            try {
                                initialStock = stockService.getGroupQuantityByCompanyProduct(selected.getCompanyArrival(), detail.getProduct()).doubleValue();
                                initialCost = productCostPriceService.getCostByCompanyProduct(selected.getCompanyArrival(), detail.getProduct()).doubleValue();

                                finalStock = initialStock + detailStock;
                                finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
                            } catch (NullPointerException e) {
                                finalCost = detailCost;
                            }
                            //</editor-fold>
                        }
                        productCostPriceService.updateGroupCostByCompanyProduct(new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP), selected.getCompanyArrival(), detail.getProduct(), sessionBean.getCurrentUser());
                        stockService.addQuantity(detail.getQuantity(), detail.getProduct(), selected.getCompanyArrival(), sessionBean.getCurrentUser());
                        purchaseDetailService.saveOrUpdate(detail);
                    }

                    if (cash) {
                        PurchasePayment purchasePayment = new PurchasePayment();
                        purchasePayment.setDatePayment(new Date());
                        purchasePayment.setQuantity(selected.getSubtotal().add(selected.getIgv()).subtract(selected.getSubtotalDiscount()).subtract(selected.getIgvDiscount()));
                        purchasePayment.setPurchase(selected);
                        purchasePayment.setCreateDate(new Date());
                        purchasePayment.setCreateUser(sessionBean.getCurrentUser());
                        purchasePayment.setDescription("PAGO POR COMPRA");
                        purchasePaymentService.saveOrUpdate(purchasePayment);
                    }
                }
                PNotifyMessage.saveMessage(content);
            } catch (Exception e) {
                PNotifyMessage.systemError(e, sessionBean);
                saved = false;
            }
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
        detailSearcher.update();
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
     * @return the detailSearcher
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
     * @return the productSearcher
     */
    public ProductSearcher getProductSearcher() {
        return productSearcher;
    }

    /**
     * @param productSearcher the productSearcher to set
     */
    public void setProductSearcher(ProductSearcher productSearcher) {
        this.productSearcher = productSearcher;
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
    //<editor-fold defaultstate="collapsed" desc="DetailSearcher">
    public class DetailSearcher implements java.io.Serializable {

        private List<Object[]> data;
        private List<Object[]> removed;

        public void update() {
            data = new ArrayList<>();
            removed = new ArrayList<>();
            if (selected.getId() == null) {
                return;
            }
            List<Object[]> details = purchaseDetailService.listHQL("SELECT "
                    + "pd.id,"
                    + "pd.product.barcode,"
                    + "pd.productName,"
                    + "pd.quantity,"
                    + "pd.unitPrice,"
                    + "pd.subtotal,"
                    + "pd.product.id,"
                    + "pd.uom.id, "
                    + "pd.igv, "
                    + "cast(null as char) "
                    + "from PurchaseDetail pd WHERE pd.purchase = ?", selected);
            details.forEach(item -> {
                try {
                    item[9] = productSalePriceService.getBasicPrice(sessionBean.getCurrentCompany(), new Product((Long) item[6]));

                } catch (Exception e) {

                }
            });
            data.addAll(details);
        }

        public void remove(int index) {
            Object[] item = data.get(index);
            if (item[0] != null) {
                removed.add(item);
            }
            data.remove(index);
        }

        public void add() {
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
         * @return the removed
         */
        public List<Object[]> getRemoved() {
            return removed;
        }

        /**
         * @param removed the removed to set
         */
        public void setRemoved(List<Object[]> removed) {
            this.removed = removed;
        }
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
    //<editor-fold defaultstate="collapsed" desc="ProductSearcher">
    public class ProductSearcher implements java.io.Serializable {

        protected Map<Long, Object[]> mapData;
        protected Pagination<Object[]> pagination;
        protected boolean valid;
        protected String terms;
        protected BigDecimal quantity;
        protected BigDecimal subtotal;
        protected boolean type;

        public ProductSearcher() {
            this.pagination = new Pagination<Object[]>(productService) {
                @Override
                public void search(int page, Object... variant) {
                    if (this.getData() != null) {
                        this.getData().forEach(item -> {
                            BigDecimal quantity = (BigDecimal) item[6];
                            if (quantity == null || quantity.doubleValue() == 0) {
                                mapData.remove(item[0]);
                            } else {
                                mapData.put((Long) item[0], new Object[]{
                                    item[10],
                                    item[2],
                                    item[3],
                                    item[6],
                                    item[7],
                                    item[8],
                                    item[0],
                                    item[4],
                                    item[9],
                                    item[5]
                                });
                            }
                        });
                    }
                    super.search(page, variant); //To change body of generated methods, choose Tools | Templates.
                    if (this.getData() != null && !this.getData().isEmpty()) {
                        this.getData().forEach(item -> {
                            Long productId = (Long) item[0];
                            Object[] d = mapData.get(productId);
                            if (d != null) {
                                item[6] = d[3];
                                item[7] = d[4];
                                item[8] = d[5];
                                item[9] = d[8];
                                item[10] = d[0];
                            }
                        });
                    }
                }
            };
        }

        public void initManaged(boolean type) {
            valid = false;
            this.type = type;
            subtotal = null;
            quantity = null;
            managedProductBean.create();
        }

        public void refresh() {
            terms = "";
            mapData = new HashMap<>();
            ManagedPurchaseBean.this.detailSearcher.data.forEach(detail -> {
                mapData.put((Long) detail[6], detail);
            });
            pagination.setData(Collections.EMPTY_LIST);
            update();
        }

        public void updateDetail() {
            if (pagination.getData() != null) {
                pagination.getData().forEach(item -> {
                    BigDecimal quantity = (BigDecimal) item[6];
                    if (quantity == null || quantity.doubleValue() == 0) {
                        mapData.remove(item[0]);
                    } else {
                        mapData.put((Long) item[0], new Object[]{
                            item[10],
                            item[2],
                            item[3],
                            item[6],
                            item[7],
                            item[8],
                            item[0],
                            item[4],
                            item[9],
                            item[5]
                        });
                    }
                });
            }
            detailSearcher.data = new ArrayList();
            mapData.forEach((key, item) -> {
                detailSearcher.data.add(item);
            });
        }

        public void update() {
            terms = terms.trim();
            ProjectionList projectionList = Projections.projectionList()
                    /*0-ID*/.add(Projections.id())
                    /*1-Imagen*/.add(Projections.property("image"))
                    /*2-Codigo de Barras*/.add(Projections.property("barcode"))
                    /*3-Nombre*/.add(Projections.property("name"))
                    /*4-ID unidad*/.add(Projections.property("uom.id"))
                    /*5-Precio de venta unit.*/.add(Projections.sqlProjection("(SELECT psp.price/psp.quantity FROM product_sale_price psp WHERE psp.id_product = {alias}.id and psp.id_company = " + sessionBean.getCurrentCompany().getId() + " ORDER BY psp.quantity LIMIT 1) as sale_price", new String[]{"sale_price"}, new Type[]{BigDecimalType.INSTANCE}))
                    /*6-cantidad*/.add(Projections.sqlProjection("cast(null as char) as f1", new String[]{"f1"}, new Type[]{CharacterType.INSTANCE}))
                    /*7-Precop comp*/.add(Projections.sqlProjection("cast(null as char) as f2", new String[]{"f2"}, new Type[]{CharacterType.INSTANCE}))
                    /*8-Subtotal*/.add(Projections.sqlProjection("cast(null as char) as f3", new String[]{"f3"}, new Type[]{CharacterType.INSTANCE}))
                    /*9-IGV*/.add(Projections.sqlProjection("cast(null as char) as f4", new String[]{"f4"}, new Type[]{CharacterType.INSTANCE}))
                    /*10-ID detail*/.add(Projections.sqlProjection("cast(null as char) as f5", new String[]{"f5"}, new Type[]{CharacterType.INSTANCE}));
            CriterionList criterionList = new CriterionList()
                    ._add(Restrictions.eq("active", true))
                    ._add(Restrictions.or(
                                    Restrictions.like("barcode", terms, MatchMode.ANYWHERE),
                                    Restrictions.like("name", terms, MatchMode.ANYWHERE).ignoreCase()));
            AliasList aliasList = new AliasList();
            aliasList.add("uom", "uom", JoinType.LEFT_OUTER_JOIN);
            OrderList orderList = new OrderList();
            orderList.add(Order.asc("name"));
            pagination.search(1, projectionList, aliasList, criterionList, orderList);

        }

        public void save() {
            valid = managedProductBean.save();
            if (valid) {
                BigDecimal priceSale = null;
                try {
                    priceSale = (BigDecimal) productSalePriceService.listHQLPage("SELECT psp.price FROM ProductSalePrice psp WHERE psp.product.id = ? and psp.company = ? ORDER BY psp.price", 1, 1, managedProductBean.getSelected().getId(), sessionBean.getCurrentCompany()).get(0);
                } catch (Exception e) {
                }
                double dUnitPrice = (subtotal.doubleValue() + (igv ? subtotal.doubleValue() * constantIGV.doubleValue() : 0)) / quantity.doubleValue();
                BigDecimal unitPrice = new BigDecimal(dUnitPrice).setScale(2, RoundingMode.HALF_UP);
                if (type) {
                    detailSearcher.data.add(new Object[]{
                        null,
                        managedProductBean.getSelected().getBarcode(),
                        managedProductBean.getSelected().getName(),
                        quantity,
                        unitPrice,
                        subtotal,
                        managedProductBean.getSelected().getId(),
                        managedProductBean.getSelected().getUom().getId(),
                        igv ? subtotal.multiply(constantIGV) : BigDecimal.ZERO,
                        priceSale
                    });
                } else {
                    mapData.put(managedProductBean.getSelected().getId(), new Object[]{
                        null,
                        managedProductBean.getSelected().getBarcode(),
                        managedProductBean.getSelected().getName(),
                        quantity,
                        unitPrice,
                        subtotal,
                        managedProductBean.getSelected().getId(),
                        managedProductBean.getSelected().getUom().getId(),
                        igv ? subtotal.multiply(constantIGV) : BigDecimal.ZERO,
                        priceSale
                    });
                    terms = managedProductBean.getSelected().getName();
                    update();
                }
            }
        }

        /**
         * @return the valid
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * @param valid the valid to set
         */
        public void setValid(boolean valid) {
            this.valid = valid;
        }

        /**
         * @return the terms
         */
        public String getTerms() {
            return terms;
        }

        /**
         * @param terms the terms to set
         */
        public void setTerms(String terms) {
            this.terms = terms;
        }

        /**
         * @return the quantity
         */
        public BigDecimal getQuantity() {
            return quantity;
        }

        /**
         * @param quantity the quantity to set
         */
        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
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
         * @return the type
         */
        public boolean isType() {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(boolean type) {
            this.type = type;
        }

        /**
         * @return the mapData
         */
        public Map<Long, Object[]> getMapData() {
            return mapData;
        }

        /**
         * @param mapData the mapData to set
         */
        public void setMapData(Map<Long, Object[]> mapData) {
            this.mapData = mapData;
        }

        /**
         * @return the pagination
         */
        public Pagination<Object[]> getPagination() {
            return pagination;
        }

        /**
         * @param pagination the pagination to set
         */
        public void setPagination(Pagination<Object[]> pagination) {
            this.pagination = pagination;
        }
    }
    //</editor-fold>

}
