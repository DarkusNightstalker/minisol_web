/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.bean.util.PNotifyMessage;
import gkfire.util.ErrorMessage;
import gkfire.util.ImportUtils;
import cs.bms.dao.util.PGSqlUtil;
import cs.bms.model.AccountingEntry;
import cs.bms.service.interfac.IAccountingEntryService;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.report.util.ReportContentType;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.AbstractImport;
import gkfire.web.util.Pagination;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class AccountingEntryBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{accountingEntryService}")
    private IAccountingEntryService accountingEntryService;

    private Export export;
    private String path;
    private String name;

    @PostConstruct
    public void init() {
        export = new Export();
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.asc("path"));
        pagination = new Pagination(accountingEntryService);
    }

    @Override
    protected void clearFields() {
        path = "";
        name = "";
    }

    @Override
    public void search() {

        path = path.trim();
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.property("id"))
                .add(Projections.property("path"))
                .add(Projections.property("name"));
        CriterionList criterionList = new CriterionList();
        if (path.length() != 0) {
            criterionList.add(Restrictions.like("path", path, MatchMode.START));
        }
        if (name.length() != 0) {
            criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
        }
        try {
            pagination.search(1, projectionList, criterionList, orderFactory.make());
        } catch (Exception e) {
            PNotifyMessage.systemError(e, sessionBean);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="initImport">
    @Override
    protected void initImport() {
        import_ = new AbstractImport() {
            @Override
            public void beginThread(int rowBegin, int trama) {
                for (int i = rowBegin; i <= rowBegin + trama; i++) {
                    if (i > totalRecords) {
                        break;
                    }
                    Object[] o = null;
                    try {
                        o = ImportUtils.readRow(file, i, 2);
                    } catch (Exception e) {
                        logError.put(i, " Contenido no legible :  " + e.getMessage());
                        continue;
                    }
                    AccountingEntry parent = null;
                    String code = null;
                    try {
                        try {
                            code = new Double(o[0].toString()).longValue() + "";
                        } catch (NumberFormatException e) {
                            code = o[0].toString().trim();
                        }
                        ErrorMessage errorMessage = AccountingEntryBean.this.getAccountingEntryService().verifyCode(code.trim(), -1L);
                        if (errorMessage != null) {
                            if (parent == null) {
                                logError.put(i, "Codigo :  " + errorMessage.getContent());
                                continue;
                            }
                        }
                    } catch (NullPointerException e) {
                        logError.put(i, "Codigo :  El campo no puede ser vacio");
                        continue;
                    }

                    AccountingEntry item = new AccountingEntry();
                    item.setPath(code);
                    item.setCreateDate(Calendar.getInstance().getTime());
                    item.setCreateUser(sessionBean.getUserService().getById(2));
                    try {
                        item.setName((String) o[1]);
                    } catch (ClassCastException e) {
                        logError.put(i, "Nombre :  El campo esta incorrecto");
                        continue;
                    }
                    item.setName((String) o[1]);
                    if (parent == null) {
                        parent = AccountingEntryBean.this.getAccountingEntryService().getParent(item.getPath());
                    }
                    item.setParent(parent);
                    item.setCode(parent != null ? item.getPath().substring(item.getParent().getPath().length()) : item.getPath());

                    try {
                        getAccountingEntryService().saveOrUpdate(item);
                        log.put(i, "Exito");
                    } catch (Exception e) {
                        if (e instanceof ConstraintViolationException) {
                            PSQLException psql = (PSQLException) ((ConstraintViolationException) e).getSQLException();
                            logError.put(i, " Error en guardado : " + psql.getMessage());
                        } else {
                            logError.put(i, " Error en guardado : " + e.getMessage());
                        }
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        };
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">
    /**
     * @return the sessionBean
     */
    @Override
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
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the export
     */
    public Export getExport() {
        return export;
    }

    /**
     * @param export the export to set
     */
    public void setExport(Export export) {
        this.export = export;
    }

    /**
     * @return the accountingEntryService
     */
    public IAccountingEntryService getAccountingEntryService() {
        return accountingEntryService;
    }

    /**
     * @param accountingEntryService the accountingEntryService to set
     */
    public void setAccountingEntryService(IAccountingEntryService accountingEntryService) {
        this.accountingEntryService = accountingEntryService;
    }

    //</editor-fold>
    public class Export implements java.io.Serializable {

        private Boolean activeHeader;
        private Boolean allowDropped;
        private ReportContentType contentType;

        public String toXlsx() {
            contentType = ReportContentType.XLSX;
            return execute(new JRXlsxExporter());
        }

        public String toPdf() {
            contentType = ReportContentType.PDF;
            return execute(new JRPdfExporter());
        }

        public String toDocx() {
            contentType = ReportContentType.DOCX;
            return execute(new JRDocxExporter());
        }

        public String toTxt() {
            contentType = ReportContentType.TXT;
            return execute(new JRTextExporter());
        }

        public String toSyBase() {
            return "index?faces-redirect=true";
        }

        public String toHtml() {
            contentType = ReportContentType.HTML;
            return execute(new JRHtmlExporter());
        }

        private String execute(JRExporter exporter) {
            try {
                Map<String, Object> param = new HashMap<>();
                ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
                InputStream is = servletContext.getResourceAsStream("/1258488425132154132154214536/Classifier.jasper");
                param.put("UNAS", servletContext.getResourceAsStream("/1258488425132154132154214536/images/unas.png"));
                param.put("active", allowDropped);
                param.put("printHeader", activeHeader);
                JasperPrint jasperPrint = JasperFillManager.fillReport(is, param, PGSqlUtil.getJdbcPostGresSQL().getConnection());
                HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                response.setContentType(contentType.getMimeType());
                response.addHeader("Content-disposition", "attachment; filename=Activiades_presupuestales." + contentType.name().toLowerCase());
                exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
                exporter.exportReport();
                FacesContext.getCurrentInstance().responseComplete();
            } catch (Exception ex) {
                getSessionBean().getMessages().add(new PNotifyMessage("ERROR EN REPORTE!", ex.getMessage(), PNotifyMessage.Type.Danger, "fa fa-warning shaked animated"));
                Logger.getLogger(AccountingEntryBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            return "index?faces-redirect=true";
        }

        /**
         * @return the activeHeader
         */
        public Boolean getActiveHeader() {
            return activeHeader;
        }

        /**
         * @param activeHeader the activeHeader to set
         */
        public void setActiveHeader(Boolean activeHeader) {
            this.activeHeader = activeHeader;
        }

        /**
         * @return the allowDropped
         */
        public Boolean getAllowDropped() {
            return allowDropped;
        }

        /**
         * @param allowDropped the allowDropped to set
         */
        public void setAllowDropped(Boolean allowDropped) {
            this.allowDropped = allowDropped;
        }

        /**
         * @return the contentType
         */
        public ReportContentType getContentType() {
            return contentType;
        }

        /**
         * @param contentType the contentType to set
         */
        public void setContentType(ReportContentType contentType) {
            this.contentType = contentType;
        }
    }
}
