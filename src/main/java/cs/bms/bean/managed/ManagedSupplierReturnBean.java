/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.OperationType;
import cs.bms.model.Product;
import cs.bms.model.Purchase;
import cs.bms.model.PurchaseDetail;
import cs.bms.model.StockReturnSupplier;
import cs.bms.model.StockReturnSupplierDetail;
import cs.bms.model.UoM;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IOperationTypeService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IProductCostPriceService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IPurchaseDetailService;
import cs.bms.service.interfac.IPurchaseService;
import cs.bms.service.interfac.IStockReturnSupplierDetailService;
import cs.bms.service.interfac.IStockReturnSupplierService;
import cs.bms.service.interfac.IStockService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Darkus Nightmare
 */
@ManagedBean
@SessionScoped
public class ManagedSupplierReturnBean extends AManagedBean<StockReturnSupplier, IStockReturnSupplierService> implements ILoadable {

    @ManagedProperty(value = "#{stockReturnSupplierService}")
    private IStockReturnSupplierService mainService;
    @ManagedProperty(value = "#{stockReturnSupplierDetailService}")
    private IStockReturnSupplierDetailService stockReturnSupplierDetailService;
    @ManagedProperty(value = "#{paymentProofService}")
    private IPaymentProofService paymentProofService;
    @ManagedProperty(value = "#{productCostPriceService}")
    private IProductCostPriceService productCostPriceService;
    @ManagedProperty(value = "#{productService}")
    private IProductService productService;
    @ManagedProperty(value = "#{purchaseDetailService}")
    private IPurchaseDetailService purchaseDetailService;
    @ManagedProperty(value = "#{identityDocumentService}")
    private IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{actorService}")
    private IActorService actorService;
    @ManagedProperty(value = "#{stockService}")
    private IStockService stockService;
    @ManagedProperty(value = "#{operationTypeService}")
    private IOperationTypeService operationTypeService;
    @ManagedProperty(value = "#{purchaseService}")
    private IPurchaseService purchaseService;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;
    @ManagedProperty(value = "#{managedProductBean}")
    private ManagedProductBean managedProductBean;

    private DetailSearcher detailSearcher;

    private PaymentProofSearcher paymentProofSearcher;
    private PurchaseSearcher purchaseSearcher;

    private Short paymentProofId;
    private String serie;
    private String documentNumber;
    private boolean electronic;
    private Long purchaseId;
    private String customerName;
    private BigDecimal repayment;
    private boolean igv;

    private BigDecimal constantIGV;

    private boolean cash;
    private BigDecimal igvDiscount;
    private BigDecimal subtotalDiscount;
    private Date dateIssue;

    @PostConstruct
    public void init() {
        constantIGV = new BigDecimal(0.18).setScale(2, RoundingMode.HALF_UP);
        detailSearcher = new DetailSearcher();
        purchaseSearcher = new PurchaseSearcher();
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
            PNotifyMessage.errorMessage("Devolución vacia");
            saved = false;
        } else {
            String content = selected.getId() == null ? "Se ha creado la devolución " + serie + "-" + documentNumber : "Se han actualizado los datos";
            try {
                saved = super.save();
                if (saved) {
                    Double initialStock, detailStock, finalStock;
                    Double initialCost, detailCost, finalCost;

                    for (Object[] item : detailSearcher.data) {
                        StockReturnSupplierDetail detail = new StockReturnSupplierDetail();
                        detail.setId((Long) item[0]);
                        detail.setQuantity((BigDecimal) item[1]);
                        detail.setProduct(new Product((Long) item[7]));
                        detail.setStockReturnSupplier(selected);
                        detail.setProductName((String) item[5]);
                        detail.setUnitCost((BigDecimal) item[6]);
                        detail.setUom(new UoM((Integer) item[3]));

                        if (detail.getQuantity() == null || detail.getQuantity().doubleValue() == 0) {
                            if (detail.getId() != null) {
                                //<editor-fold defaultstate="collapsed" desc="Formula de Costo Inicial">
                                finalStock = stockService.getGroupQuantityByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()).doubleValue();
                                finalCost = productCostPriceService.getCostByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()).doubleValue();
                                detailCost = stockReturnSupplierDetailService.getUnitCostById(detail.getId()).doubleValue();
                                detailStock = stockReturnSupplierDetailService.getQuantityById(detail.getId()).doubleValue();

                                initialStock = finalStock + detailStock;
                                initialCost = ((finalCost * finalStock) + (detailStock * detailCost)) / initialStock;
                                //</editor-fold>

                                stockService.addQuantity(
                                        new BigDecimal(detailStock),
                                        detail.getProduct(),
                                        sessionBean.getCurrentCompany(),
                                        sessionBean.getCurrentUser()
                                );
                                productCostPriceService.updateGroupCostByCompanyProduct(
                                        new BigDecimal(initialCost).setScale(4, RoundingMode.HALF_UP),
                                        sessionBean.getCurrentCompany(),
                                        detail.getProduct(),
                                        sessionBean.getCurrentUser()
                                );

                                stockReturnSupplierDetailService.delete(detail);
                            }
                        } else {
                            if (detail.getId() != null) {
                                //<editor-fold defaultstate="collapsed" desc="Formula de Costo Inicial">
                                finalStock = stockService.getGroupQuantityByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()).doubleValue();
                                finalCost = productCostPriceService.getCostByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()).doubleValue();
                                detailCost = stockReturnSupplierDetailService.getUnitCostById(detail.getId()).doubleValue();
                                detailStock = stockReturnSupplierDetailService.getQuantityById(detail.getId()).doubleValue();

                                initialStock = finalStock + detailStock;
                                initialCost = ((finalCost * finalStock) + (detailStock * detailCost)) / initialStock;
                                //</editor-fold>
                                //<editor-fold defaultstate="collapsed" desc="Formula de Costo final">
                                detailCost = detail.getUnitCost().doubleValue();
                                detailStock = detail.getQuantity().doubleValue();
                                finalStock = initialStock - detailStock;

                                finalCost = ((initialCost * initialStock) - (detailCost * detailStock)) / finalStock;

                                //</editor-fold>
                                stockService.addQuantity(
                                        purchaseDetailService.getQuantityById(detail.getId()),
                                        detail.getProduct(),
                                        sessionBean.getCurrentCompany(), sessionBean.getCurrentUser()
                                );
                            } else {
                                //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">
                                detailCost = detail.getUnitCost().doubleValue();
                                detailStock = detail.getQuantity().doubleValue();
                                try {
                                    initialStock = stockService.getGroupQuantityByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()).doubleValue();
                                    initialCost = productCostPriceService.getCostByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()).doubleValue();

                                    finalStock = initialStock - detailStock;
                                    finalCost = ((initialCost * initialStock) - (detailCost * detailStock)) / finalStock;
                                } catch (NullPointerException e) {
                                    finalCost = detailCost;
                                }
                                //</editor-fold>
                            }
                            productCostPriceService.updateGroupCostByCompanyProduct(
                                    new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                                    sessionBean.getCurrentCompany(),
                                    detail.getProduct(),
                                    sessionBean.getCurrentUser()
                            );
                            stockService.substractQuantity(
                                    detail.getQuantity(),
                                    detail.getProduct(),
                                    sessionBean.getCurrentCompany(),
                                    sessionBean.getCurrentUser()
                            );
                            stockReturnSupplierDetailService.saveOrUpdate(detail);
                        }
                    }
                    PNotifyMessage.saveMessage(content);
                }
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
            paymentProofId = paymentProofService.getIdByAbbr("NC");
        }

        serie = selected.getSerie();
        documentNumber = selected.getDocumentNumber();
        electronic = selected.getElectronic();
        try {
            purchaseId = selected.getPurchase().getId();
        } catch (NullPointerException npe) {
            purchaseId = null;
        }

        purchaseSearcher.update();
        cash = false;
        igv = selected.getIgv().doubleValue() != 0;
        dateIssue = selected.getDateIssue();
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
        if (purchaseId != null) {
            selected.setPurchase(new Purchase(purchaseId));
        } else {
            selected.setPurchase(null);
        }

        selected.setSerie(serie.toUpperCase());
        documentNumber = String.format("%08d", Long.parseLong(this.documentNumber));
        selected.setDocumentNumber(documentNumber);
        selected.setOperationType(new OperationType(operationTypeService.getSupplierReturnTypeId()));
        selected.setElectronic(true);
        selected.setRepayment(repayment);
        selected.setIgv(igv ? repayment.multiply(constantIGV).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        selected.setDateIssue(dateIssue == null ? new Date() : dateIssue);
        Auditory.make(selected, sessionBean.getCurrentUser());
    }
    //<editor-fold defaultstate="collapsed" desc="Gets && Sets">

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
     * @return the repayment
     */
    public BigDecimal getRepayment() {
        return repayment;
    }

    /**
     * @param repayment the repayment to set
     */
    public void setRepayment(BigDecimal repayment) {
        this.repayment = repayment;
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
     * @return the mainService
     */
    @Override
    public IStockReturnSupplierService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    @Override
    public void setMainService(IStockReturnSupplierService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the stockReturnSupplierDetailService
     */
    public IStockReturnSupplierDetailService getStockReturnSupplierDetailService() {
        return stockReturnSupplierDetailService;
    }

    /**
     * @param stockReturnSupplierDetailService the
     * stockReturnSupplierDetailService to set
     */
    public void setStockReturnSupplierDetailService(IStockReturnSupplierDetailService stockReturnSupplierDetailService) {
        this.stockReturnSupplierDetailService = stockReturnSupplierDetailService;
    }

    /**
     * @return the purchaseService
     */
    public IPurchaseService getPurchaseService() {
        return purchaseService;
    }

    /**
     * @param purchaseService the purchaseService to set
     */
    public void setPurchaseService(IPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * @return the purchaseSearcher
     */
    public PurchaseSearcher getPurchaseSearcher() {
        return purchaseSearcher;
    }

    /**
     * @param purchaseSearcher the purchaseSearcher to set
     */
    public void setPurchaseSearcher(PurchaseSearcher purchaseSearcher) {
        this.purchaseSearcher = purchaseSearcher;
    }

    /**
     * @return the purchaseId
     */
    public Long getPurchaseId() {
        return purchaseId;
    }

    /**
     * @param purchaseId the purchaseId to set
     */
    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
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
     * @return the operationTypeService
     */
    public IOperationTypeService getOperationTypeService() {
        return operationTypeService;
    }

    /**
     * @param operationTypeService the operationTypeService to set
     */
    public void setOperationTypeService(IOperationTypeService operationTypeService) {
        this.operationTypeService = operationTypeService;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="DetailSearcher">
    public class DetailSearcher implements java.io.Serializable {

        private List<Object[]> data;
        private List<Object[]> removed;

        public void update() {
            if (getPurchaseId() != null) {
                data = getPurchaseDetailService().listHQL(""
                        + "SELECT "
                        + "cast(null as char),"
                        + "cast(null as char),"
                        + "pd.quantity,"
                        + "pd.uom.id,"
                        + "pd.uom.abbr,"
                        + "pd.productName,"
                        + "pd.unitPrice,"
                        + "pd.product.id,"
                        + "pd.product.barcode "
                        + "FROM PurchaseDetail pd "
                        + "WHERE pd.purchase.id = ?", getPurchaseId());
                List<Object[]> details = Collections.EMPTY_LIST;
                if (selected.getId() != null) {
                    details = getStockReturnSupplierDetailService().getDataBySupplierReturn(selected.getId());
                }

                for (Object[] item : data) {
                    BigDecimal currentReturn = getStockReturnSupplierDetailService().getSumQuantityByProduct(getPurchaseId(), (Long) item[7], selected.getId());
                    item[2] = ((BigDecimal) item[2]).subtract(currentReturn);

                    details.forEach(detail -> {
                        if (((Long) item[7]).longValue() == (Long) detail[1]) {
                            item[0] = detail[0];
                            item[1] = detail[5];
                            item[6] = detail[6];
                            item[5] = detail[4];
                        }
                    });
                }
            } else {
                data = Collections.EMPTY_LIST;
            }
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
            data = getPaymentProofService().getForReturn();
        }

        public List<Object[]> getData() {
            return data;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="PurchaseSearcher">
    public class PurchaseSearcher implements java.io.Serializable {

        private Object[] currentSupplier;
        private Object[] currentPurchase;
        private List<Object[]> purchases;

        public void update() {
            if (getPurchaseId() != null) {
                currentPurchase = (Object[]) getPurchaseService().getByHQL(""
                        + "SELECT "
                        + "p.id,"
                        + "p.paymentProof.abbr||' '||p.fullDocument,"
                        + "p.dateIssue,"
                        + "p.dateDue,"
                        + "s.id,"
                        + "p.serie "
                        + "FROM Purchase p left join p.supplier s "
                        + "WHERE "
                        + "p.id = ?", getPurchaseId());
                if (currentPurchase == null) {
                    currentSupplier = null;
                } else {
                    currentSupplier = (Object[]) getActorService().getByHQL(""
                            + "SELECT "
                            + "a.id,"
                            + "a.identityDocument.abbr,"
                            + "a.identityNumber,"
                            + "COALESCE(a.other,a.name),"
                            + "u.name,"
                            + "a.address "
                            + "FROM Actor a left join a.ubigeo u "
                            + "WHERE "
                            + "a.id = ?", currentPurchase[4] == null ? -1L : currentPurchase[4]);
                    updatePurchase();
                }
            } else {
                currentSupplier = null;
                currentPurchase = null;
            }
        }

        public void changeSupplier() {
            currentPurchase = null;
            updatePurchase();
            changePurchase();
        }

        public void changePurchase() {
            if (currentPurchase == null) {
                setPurchaseId(null);
                setSerie(null);
            } else {
                setPurchaseId((Long) currentPurchase[0]);
                setSerie((String) currentPurchase[5]);
            }
            getDetailSearcher().update();
        }

        public void updatePurchase() {
            if (currentSupplier != null) {
                purchases = getPurchaseService().getBasicData((Long) currentSupplier[0]);
            } else {
                purchases = Collections.EMPTY_LIST;
            }
        }

        public List<Object[]> searchSupplier(String query) {
            return getActorService().forSupplierAutocomplete(6, query);
        }

        /**
         * @return the currentSupplier
         */
        public Object[] getCurrentSupplier() {
            return currentSupplier;
        }

        /**
         * @param currentSupplier the currentSupplier to set
         */
        public void setCurrentSupplier(Object[] currentSupplier) {
            this.currentSupplier = currentSupplier;
        }

        /**
         * @return the currentPurchase
         */
        public Object[] getCurrentPurchase() {
            return currentPurchase;
        }

        /**
         * @param currentPurchase the currentPurchase to set
         */
        public void setCurrentPurchase(Object[] currentPurchase) {
            this.currentPurchase = currentPurchase;
        }

        /**
         * @return the purchases
         */
        public List<Object[]> getPurchases() {
            return purchases;
        }

        /**
         * @param purchases the purchases to set
         */
        public void setPurchases(List<Object[]> purchases) {
            this.purchases = purchases;
        }

    }
//</editor-fold>

}
