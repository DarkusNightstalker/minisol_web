/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.EntityMapBean;
import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IStockService;
import cs.bms.report.util.ReportExport;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import net.sf.jasperreports.engine.JRExporter;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ReportStockBean implements java.io.Serializable {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{stockService}")
    protected IStockService stockService;
    @ManagedProperty(value = "#{productService}")
    protected IProductService productService;

    protected ReportExport kardexValuateTotal;
    protected ReportExport kardexPhysicalTotal;
    protected ReportExport kardexValuate;
    protected ReportExport kardexPhysical;
    protected ReportExport currentStock;
    protected ReportExport totalStock;

    @PostConstruct
    public void init() {

        Map<String, Object> map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        map.put("id_product", null);
        map.put("product", null);
        map.put("ruc", sessionBean.getCurrentCompany().getRuc());
        map.put("name_company", sessionBean.getCurrentCompany().getName());
        map.put("date_init", null);
        map.put("date_end", null);
        kardexValuate = new ReportExport("/1258488425132154132154214536/kardex_valuate.jasper", "Inventario en unidades valorizadas", sessionBean, map) {

            @Override
            protected String execute(JRExporter exporter) {
                Object[] product = (Object[]) params.get("product");
                params.put("id_product", product[0]);

                return super.execute(exporter); //To change body of generated methods, choose Tools | Templates.
            }

        };
        /**/
        map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        map.put("ruc", sessionBean.getCurrentCompany().getRuc());
        map.put("name_company", sessionBean.getCurrentCompany().getName());
        map.put("date_init", null);
        map.put("date_end", null);
        kardexValuateTotal = new ReportExport("/1258488425132154132154214536/kardex_valuate_total.jasper", "Inventario en unidades valorizadas totales", sessionBean, map);
        /**/
        map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        map.put("id_product", null);
        map.put("product", null);
        map.put("ruc", sessionBean.getCurrentCompany().getRuc());
        map.put("name_company", sessionBean.getCurrentCompany().getName());
        map.put("date_init", null);
        map.put("date_end", null);
        kardexPhysical = new ReportExport("/1258488425132154132154214536/kardex_physical.jasper", "Inventario en unidades fisicas", sessionBean, map) {

            @Override
            protected String execute(JRExporter exporter) {
                Object[] product = (Object[]) params.get("product");
                params.put("id_product", product[0]);

                return super.execute(exporter); //To change body of generated methods, choose Tools | Templates.
            }

        };
        /**/
        map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        map.put("ruc", sessionBean.getCurrentCompany().getRuc());
        map.put("name_company", sessionBean.getCurrentCompany().getName());
        map.put("date_init", null);
        map.put("date_end", null);
        kardexPhysicalTotal = new ReportExport("/1258488425132154132154214536/kardex_physical_total.jasper", "Inventario en unidades valorizadas totales", sessionBean, map);
        /**/
        map = new HashMap();
        map.put("company", sessionBean.getCurrentCompany().getName() + " " + sessionBean.getCurrentCompany().getCity() + " (" + sessionBean.getCurrentCompany().getAddress() + ")");
        map.put("id_company", sessionBean.getCurrentCompany().getId());
        currentStock = new ReportExport("/1258488425132154132154214536/stock_current.jasper", "Existencia actual", sessionBean, map);
        /**/
        map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        totalStock = new ReportExport("/1258488425132154132154214536/stock_total.jasper", "Existencias totales", sessionBean, map);
    }

    public void clearReports() {
        /**/
        currentStock.getParams().put("id_company", sessionBean.getCurrentCompany().getId());
        currentStock.getParams().put("company", sessionBean.getCurrentCompany().getName() + " " + sessionBean.getCurrentCompany().getCity() + " (" + sessionBean.getCurrentCompany().getAddress() + ")");
        /**/
        totalStock.getParams().put("id_companies", Collections.EMPTY_LIST);
        /**/
        kardexValuate.getParams().put("id_companies", Collections.EMPTY_LIST);
        kardexValuate.getParams().put("ruc", sessionBean.getCurrentCompany().getRuc());
        kardexValuate.getParams().put("name_company", sessionBean.getCurrentCompany().getName());
        kardexValuate.getParams().put("id_product", null);
        kardexValuate.getParams().put("product", null);
        /**/
        kardexPhysical.getParams().put("id_companies", Collections.EMPTY_LIST);
        kardexPhysical.getParams().put("ruc", sessionBean.getCurrentCompany().getRuc());
        kardexPhysical.getParams().put("name_company", sessionBean.getCurrentCompany().getName());
        kardexPhysical.getParams().put("id_product", null);
        kardexPhysical.getParams().put("product", null);
        /**/
        kardexValuateTotal.getParams().put("id_companies", Collections.EMPTY_LIST);
        kardexValuateTotal.getParams().put("ruc", sessionBean.getCurrentCompany().getRuc());
        kardexValuateTotal.getParams().put("name_company", sessionBean.getCurrentCompany().getName());
        /**/
        kardexPhysicalTotal.getParams().put("id_companies", Collections.EMPTY_LIST);
        kardexPhysicalTotal.getParams().put("ruc", sessionBean.getCurrentCompany().getRuc());
        kardexPhysicalTotal.getParams().put("name_company", sessionBean.getCurrentCompany().getName());
    }

    public List<Object[]> searchProduct(String query) {
        return productService.forAutocomplete(6, query);
    }

    public void refreshValuate(Long idProduct) {
        kardexValuate.getParams().put("id_product", idProduct);
        kardexValuate.getParams().put("date_init", null);
        kardexValuate.getParams().put("date_end", null);
    }

    public void refreshPhysical(Long idProduct) {
        kardexValuate.getParams().put("id_product", idProduct);
        kardexValuate.getParams().put("date_init", null);
        kardexValuate.getParams().put("date_end", null);
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
     * @param kardexPhysical the kardexPhysical to set
     */
    public void setKardexPhysical(ReportExport kardexPhysical) {
        this.kardexPhysical = kardexPhysical;
    }

    /**
     * @return the currentStock
     */
    public ReportExport getCurrentStock() {
        return currentStock;
    }

    /**
     * @param currentStock the currentStock to set
     */
    public void setCurrentStock(ReportExport currentStock) {
        this.currentStock = currentStock;
    }

    /**
     * @return the totalStock
     */
    public ReportExport getTotalStock() {
        return totalStock;
    }

    /**
     * @param totalStock the totalStock to set
     */
    public void setTotalStock(ReportExport totalStock) {
        this.totalStock = totalStock;
    }

    /**
     * @return the kardexValuateTotal
     */
    public ReportExport getKardexValuateTotal() {
        return kardexValuateTotal;
    }

    /**
     * @param kardexValuateTotal the kardexValuateTotal to set
     */
    public void setKardexValuateTotal(ReportExport kardexValuateTotal) {
        this.kardexValuateTotal = kardexValuateTotal;
    }

    /**
     * @return the kardexPhysicalTotal
     */
    public ReportExport getKardexPhysicalTotal() {
        return kardexPhysicalTotal;
    }

    /**
     * @param kardexPhysicalTotal the kardexPhysicalTotal to set
     */
    public void setKardexPhysicalTotal(ReportExport kardexPhysicalTotal) {
        this.kardexPhysicalTotal = kardexPhysicalTotal;
    }

    /**
     * @return the kardexValuate
     */
    public ReportExport getKardexValuate() {
        return kardexValuate;
    }

    /**
     * @param kardexValuate the kardexValuate to set
     */
    public void setKardexValuate(ReportExport kardexValuate) {
        this.kardexValuate = kardexValuate;
    }

    /**
     * @return the kardexPhysical
     */
    public ReportExport getKardexPhysical() {
        return kardexPhysical;
    }

}
