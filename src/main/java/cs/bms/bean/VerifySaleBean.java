/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.bean.managed.ManagedSaleBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Product;
import cs.bms.model.Sale;
import cs.bms.model.SalePayment;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IPaymentVoucherService;
import cs.bms.service.interfac.ISaleDetailService;
import cs.bms.service.interfac.ISalePaymentService;
import cs.bms.service.interfac.ISaleService;
import cs.bms.service.interfac.IStockService;
import cs.bms.util.DownloadUtil;
import gkfire.auditory.Auditory;
import gkfire.web.util.BeanUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class VerifySaleBean implements java.io.Serializable {

    @ManagedProperty(value = "#{saleService}")
    protected ISaleService saleService;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{stockService}")
    protected IStockService stockService;
    @ManagedProperty(value = "#{paymentVoucherService}")
    protected IPaymentVoucherService paymentVoucherService;
    @ManagedProperty(value = "#{saleDetailService}")
    protected ISaleDetailService saleDetailService;
    @ManagedProperty(value = "#{salePaymentService}")
    protected ISalePaymentService salePaymentService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    protected Info info;
    protected List<Object[]> data;
    protected List<Object[]> newData;
    protected List<Long> ids;

    @PostConstruct
    public void init() {
        info = new Info();
        ids = new ArrayList<>();
    }

    public void update() {
        ids = new ArrayList<>();
//        data = saleService.listHQL("SELECT "
//                + "s.id,"
//                + "s.dateIssue,"
//                + "s.paymentProof.abbr||' '||s.fullDocument,"
//                + "s.customerName,"
//                + "s.subtotal+s.igv-s.subtotalDiscount-s.igvDiscount FROM Sale s WHERE s.company = ? and s.verified = false ORDER BY id", sessionBean.getCurrentCompany());
//        ids = new ArrayList(saleService.listHQL("SELECT "
//                + "s.id  FROM Sale s WHERE s.company = ? and s.verified = false ORDER BY id", sessionBean.getCurrentCompany()));
    }

    @Asynchronous
    public void asyncUpdate() {
        newData = saleService.getNotVerifySales(sessionBean.getCurrentCompany(), ids);
    }

    public List<Object[]> getNewData() {
        return newData;
    }

    public void delete() {
        Long id = Long.parseLong(BeanUtil.getParameter("id"));
        Sale sale = saleService.getById( id);
        if (sale.getCustomer() != null) {
            Long currentPoints = sale.getPoints() - new Double(sale.getSubtotalDiscount().doubleValue() * 100).longValue();
            if (currentPoints > 0) {
                actorService.subtractPoints(sale.getCustomer().getId(), sale.getPoints(),sessionBean.getCurrentUser());
            }
        }
        saleDetailService.getProductDataBySale(sale).forEach(item -> {
            stockService.addQuantity((BigDecimal) item[1], new Product((Long) item[0]), sale.getCompany(), sessionBean.getCurrentUser());
        });

        Auditory.make(sale, sessionBean.getCurrentUser());
        sale.setSent(false);
        saleService.delete(sale);
        BeanUtil.exceuteJS("SaleVerification.after_delete(" + id + ");");
    }

    public String serializeNewData() {
        if (newData == null) {
            return null;
        }
        String markup = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        for (Object[] item : newData) {
            markup += "<div>";
            markup += "<span>" + item[0] + "</span>";
            markup += "<span>" + sdf.format(item[1]) + "</span>";
            markup += "<span>" + item[2] + "</span>";
            markup += "<span>" + (item[3] == null ? "" : item[3].toString().split(" ")[0]) + "</span>";
            markup += "<span>" + item[4] + "</span>";
            markup += "</div>";
        }
        newData = null;
        return markup;
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
     * @return the ids
     */
    public List<Long> getIds() {
        return ids;
    }

    /**
     * @param ids the ids to set
     */
    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public class Info implements java.io.Serializable {

        protected Object[] data;
        protected List<String> vourcherCodes;
        private Integer vouchersTotal;
        protected boolean vouchersVerified;
        protected List<Object[]> detail;
        protected boolean contado;
        protected boolean visa;
        protected boolean vouchers;

        public Info() {
            vourcherCodes = Arrays.asList(null,null,null);
         
        }

        
        
        public void load(boolean contado, boolean visa, boolean vouchers) {
            this.contado = contado;
            this.visa = visa;
            this.vouchers = vouchers;

            vourcherCodes.set(0, null);
            vourcherCodes.set(1, null);
            vourcherCodes.set(2, null);
            Long id = Long.parseLong(BeanUtil.getParameter("id"));
            data = (Object[]) saleService.getByHQL("SELECT "
                    + "s.id,"
                    + "s.paymentProof.abbr||' '||s.fullDocument,"
                    + "s.dateIssue,"
                    + "s.customerName,"
                    + "s.subtotal,"
                    + "s.igv,"
                    + "s.subtotalDiscount,"
                    + "s.points FROM Sale s WHERE s.id = ?", id);
            detail = saleDetailService.getBasicDataBySale(new Sale(id));
            vouchersVerified = false;
            vouchersTotal = 0;
        }

        public void verifyVouchers() {
            vouchersTotal = 0;
            BigDecimal total = ((BigDecimal) data[4]).add((BigDecimal) data[5]).subtract((BigDecimal) data[6]);
            for (String code : vourcherCodes) {
                if (code == null || code.equalsIgnoreCase("")) {
                    continue;
                }
                if (paymentVoucherService.verifyNumeration(code)) {
                    vouchersVerified = false;
                    PNotifyMessage.errorMessage("Vale de consumo \"" + code + "\" : ya ha sido usado");
                    return;
                } else if (!paymentVoucherService.existNumeration(code)) {
                    vouchersVerified = false;
                    PNotifyMessage.errorMessage("Vale de consumo \"" + code + "\" : no existe");
                    return;
                }
                vouchersTotal += paymentVoucherService.getValueByCode(code);
            }
            if(vouchersTotal == 0){
                vouchersVerified = false;
                PNotifyMessage.errorMessage("No se ha ingresado ningun vale de consumo");
                return;
            }
            if (vouchersTotal > total.doubleValue()) {
                vouchersVerified = false;
                PNotifyMessage.errorMessage("El valor de los vales de consumo sobrepasa la venta");
            } else {
                vouchersVerified = true;
            }
        }

        public void print() {
            BigDecimal total = ((BigDecimal) data[4]).add((BigDecimal) data[5]);
            if (contado) {
                if (vouchers) {
                    BigDecimal discount = new BigDecimal(((BigDecimal) data[6]).doubleValue() + vouchersTotal).setScale(2, RoundingMode.HALF_UP);
                    Long points = (Long) data[7];
                    total = total.subtract(discount);
                    if (points > total.doubleValue()) {
                        points = total.longValue();
                    }
                    saleService.updatePoints(points, (Long) data[0]);
                    saleService.updateDiscount(discount, (Long) data[0]);
                    for (String code : vourcherCodes) {
                        if (code == null || code.equalsIgnoreCase("")) {
                            continue;
                        }
                        paymentVoucherService.useVoucher(code, (Long) data[0]);
                    }
                } else {
                    total = total.subtract((BigDecimal) data[6]);
                }
                SalePayment salePayment = new SalePayment(
                        new Sale((Long) data[0]),
                        new Date(),
                        total
                );
                salePaymentService.saveOrUpdate(salePayment);
            }
            try {
                saleService.verified((Long) data[0], visa);
                DownloadUtil.downloadJSONSale((Long) data[0], sessionBean);
            } catch (Exception ex) {
                PNotifyMessage.systemError(ex, sessionBean);
            }
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

        /**
         * @return the detail
         */
        public List<Object[]> getDetail() {
            return detail;
        }

        /**
         * @param detail the detail to set
         */
        public void setDetail(List<Object[]> detail) {
            this.detail = detail;
        }

        /**
         * @return the contado
         */
        public boolean isContado() {
            return contado;
        }

        /**
         * @param contado the contado to set
         */
        public void setContado(boolean contado) {
            this.contado = contado;
        }

        /**
         * @return the visa
         */
        public boolean isVisa() {
            return visa;
        }

        /**
         * @param visa the visa to set
         */
        public void setVisa(boolean visa) {
            this.visa = visa;
        }

        /**
         * @return the vouchers
         */
        public boolean isVouchers() {
            return vouchers;
        }

        /**
         * @param vouchers the vouchers to set
         */
        public void setVouchers(boolean vouchers) {
            this.vouchers = vouchers;
        }

        /**
         * @return the vourcherCodes
         */
        public List<String> getVourcherCodes() {
            return vourcherCodes;
        }

        /**
         * @param vourcherCodes the vourcherCodes to set
         */
        public void setVourcherCodes(List<String> vourcherCodes) {
            this.vourcherCodes = vourcherCodes;
        }

        /**
         * @return the vouchersVerified
         */
        public boolean isVouchersVerified() {
            return vouchersVerified;
        }

        /**
         * @param vouchersVerified the vouchersVerified to set
         */
        public void setVouchersVerified(boolean vouchersVerified) {
            this.vouchersVerified = vouchersVerified;
        }
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
     * @param newData the newData to set
     */
    public void setNewData(List<Object[]> newData) {
        this.newData = newData;
    }

    /**
     * @return the info
     */
    public Info getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * @return the salePaymentService
     */
    public ISalePaymentService getSalePaymentService() {
        return salePaymentService;
    }

    /**
     * @param salePaymentService the salePaymentService to set
     */
    public void setSalePaymentService(ISalePaymentService salePaymentService) {
        this.salePaymentService = salePaymentService;
    }

    /**
     * @return the paymentVoucherService
     */
    public IPaymentVoucherService getPaymentVoucherService() {
        return paymentVoucherService;
    }

    /**
     * @param paymentVoucherService the paymentVoucherService to set
     */
    public void setPaymentVoucherService(IPaymentVoucherService paymentVoucherService) {
        this.paymentVoucherService = paymentVoucherService;
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

}
