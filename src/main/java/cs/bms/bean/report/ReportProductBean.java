/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IProductService;
import cs.bms.report.util.ReportExport;
import java.util.HashMap;
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
public class ReportProductBean implements java.io.Serializable {
    
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{productService}")
    protected IProductService productService;
    
    protected ReportExport productExport;
    
    @PostConstruct
    public void init() {    
        Map<String, Object> map = new HashMap();
        map.put("id_company", sessionBean.getCurrentCompany().getId());
        map.put("company", sessionBean.getCurrentCompany().getName() + " " + sessionBean.getCurrentCompany().getCity() + " (" + sessionBean.getCurrentCompany().getAddress() + ")");
        productExport = new ReportExport("/1258488425132154132154214536/exp_product.jasper", "Productos minisol", sessionBean, map);
    }
    
    public void clearReports() {
        productExport.getParams().put("id_company", sessionBean.getCurrentCompany().getId());
        productExport.getParams().put("company", sessionBean.getCurrentCompany().getName() + " " + sessionBean.getCurrentCompany().getCity() + " (" + sessionBean.getCurrentCompany().getAddress() + ")");
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
     * @return the productExport
     */
    public ReportExport getProductExport() {
        return productExport;
    }

    /**
     * @param productExport the productExport to set
     */
    public void setProductExport(ReportExport productExport) {
        this.productExport = productExport;
    }
    
    
}
