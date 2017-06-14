/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import gkfire.web.util.BeanUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ShoppingCartBean implements java.io.Serializable{
    
    public void renderJson() throws IOException {
        HttpServletResponse response = BeanUtil.getResponse();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("[");
        for (int i = 0; i < 5; i++) {
            writer.write("{"
                    + "  \"ProductId\": 1,"
                    + "  \"Product\": \"Agua Cielo 500 ml\","
                    + "  \"UoM\": \"Unid.\","
                    + "  \"UnitPrice\": 1.00,"
                    + "  \"Quantity\": 12.00"
                    + "}");
            if(i  != 4){
                writer.write(",");
            }
        }
        writer.write("]");
        writer.flush();
        FacesContext.getCurrentInstance().responseComplete();
    }
}
