/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.bean.electronicBook.ABookBean;
import cs.bms.service.interfac.IElectronicBookService;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class AccountingMenuBean implements java.io.Serializable, ILoadable {

    @ManagedProperty(value = "#{electronicBookService}")
    private IElectronicBookService electronicBookService;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    private ElectronicBookSearcher electronicBookSearcher;

    @PostConstruct
    public void init() {
        electronicBookSearcher = new ElectronicBookSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        electronicBookSearcher.update();
    }

    public void charge(String st) {
        FacesContext context = FacesContext.getCurrentInstance();
        ABookBean bookBean = context.getApplication().evaluateExpressionGet(context, "#{bean"+st.replace(".","")+"}", ABookBean.class);
        bookBean.setEBbyCode(st);
        bookBean.onLoad(true);
        sessionBean.setLoadable(bookBean);
    }

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
     * @return the electronicBookSearcher
     */
    public ElectronicBookSearcher getElectronicBookSearcher() {
        return electronicBookSearcher;
    }

    /**
     * @param electronicBookSearcher the electronicBookSearcher to set
     */
    public void setElectronicBookSearcher(ElectronicBookSearcher electronicBookSearcher) {
        this.electronicBookSearcher = electronicBookSearcher;
    }

    public class ElectronicBookSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getElectronicBookService().listHQL("SELECT eb.id,eb.code,eb.html FROM ElectronicBook eb WHERE eb.active = true  ORDER by version(eb.code)");
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
}
