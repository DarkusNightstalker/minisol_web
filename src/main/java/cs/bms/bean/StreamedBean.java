/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import gkfire.report.util.ReportContentType;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author CTIC
 */
@ManagedBean
@ApplicationScoped
public class StreamedBean implements java.io.Serializable {

    private StreamedContent saleTemplate;
    private StreamedContent purchaseTemplate;

    @PostConstruct
    public void init() {
        saleTemplate = new DefaultStreamedContent(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/cs/bms/files/temp_sale.xlsx"), ReportContentType.XLSX.getMimeType(), "Plantilla Ventas.xlsx");
        purchaseTemplate = new DefaultStreamedContent(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/cs/bms/files/temp_purchase.xlsx"), ReportContentType.XLSX.getMimeType(), "Plantilla Compras.xlsx");
    }

    /**
     * @return the saleTemplate
     */
    public StreamedContent getSaleTemplate() {
        return saleTemplate;
    }

    /**
     * @param saleTemplate the saleTemplate to set
     */
    public void setSaleTemplate(StreamedContent saleTemplate) {
        this.saleTemplate = saleTemplate;
    }

    /**
     * @return the purchaseTemplate
     */
    public StreamedContent getPurchaseTemplate() {
        return purchaseTemplate;
    }

    /**
     * @param purchaseTemplate the purchaseTemplate to set
     */
    public void setPurchaseTemplate(StreamedContent purchaseTemplate) {
        this.purchaseTemplate = purchaseTemplate;
    }
    
    
}
