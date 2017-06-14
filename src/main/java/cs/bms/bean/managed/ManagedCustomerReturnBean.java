/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.PaymentProof;
import cs.bms.model.Product;
import cs.bms.model.Sale;
import cs.bms.model.StockReturnCustomer;
import cs.bms.model.StockReturnCustomerDetail;
import cs.bms.model.UoM;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.ISaleDetailService;
import cs.bms.service.interfac.ISaleService;
import cs.bms.service.interfac.IStockReturnCustomerDetailService;
import cs.bms.service.interfac.IStockReturnCustomerService;
import cs.bms.service.interfac.IStockService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.io.Serializable;
import java.math.BigDecimal;
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
public class ManagedCustomerReturnBean extends AManagedBean<StockReturnCustomer, IStockReturnCustomerService> implements ILoadable {

    @ManagedProperty(value = "#{stockReturnCustomerService}")
    private IStockReturnCustomerService mainService;
    @ManagedProperty(value = "#{stockReturnCustomerDetailService}")
    private IStockReturnCustomerDetailService stockReturnCustomerDetailService;
    @ManagedProperty(value = "#{saleService}")
    private ISaleService saleService;
    @ManagedProperty(value = "#{saleDetailService}")
    private ISaleDetailService saleDetailService;
    @ManagedProperty(value = "#{stockService}")
    private IStockService stockService;
    @ManagedProperty(value = "#{documentNumberingService}")
    private IDocumentNumberingService documentNumberingService;
    @ManagedProperty(value = "#{paymentProofService}")
    private IPaymentProofService paymentProofService;
    @ManagedProperty(value = "#{companyService}")
    private ICompanyService companyService;
    @ManagedProperty(value = "#{actorService}")
    private IActorService actorService;
    @ManagedProperty(value = "#{productService}")
    private IProductService productService;
    @ManagedProperty(value = "#{identityDocumentService}")
    private IIdentityDocumentService identityDocumentService;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;

    private Short paymentProofId;
    private Date dateIssue;
    private String serie;
    private String documentNumber;
    private BigDecimal repayment;
    private Long saleId;

    private SaleSearcher saleSearcher;
    private PaymentProofSearcher paymentProofSearcher;
    private DetailSearcher detailSearcher;

    @Override
    public void delete(Serializable id) {
        StockReturnCustomer s = mainService.getById((Long) id);
        Auditory.make(s, sessionBean.getCurrentUser());
        ((List<Object[]>) stockReturnCustomerDetailService.listHQL(""
                + "SELECT "
                + "srcd.quantity,"
                + "srcd.product.id FROM StockReturnCustomerDetail srcd "
                + "WHERE srcd.stockReturnCustomer = ?", s)).forEach(item -> {
                    try {
                        stockService.updateHQL("UPDATE Stock SET quantity = (quantity - ?) WHERE product = ? AND company = ?", item[0], item[1], s.getSale().getCompany());
                    } catch (Exception ex) {
                        Logger.getLogger(ManagedStockReductionBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
        mainService.delete(s);
    }

    public void send(Serializable id) {
        StockReturnCustomer s = mainService.getById((Long) id);
        Auditory.make(s, sessionBean.getCurrentUser());
        s.setSend(true);
        mainService.saveOrUpdate(s);
    }

    @Override
    public void recovery(Serializable id) {
        StockReturnCustomer s = mainService.getById((Long) id);
        s.setActive(true);
        ((List<Object[]>) stockReturnCustomerDetailService.listHQL(""
                + "SELECT "
                + "srcd.quantity,"
                + "srcd.product.id FROM StockReturnCustomerDetail srcd "
                + "WHERE srcd.stockReturnCustomer = ?", s)).forEach(item -> {
                    try {
                        stockService.updateHQL("UPDATE Stock SET quantity = (quantity + ?) WHERE product = ? AND company = ?", item[0], item[1], sessionBean.getCurrentCompany());
                    } catch (Exception ex) {
                        Logger.getLogger(ManagedStockReductionBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
        mainService.update(s);
    }

    @PostConstruct
    public void init() {
        saleSearcher = new SaleSearcher();
        detailSearcher = new DetailSearcher();
        paymentProofSearcher = new PaymentProofSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        paymentProofSearcher.update();
    }

    @Override
    public boolean save() {
        boolean allow = super.save();
        if (allow) {
            detailSearcher.data.forEach(item -> {
                StockReturnCustomerDetail detail = new StockReturnCustomerDetail();
                detail.setId((Long) item[0]);
                detail.setQuantity((BigDecimal) item[1]);
                detail.setProduct(new Product((Long) item[9]));
                detail.setStockReturnCustomer(selected);
                detail.setProductName((String) item[6]);
                detail.setUom(new UoM((Integer) item[4]));
                detail.setUnitCost((BigDecimal) productService.getByHQL("SELECT p.basicCost FROM Product p WHERE p.id = ?", item[9]));

                if (detail.getId() != null) {
                    BigDecimal quantity = (BigDecimal) stockReturnCustomerDetailService.getByHQL("SELECT srd.quantity FROM StockReductionDetail srd WHERE srd.id = ?", detail.getId());
                    try {
                        stockService.updateHQL("UPDATE Stock SET quantity = (quantity - ?) WHERE product = ? AND company = ?", quantity, detail.getProduct(), sessionBean.getCurrentCompany());
                    } catch (Exception ex) {
                        Logger.getLogger(ManagedSaleBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (detail.getQuantity() == null || detail.getQuantity().doubleValue() == 0) {
                    if (detail.getId() != null) {
                        stockReturnCustomerDetailService.delete(detail);
                    }
                } else {
                    try {
                        stockService.updateHQL("UPDATE Stock SET quantity = (quantity + ?) WHERE product = ? AND company = ?", detail.getQuantity(), detail.getProduct(), sessionBean.getCurrentCompany());
                    } catch (Exception ex) {
                        Logger.getLogger(ManagedSaleBean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    stockReturnCustomerDetailService.saveOrUpdate(detail);
                }
            });
        }
        new PNotifyMessage("Datos guardados!!", "Se ha creado la devolución " + serie + "-" + documentNumber, PNotifyMessage.Type.Success, "fa fa-save shaked animated", 3000L).execute();
        BeanUtil.exceuteJS("window.open('" + BeanUtil.getRequest().getContextPath() + "/service/print_return.xhtml?id=" + selected.getId() + "')");
        return allow;
    }

    @Override
    protected void fillFields() {
        try {
            paymentProofId = selected.getPaymentProof().getId();
        } catch (Exception e) {
            paymentProofId = null;
        }
        repayment = selected.getRepayment();
        dateIssue = selected.getDateIssue();
        serie = selected.getSerie();
        documentNumber = selected.getDocumentNumber();
        if (selected.getId() != null) {
            saleSearcher.documentNumber = (String) mainService.getByHQL("SELECT src.sale.fullDocument FROM StockReturnCustomer src WHERE src.id = ?", selected.getId());
            saleSearcher.search();
        } else {
            saleSearcher.documentNumber = "";
        }
        onLoad(true);
        saleSearcher.search();
        detailSearcher.update();
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        try {
            selected.setPaymentProof(new PaymentProof(paymentProofId));
        } catch (Exception e) {
            selected.setPaymentProof(null);
        }
        selected.setDateIssue(dateIssue);
        selected.setRepayment(repayment);
        selected.setSale(new Sale(saleId));
        if (selected.getId() != null) {
            selected.setSerie(serie);
            selected.setDocumentNumber(documentNumber);
        } else {
            serie = saleSearcher.documentNumber.split("-")[0];
            selected.setSerie(serie);
            documentNumber = String.format("%08d",
                    documentNumberingService.getByHQL("SELECT dn.numbering+1 FROM DocumentNumbering dn WHERE dn.rucCompany  LIKE ? AND dn.serie LIKE ? and dn.paymentProof.id = ?", sessionBean.getCurrentCompany().getRuc(), serie, paymentProofId));
            selected.setDocumentNumber(documentNumber);
        }

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

    //<editor-fold defaultstate="collapsed" desc="DetailSearcher">    
    public class DetailSearcher implements java.io.Serializable {

        public List<Object[]> data;

        public DetailSearcher() {
        }

        public void update() {
            if (getSaleId() != null) {
                data = getSaleDetailService().listHQL(""
                        + "SELECT "
                        + "cast(null as char),"
                        + "cast(null as char),"
                        + "cast(null as char),"
                        + "sd.quantity,"
                        + "sd.uom.id,"
                        + "sd.uom.abbr,"
                        + "sd.productName,"
                        + "sd.unitPrice,"
                        + "sd.subtotal,"
                        + "sd.product.id FROM SaleDetail sd WHERE sd.sale.id = ?", getSaleId());
                List<Object[]> details = Collections.EMPTY_LIST;
                if (selected.getId() != null) {
                    details = getStockReturnCustomerDetailService().listHQL("SELECT "
                            + "srcd.id,"
                            + "srcd.quantity,"
                            + "srcd.product.id FROM StockReturnCustomerDetail srcd WHERE srcd.stockReturnCustomer.id = ?", selected.getId());
                }
                for (int i = 0; i < data.size(); i++) {
                    Object[] item = data.get(i);
                    item[2] = getStockReturnCustomerDetailService().getByHQL("SELECT "
                            + "COALESCE(srcd.quantity,0) "
                            + "FROM StockReturnCustomerDetail srcd WHERE srcd.stockReturnCustomer.sale.id = ? and srcd.product.id = ? and srcd.stockReturnCustomer.id <> ?", getSaleId(), item[9], selected.getId() == null ? -1L : selected.getId());
                    details.forEach(detail -> {
                        if (((Long) item[9]).longValue() == (Long) detail[2]) {
                            item[0] = detail[0];
                            item[1] = detail[1];
                        }
                    });
                };

            }
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
            data = getPaymentProofService().listHQL("SELECT pp.id,pp.abbr,pp.name FROM PaymentProof pp WHERE pp.forReturn = ?", true);
        }

        public List<Object[]> getData() {
            return data;
        }

    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="SaleSearcher">
    public class SaleSearcher implements java.io.Serializable {

        private String documentNumber;
        private Object[] data;

        public void search() {
            documentNumber = documentNumber.trim();
            if (documentNumber.length() != 0) {
                data = (Object[]) getSaleService().getByHQL("SELECT "
                        + "s.id,"
                        + "s.paymentProof.name,"
                        + "s.serie,"
                        + "s.documentNumber,"
                        + "s.dateIssue,"
                        + "s.customerName,"
                        + "s.subtotal,"
                        + "s.igv,"
                        + "s.subtotalDiscount,"
                        + "s.points,"
                        + "s.active FROM Sale s WHERE s.fullDocument LIKE ? and s.company = ?", documentNumber, getSessionBean().getCurrentCompany());
                if (data != null && (Boolean) data[10]) {
                    setSaleId((Long) data[0]);
                    getDetailSearcher().update();
                } else {
                    if (data != null) {
                        new PNotifyMessage("Documento anulado", "La venta " + documentNumber + " ha sido anulada", PNotifyMessage.Type.Info, "fa fa-times shaked animated").execute();
                    } else {
                        new PNotifyMessage("Documento inexistente", "No existe una venta con el documento " + documentNumber, PNotifyMessage.Type.Warning, "fa fa-warning shaked animated").execute();
                    }
                    data = null;
                    setSaleId(null);
                }
            } else {
                data = null;
                setSaleId(null);
            }
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
         * @return the data
         */
        public Object[] getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(Object[] data) {
            this.data = data;
        }

    }
    //</editor-fold>

    /**
     * @return the stockReturnCustomerDetailService
     */
    public IStockReturnCustomerDetailService getStockReturnCustomerDetailService() {
        return stockReturnCustomerDetailService;
    }

    /**
     * @param stockReturnCustomerDetailService the
     * stockReturnCustomerDetailService to set
     */
    public void setStockReturnCustomerDetailService(IStockReturnCustomerDetailService stockReturnCustomerDetailService) {
        this.stockReturnCustomerDetailService = stockReturnCustomerDetailService;
    }

    /**
     * @return the saleService
     */
    public ISaleService getSaleService() {
        return saleService;
    }

    /**
     * @param saleService the saleService to set
     */
    public void setSaleService(ISaleService saleService) {
        this.saleService = saleService;
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
     * @return the saleId
     */
    public Long getSaleId() {
        return saleId;
    }

    /**
     * @param saleId the saleId to set
     */
    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    /**
     * @return the mainService
     */
    public IStockReturnCustomerService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IStockReturnCustomerService mainService) {
        this.mainService = mainService;
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
     * @return the saleSearcher
     */
    public SaleSearcher getSaleSearcher() {
        return saleSearcher;
    }

    /**
     * @param saleSearcher the saleSearcher to set
     */
    public void setSaleSearcher(SaleSearcher saleSearcher) {
        this.saleSearcher = saleSearcher;
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
}
