/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SaleBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Actor;
import cs.bms.model.IdentityDocument;
import cs.bms.model.Product;
import cs.bms.model.Sale;
import cs.bms.model.SaleDetail;
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
import cs.bms.service.interfac.ISaleDetailService;
import cs.bms.service.interfac.ISaleService;
import cs.bms.service.interfac.IStockService;
import cs.bms.util.ActorSearcher;
import gkfire.auditory.Auditory;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
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
import javax.faces.bean.ViewScoped;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@ViewScoped
public class ManagedSaleBean extends AManagedBean<Sale, ISaleService> implements ILoadable {

    @ManagedProperty(value = "#{saleService}")
    protected ISaleService mainService;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{stockService}")
    protected IStockService stockService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{saleDetailService}")
    protected ISaleDetailService saleDetailService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{paymentProofService}")
    protected IPaymentProofService paymentProofService;
    @ManagedProperty(value = "#{productSalePriceService}")
    protected IProductSalePriceService productSalePriceService;
    @ManagedProperty(value = "#{productCostPriceService}")
    protected IProductCostPriceService productCostPriceService;
    @ManagedProperty(value = "#{documentNumberingService}")
    protected IDocumentNumberingService documentNumberingService;
    @ManagedProperty(value = "#{productService}")
    protected IProductService productService;
    @ManagedProperty(value = "#{districtService}")
    protected IDistrictService districtService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{saleBean}")
    protected SaleBean saleBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    @ManagedProperty(value = "#{managedSupplierBean}")
    protected ManagedSupplierBean managedSupplierBean;

    protected DetailSearcher detailSearcher;
    protected StockSearcher stockSearcher;
    protected PaymentProofSearcher paymentProofSearcher;
    protected CustomerSearcher customerSearcher;

    protected Long points;
    protected Short paymentProofId;
    protected String serie;
    protected String documentNumber;
    protected boolean electronic;
    protected Long customerId;
    protected String customerName;
    protected BigDecimal subtotal;
    protected BigDecimal igv;
    protected BigDecimal igvDiscount;
    protected BigDecimal subtotalDiscount;
    protected Date dateIssue;
    protected Integer companyId;
    protected BigDecimal igvPercent;

    @PostConstruct
    public void init() {
        detailSearcher = new DetailSearcher();
        stockSearcher = new StockSearcher();
        customerSearcher = new CustomerSearcher();
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
    public void delete(Serializable id) {
        if (mainService.isActive((Long) id)) {
            Sale sale = mainService.getById((Long) id);
            if (sale.getCustomer() != null) {
                Long currentPoints = sale.getPoints() - new Double(sale.getSubtotalDiscount().doubleValue() * 100).longValue();
                if (currentPoints > 0) {
                    actorService.subtractPoints(sale.getCustomer().getId(), sale.getPoints(), sessionBean.getCurrentUser());
                }
            }
            saleDetailService.getProductDataBySale(sale).forEach(item -> {
                stockService.addQuantity((BigDecimal) item[1], new Product((Long) item[0]), sale.getCompany(), sessionBean.getCurrentUser());
            });

            Auditory.make(sale, sessionBean.getCurrentUser());
            sale.setSent(false);
            getMainService().delete(sale);
        } else {
            PNotifyMessage.errorMessage("Esta venta ya fue anulada!!");
        }
    }

    @Override
    public void recovery(Serializable id) {
        if (!mainService.isActive((Long) id)) {
            Sale sale = mainService.getById((Long) id);
            if (sale.getCustomer() != null) {
                Long currentPoints = sale.getPoints() - new Double(sale.getSubtotalDiscount().doubleValue() * 100).longValue();
                if (currentPoints > 0) {
                    actorService.addPoints(sale.getCustomer().getId(), sale.getPoints(), sessionBean.getCurrentUser());
                }
            }
            saleDetailService.getProductDataBySale(sale).forEach(item -> {
                stockService.substractQuantity((BigDecimal) item[1], new Product((Long) item[0]), sale.getCompany(), sessionBean.getCurrentUser());
            });
            Auditory.make(sale, sessionBean.getCurrentUser());
            sale.setSent(false);
            sale.setActive(true);
            getMainService().saveOrUpdate(sale);
        }else{
            PNotifyMessage.errorMessage("Esta venta ya fue recuperada!!");
        }
    }

    @Override
    public boolean save() {
        saved = false;
        try {
            if (detailSearcher.data.isEmpty()) {
                PNotifyMessage.saveMessage("La venta esta vacia");
                saved = false;
                return saved;

            }
            saved = super.save();
        } catch (Exception e) {
            PNotifyMessage.systemError(e, sessionBean);
            saved = false;
            return saved;
        }
        if (saved) {
            if (selected.getCustomer() != null) {         
                Long currentPoints = points - new Double(subtotalDiscount.doubleValue() * 100).longValue();
                if (currentPoints > 0) {
                    actorService.addPoints(selected.getCustomer().getId(), currentPoints, sessionBean.getCurrentUser());
                }else if (currentPoints < 0){
                    actorService.subtractPoints(selected.getCustomer().getId(), currentPoints*-1, sessionBean.getCurrentUser());
                }
            }
            for (Object[] item : detailSearcher.removed) {
                SaleDetail saleDetail = new SaleDetail();
                saleDetail.setId((Long) item[0]);
                saleDetail.setQuantity((BigDecimal) item[1]);
                saleDetail.setProduct(new Product((Long) item[6]));
                saleDetailService.delete(saleDetail);
                try {
                    stockService.addQuantity(saleDetail.getQuantity(), saleDetail.getProduct(), selected.getCompany(), sessionBean.getCurrentUser());
                } catch (Exception e) {
                    PNotifyMessage.systemError(e, sessionBean);
                    saved = false;
                    return saved;
                }

            }
            detailSearcher.data.forEach(item -> {
                SaleDetail detail = new SaleDetail();
                detail.setId((Long) item[0]);
                detail.setQuantity((BigDecimal) item[1]);
                detail.setProduct(new Product((Long) item[6]));
                detail.setSale(selected);
                detail.setProductName((String) item[3]);
                detail.setUnitPrice((BigDecimal) item[4]);
                detail.setUom(new UoM((Integer) item[7]));
                detail.setSubtotal((BigDecimal) item[9]);
                detail.setPointsPrice((BigDecimal) item[10]);
                detail.setUnitCost(productCostPriceService.getCostByCompanyProduct(sessionBean.getCurrentCompany(), detail.getProduct()));
                if (detail.getUnitCost() == null) {
                    detail.setUnitCost(BigDecimal.ZERO);
                }
                if (detail.getId() != null) {
                    BigDecimal quantity = saleDetailService.getQuantityById(detail.getId());
                    stockService.addQuantity(quantity, detail.getProduct(), selected.getCompany(), sessionBean.getCurrentUser());

                }
                stockService.substractQuantity(detail.getQuantity(), detail.getProduct(), selected.getCompany(), sessionBean.getCurrentUser());
                saleDetailService.saveOrUpdate(detail);
            });
        }
        PNotifyMessage.saveMessage("Se ha creado la venta " + serie + "-" + documentNumber);
        return saved;
    }

    public void doSave() {
        if (save()) {
            if (!createAgain) {
                BeanUtil.exceuteJS("$.magnificPopup.close();");
                setSelected(null);
            } else {
                Integer companyId = this.companyId;
                create();
                setCompanyId(companyId);
            }
            saleBean.search();
        }
    }

    @Override
    protected void fillFields() {
        try {
            paymentProofId = selected.getPaymentProof().getId();
        } catch (NullPointerException npe) {
            paymentProofId = null;
        }
        serie = selected.getSerie();
        documentNumber = selected.getDocumentNumber();
        electronic = selected.getElectronic();
        try {
            customerId = selected.getCustomer().getId();
        } catch (NullPointerException npe) {
            customerId = null;
        }
        if (customerId != null) {
            customerSearcher.setActor(selected.getCustomer());
            customerSearcher.setIdentityNumber(selected.getCustomer().getIdentityNumber());
        } else {
            customerSearcher.setActor(null);
            customerSearcher.setIdentityNumber(null);
        }
        customerName = selected.getCustomerName();
        subtotal = selected.getSubtotal();
        igv = selected.getIgv();
        igvDiscount = selected.getIgvDiscount();
        subtotalDiscount = selected.getSubtotalDiscount();
        dateIssue = selected.getDateIssue();
        points = selected.getPoints();
        try {
            companyId = selected.getCompany().getId();
            igvPercent = selected.getCompany().getIgvPercent();
        } catch (NullPointerException npe) {
            companyId = sessionBean.getCurrentCompany().getId();
            igvPercent = sessionBean.getCurrentCompany().getIgvPercent();
        }
        createAgain = selected.getId() == null;
        detailSearcher.update();
        stockSearcher.refresh();
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
        selected.setCompany(companyService.getById(companyId));
        if (documentNumber == null) {
            newDocumentNumber();
        }

        selected.setSerie(serie);
        selected.setDocumentNumber(documentNumber);

        selected.setElectronic(true);
        selected.setCustomer(customerSearcher.getActor());
        selected.setCustomerName(selected.getCustomer() == null ? customerName : (selected.getCustomer().getOther() != null ? selected.getCustomer().getOther() : selected.getCustomer().getName()));
        selected.setPoints(points);
        selected.setSubtotal(subtotal);
        selected.setIgv(igv);

        selected.setIgvDiscount(igvDiscount);
        selected.setSubtotalDiscount(subtotalDiscount);
        selected.setDateIssue(dateIssue == null ? new Date() : dateIssue);
        Auditory.make(selected, sessionBean.getCurrentUser());
    }

    public synchronized void newDocumentNumber() {

        if (sessionBean.getCurrentUser().getSuperUser()) {
            serie = selected.getPaymentProof().getName().equalsIgnoreCase("Factura") ? "F001" : "B001";
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
                + "WHERE dn.rucCompany LIKE ? AND dn.serie LIKE ? and dn.paymentProof = ?", selected.getCompany().getRuc(), serie, selected.getPaymentProof()));
        try {
            documentNumberingService.updateHQL("UPDATE DocumentNumbering dn SET dn.numbering = dn.numbering+1 WHERE dn.rucCompany = ? AND dn.serie = ? AND dn.paymentProof = ?", selected.getCompany().getRuc(), serie, selected.getPaymentProof());
        } catch (Exception ex) {
            Logger.getLogger(ManagedSaleBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the mainService
     */
    public ISaleService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(ISaleService mainService) {
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
     * @return the customerSearcher
     */
    public CustomerSearcher getCustomerSearcher() {
        return customerSearcher;
    }

    /**
     * @param customerSearcher the customerSearcher to set
     */
    public void setCustomerSearcher(CustomerSearcher customerSearcher) {
        this.customerSearcher = customerSearcher;
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
     * @return the customerId
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId the customerId to set
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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
    public BigDecimal getIgv() {
        return igv;
    }

    /**
     * @param igv the igv to set
     */
    public void setIgv(BigDecimal igv) {
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
    public Integer getCompanyId() {
        return companyId;
    }

    /**
     * @param companyId the companyId to set
     */
    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
        this.igvPercent = (BigDecimal) companyService.getByHQL("SELECT c.igvPercent FROM Company c WHERE c.id = ?", companyId);
        stockSearcher.refresh();
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
            List<Object[]> details = getSaleDetailService().listHQL("SELECT "
                    + "sd.id,"
                    + "sd.quantity,"
                    + "sd.uom.abbr,"
                    + "sd.product.name,"
                    + "sd.unitPrice,"
                    + "sd.id,"
                    + "sd.product.id,"
                    + "sd.uom.id,"
                    + "(sd.quantity+(SELECT st.quantity FROM Stock st WHERE st.product.id = sd.product.id AND st.company.id = sd.sale.company.id)), "
                    + "sd.subtotal,"
                    + "sd.pointsPrice "
                    + "from SaleDetail sd WHERE sd.sale = ?", selected);
            /**
             * Obtencion del catalogo de precios
             */
            details.forEach(d -> {
                d[5] = getProductSalePriceService().listHQL(""
                        + "SELECT psp.quantity,psp.price "
                        + "FROM ProductSalePrice psp "
                        + "WHERE psp.company.id = ? AND psp.product.id = ? ORDER BY psp.quantity", getCompanyId(), d[6]);
                data.add(d);
            });
        }

        public void remove(int index) {
            Object[] item = data.get(index);
            if (item[0] != null) {
                removed.add(item);
            }
            data.remove(index);
            getStockSearcher().searchAfterRemoved();
        }

        public void add() {
            int index = Integer.parseInt(BeanUtil.getParameter("index"));
            Object[] item = getStockSearcher().getPagination().getData().get(index);
            add(item, 1, false);
        }

        public void add(Object[] item, Integer quantity, boolean escapeUpdate) {
            try {
                BigDecimal subtotal = new BigDecimal((((BigDecimal) ((List<Object[]>) item[6]).get(0)[1]).doubleValue() * quantity.doubleValue()) / ((Integer) ((List<Object[]>) item[6]).get(0)[0]).doubleValue()).setScale(2, RoundingMode.HALF_UP);
                Object[] detailItem = new Object[]{
                    null, // 0 ID del detalle
                    quantity, // 1  Cantidad del detalle
                    item[4], // 2 Abreviatura de la unidad de medida
                    item[2], // 3 Nombre del producto
                    ((List<Object[]>) item[6]).get(0)[1], // 4 Precio Unitario
                    item[6], // 5 Catalogo de Precios
                    item[0], // 6 ID del producto
                    item[3], // 7 ID de la unidad de medida
                    item[5], // 8 MAXIMA CAPACIDAD
                    subtotal, // 9 SUBTOTAL
                    subtotal.setScale(0, RoundingMode.DOWN).intValue(), // 10 Precios de punto
                };
                data.add(detailItem);
                BeanUtil.exceuteJS("$('#formc\\\\:detail-wrapper table tr[data-p-id=" + item[0] + "] .detail-quantity').focus()");
                if (!escapeUpdate) {
                    getStockSearcher().search();
                }
            } catch (java.lang.IndexOutOfBoundsException e) {
                new PNotifyMessage("ERROR!", "El producto " + item[2] + " no tiene un precio de venta establecido", PNotifyMessage.Type.Danger, "fa fa-warning shaked animated").execute();
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
            data = getPaymentProofService().listHQL("SELECT pp.id, pp.name FROM PaymentProof pp WHERE pp.active = true AND pp.forSale=true");
        }

        public List<Object[]> getData() {
            return data;
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="StockSearcher">
    public class StockSearcher implements java.io.Serializable {

        private String terms;
        private Pagination<Object[]> pagination;
        private OrderFactory orderFactory;
        private ProjectionList projectionList;
        private AliasList aliasList;

        public StockSearcher() {
            pagination = new Pagination<Object[]>(getStockService()) {
                @Override
                public void search(int page, Object... variant) {
                    super.search(page, variant); //To change body of generated methods, choose Tools | Templates.
                    getData().forEach((item) -> {
                        item[6] = productSalePriceService.getBasicDataByCompanyProduct(sessionBean.getCurrentCompany(), new Product((Long) item[0]));
                    });
                }
            };
            pagination.setRows(10);
            orderFactory = new OrderFactory(new OrderList());
            orderFactory.setDefaultOrder(Order.asc("p.name"));
            projectionList = Projections.projectionList()
                    .add(Projections.property("p.id"))
                    .add(Projections.property("p.image"))
                    .add(Projections.property("p.name"))
                    .add(Projections.property("uom.id"))
                    .add(Projections.property("uom.abbr"))
                    .add(Projections.property("quantity"))
                    .add(Projections.property("uom.id"));
            aliasList = new AliasList();
            aliasList.add("product", "p");
            aliasList.add("p.uom", "uom");
            aliasList.add("company", "c");
            terms = "";
        }

        public void refresh() {
            terms = "";
            search();
        }

        public void searchAfterRemoved() {
            if (getCompanyId() == null) {
                pagination.setData(Collections.EMPTY_LIST);
                return;
            }
            terms = terms.trim();
            CriterionList criterionList = new CriterionList()
                    ._add(Restrictions.eq("p.active", true))
                    ._add(Restrictions.eq("c.id", getCompanyId()))
                    ._add(Restrictions.ne("quantity", BigDecimal.ZERO));
            if (terms.length() != 0) {
                criterionList.add(
                        Restrictions.or(
                                Restrictions.like("p.barcode", terms, MatchMode.START),
                                Restrictions.like("p.name", terms, MatchMode.ANYWHERE).ignoreCase()
                        )
                );
            }
            pagination.search(1, aliasList, criterionList, projectionList, orderFactory.make());
        }

        public void search() {
            if (getCompanyId() == null) {
                pagination.setData(Collections.EMPTY_LIST);
                return;
            }
            terms = terms.trim();
            CriterionList criterionList = new CriterionList()
                    ._add(Restrictions.eq("p.active", true))
                    ._add(Restrictions.eq("c.id", getCompanyId()))
                    ._add(Restrictions.ne("quantity", BigDecimal.ZERO));
            if (terms.length() != 0) {
                criterionList.add(
                        Restrictions.or(
                                Restrictions.like("p.barcode", terms, MatchMode.START),
                                Restrictions.like("p.name", terms, MatchMode.ANYWHERE).ignoreCase()
                        )
                );
            }
            pagination.search(1, aliasList, criterionList, projectionList, orderFactory.make());
            if (pagination.getData().isEmpty() && productService.existBarCode(terms, null)) {
                PNotifyMessage.errorMessage("No existe existencias de ese producto");
            }
            if (NumberUtils.isDigits(terms) && pagination.getData().size() == 1) {
                Integer quantity = 0;
                for (Object[] item : detailSearcher.getData()) {
                    if (((Long) item[6]).longValue() == ((Long) pagination.getData().get(0)[0]).longValue()) {

                        quantity = ((BigDecimal) item[1]).intValue() + 1;
                        if (quantity > ((BigDecimal) item[8]).doubleValue()) {
                            PNotifyMessage.errorMessage("Existencia maxima alcanzada");
                        } else {
                            item[1] = quantity;
                        }
                        return;
                    }
                }
                quantity++;
                detailSearcher.add(pagination.getData().get(0), quantity, true);
            }
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
         * @return the pagination
         */
        public Pagination<Object[]> getPagination() {
            return pagination;
        }

        /**
         * @return the orderFactory
         */
        public OrderFactory getOrderFactory() {
            return orderFactory;
        }

        /**
         * @param pagination the pagination to set
         */
        public void setPagination(Pagination<Object[]> pagination) {
            this.pagination = pagination;
        }

        /**
         * @param orderFactory the orderFactory to set
         */
        public void setOrderFactory(OrderFactory orderFactory) {
            this.orderFactory = orderFactory;
        }

        /**
         * @return the projectionList
         */
        public ProjectionList getProjectionList() {
            return projectionList;
        }

        /**
         * @param projectionList the projectionList to set
         */
        public void setProjectionList(ProjectionList projectionList) {
            this.projectionList = projectionList;
        }

        /**
         * @return the aliasList
         */
        public AliasList getAliasList() {
            return aliasList;
        }

        /**
         * @param aliasList the aliasList to set
         */
        public void setAliasList(AliasList aliasList) {
            this.aliasList = aliasList;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CustomerSearcher">
    public class CustomerSearcher extends ActorSearcher {

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
            actor.setSynchronized_(Boolean.TRUE);
            actor.setName(name);
            actor.setCustomer(true);
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
                BeanUtil.exceuteJS("open_create_customer();");
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
            actor.setCustomer(true);
            actor.setOther(data[5]);
            Auditory.make(actor, sessionBean.getCurrentUser());
        }

        @Override
        public void search() {
            super.search();
            if (actor != null) {
                actor.setCustomer(Boolean.TRUE);
            }
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
            managedSupplierBean.setSupplier(true);
        }

        public void save() {
            managedSupplierBean.save();
            if (managedSupplierBean.isSaved()) {
                actor = getManagedSupplierBean().getSelected();
                identityNumber = actor.getIdentityNumber();
                setCustomerId(getManagedSupplierBean().getSelected().getId());
            }
        }

    }
    //</editor-fold>

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
