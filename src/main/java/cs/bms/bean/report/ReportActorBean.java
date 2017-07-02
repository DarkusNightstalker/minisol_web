/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.report;

import cs.bms.bean.SessionBean;
import cs.bms.service.interfac.IActorService;
import cs.bms.report.util.ReportExport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import net.sf.jasperreports.engine.JRExporter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ReportActorBean implements java.io.Serializable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{actorService}")
    private IActorService actorService;

    private ReportExport customerExport;
    private ReportExport supplierExport;
    private ReportExport customerSalesExport;

    @PostConstruct
    public void init() {
        Map<String, Object> map = new HashMap();
        map.put("id_identity_documents", Collections.EMPTY_LIST);
        customerExport = new ReportExport("/1258488425132154132154214536/exp_customer.jasper", "Clientes minisol", sessionBean, map);
        map = new HashMap();
        map.put("id_identity_documents", Collections.EMPTY_LIST);
        supplierExport = new ReportExport("/1258488425132154132154214536/exp_supplier.jasper", "Proveedores Minisol", sessionBean, map);
        map = new HashMap();
        map.put("customer", null);
        map.put("id_customer", null);
        map.put("name_customer", null);
        map.put("document_customer", null);
        map.put("points_customer", null);
        map.put("date_init", null);
        map.put("date_end", null);
        customerSalesExport = new ReportExport("/1258488425132154132154214536/customer_points.jasper", "Registro de ventas de cliente", sessionBean, map) {
            @Override
            protected String execute(JRExporter exporter) {
                Object[] customer = (Object[]) params.get("customer");
                params.put("id_customer", customer[0]);
                params.put("name_customer", customer[3]);
                params.put("document_customer", customer[1]+" "+customer[2]);
                params.put("points_customer", customer[4]);
                return super.execute(exporter);
            }
        };

    }

    public void clearReports() {
        customerExport.getParams().put("id_identity_documents", Collections.EMPTY_LIST);
        supplierExport.getParams().put("id_identity_documents", Collections.EMPTY_LIST);
        customerSalesExport.getParams().put("date_init", null);
        customerSalesExport.getParams().put("date_end", null);
        customerSalesExport.getParams().put("customer", null);
        customerSalesExport.getParams().put("id_customer", null);
        customerSalesExport.getParams().put("name_customer", null);
        customerSalesExport.getParams().put("document_customer", null);
        customerSalesExport.getParams().put("points_customer", null);
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
     * @return the customerExport
     */
    public ReportExport getCustomerExport() {
        return customerExport;
    }

    /**
     * @param customerExport the customerExport to set
     */
    public void setCustomerExport(ReportExport customerExport) {
        this.customerExport = customerExport;
    }

    /**
     * @return the supplierExport
     */
    public ReportExport getSupplierExport() {
        return supplierExport;
    }

    /**
     * @param supplierExport the supplierExport to set
     */
    public void setSupplierExport(ReportExport supplierExport) {
        this.supplierExport = supplierExport;
    }

    /**
     * @return the customerSalesExport
     */
    public ReportExport getCustomerSalesExport() {
        return customerSalesExport;
    }

    /**
     * @param customerSalesExport the customerSalesExport to set
     */
    public void setCustomerSalesExport(ReportExport customerSalesExport) {
        this.customerSalesExport = customerSalesExport;
    }

    public List<Object[]> searchCustomer(String query) {
        return actorService.forAutocomplete(6, query);
    }

    @FacesConverter("dn.bms.minisol.converter.ArrayActorConverter")
    public class CustomerConverter implements Converter {

        @SuppressWarnings("unchecked")
        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {

            if (value == null || value.length() < 1) {
                return Collections.EMPTY_LIST;
            }
            String[] strSplit = value.split("::");
            List<Long> result = new ArrayList();
            for (String str : strSplit) {
                result.add(Long.parseLong(str));
            }
            return result;
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
            if (value == null) {
                return "";
            }
            List<Long> entry = (List<Long>) value;
            return StringUtils.join(entry, "::");

        }
    }

}
