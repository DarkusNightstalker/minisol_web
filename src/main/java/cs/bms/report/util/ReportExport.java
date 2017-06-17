package cs.bms.report.util;

import cs.bms.bean.util.PNotifyMessage;
import cs.bms.dao.util.PGSqlUtil;
import gkfire.report.util.ReportExportGeneric;
import gkfire.web.bean.AbstractSessionBean;
import gkfire.web.util.BeanUtil;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;

public class ReportExport extends ReportExportGeneric {

    public ReportExport(String pathJasper, String fileName, AbstractSessionBean sessionBean, Map<String, Object> defaultParams) {
        super(pathJasper, fileName, sessionBean, defaultParams);
        functionOutputstream = BeanUtil.getResponse()::getOutputStream;
        functionInputstream = BeanUtil.getContext()::getResourceAsStream;
        functionConnection = PGSqlUtil.getJdbcPostGresSQL()::getConnection;
        reportsDirPath = BeanUtil.getContext().getRealPath("/") + "1258488425132154132154214536";
    }

    public String toXlsx() {
        try {
            toGenericXlsx();
        } catch (Exception e) {
            sessionBean.getMessages().add(PNotifyMessage.systemError(e, sessionBean));
        }
        return "index?faces-redirect=true";
    }

    public String toPdf() {
        try {
            toGenericPdf();
        } catch (Exception e) {
            sessionBean.getMessages().add(PNotifyMessage.systemError(e, sessionBean));
        }
        return "index?faces-redirect=true";
    }

    public String toDocx() {
        try {
            toGenericDocx();
        } catch (Exception e) {
            sessionBean.getMessages().add(PNotifyMessage.systemError(e, sessionBean));
        }
        return "index?faces-redirect=true";
    }

    public String toTxt() {
        try {
            toGenericTxt();
        } catch (Exception e) {
            sessionBean.getMessages().add(PNotifyMessage.systemError(e, sessionBean));
        }
        return "index?faces-redirect=true";
    }

    public String toHtml() {
        try {
            toGenericXlsx();
        } catch (Exception e) {
            sessionBean.getMessages().add(PNotifyMessage.systemError(e, sessionBean));
        }
        return "index?faces-redirect=true";
    }

    @Override
    protected void execute(JRExporter exporter) {
        try {
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            response.setContentType(this.contentType.getMimeType());
            response.addHeader("Content-disposition", "attachment; filename=\"" + this.fileName + "." + this.contentType.name().toLowerCase() + "\"");
            super.execute(exporter);
        } catch (Exception ex) {
            this.sessionBean.getMessages().add(PNotifyMessage.systemError(ex, sessionBean));
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

}
