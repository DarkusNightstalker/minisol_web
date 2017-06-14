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
import cs.bms.model.InternalStockMovement;
import cs.bms.model.InternalStockMovementDetail;
import cs.bms.model.OperationType;
import cs.bms.model.Product;
import cs.bms.model.UoM;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IInternalStockMovementDetailService;
import cs.bms.service.interfac.IInternalStockMovementService;
import cs.bms.service.interfac.IOperationTypeService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IProductCostPriceService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IStockService;
import cs.bms.service.interfac.IUoMService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
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
public class ManagedStockMovementBean extends AManagedBean<InternalStockMovement, IInternalStockMovementService> implements ILoadable {

    @ManagedProperty(value = "#{internalStockMovementService}")
    protected IInternalStockMovementService mainService;
    @ManagedProperty(value = "#{paymentProofService}")
    protected IPaymentProofService paymentProofService;
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
    @ManagedProperty(value = "#{productCostPriceService}")
    protected IProductCostPriceService productCostPriceService;
    @ManagedProperty(value = "#{operationTypeService}")
    protected IOperationTypeService operationTypeService;
    @ManagedProperty(value = "#{documentNumberingService}")
    protected IDocumentNumberingService documentNumberingService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{internalStockMovementDetailService}")
    protected IInternalStockMovementDetailService internalStockMovementDetailService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;

    protected boolean typeCarrier;
    protected Short paymentProofId;
    protected String serie;
    protected String documentNumber;
    protected Integer sourceCompanyId;
    protected Integer targetCompanyId;
    protected Long carrierId;
    protected Date dateShipping;
    protected Date dateArrival;
    protected String transportDescription;
    protected String driverLicense;

    protected PrivateManage privateManage;
    protected UoMSearcher uomSearcher;
    protected DetailSearcher stockMovementDetailSearcher;
    protected PaymentProofSearcher paymentProofSearcher;
    protected CompanySearcher companySearcher;
    protected StockSearcher stockSearcher;
    protected CarrierSearcher carrierSearcher;

    @PostConstruct
    public void init() {
        privateManage = new PrivateManage();
        paymentProofSearcher = new PaymentProofSearcher();
        uomSearcher = new UoMSearcher();
        companySearcher = new CompanySearcher();
        stockSearcher = new StockSearcher();
        carrierSearcher = new CarrierSearcher();
        stockMovementDetailSearcher = new DetailSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        paymentProofSearcher.update();
        companySearcher.updateTarget();
        //stockSearcher.update();
        uomSearcher.update();
        carrierSearcher.update();
    }

    public void send(Long id) {
        InternalStockMovement s = mainService.getById(id);
        s.setSent(true);
        Auditory.make(s, sessionBean.getCurrentUser());
        mainService.saveOrUpdate(s);
    }

    public void receive(Long id) {
        InternalStockMovement selected = mainService.getById(id);
        selected.setDateRealArrival(new Date());
        Auditory.make(selected, sessionBean.getCurrentUser());
        internalStockMovementDetailService.getDataForManage(selected).forEach(item -> {
            //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">

            Double initialStock, detailStock, finalStock;
            Double initialCost, detailCost, finalCost;
            detailCost = ((BigDecimal) item[2]).doubleValue();
            detailStock = ((BigDecimal) item[0]).doubleValue();
            try {
                initialStock = stockService.getGroupQuantityByCompanyProduct(selected.getTargetCompany(), new Product((Long) item[1])).doubleValue();
                initialCost = productCostPriceService.getCostByCompanyProduct(selected.getTargetCompany(), new Product((Long) item[1])).doubleValue();

                finalStock = initialStock + detailStock;
                finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
            } catch (NullPointerException e) {
                finalCost = detailCost;
            }
            //</editor-fold>
            productCostPriceService.updateGroupCostByCompanyProduct(
                    new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                    selected.getTargetCompany(),
                    new Product((Long) item[1]),
                    sessionBean.getCurrentUser());
            stockService.addQuantity((BigDecimal) item[0], new Product((Long) item[1]), selected.getTargetCompany(), sessionBean.getCurrentUser());
        });
        mainService.saveOrUpdate(selected);
    }

    @Override
    public void delete(Serializable id) {
        InternalStockMovement selected = mainService.getById((Long) id);
        Auditory.make(selected, sessionBean.getCurrentUser());
        internalStockMovementDetailService.getDataForManage(selected).forEach(item -> {
            if (selected.getDateRealArrival() != null) {
                //<editor-fold defaultstate="collapsed" desc="Formula de Costo Inicial">
                Double initialStock, detailStock, finalStock;
                Double initialCost, detailCost, finalCost;
                finalStock = stockService.getGroupQuantityByCompanyProduct(selected.getTargetCompany(), new Product((Long) item[1])).doubleValue();
                finalCost = productCostPriceService.getCostByCompanyProduct(selected.getTargetCompany(), new Product((Long) item[1])).doubleValue();
                detailCost = ((BigDecimal) item[2]).doubleValue();
                detailStock = ((BigDecimal) item[0]).doubleValue();

                initialStock = finalStock - detailStock;
                initialCost = ((finalCost * finalStock) - (detailStock * detailCost)) / initialStock;
                //</editor-fold>
                productCostPriceService.updateGroupCostByCompanyProduct(new BigDecimal(initialCost).setScale(4, RoundingMode.HALF_UP), selected.getTargetCompany(), new Product((Long) item[1]), sessionBean.getCurrentUser());
                stockService.substractQuantity((BigDecimal) item[0], new Product((Long) item[1]), selected.getTargetCompany(), sessionBean.getCurrentUser());
            }
            stockService.addQuantity((BigDecimal) item[0], new Product((Long) item[1]), selected.getSourceCompany(), sessionBean.getCurrentUser());

        });
        mainService.delete(selected);
    }

    @Override
    public void recovery(Serializable id) {
        InternalStockMovement selected = mainService.getById((Long) id);
        selected.setActive(true);
        internalStockMovementDetailService.getDataForManage(selected).forEach(item -> {
            if (selected.getDateRealArrival() != null) {
                //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">

                Double initialStock, detailStock, finalStock;
                Double initialCost, detailCost, finalCost;
                detailCost = ((BigDecimal) item[2]).doubleValue();
                detailStock = ((BigDecimal) item[0]).doubleValue();
                try {
                    initialStock = stockService.getGroupQuantityByCompanyProduct(selected.getTargetCompany(), new Product((Long) item[1])).doubleValue();
                    initialCost = productCostPriceService.getCostByCompanyProduct(selected.getTargetCompany(), new Product((Long) item[1])).doubleValue();

                    finalStock = initialStock + detailStock;
                    finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
                } catch (NullPointerException e) {
                    finalCost = detailCost;
                }
                //</editor-fold>
                productCostPriceService.updateGroupCostByCompanyProduct(
                        new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                        selected.getTargetCompany(),
                        new Product((Long) item[1]),
                        sessionBean.getCurrentUser());
                stockService.addQuantity((BigDecimal) item[0], new Product((Long) item[1]), selected.getTargetCompany(), sessionBean.getCurrentUser());
            }
            stockService.substractQuantity((BigDecimal) item[0], new Product((Long) item[1]), selected.getSourceCompany(), sessionBean.getCurrentUser());

        });
        mainService.update(selected);
    }

    @Override
    public boolean save() {
        Object lastId = selected.getId();
        stockMovementDetailSearcher.add();
        if (stockMovementDetailSearcher.data.isEmpty()) {
            PNotifyMessage.errorMessage("Movimiento vacio");
            saved = false;
            return saved;
        }
        boolean allow = super.save();
        if (allow) {
            List<Long> productDetails = new ArrayList();
            stockMovementDetailSearcher.data.forEach((detail) -> {
                productDetails.add((Long) detail[7]);
            });
            Double initialStock, detailStock, finalStock;
            Double initialCost, detailCost, finalCost;
            List<Object[]> removeds = internalStockMovementDetailService.getRemoveds(productDetails, selected.getId());

            removeds.forEach(item -> {
                InternalStockMovementDetail detail = new InternalStockMovementDetail();
                detail.setId((Long) item[0]);
                detail.setQuantity((BigDecimal) item[1]);
                detail.setProduct(new Product((Long) item[2]));

                internalStockMovementDetailService.delete(detail);
                if (selected.getDateRealArrival() != null) {
                    stockService.substractQuantity(detail.getQuantity(), detail.getProduct(), selected.getTargetCompany(), sessionBean.getCurrentUser());
                }
                stockService.addQuantity(detail.getQuantity(), detail.getProduct(), selected.getSourceCompany(), sessionBean.getCurrentUser());
            });
            for (Object[] item : stockMovementDetailSearcher.data) {

                InternalStockMovementDetail detail = new InternalStockMovementDetail();
                detail.setId((Long) item[0]);
                detail.setQuantity((BigDecimal) item[1]);
                detail.setProduct(new Product((Long) item[7]));
                detail.setInternalStockMovement(selected);
                detail.setProductName((String) item[4]);
                detail.setWeight((BigDecimal) item[5]);
                detail.setWeightUoM(item[6] == null ? null : new UoM((Integer) item[6]));
                detail.setUom(new UoM((Integer) item[2]));
                detail.setUnitCost(productCostPriceService.getCostByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()));

                if (detail.getId() != null) {
                    BigDecimal quantity = internalStockMovementDetailService.getQuantityById(detail.getId());
                    
                    if (selected.getDateRealArrival() != null) {
                        //<editor-fold defaultstate="collapsed" desc="Formula de Costo Inicial">
                        finalStock = stockService.getGroupQuantityByCompanyProduct(selected.getTargetCompany(), detail.getProduct()).doubleValue();
                        finalCost = productCostPriceService.getCostByCompanyProduct(selected.getTargetCompany(), detail.getProduct()).doubleValue();
                        detailCost = internalStockMovementDetailService.getUnitCostById(detail.getId()).doubleValue();
                        detailStock = quantity.doubleValue();

                        initialStock = finalStock - detailStock;
                        initialCost = ((finalCost * finalStock) - (detailStock * detailCost)) / initialStock;
                        //</editor-fold>
                        //<editor-fold defaultstate="collapsed" desc="Formula de Costo final">
                        detailCost = detail.getUnitCost().doubleValue();
                        detailStock = detail.getQuantity().doubleValue();
                        finalStock = initialStock + detailStock;

                        finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;

                        //</editor-fold>
                        productCostPriceService.updateGroupCostByCompanyProduct(new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP), selected.getTargetCompany(), detail.getProduct(), sessionBean.getCurrentUser());

                        stockService.substractQuantity(quantity, detail.getProduct(), selected.getTargetCompany(), sessionBean.getCurrentUser());
                    }
                    stockService.addQuantity(quantity, detail.getProduct(), selected.getSourceCompany(), sessionBean.getCurrentUser());
                }

                if (selected.getDateRealArrival() != null) {
                    //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">
                    detailCost = detail.getUnitCost().doubleValue();
                    detailStock = detail.getQuantity().doubleValue();
                    try {
                        initialStock = stockService.getGroupQuantityByCompanyProduct(selected.getTargetCompany(), detail.getProduct()).doubleValue();
                        initialCost = productCostPriceService.getCostByCompanyProduct(selected.getTargetCompany(), detail.getProduct()).doubleValue();

                        finalStock = initialStock + detailStock;
                        finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
                    } catch (NullPointerException e) {
                        finalCost = detailCost;
                    }
                    //</editor-fold>
                    productCostPriceService.updateGroupCostByCompanyProduct(new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP), selected.getTargetCompany(), detail.getProduct(), sessionBean.getCurrentUser());
                    stockService.addQuantity(detail.getQuantity(), detail.getProduct(), selected.getTargetCompany(), sessionBean.getCurrentUser());
                }
                stockService.substractQuantity(detail.getQuantity(), detail.getProduct(), selected.getSourceCompany(), sessionBean.getCurrentUser());

                internalStockMovementDetailService.saveOrUpdate(detail);

            }
        }
        PNotifyMessage.saveMessage("Se ha creado el movimiento " + serie + "-" + documentNumber);
        BeanUtil.exceuteJS("window.open('" + BeanUtil.getRequest().getContextPath() + "/resources/ism/print.pdf?id=" + selected.getId() + "')");
        return allow;
    }

    public synchronized void newDocumentNumber() {

        if (sessionBean.getCurrentUser().getSuperUser()) {
            serie = "G001";
        } else {
            serie = null;
            for (Object[] item : sessionBean.getDocumentNumberings()) {
                if ((Short) item[1] == paymentProofId.shortValue()) {
                    serie = (String) item[2];
                }
            }
        }
        documentNumber = String.format("%08d", documentNumberingService.getByHQL("SELECT "
                + "dn.numbering+1 "
                + "FROM "
                + "DocumentNumbering dn "
                + "WHERE dn.rucCompany LIKE ? AND dn.serie LIKE ? and dn.paymentProof = ?", selected.getSourceCompany().getRuc(), serie, selected.getPaymentProof()));
        try {
            documentNumberingService.updateHQL("UPDATE DocumentNumbering dn SET dn.numbering = dn.numbering+1 WHERE dn.rucCompany = ? AND dn.serie = ? AND dn.paymentProof = ?", selected.getSourceCompany().getRuc(), serie, selected.getPaymentProof());
        } catch (Exception ex) {
            Logger.getLogger(ManagedSaleBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void changeCarrierType() {
        if (selected.getId() == null) {
            driverLicense = sessionBean.getCurrentCompany().getPrivateDriverLicense();
            transportDescription = sessionBean.getCurrentCompany().getPrivateTransportDescription();
        }
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
            sourceCompanyId = sessionBean.getCurrentCompany().getId();
        } catch (Exception e) {
            sourceCompanyId = null;
        }
        try {
            targetCompanyId = selected.getTargetCompany().getId();
        } catch (Exception e) {
            targetCompanyId = null;
        }
        try {
            carrierId = selected.getCarrier().getId();
        } catch (Exception e) {
            carrierId = null;
        }
        dateShipping = selected.getDateShipping();
        dateArrival = selected.getDateArrival();

        if (selected.getId() == null) {
            transportDescription = selected.getTransportDescription();
            driverLicense = selected.getDriverLicense();
        } else {
            transportDescription = sessionBean.getCurrentCompany().getPrivateDriverLicense();
            driverLicense = sessionBean.getCurrentCompany().getPrivateDriverLicense();
        }
        if (selected.getId() != null) {
            typeCarrier = selected.getCarrier() != null;
        } else {
            typeCarrier = true;
            changeCarrierType();
        }
        onLoad(true);
        stockMovementDetailSearcher.update();
        stockSearcher.refresh();
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        if (paymentProofSearcher.data.size() == 1) {
            paymentProofId = (Short) paymentProofSearcher.data.get(0)[0];
        }

        try {
            selected.setPaymentProof(paymentProofService.getById(paymentProofId));
        } catch (Exception e) {
            selected.setPaymentProof(null);
        }
        selected.setOperationTypeSource(new OperationType(operationTypeService.getISMOutTypeId()));
        selected.setOperationTypeTarget(new OperationType(operationTypeService.getISMInTypeId()));

        if (!typeCarrier) {
            try {
                selected.setCarrier(actorService.getById(carrierId));
            } catch (Exception e) {
                selected.setCarrier(null);
            }
            selected.setTransportDescription(null);
            selected.setDriverLicense(null);
        } else {
            selected.setCarrier(null);
            selected.setTransportDescription(transportDescription);
            selected.setDriverLicense(driverLicense);
        }
        try {
            selected.setTargetCompany(companyService.getById(targetCompanyId));
        } catch (Exception e) {
            selected.setTargetCompany(null);
        }

        try {
            selected.setSourceCompany(sessionBean.getCurrentCompany());
        } catch (Exception e) {
            selected.setSourceCompany(null);
        }

        if (documentNumber == null) {
            newDocumentNumber();
        }
        selected.setSerie(serie);
        selected.setDocumentNumber(documentNumber);

        selected.setDateShipping(dateShipping);
        selected.setDateArrival(dateArrival);
        Auditory.make(selected, sessionBean.getCurrentUser());
    }

    /**
     * @return the mainService
     */
    @Override
    public IInternalStockMovementService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    @Override
    public void setMainService(IInternalStockMovementService mainService) {
        this.mainService = mainService;
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
     * @return the sourceCompanyId
     */
    public Integer getSourceCompanyId() {
        return sourceCompanyId;
    }

    /**
     * @param sourceCompanyId the sourceCompanyId to set
     */
    public void setSourceCompanyId(Integer sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    /**
     * @return the targetCompanyId
     */
    public Integer getTargetCompanyId() {
        return targetCompanyId;
    }

    /**
     * @param targetCompanyId the targetCompanyId to set
     */
    public void setTargetCompanyId(Integer targetCompanyId) {
        this.targetCompanyId = targetCompanyId;
    }

    /**
     * @return the carrierId
     */
    public Long getCarrierId() {
        return carrierId;
    }

    /**
     * @param carrierId the carrierId to set
     */
    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    /**
     * @return the dateShipping
     */
    public Date getDateShipping() {
        return dateShipping;
    }

    /**
     * @param dateShipping the dateShipping to set
     */
    public void setDateShipping(Date dateShipping) {
        this.dateShipping = dateShipping;
    }

    /**
     * @return the dateArrival
     */
    public Date getDateArrival() {
        return dateArrival;
    }

    /**
     * @param dateArrival the dateArrival to set
     */
    public void setDateArrival(Date dateArrival) {
        this.dateArrival = dateArrival;
    }

    /**
     * @return the transportDescription
     */
    public String getTransportDescription() {
        return transportDescription;
    }

    /**
     * @param transportDescription the transportDescription to set
     */
    public void setTransportDescription(String transportDescription) {
        this.transportDescription = transportDescription;
    }

    /**
     * @return the driverLicense
     */
    public String getDriverLicense() {
        return driverLicense;
    }

    /**
     * @param driverLicense the driverLicense to set
     */
    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
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
     * @return the companySearcher
     */
    public CompanySearcher getCompanySearcher() {
        return companySearcher;
    }

    /**
     * @param companySearcher the companySearcher to set
     */
    public void setCompanySearcher(CompanySearcher companySearcher) {
        this.companySearcher = companySearcher;
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
     * @return the carrierSearcher
     */
    public CarrierSearcher getCarrierSearcher() {
        return carrierSearcher;
    }

    /**
     * @param carrierSearcher the carrierSearcher to set
     */
    public void setCarrierSearcher(CarrierSearcher carrierSearcher) {
        this.carrierSearcher = carrierSearcher;
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
    public DetailSearcher getStockMovementDetailSearcher() {
        return stockMovementDetailSearcher;
    }

    /**
     * @param stockMovementDetailSearcher the stockMovementDetailSearcher to set
     */
    public void setStockMovementDetailSearcher(DetailSearcher stockMovementDetailSearcher) {
        this.stockMovementDetailSearcher = stockMovementDetailSearcher;
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
     * @return the uomSearcher
     */
    public UoMSearcher getUomSearcher() {
        return uomSearcher;
    }

    /**
     * @param uomSearcher the uomSearcher to set
     */
    public void setUomSearcher(UoMSearcher uomSearcher) {
        this.uomSearcher = uomSearcher;
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

    //<editor-fold defaultstate="collapsed" desc="movementDetailSearcher">    
    public class DetailSearcher implements java.io.Serializable {

        public List<Object[]> data;

        public DetailSearcher() {
        }

        public void update() {
            data = new ArrayList();
            if (selected.getId() != null) {
                data.addAll(getInternalStockMovementDetailService().listHQL(""
                        + "SELECT "
                        + "ismd.id,"
                        + "ismd.quantity,"
                        + "ismd.uom.id,"
                        + "ismd.uom.abbr,"
                        + "ismd.productName,"
                        + "ismd.weight,"
                        + "ismd.weightUoM.id,"
                        + "ismd.product.id FROM InternalStockMovementDetail ismd WHERE ismd.internalStockMovement = ?", selected));
            }

        }

        public void add() {
            data = new ArrayList();
            stockSearcher.data.forEach(item -> {
                BigDecimal quantity = (BigDecimal) item[8];
                if (quantity != null && quantity.doubleValue() != 0) {
                    data.add(new Object[]{
                        item[9],
                        item[8],
                        item[5],
                        item[6],
                        item[3],
                        null,
                        null,
                        item[1]
                    });
                }
            });
        }

        public List<Object[]> getData() {
            return data;
        }

        public void setData(List<Object[]> data) {
            this.data = data;
        }

    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="UoMSearcher">
    public class UoMSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getUoMService().listHQL("SELECT uom.id,uom.abbr,uom.name FROM UoM uom WHERE uom.active = ?", true);
        }

        public List<Object[]> getData() {
            return data;
        }

        public void setData(List<Object[]> data) {
            this.data = data;
        }

    }

    //</editor-fold>    
    //<editor-fold defaultstate="collapsed" desc="PaymentProofSearcher">
    public class PaymentProofSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getPaymentProofService().listHQL("SELECT pp.id,pp.abbr,pp.name FROM PaymentProof pp WHERE pp.forStored = ?", true);
        }

        public List<Object[]> getData() {
            return data;
        }

    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="CompanySearcher">
    public class CompanySearcher implements java.io.Serializable {

        private List<Object[]> targetData;

        public void updateTarget() {
            targetData = getCompanyService().listHQL("SELECT c.id,c.name,c.city,c.address FROM Company c " + (getSourceCompanyId() == null ? "" : ("WHERE c.id != " + getSourceCompanyId())));
        }

        public List<Object[]> getTargetData() {
            return targetData;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="StockSearcher">
    public class StockSearcher implements java.io.Serializable {

        protected List<Object[]> data;

        public void refresh() {
            data = Collections.EMPTY_LIST;
            update();
        }

        public void update() {
            if (getSourceCompanyId() == null || getTargetCompanyId() == null) {
                data = Collections.EMPTY_LIST;
//                pagination.setData(Collections.EMPTY_LIST);
                return;
            }
            data = stockService.listHQL("SELECT "
                    + "s.id,"
                    + "s.product.id,"
                    + "s.product.barcode,"
                    + "s.product.name,"
                    + "s.quantity,"
                    + "s.product.uom.id,"
                    + "s.product.uom.abbr,"
                    + "COALESCE((SELECT st.quantity FROM Stock st WHERE st.company.id = ? AND st.product.id = s.product.id),0),"
                    + "0 as quantity,"
                    + "cast(null as char) "
                    + "FROM Stock s WHERE s.product.active = true AND s.company.id = ? AND s.quantity <> 0", targetCompanyId, sourceCompanyId);

            data.forEach(item -> {
                Long productId = (Long) item[1];
                stockMovementDetailSearcher.getData().forEach(detail -> {
                    if (((Long) item[1]).longValue() == (Long) detail[7]) {
                        item[8] = detail[1];
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
    //<editor-fold defaultstate="collapsed" desc="CarrierSearcher">
    public class CarrierSearcher implements java.io.Serializable {

        private List<Object[]> data;
        private Actor carrier;
        private boolean saved;

        public CarrierSearcher() {
            saved = false;
        }

        public void update() {
            data = getActorService().listHQL("SELECT a.id,a.identityDocument.abbr,a.identityNumber,a.name FROM Actor a WHERE a.active = true AND a.identityDocument.code like ?", "6");
        }

        public void create() {
            saved = false;
            carrier = new Actor();
        }

        public void save() {
            if (getActorService().getByHQL("SELECT 1 FROM Actor a where a.identityNumber LIKE ? ", carrier.getIdentityNumber()) != null) {
                new PNotifyMessage("ERROR", "El ruc ingresado ya esta asignado a otra empresa", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated").execute();
                saved = false;
                return;
            }
            carrier.setIdentityDocument((IdentityDocument) getIdentityDocumentService().getByHQL("FROM IdentityDocument idd Where idd.code LIKE ? OR idd.code LIKE ?", "6", "06"));

            Auditory.make(carrier, getSessionBean().getCurrentUser());
            getActorService().saveOrUpdate(carrier);
            saved = true;
            setCarrierId(carrier.getId());
            update();
        }

        /**
         * @return the carrier
         */
        public Actor getCarrier() {
            return carrier;
        }

        public Actor getReanOnlyCarrier() {
            return carrier;
        }

        public void setReanOnlyCarrier(Actor carrier) {
        }

        /**
         * @param carrier the carrier to set
         */
        public void setCarrier(Actor carrier) {
            this.carrier = carrier;
        }

        /**
         * @return the saved
         */
        public boolean isSaved() {
            return saved;
        }

        /**
         * @param saved the saved to set
         */
        public void setSaved(boolean saved) {
            this.saved = saved;
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

    public class PrivateManage implements java.io.Serializable {

        private String transportDescription;
        private String driverLicense;

        public PrivateManage() {
        }

        public void begin() {
            transportDescription = getSessionBean().getCurrentCompany().getPrivateTransportDescription();
            driverLicense = getSessionBean().getCurrentCompany().getPrivateDriverLicense();
        }

        public void save() {
            transportDescription = transportDescription.length() == 0 ? null : transportDescription;
            driverLicense = driverLicense.length() == 0 ? null : driverLicense;
            getSessionBean().getCurrentCompany().setPrivateDriverLicense(driverLicense);
            getSessionBean().getCurrentCompany().setPrivateTransportDescription(transportDescription);
            getCompanyService().saveOrUpdate(getSessionBean().getCurrentCompany());
            ManagedStockMovementBean.this.setDriverLicense(driverLicense);
            ManagedStockMovementBean.this.setTransportDescription(transportDescription);
        }

        /**
         * @return the transportDescription
         */
        public String getTransportDescription() {
            return transportDescription;
        }

        /**
         * @param transportDescription the transportDescription to set
         */
        public void setTransportDescription(String transportDescription) {
            this.transportDescription = transportDescription;
        }

        /**
         * @return the driverLicense
         */
        public String getDriverLicense() {
            return driverLicense;
        }

        /**
         * @param driverLicense the driverLicense to set
         */
        public void setDriverLicense(String driverLicense) {
            this.driverLicense = driverLicense;
        }

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
     * @return the typeCarrier
     */
    public boolean isTypeCarrier() {
        return typeCarrier;
    }

    /**
     * @param typeCarrier the typeCarrier to set
     */
    public void setTypeCarrier(boolean typeCarrier) {
        this.typeCarrier = typeCarrier;
    }

    /**
     * @return the privateManage
     */
    public PrivateManage getPrivateManage() {
        return privateManage;
    }

    /**
     * @param privateManage the privateManage to set
     */
    public void setPrivateManage(PrivateManage privateManage) {
        this.privateManage = privateManage;
    }

    /**
     * @return the internalStockMovementDetailService
     */
    public IInternalStockMovementDetailService getInternalStockMovementDetailService() {
        return internalStockMovementDetailService;
    }

    /**
     * @param internalStockMovementDetailService the
     * internalStockMovementDetailService to set
     */
    public void setInternalStockMovementDetailService(IInternalStockMovementDetailService internalStockMovementDetailService) {
        this.internalStockMovementDetailService = internalStockMovementDetailService;
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
}
