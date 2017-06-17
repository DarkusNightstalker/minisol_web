/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.ICashRegisterService;
import cs.bms.service.interfac.IWorkShiftService;
import cs.bms.report.util.ReportExport;
import java.util.Collections;
import java.util.HashMap;
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
public class ReportCashRegisterBean implements java.io.Serializable {

    protected ReportExport cashRegisterTotal;
    protected ReportExport cashRegisterByUser;
    protected ReportExport cashRegisterByWorkShift;
    protected WorkShiftSearcher workShiftSearcher;

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{cashRegisterService}")
    protected ICashRegisterService cashRegisterService;
    @ManagedProperty(value = "#{workShiftService}")
    protected IWorkShiftService workShiftService;

    @PostConstruct
    public void init() {
        workShiftSearcher = new WorkShiftSearcher();
        Map<String, Object> map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        map.put("date_init", null);
        map.put("date_end", null);
        cashRegisterTotal = new ReportExport("/1258488425132154132154214536/cash_register_total.jasper", "Arqueos de caja", sessionBean, map);
        //
        cashRegisterByUser = new ReportExport("/1258488425132154132154214536/cash_register_total.jasper", "Arqueos de caja por usuarios", sessionBean, map);
        map = new HashMap();
        map.put("id_work_shift", null);
        map.put("date_arcing", null);
        cashRegisterByWorkShift = new ReportExport("/1258488425132154132154214536/cash_register_work_shift.jasper", "Arqueos de caja por turno", sessionBean, map);
    }

    public void cleanReports() {
        cashRegisterTotal.getParams().put("id_companies", Collections.EMPTY_LIST);
        cashRegisterTotal.getParams().put("date_init", null);
        cashRegisterTotal.getParams().put("date_end", null);

        cashRegisterByWorkShift.getParams().put("id_work_shift", null);
        cashRegisterByWorkShift.getParams().put("date_arcing", null);
        workShiftSearcher.update();
    }

    /**
     * @return the cashRegisterTotal
     */
    public ReportExport getCashRegisterTotal() {
        return cashRegisterTotal;
    }

    /**
     * @param cashRegisterTotal the cashRegisterTotal to set
     */
    public void setCashRegisterTotal(ReportExport cashRegisterTotal) {
        this.cashRegisterTotal = cashRegisterTotal;
    }

    /**
     * @return the cashRegisterByUser
     */
    public ReportExport getCashRegisterByUser() {
        return cashRegisterByUser;
    }

    /**
     * @param cashRegisterByUser the cashRegisterByUser to set
     */
    public void setCashRegisterByUser(ReportExport cashRegisterByUser) {
        this.cashRegisterByUser = cashRegisterByUser;
    }

    /**
     * @return the cashRegisterByWorkShift
     */
    public ReportExport getCashRegisterByWorkShift() {
        return cashRegisterByWorkShift;
    }

    /**
     * @param cashRegisterByWorkShift the cashRegisterByWorkShift to set
     */
    public void setCashRegisterByWorkShift(ReportExport cashRegisterByWorkShift) {
        this.cashRegisterByWorkShift = cashRegisterByWorkShift;
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
     * @return the cashRegisterService
     */
    public ICashRegisterService getCashRegisterService() {
        return cashRegisterService;
    }

    /**
     * @param cashRegisterService the cashRegisterService to set
     */
    public void setCashRegisterService(ICashRegisterService cashRegisterService) {
        this.cashRegisterService = cashRegisterService;
    }

    public class WorkShiftSearcher implements java.io.Serializable {

        protected List<Object[]> data;

        void update() {
            data = workShiftService.getBasicDataByCompany(sessionBean.getCurrentCompany());
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
}
