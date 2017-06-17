package cs.bms.report.util;

import cs.bms.dao.util.JDBCPostGresSQL;
import cs.bms.model.Sale;
import cs.bms.model.User;
import cs.bms.util.NumberToLetterConverter;
import gkfire.web.util.BeanUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;

public class SaleExport {

    public static void printSale(Sale sale, OutputStream os, JRExporter exporter, User creator) throws JRException, SQLException, IOException {
        printTicket(sale, os, exporter, creator);
    }

    private static void printTicket(Sale sale, OutputStream os, JRExporter exporter, User creator) throws JRException, SQLException, IOException {
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        InputStream is = servletContext.getResourceAsStream("/1258488425132154132154214536/Factura_Electronica.jasper");

        Map<String, Object> parameters = new HashMap();
        parameters.put("document_number", sale.getSerie() + "-" + sale.getDocumentNumber());
        parameters.put("date_issue", sale.getDateIssue());
        parameters.put("id_sale", sale.getId());
        parameters.put("type", sale.getPaymentProof().getName());
        parameters.put("customer_name", sale.getCustomerName());
        parameters.put("customer_address", sale.getCustomer() == null ? null : sale.getCustomer().getAddress());
        parameters.put("address", sale.getCompany().getAddress() + " - " + sale.getCompany().getCity() + " \n " + sale.getCompany().getDistrict().getSubregion().getRegion().getName() + " - " + sale.getCompany().getDistrict().getSubregion().getName() + " - " + sale.getCompany().getDistrict().getName());
        parameters.put("ruc", sale.getCompany().getRuc());
        parameters.put("city", sale.getCompany().getCity());
        parameters.put("seller", creator.getUsername());
        parameters.put("identity_document", sale.getCustomer() == null ? null : sale.getCustomer().getIdentityNumber());
        parameters.put("document_type", sale.getCustomer() == null ? null : sale.getCustomer().getIdentityDocument().getAbbr());
        parameters.put("subtotal", sale.getSubtotal());
        parameters.put("igv", sale.getIgv());
        parameters.put("discount", sale.getSubtotalDiscount());
        parameters.put("customer", Boolean.valueOf(sale.getCustomer() != null));
        parameters.put("points", Long.valueOf(sale.getCustomer() == null ? 0L : sale.getCustomer().getPoints().longValue()));
        parameters.put("current_points", sale.getPoints());
        parameters.put("cant", NumberToLetterConverter.convertNumberToLetter(sale.getSubtotal().add(sale.getIgv()).subtract(sale.getSubtotalDiscount()).doubleValue()));

        JasperPrint jasperPrint = JasperFillManager.fillReport(is, parameters, new JDBCPostGresSQL().getConnection());
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, os);
        exporter.setParameter(JRPdfExporterParameter.PDF_JAVASCRIPT, "this.print();");
        exporter.exportReport();
        BeanUtil.getResponse().setContentType("application/pdf");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.responseComplete();
        os.flush();
        os.close();
    }
}
