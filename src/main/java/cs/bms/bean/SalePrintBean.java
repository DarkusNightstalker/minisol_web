/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.Sale;
import cs.bms.model.User;
import cs.bms.service.interfac.ISaleService;
import cs.bms.service.interfac.IUserService;
import cs.bms.util.SaleExport;
import gkfire.web.util.BeanUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.export.JRPdfExporter;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class SalePrintBean implements java.io.Serializable {

    @ManagedProperty(value = "#{saleService}")
    private ISaleService saleService;
    @ManagedProperty(value = "#{userService}")
    private IUserService userService;

    public void print() {
        Sale sale = saleService.getById(Long.parseLong(BeanUtil.getParameter("id")));
        if (sale == null) {
            BeanUtil.getResponse().setContentType("text/html;charset=UTF-8");
            return;
        }
        try {
            User creator = (User) userService.getByHQL("SELECT s.createUser FROM Sale s WHERE s.id=?",Long.parseLong(BeanUtil.getParameter("id")));
            SaleExport.printSale(sale,  BeanUtil.getResponse().getOutputStream(), new JRPdfExporter(),creator);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (JRException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SalePrintBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the saleService
     */
    public ISaleService getSaleService() {
        return saleService;
    }

    /**
     * @param saleService the saleService to set
     */
    public void setSaleService(ISaleService saleService) {
        this.saleService = saleService;
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
