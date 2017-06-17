/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.dao.util.JDBCPostGresSQL;
import cs.bms.model.InternalStockMovement;
import cs.bms.model.User;
import cs.bms.service.interfac.IInternalStockMovementService;
import cs.bms.service.interfac.IUserService;
import gkfire.web.util.BeanUtil;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class IsmPrintBean implements java.io.Serializable {

    @ManagedProperty(value = "#{internalStockMovementService}")
    private IInternalStockMovementService internalStockMovementService;
    @ManagedProperty(value = "#{userService}")
    private IUserService userService;

    public void print() {
        InternalStockMovement ism = internalStockMovementService.getById(Long.parseLong(BeanUtil.getParameter("id")));
        if (ism == null) {
            BeanUtil.getResponse().setContentType("text/html;charset=UTF-8");
            return;
        }
        try {
            User creator = (User) getUserService().getByHQL("SELECT s.createUser FROM InternalStockMovement s WHERE s.id=?", Long.parseLong(BeanUtil.getParameter("id")));
            ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
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
            JRTextExporter exporter = new JRTextExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, BeanUtil.getResponse().getOutputStream());
            exporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, 10);

            exporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, 10);

            exporter.setParameter(JRTextExporterParameter.BETWEEN_PAGES_TEXT, "");
            exporter.setParameter(JRPdfExporterParameter.PDF_JAVASCRIPT, "this.print();");
            exporter.exportReport();
            BeanUtil.getResponse().setContentType("text/plain");
            FacesContext facesContext = FacesContext.getCurrentInstance(); //Get the context ONCE
            facesContext.responseComplete();
            BeanUtil.getResponse().getOutputStream().flush();
            BeanUtil.getResponse().getOutputStream().close();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (JRException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SalePrintBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the internalStockMovementService
     */
    public IInternalStockMovementService getInternalStockMovementService() {
        return internalStockMovementService;
    }

    /**
     * @param internalStockMovementService the internalStockMovementService to
     * set
     */
    public void setInternalStockMovementService(IInternalStockMovementService internalStockMovementService) {
        this.internalStockMovementService = internalStockMovementService;
    }

    /**
     * @return the userService
     */
    public IUserService getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

}
