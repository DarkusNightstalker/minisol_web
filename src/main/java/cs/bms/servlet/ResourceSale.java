/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.servlet;

import cs.bms.dao.util.JDBCPostGresSQL;
import cs.bms.model.Sale;
import cs.bms.model.User;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IDistrictService;
import cs.bms.service.interfac.ISaleDetailService;
import cs.bms.service.interfac.ISaleService;
import cs.bms.service.interfac.IUserService;
import cs.bms.util.AESKeys;
import cs.bms.util.NumberToLetterConverter;
import gkfire.util.AES;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Jhoan Brayam
 */
@Path("sale")
@javax.enterprise.context.RequestScoped
public class ResourceSale {

    @Autowired
    @Qualifier("actorService")
    protected IActorService actorService;
    @Autowired
    @Qualifier("saleService")
    private ISaleService saleService;
    @Autowired
    @Qualifier("userService")
    private IUserService userService;
    @Autowired
    @Qualifier("saleDetailService")
    private ISaleDetailService saleDetailService;

    @javax.ws.rs.core.Context
    private ServletContext servletContext;

    @GET
    @Path("print.pdf")
    @Produces("application/pdf")
    public Response print(@QueryParam("id") String id) {
        Sale sale = saleService.getById(Long.parseLong(id));
        if (sale == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            User creator = (User) userService.getByHQL("SELECT s.createUser FROM Sale s WHERE s.id=?", Long.parseLong(id));
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
            parameters.put("ubigeo", sale.getCompany().getDistrict().getSubregion().getRegion().getName()+"-"+sale.getCompany().getDistrict().getSubregion().getName()+"-"+sale.getCompany().getDistrict().getName());
            parameters.put("seller", creator.getEmployee().getName());
            parameters.put("identity_document", sale.getCustomer() == null ? null : sale.getCustomer().getIdentityNumber());
            parameters.put("document_type", sale.getCustomer() == null ? null : sale.getCustomer().getIdentityDocument().getAbbr());
            parameters.put("subtotal", sale.getSubtotal());
            parameters.put("igv", sale.getIgv());
            parameters.put("discount", sale.getSubtotalDiscount());
            parameters.put("customer", sale.getCustomer() != null);
            parameters.put("points", sale.getCustomer() == null ? 0L : sale.getCustomerPoints());
            parameters.put("current_points", sale.getPoints());
            parameters.put("cant", NumberToLetterConverter.convertNumberToLetter(sale.getSubtotal().add(sale.getIgv()).subtract(sale.getSubtotalDiscount()).doubleValue()));
            JRPdfExporter exporter = new JRPdfExporter();
            File tempFile = File.createTempFile("darkus", null);
            FileOutputStream output = new FileOutputStream(tempFile);
            JasperPrint jasperPrint = JasperFillManager.fillReport(is, parameters, new JDBCPostGresSQL().getConnection());
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output);

            exporter.exportReport();
            ResponseBuilder response = Response.ok(FileUtils.readFileToByteArray(tempFile));
            tempFile.delete();
            return response.header("Content-Disposition", "attachment; filename=\"bms_sale.pdf\"").build();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (JRException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ResourceSale.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("data_sale.dkn")
    @Produces(MediaType.TEXT_PLAIN)
    public String searchById(@QueryParam("q") String aes) throws Exception {
        try {
            String[] data = AES.decrypt(aes, AESKeys.SALE_ID).split(",");
            Date currentDate = new Date();
            double minutes = new Long(currentDate.getTime() - Long.parseLong(data[1])).doubleValue() / 60000.0;
            if (minutes > 3) {
                Response.status(Response.Status.NOT_FOUND);
                return null;
            }
            Long id = Long.parseLong(data[0]);
            Sale sale = saleService.getById(id);
            JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("__type__", "SALE")
                    .add("document_number", sale.getSerie() + "-" + sale.getDocumentNumber())
                    .add("date_issue", new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(sale.getDateIssue()))
                    .add("id_sale", sale.getId())
                    .add("type", sale.getPaymentProof().getName())
                    .add("address", sale.getCompany().getAddress() + " - " + sale.getCompany().getCity() + " \n " + sale.getCompany().getDistrict().getSubregion().getRegion().getName() + " - " + sale.getCompany().getDistrict().getSubregion().getName() + " - " + sale.getCompany().getDistrict().getName())
                    .add("ruc", sale.getCompany().getRuc())
                    .add("city", sale.getCompany().getCity())
                    .add("seller", userService.getCreatorUsername(Sale.class, id))
                    .add("subtotal", sale.getSubtotal())
                    .add("igv", sale.getIgv())
                    .add("discount", sale.getSubtotalDiscount())
                    .add("customer", sale.getCustomer() != null)
                    .add("points", sale.getCustomer() == null ? 0L : sale.getCustomer().getPoints())
                    .add("current_points", sale.getPoints())
                    .add("cant", NumberToLetterConverter.convertNumberToLetter(sale.getSubtotal().add(sale.getIgv()).subtract(sale.getSubtotalDiscount()).doubleValue()));
            if (sale.getCustomer() != null) {
                builder.add("identity_document", sale.getCustomer().getIdentityNumber())
                        .add("document_type", sale.getCustomer().getIdentityDocument().getAbbr());
                if (sale.getCustomer().getAddress() != null) {
                    builder.add("customer_address", sale.getCustomer().getAddress());
                }
            }
            if (sale.getCustomerName() != null) {
                builder.add("customer_name", sale.getCustomerName());
            }

            JsonArrayBuilder detailsBuilder = Json.createArrayBuilder();
            saleDetailService.getBasicDataBySale(sale).forEach(detail -> {
                detailsBuilder.add(
                        Json.createObjectBuilder()
                                .add("product_name", (String) detail[0])
                                .add("quantity", (BigDecimal) detail[1])
                                .add("uom_abbr", (String) detail[2])
                                .add("unit_price", (BigDecimal) detail[3])
                                .add("subtotal", (BigDecimal) detail[4])
                );
            });
            builder.add("details", detailsBuilder);
            JsonObject saleJSON = builder.build();
            return AES.encrypt(saleJSON.toString(), AESKeys.JSON_RESOURCE);
        } catch (javax.crypto.IllegalBlockSizeException | NullPointerException | NumberFormatException ex) {
            Response.status(Response.Status.NOT_FOUND);
            ex.printStackTrace();
        }
        return null;
    }
}
