/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.OpeningBalance;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IOpeningBalanceService;
import gkfire.util.Month;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.util.Arrays;
import java.util.Calendar;
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
public class ManagedOpeningBalance extends AManagedBean<OpeningBalance, IOpeningBalanceService> implements ILoadable {

    @ManagedProperty(value = "#{openingBalanceService}")
    protected IOpeningBalanceService mainService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;

    protected CompanySearcher companySearcher;
    protected Integer year;
    protected Month month;
    protected List<Month> months;

    @PostConstruct
    public void init() {
        companySearcher = new CompanySearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
    }

    @Override
    public boolean save() {
        saved = false;
        if (companySearcher.data.isEmpty()) {
            PNotifyMessage.errorMessage("");
            saved = false;
        } else {
            try {
                companySearcher.data.forEach(item -> {
                    if (companySearcher.selected.get(item[0])) {
                        mainService.generate(year, month, (Integer) item[0],sessionBean.getCurrentUser());
                    }
                });
                saved = true;
            } catch (Exception e) {
                PNotifyMessage.systemError(e, sessionBean);
                saved = false;
            }
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        months = Arrays.asList(Month.values());
        year = Calendar.getInstance().get(Calendar.YEAR);
        month = null;
        companySearcher.update();
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {

    }
    
    //<editor-fold defaultstate="collapsed" desc="CompanySearcher">
    public class CompanySearcher implements java.io.Serializable {

        protected List<Object[]> data;
        protected Map<Integer, Boolean> selected;

        public void update() {
            data = companyService.getBasicDataNotOpening(year, month);
            selected = new HashMap();
            data.forEach(item -> {
                selected.put((Integer) item[0], Boolean.FALSE);
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

        /**
         * @return the selected
         */
        public Map<Integer, Boolean> getSelected() {
            return selected;
        }

        /**
         * @param selected the selected to set
         */
        public void setSelected(Map<Integer, Boolean> selected) {
            this.selected = selected;
        }

    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">
    
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
     * @return the months
     */
    public List<Month> getMonths() {
        return months;
    }

    /**
     * @param months the months to set
     */
    public void setMonths(List<Month> months) {
        this.months = months;
    }
    /**
     * @return the mainService
     */
    public IOpeningBalanceService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IOpeningBalanceService mainService) {
        this.mainService = mainService;
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
//</editor-fold>

}
