/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.dao.util.PGSqlUtil;
import cs.bms.model.Product;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IStockService;
import cs.bms.util.ReportExport;
import cs.bms.util.ReportExportList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

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

    protected ReportExportList kardexValuate;
    protected ReportExportList kardexPhysical;
    protected ReportExport currentStock;
    protected ReportExport totalStock;

    protected ProductSearcher productSearcher;

    @PostConstruct
    public void init() {
        productSearcher = new ProductSearcher();

        Map<String, Object> map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        map.put("barcode_product", null);
        map.put("ruc", sessionBean.getCurrentCompany().getRuc());
        map.put("name_company", sessionBean.getCurrentCompany().getName());
        map.put("date_init", null);
        map.put("date_end", null);
        kardexValuate = new ReportExportList("/1258488425132154132154214536/kardex_valuate.jasper", "Inventario en unidades valorizadas", sessionBean, map, (Map<String, Object> params) -> {
            try {
                List<JasperPrint> jasperPrints = new ArrayList();
                if (params.get("barcode_product") == null || params.get("barcode_product").toString().equalsIgnoreCase("")) {
                    List<String> barcodes = productService.getAllBarcodes();
                    for (String barcode : barcodes) {
                        params.put("barcode_product", barcode);
                        jasperPrints.add(JasperFillManager.fillReport(kardexValuate.makeInputStream(), params, PGSqlUtil.getJdbcPostGresSQL().getConnection()));
                    }
                } else {
                    jasperPrints.add(JasperFillManager.fillReport(kardexValuate.makeInputStream(), params, PGSqlUtil.getJdbcPostGresSQL().getConnection()));
                }
                return jasperPrints;
            } catch (SQLException | JRException ex) {
                Logger.getLogger(ReportStockBean.class.getName()).log(Level.SEVERE, null, ex);
                return Collections.EMPTY_LIST;
            }
        });
        /**/
        map = new HashMap();
        map.put("id_companies", Collections.EMPTY_LIST);
        map.put("barcode_product", null);
        map.put("ruc", sessionBean.getCurrentCompany().getRuc());
        map.put("name_company", sessionBean.getCurrentCompany().getName());
        map.put("date_init", null);
        map.put("date_end", null);
        kardexPhysical = new ReportExportList("/1258488425132154132154214536/kardex_physical.jasper", "Inventario en unidades fisicas", sessionBean, map, (Map<String,Object> params) -> {
            try {
                List<JasperPrint> jasperPrints = new ArrayList();
                if (params.get("barcode_product") == null || params.get("barcode_product").toString().equalsIgnoreCase("")) {
                    List<String> barcodes = productService.getAllBarcodes();
                    for (String barcode : barcodes) {
                        params.put("barcode_product", barcode);
                        jasperPrints.add(JasperFillManager.fillReport(kardexPhysical.makeInputStream(), params, PGSqlUtil.getJdbcPostGresSQL().getConnection()));
                    }
                } else {
                    jasperPrints.add(JasperFillManager.fillReport(kardexPhysical.makeInputStream(), params, PGSqlUtil.getJdbcPostGresSQL().getConnection()));
                }
                return jasperPrints;
            } catch (SQLException | JRException ex) {
                Logger.getLogger(ReportStockBean.class.getName()).log(Level.SEVERE, null, ex);
                return Collections.EMPTY_LIST;
            }
        });
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
        kardexValuate.getParams().put("barcode_product", null);
        kardexValuate.getParams().put("name_product", null);
        /**/
        kardexPhysical.getParams().put("id_companies", Collections.EMPTY_LIST);
        kardexPhysical.getParams().put("ruc", sessionBean.getCurrentCompany().getRuc());
        kardexPhysical.getParams().put("name_company", sessionBean.getCurrentCompany().getName());
        kardexPhysical.getParams().put("barcode_product", null);
        kardexValuate.getParams().put("name_product", null);
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
     * @return the kardexValuate
     */
    public ReportExportList getKardexValuate() {
        return kardexValuate;
    }

    /**
     * @param kardexValuate the kardexValuate to set
     */
    public void setKardexValuate(ReportExportList kardexValuate) {
        this.kardexValuate = kardexValuate;
    }

    /**
     * @return the kardexPhysical
     */
    public ReportExportList getKardexPhysical() {
        return kardexPhysical;
    }

    /**
     * @param kardexPhysical the kardexPhysical to set
     */
    public void setKardexPhysical(ReportExportList kardexPhysical) {
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

    public class ProductSearcher implements java.io.Serializable {

        protected String terms;
        protected String productName;
        protected Pagination<Object[]> pagination;
        protected OrderFactory orderFactory;

        public ProductSearcher() {
            pagination = new Pagination<>(productService);
            orderFactory = new OrderFactory(new OrderList());
            orderFactory.setDefaultOrder(Order.desc("name"));
        }

        public void refresh() {
            terms = "";
            kardexValuate.getParams().put("name_product", null);
            kardexPhysical.getParams().put("name_product", null);
            search();
        }

        public void select() {
            int index = Integer.parseInt(BeanUtil.getParameter("index"));
            kardexValuate.getParams().put("name_product", pagination.getData().get(index)[2]);
            kardexPhysical.getParams().put("name_product", pagination.getData().get(index)[2]);
            kardexValuate.getParams().put("barcode_product", pagination.getData().get(index)[1]);
            kardexPhysical.getParams().put("barcode_product", pagination.getData().get(index)[1]);
        }

        public void searchByBarcode() {
            String barcode = BeanUtil.getParameter("barcode");
            if (barcode.equalsIgnoreCase("")) {
                kardexValuate.getParams().put("name_product", null);
                kardexPhysical.getParams().put("name_product", null);

            } else {
                Product p = productService.getByBarcode(barcode);
                if (p == null) {
                    kardexValuate.getParams().put("name_product", null);
                    kardexPhysical.getParams().put("name_product", null);
                } else {
                    kardexValuate.getParams().put("name_product", p.getName());
                    kardexPhysical.getParams().put("name_product", p.getName());
                }
            }
        }

        public void search() {
            terms = terms.trim();
            ProjectionList projectionList = Projections.projectionList()
                    .add(Projections.id())
                    .add(Projections.property("barcode"))
                    .add(Projections.property("name"))
                    .add(Projections.property("active"));
            CriterionList criterionList = new CriterionList();
            criterionList.add(Restrictions.eq("active", true));
            if (terms.length() != 0) {
                criterionList.add(
                        Restrictions.or(
                                Restrictions.like("barcode", terms, MatchMode.ANYWHERE),
                                Restrictions.like("name", terms, MatchMode.ANYWHERE)
                        ));
            }
            pagination.search(1, projectionList, criterionList, orderFactory.make());
        }
        //<editor-fold defaultstate="collapsed" desc="Gets & Sets">

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
         * @param pagination the pagination to set
         */
        public void setPagination(Pagination<Object[]> pagination) {
            this.pagination = pagination;
        }

        /**
         * @return the orderFactory
         */
        public OrderFactory getOrderFactory() {
            return orderFactory;
        }

        /**
         * @param orderFactory the orderFactory to set
         */
        public void setOrderFactory(OrderFactory orderFactory) {
            this.orderFactory = orderFactory;
        }

        /**
         * @return the productName
         */
        public String getProductName() {
            return productName;
        }

        /**
         * @param productName the productName to set
         */
        public void setProductName(String productName) {
            this.productName = productName;
        }
//</editor-fold>
    }

    /**
     * @return the productSearcher
     */
    public ProductSearcher getProductSearcher() {
        return productSearcher;
    }

    /**
     * @param productSearcher the productSearcher to set
     */
    public void setProductSearcher(ProductSearcher productSearcher) {
        this.productSearcher = productSearcher;
    }
}
