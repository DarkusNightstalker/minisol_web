/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.servlet;

import cs.bms.dao.util.JDBCPostGresSQL;
import cs.bms.model.InternalStockMovement;
import cs.bms.model.User;
import cs.bms.service.interfac.IInternalStockMovementDetailService;
import cs.bms.service.interfac.IInternalStockMovementService;
import cs.bms.service.interfac.IUserService;
import cs.bms.util.AESKeys;
import gkfire.util.AES;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedProperty;
import javax.json.JsonObject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Jhoan Brayam
 */
@Path("ism")
@javax.enterprise.context.RequestScoped
public class ResourceISM {

    @Autowired
    @Qualifier("internalStockMovementService")
    private IInternalStockMovementService internalStockMovementService;
    @Autowired
    @Qualifier("internalStockMovementDetailService")
    private IInternalStockMovementDetailService internalStockMovementDetailService;
    @Autowired
    @Qualifier("userService")
    @ManagedProperty(value = "#{userService}")
    private IUserService userService;

    @javax.ws.rs.core.Context
    private ServletContext servletContext;

    @GET
    @Path("print.pdf")
    @Produces("application/pdf")
    public Response print(@QueryParam("id") String id) {
        InternalStockMovement ism = internalStockMovementService.getById(Long.parseLong(id));
        if (ism == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            User creator = (User) userService.getByHQL("SELECT s.createUser FROM InternalStockMovement s WHERE s.id=?", Long.parseLong(id));
            InputStream is = servletContext.getResourceAsStream("/1258488425132154132154214536/internal_movement_big.jasper");

            Map<String, Object> parameters = new HashMap();
            parameters.put("document_number", ism.getSerie() + "-" + ism.getDocumentNumber());
            parameters.put("type", ism.getPaymentProof().getName());
            parameters.put("address_source", ism.getSourceCompany().getAddress() + " - " + ism.getSourceCompany().getCity() + " \n " + ism.getSourceCompany().getDistrict().getSubregion().getRegion().getName() + " - " + ism.getSourceCompany().getDistrict().getSubregion().getName() + " - " + ism.getSourceCompany().getDistrict().getName());
            parameters.put("address_target", ism.getTargetCompany().getAddress() + " - " + ism.getTargetCompany().getCity() + " \n " + ism.getTargetCompany().getDistrict().getSubregion().getRegion().getName() + " - " + ism.getTargetCompany().getDistrict().getSubregion().getName() + " - " + ism.getTargetCompany().getDistrict().getName());
            parameters.put("private_mark", ism.getTransportDescription());
            parameters.put("private_license", ism.getDriverLicense());
            parameters.put("ruc_transport", ism.getCarrier() == null ? null : ism.getCarrier().getIdentityNumber());
            parameters.put("name_transport", ism.getCarrier() == null ? null : ism.getCarrier().getName());
            parameters.put("ruc", ism.getSourceCompany().getRuc());
            parameters.put("destinatary_ruc", ism.getTargetCompany().getRuc());
            parameters.put("destinatary", ism.getTargetCompany().getName());
            parameters.put("date_shipping", ism.getDateShipping());
            parameters.put("date_arrival", ism.getDateArrival());
            parameters.put("id_ism", ism.getId());
            JasperPrint jasperPrint = JasperFillManager.fillReport(is, parameters, new JDBCPostGresSQL().getConnection());
            JRPdfExporter exporter = new JRPdfExporter();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, output);

            exporter.exportReport();
            Response.ResponseBuilder response = Response.ok(output.toByteArray());
            return response.header("Content-Disposition", "attachment; filename=\"Guia de Remisión Remitente " + ism.getSerie() + "-" + ism.getDocumentNumber() + ".pdf\"").build();

        } catch (JRException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ResourceSale.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("product_quantity.dkn")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public BigDecimal getProductQuantity(JsonObject jsonInput) throws Exception {

        Long time = jsonInput.getJsonNumber("time").longValue();

        Date currentDate = new Date();
        double minutes = new Long(currentDate.getTime() - time).doubleValue() / 60000.0;
        if (minutes > 3) {
            Response.status(Response.Status.REQUEST_TIMEOUT);
            return null;
        }

        String username = AES.decrypt(jsonInput.getString("username"), AESKeys.SYNCHRO_TRANSFERENCE);
        String password = AES.decrypt(jsonInput.getString("password"), AESKeys.SYNCHRO_TRANSFERENCE);
        User user = userService.login(username, password);
        if (user != null && user.getActive()) {
            String paymentProofCode = jsonInput.getString("paymentProofCode");
            String serie = jsonInput.getString("serie");
            String documentNumber = jsonInput.getString("documentNumber");
            String productBarcode = jsonInput.getString("productBarcode");
            BigDecimal quantity = internalStockMovementDetailService.getQuantityByDocumentBarcode(
                    paymentProofCode, 
                    serie, 
                    documentNumber, 
                    productBarcode
            );
            return quantity;
        } else {
            Response.status(Response.Status.UNAUTHORIZED).build();
            return null;
        }

    }
}
