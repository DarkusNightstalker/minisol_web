/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.electronicBook;

import cs.bms.bean.SessionBean;
import cs.bms.model.ElectronicBook;
import cs.bms.service.interfac.IAccountingRecordDetailService;
import cs.bms.service.interfac.IAccountingRecordService;
import cs.bms.service.interfac.IElectronicBookService;
import gkfire.util.Month;
import gkfire.web.bean.ILoadable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.faces.bean.ManagedProperty;

/**
 *
 * @author Jhoan Brayam
 */
public abstract class ABookBean implements java.io.Serializable, ILoadable {

    @ManagedProperty(value = "#{electronicBookService}")
    protected IElectronicBookService electronicBookService;
    @ManagedProperty(value = "#{accountingRecordService}")
    protected IAccountingRecordService accountingRecordService;
    @ManagedProperty(value = "#{accountingRecordDetailService}")
    protected IAccountingRecordDetailService accountingRecordDetailService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    protected ElectronicBook electronicBook;
    protected Integer year;
    protected List<Integer> years;
 
    protected Integer createYear;
    protected Integer createDay;
    protected Month createMonth;
    protected List<Month> createMonths;
    protected List<Integer> createDays;
    
    protected List<Object[]> data;
    protected Month month;
    protected String code;

    public void refresh() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = Month.byOrdinal(calendar.get(Calendar.MONTH) - 1);
        Integer minYear = (Integer) electronicBookService.getByHQL("SELECT MIN(ar.year) FROM AccountingRecord ar WHERE ar.electronicBook = ?", electronicBook);
        years = new ArrayList();
        if(minYear == null) minYear = year;
        for (int i = minYear; i <= year; i++) {
            years.add(i);
        }
        search();
    }

    public void setEBbyCode(String code) {
        electronicBook = (ElectronicBook) electronicBookService.getByHQL("FROM ElectronicBook eb WHERE eb.code LIKE ?", code);
    }

    public abstract void search();

    public abstract void generate();

    /**
     * @return the electronicBookService
     */
    public IElectronicBookService getElectronicBookService() {
        return electronicBookService;
    }

    /**
     * @param electronicBookService the electronicBookService to set
     */
    public void setElectronicBookService(IElectronicBookService electronicBookService) {
        this.electronicBookService = electronicBookService;
    }

    /**
     * @return the accountingRecordService
     */
    public IAccountingRecordService getAccountingRecordService() {
        return accountingRecordService;
    }

    /**
     * @param accountingRecordService the accountingRecordService to set
     */
    public void setAccountingRecordService(IAccountingRecordService accountingRecordService) {
        this.accountingRecordService = accountingRecordService;
    }

    /**
     * @return the accountingRecordDetailService
     */
    public IAccountingRecordDetailService getAccountingRecordDetailService() {
        return accountingRecordDetailService;
    }

    /**
     * @param accountingRecordDetailService the accountingRecordDetailService to
     * set
     */
    public void setAccountingRecordDetailService(IAccountingRecordDetailService accountingRecordDetailService) {
        this.accountingRecordDetailService = accountingRecordDetailService;
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
     * @return the electronicBook
     */
    public ElectronicBook getElectronicBook() {
        return electronicBook;
    }

    /**
     * @param electronicBook the electronicBook to set
     */
    public void setElectronicBook(ElectronicBook electronicBook) {
        this.electronicBook = electronicBook;
    }

    /**
     * @return the year
     */
    public Integer getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * @return the month
     */
    public Month getMonth() {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(Month month) {
        this.month = month;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the years
     */
    public List<Integer> getYears() {
        return years;
    }

    /**
     * @param years the years to set
     */
    public void setYears(List<Integer> years) {
        this.years = years;
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
     * @return the createYear
     */
    public Integer getCreateYear() {
        return createYear;
    }

    /**
     * @param createYear the createYear to set
     */
    public void setCreateYear(Integer createYear) {
        this.createYear = createYear;
    }

    /**
     * @return the createDay
     */
    public Integer getCreateDay() {
        return createDay;
    }

    /**
     * @param createDay the createDay to set
     */
    public void setCreateDay(Integer createDay) {
        this.createDay = createDay;
    }

    /**
     * @return the createMonth
     */
    public Month getCreateMonth() {
        return createMonth;
    }

    /**
     * @param createMonth the createMonth to set
     */
    public void setCreateMonth(Month createMonth) {
        this.createMonth = createMonth;
    }

    /**
     * @return the createMonths
     */
    public List<Month> getCreateMonths() {
        return createMonths;
    }

    /**
     * @param createMonths the createMonths to set
     */
    public void setCreateMonths(List<Month> createMonths) {
        this.createMonths = createMonths;
    }

    /**
     * @return the createDays
     */
    public List<Integer> getCreateDays() {
        return createDays;
    }

    /**
     * @param createDays the createDays to set
     */
    public void setCreateDays(List<Integer> createDays) {
        this.createDays = createDays;
    }

}
