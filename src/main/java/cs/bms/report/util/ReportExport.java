package cs.bms.report.util;

import cs.bms.bean.util.PNotifyMessage;
import cs.bms.dao.util.PGSqlUtil;
import gkfire.report.util.ReportContentType;
import gkfire.web.bean.AbstractSessionBean;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.util.SimpleFileResolver;

public class ReportExport implements Serializable {

    protected String pathJasper;
    protected String fileName;
    protected AbstractSessionBean sessionBean;
    protected Map<String, Object> params;
    protected ReportContentType contentType;

    public ReportExport(String pathJasper, String fileName, AbstractSessionBean sessionBean, Map<String, Object> defaultParams) {
        this.pathJasper = pathJasper;
        this.sessionBean = sessionBean;
        this.fileName = fileName;
        this.params = defaultParams;
    }

    public String toXlsx() {
        this.contentType = ReportContentType.XLS;
        this.params.put("IS_IGNORE_PAGINATION", true);
        return execute(new JRXlsExporter());
    }

    public String toPdf() {
        this.contentType = ReportContentType.PDF;
        this.params.put("IS_IGNORE_PAGINATION", false);
        return execute((JRExporter) this.contentType.getExporter());
    }

    public String toDocx() {
        this.contentType = ReportContentType.DOCX;
        this.params.put("IS_IGNORE_PAGINATION", false);
        return execute((JRExporter) this.contentType.getExporter());
    }

    public String toTxt() {
        this.contentType = ReportContentType.TXT;
        this.params.put("IS_IGNORE_PAGINATION", true);
        return execute((JRExporter) this.contentType.getExporter());
    }

    public String toHtml() {
        this.contentType = ReportContentType.HTML;
        this.params.put("IS_IGNORE_PAGINATION", true);
        return execute((JRExporter) this.contentType.getExporter());
    }

    protected String execute(JRExporter exporter) {
        try {
            ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            InputStream is = servletContext.getResourceAsStream(this.pathJasper);
            String reportsDirPath = servletContext.getRealPath("/") + "1258488425132154132154214536";
            File reportsDir = new File(reportsDirPath);
            this.params.put("REPORT_FILE_RESOLVER", new SimpleFileResolver(reportsDir));

            JasperPrint jasperPrint = JasperFillManager.fillReport(is, this.params, PGSqlUtil.getJdbcPostGresSQL().getConnection());
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            response.setContentType(this.contentType.getMimeType());
            response.addHeader("Content-disposition", "attachment; filename=\"" + this.fileName + "." + this.contentType.name().toLowerCase() + "\"");
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());

            exporter.exportReport();
            FacesContext.getCurrentInstance().responseComplete();
        } catch (Exception ex) {
            this.sessionBean.getMessages().add(new PNotifyMessage("ERROR EN REPORTE!", ex.toString(), PNotifyMessage.Type.Danger, "fa fa-warning shaked animated"));
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return "index?faces-redirect=true";
    }

    public ReportContentType getContentType() {
        return this.contentType;
    }

    public void setContentType(ReportContentType contentType) {
        this.contentType = contentType;
    }

    public String getPathJasper() {
        return this.pathJasper;
    }

    public void setPathJasper(String pathJasper) {
        this.pathJasper = pathJasper;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public AbstractSessionBean getSessionBean() {
        return this.sessionBean;
    }

    public void setSessionBean(AbstractSessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public Map<String, Object> getParams() {
        return this.params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
