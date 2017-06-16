/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.enumerated.CashType;
import cs.bms.model.CashRegister;
import cs.bms.model.CashRegisterDetail;
import cs.bms.service.interfac.ICashRegisterDetailService;
import cs.bms.service.interfac.ICashRegisterService;
import cs.bms.service.interfac.IPaymentVoucherService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.ISalePaymentService;
import cs.bms.service.interfac.ISaleService;
import cs.bms.service.interfac.IWorkShiftService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
public class ManagedCashRegisterBean extends AManagedBean<CashRegister, ICashRegisterService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{cashRegisterService}")
    protected ICashRegisterService mainService;
    @ManagedProperty(value = "#{cashRegisterDetailService}")
    protected ICashRegisterDetailService cashRegisterDetailService;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    @ManagedProperty(value = "#{workShiftService}")
    protected IWorkShiftService workShiftService;
    @ManagedProperty(value = "#{salePaymentService}")
    protected ISalePaymentService salePaymentService;
    @ManagedProperty(value = "#{saleService}")
    protected ISaleService saleService;
    @ManagedProperty(value = "#{purchasePaymentService}")
    protected IPurchasePaymentService purchasePaymentService;
    @ManagedProperty(value = "#{paymentVoucherService}")
    protected IPaymentVoucherService paymentVoucherService;

    protected WorkShiftSearcher workShiftSearcher;
    protected DetailSearcher detailSearcher;

    protected Integer workShiftId;
    private Integer paymentVoucherTotal;
    protected Date dateArcing;
    protected BigDecimal initialCash;
    protected BigDecimal expectedCash;
    protected BigDecimal realCash;
    protected BigDecimal outs;
    protected BigDecimal credit;
    protected BigDecimal visa;

    @PostConstruct
    public void init() {
        workShiftSearcher = new WorkShiftSearcher();
        detailSearcher = new DetailSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        workShiftSearcher.update();
    }

    @Override
    public void delete(Serializable id) {
        CashRegister cr = mainService.getById((Long)id);
        cashRegisterDetailService.deleteBy(cr);
        mainService.delete(cr);
    }
    

    @Override
    public boolean save() {
        boolean valid = mainService.getByHQL("SELECT 1 FROM CashRegister cr WHERE cr.workShift.id = ? AND cr.dateArcing = ? AND cr.id <> ?", workShiftId, dateArcing, selected.getId() == null ? -1L : selected.getId()) == null;
        Long lastId = selected.getId();

        if (!valid) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            PNotifyMessage.errorMessage("Este turno ya posee su cierre de caja para la fecha " + sdf.format(dateArcing));
            saved = false;
        } else {
            if (!saleService.getNotVerifySales(sessionBean.getCurrentCompany(), Collections.EMPTY_LIST).isEmpty()) {
                PNotifyMessage.errorMessage("Hay ventas sin verificar");
                saved = false;
                return saved;
            }
            try {
                String content = getSelected().getId() != null ? "Se ha actualizado un cierre de caja" : "Se ha realizado un cierre de caja";
                saved = super.save();
                List<CashType> cashTypes = new ArrayList(detailSearcher.data.keySet());
                cashTypes.forEach((type) -> {
                    Integer quantity = detailSearcher.data.get(type);
                    CashRegisterDetail crd = new CashRegisterDetail();
                    crd.setQuantity(quantity);
                    crd.setCashRegister(selected);
                    crd.setCashType(type);
                    if (lastId != null && (crd.getQuantity() == null || crd.getQuantity() == 0)) {
                        cashRegisterDetailService.delete(crd);
                    }
                    if (quantity != null && quantity != 0) {
                        cashRegisterDetailService.saveOrUpdate(crd);
                    }
                });
                PNotifyMessage.saveMessage(content);
                saved = true;
            } catch (Exception e) {
                PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(e));
                saved = false;
            }
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        try {
            workShiftId = selected.getWorkShift().getId();
        } catch (Exception e) {
            workShiftId = null;
        }
        dateArcing = selected.getDateArcing();
        if (dateArcing == null) {
            dateArcing = new Date();
        }
        outs = selected.getOuts();
        visa = selected.getVisa();
        initialCash = selected.getInitialCash();
        expectedCash = selected.getExpectedCash();
        credit = selected.getCredit();
        realCash = selected.getRealCash();
        detailSearcher.update();
        workShiftSearcher.update();
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        try {
            selected.setWorkShift(workShiftService.getById(workShiftId));
        } catch (Exception e) {
            selected.setWorkShift(null);
        }
        selected.setDateArcing(dateArcing);
        selected.setInitialCash(BigDecimal.ZERO);

        if (selected.getId() == null) {
            Date dateResult = mainService.getLastDate(sessionBean.getCurrentCompany());

            BigDecimal ins = salePaymentService.getCashAfterByCompany(dateResult, sessionBean.getCurrentCompany());
            credit = salePaymentService.getCreditAfterByCompany(dateResult, sessionBean.getCurrentCompany());
            outs = purchasePaymentService.getSumAfterByCompany(dateResult, sessionBean.getCurrentCompany());
            paymentVoucherTotal = paymentVoucherService.getTotalSumAfterByCompany(dateResult, sessionBean.getCurrentCompany());
            expectedCash = ins.add(credit);
            realCash = realCash.add(visa).add(new BigDecimal(paymentVoucherTotal));
        }
        selected.setVisa(visa);
        selected.setOuts(outs);
        selected.setExpectedCash(expectedCash);
        selected.setCredit(credit);
        selected.setPaymentVoucherTotal(paymentVoucherTotal);
        selected.setRealCash(realCash);
        selected.setCompany(sessionBean.getCurrentCompany());

        Auditory.make(selected, sessionBean.getCurrentUser());
    }

    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">
    /**
     * @return the mainService
     */
    @Override
    public ICashRegisterService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    @Override
    public void setMainService(ICashRegisterService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the cashRegisterDetailService
     */
    public ICashRegisterDetailService getCashRegisterDetailService() {
        return cashRegisterDetailService;
    }

    /**
     * @param cashRegisterDetailService the cashRegisterDetailService to set
     */
    public void setCashRegisterDetailService(ICashRegisterDetailService cashRegisterDetailService) {
        this.cashRegisterDetailService = cashRegisterDetailService;
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
     * @return the workShiftId
     */
    public Integer getWorkShiftId() {
        return workShiftId;
    }

    /**
     * @param workShiftId the workShiftId to set
     */
    public void setWorkShiftId(Integer workShiftId) {
        this.workShiftId = workShiftId;
    }

    /**
     * @return the dateArcing
     */
    public Date getDateArcing() {
        return dateArcing;
    }

    /**
     * @param dateArcing the dateArcing to set
     */
    public void setDateArcing(Date dateArcing) {
        this.dateArcing = dateArcing;
    }

    /**
     * @return the initialCash
     */
    public BigDecimal getInitialCash() {
        return initialCash;
    }

    /**
     * @param initialCash the initialCash to set
     */
    public void setInitialCash(BigDecimal initialCash) {
        this.initialCash = initialCash;
    }

    /**
     * @return the expectedCash
     */
    public BigDecimal getExpectedCash() {
        return expectedCash;
    }

    /**
     * @param expectedCash the expectedCash to set
     */
    public void setExpectedCash(BigDecimal expectedCash) {
        this.expectedCash = expectedCash;
    }

    /**
     * @return the realCash
     */
    public BigDecimal getRealCash() {
        return realCash;
    }

    /**
     * @param realCash the realCash to set
     */
    public void setRealCash(BigDecimal realCash) {
        this.realCash = realCash;
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
     * @return the credit
     */
    public BigDecimal getCredit() {
        return credit;
    }

    /**
     * @param credit the credit to set
     */
    public void setCredit(BigDecimal credit) {
        this.credit = credit;
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
     * @return the visa
     */
    public BigDecimal getVisa() {
        return visa;
    }

    /**
     * @param visa the visa to set
     */
    public void setVisa(BigDecimal visa) {
        this.visa = visa;
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

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="WorkShiftSearcher">
    public class WorkShiftSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = workShiftService.getFreeBasicDataByCompany(new Date(), sessionBean.getCurrentCompany());
        }

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
    //<editor-fold defaultstate="collapsed" desc="DetailSearcher">

    public class DetailSearcher implements java.io.Serializable {

        protected Map<CashType, Integer> data;
        protected CashType[] types;

        public void update() {
            types = CashType.values();
            data = (Map<CashType, Integer>) cashRegisterDetailService.getQuantitiesAsMap(selected.getId())[0];
        }

        /**
         * @return the data
         */
        public Map<CashType, Integer> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(Map<CashType, Integer> data) {
            this.data = data;
        }

        /**
         * @return the types
         */
        public CashType[] getTypes() {
            return types;
        }

        /**
         * @param types the types to set
         */
        public void setTypes(CashType[] types) {
            this.types = types;
        }

    }
    //</editor-fold>
}
