/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Actor;
import cs.bms.model.IdentityDocument;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.util.IdentitySearch;
import cs.bms.util.exception.CaptchaFailedException;
import cs.bms.util.exception.IdentityException;
import gkfire.auditory.Auditory;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.util.ImportUtils;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.AbstractImport;
import gkfire.web.util.AsynchronousTask;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.websocket.SessionException;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class ActorBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;

    protected IdentityDocumentSearcher identityDocumentSearcher;
    protected AsynchroUpdate synchroDataWithWeb;
    protected Short identityDocumentId;
    protected String identityNumber;
    protected Boolean customer;
    protected Boolean supplier;
    protected String name;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(actorService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("id"));
        identityDocumentSearcher = new IdentityDocumentSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        identityDocumentSearcher.update();
        refresh();
    }

    @Override
    protected void clearFields() {
        supplier = true;
        customer = true;
        identityDocumentId = null;
        identityNumber = "";
        name = "";
    }

    @Override
    protected void initImport() {
        //<editor-fold defaultstate="collapsed" desc="Init import">
        this.import_ = new AbstractImport() {
            @Override
            public void beginThread(int rowBegin, int trama) {
                for (int i = rowBegin; i < rowBegin + trama; i++) {
                    if (i > totalRecords) {
                        break;
                    }
                    Object[] o = null;
                    try {
                        o = ImportUtils.readRow(fileObject, i, 7);
                    } catch (Exception e) {
                        addError(i, " Contenido no legible :  " + e.getMessage());
                        continue;
                    }

                    if (o[0] == null && o[1] == null) {
                        addError(i, "DOCUMENTOS  :  EL RUC Y DNI ESTAN VACIOS");
                        continue;
                    }

                    if (o[0] == null && o[1] == null) {
                        addError(i, "DOCUMENTOS  :  EL RUC Y DNI ESTAN VACIOS");
                        continue;
                    }
                    try {
                        if (o[6] != null && !"A".equalsIgnoreCase((String) o[6]) && !"R".equalsIgnoreCase((String) o[6])) {
                            addError(i, "OPERACION DE PUNTOS  :  DEBE ESTAR VACIO O CON VALORES ACEPTADOS DE 'A' o 'R'");
                            continue;
                        }
                    } catch (ClassCastException exception) {
                        addError(i, "OPERACION DE PUNTOS  :  DEBE ESTAR VACIO O CON VALORES ACEPTADOS DE 'A' o 'R'");
                        continue;
                    }
                    String name = o[2] == null ? null : String.valueOf(o[2]);
                    try {
                        if (!"P".equalsIgnoreCase((String) o[5]) && !"C".equalsIgnoreCase((String) o[5]) && !"CP".equalsIgnoreCase((String) o[5])) {
                            addError(i, "TIPO  :  EL TIPO DEBE SER 'P' O 'C' o 'CP'");
                            continue;
                        }
                    } catch (ClassCastException exception) {
                        addError(i, "TIPO  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }
                    String dni = null;
                    try {
                        if (o[0] instanceof Number) {
                            throw new ClassCastException();
                        } else if (o[0] instanceof String) {
                            dni = (String) o[0];
                        } else if (o[0] != null) {
                            throw new ClassCastException();
                        }
                    } catch (ClassCastException exception) {
                        addError(i, "DNI  :  FORMATO DE CAMPO INCORRECTO, DEBE SER  TEXTO");
                        continue;
                    }

                    if (dni != null && dni.length() != 8) {
                        addError(i, "DNI  :  EL DNI DEBE TENER  8 DIGITOS");
                        continue;
                    }
                    String ruc = null;
                    try {
                        if (o[1] instanceof Number) {
                            throw new ClassCastException();
                        } else if (o[1] instanceof String) {
                            ruc = (String) o[1];
                        } else if (o[1] != null) {
                            throw new ClassCastException();
                        }
                    } catch (ClassCastException exception) {
                        addError(i, "RUC  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }

                    if (ruc != null && ruc.length() != 11) {
                        addError(i, "RUC  :  EL RUC DEBE TENER 11 DIGITOS");
                        continue;
                    }

                    String identityNumber = ruc == null ? dni : ruc;

                    if (identityNumber == null) {
                        addError(i, "DNI/RUC  :  SE NECESITA UNA DE LOS DOS");
                        continue;
                    }

                    Actor a = (Actor) actorService.getByHQL("FROM Actor a WHERE a.identityNumber = ?", identityNumber);
                    if (o[6] == null && a != null) {
                        addError(i, "DOCUMENTO REPETIDO  :  YA SE HA REGISTRADO A ALGUIEN CON EL DOCUMENTO '" + identityNumber + "'");
                        continue;
                    }
                    long points;
                    try {
                        points = o[4] == null ? 0 : ((Double) o[4]).longValue();
                        if (points < 0) {
                            points = 0;
                        }
                    } catch (ClassCastException e) {
                        addError(i, "PUNTOS  :  FORMATO INCORRECTO DE CAMPO , DEBE SER UN NUMERO NO DECIMAL NI NEGATIVO");
                        continue;
                    }
                    if (a == null) {
                        a = new Actor();
                        a.setIdentityNumber(identityNumber);
                        a.setIdentityDocument((IdentityDocument) identityDocumentService.getByHQL("FROM IdentityDocument idd WHERE idd.length_ = ?", new Short(identityNumber.length() + "")));
                        a.setName(name);
                        a.setAddress(o[3] == null ? null : String.valueOf(o[3]));
                        a.setRepresentative(null);
                        a.setSupplier("P".equalsIgnoreCase((String) o[5]) || "CP".equalsIgnoreCase((String) o[5]));
                        a.setCustomer("C".equalsIgnoreCase((String) o[5]) || "CP".equalsIgnoreCase((String) o[5]));
                        a.setPoints(points);
                    } else {
                        if (o[6] == "R") {
                            a.setIdentityNumber(identityNumber);
                            a.setIdentityDocument((IdentityDocument) identityDocumentService.getByHQL("FROM IdentityDocument idd WHERE idd.length_ = ?", new Short(identityNumber.length() + "")));
                            a.setName(name);
                            a.setAddress(o[3] == null ? null : String.valueOf(o[3]));
                            a.setRepresentative(null);
                            a.setSupplier("P".equalsIgnoreCase((String) o[5]) || "CP".equalsIgnoreCase((String) o[5]));
                            a.setCustomer("C".equalsIgnoreCase((String) o[5]) || "CP".equalsIgnoreCase((String) o[5]));
                            a.setPoints(points);
                        } else {
                            a.setPoints(a.getPoints() + points);
                        }
                    }
                    Auditory.make(a, sessionBean.getCurrentUser());

                    System.out.println("FILA FINAL : " + i);
                    try {
                        actorService.saveOrUpdate(a);
                        addSaved(i, "Exito");
                    } catch (Exception e) {
                        if (e instanceof ConstraintViolationException) {
                            PSQLException psql = (PSQLException) ((ConstraintViolationException) e).getSQLException();
                            addError(i, " Error en guardado : " + psql.getMessage());
                        } else {
                            addError(i, " Error en guardado : " + e.getMessage());
                        }
                        e.printStackTrace();
                        continue;
                    }
                }
                if (log.keySet().size() + logError.keySet().size() == totalRecords) {
                    state = ImportUtils.State.SUCCESS;
                }
            }

        };
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Init Synchro">

        synchroDataWithWeb = new AsynchroUpdate("");
        //</editor-fold>
    }

    @Override
    public void search() {
        identityNumber = identityNumber.trim();
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("idd.abbr"))
                .add(Projections.property("identityNumber"))
                .add(Projections.property("name"))
                .add(Projections.property("customer"))
                .add(Projections.property("supplier"))
                .add(Projections.property("points"))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        AliasList aliasList = new AliasList();
        aliasList.add("identityDocument", "idd");
        criterionList.add(Restrictions.eq("active", true));
        Disjunction d = Restrictions.disjunction();

        if (supplier) {
            d.add(Restrictions.eq("supplier", true));
        }
        if (customer) {
            d.add(Restrictions.eq("customer", true));
        }
        criterionList.add(d);

        try {
            if (identityNumber.length() != 0) {
                criterionList.add(Restrictions.like("identityNumber", identityNumber, MatchMode.ANYWHERE));
            }
            if (identityDocumentId != null) {
                criterionList.add(Restrictions.eq("idd.id", identityDocumentId));
            }
            if (name.length() != 0) {
                criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
            }
            pagination.search(1, projectionList, criterionList, aliasList, orderFactory.make());
        } catch (Exception e) {
            PNotifyMessage.systemError(e, sessionBean);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">
    /**
     * @return the sessionBean
     */
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
     * @return the actorService
     */
    public IActorService getActorService() {
        return actorService;
    }

    /**
     * @param actorService the actorService to set
     */
    public void setActorService(IActorService actorService) {
        this.actorService = actorService;
    }

    /**
     * @return the identityDocumentService
     */
    public IIdentityDocumentService getIdentityDocumentService() {
        return identityDocumentService;
    }

    /**
     * @param identityDocumentService the identityDocumentService to set
     */
    public void setIdentityDocumentService(IIdentityDocumentService identityDocumentService) {
        this.identityDocumentService = identityDocumentService;
    }

    /**
     * @return the identityDocumentSearcher
     */
    public IdentityDocumentSearcher getIdentityDocumentSearcher() {
        return identityDocumentSearcher;
    }

    /**
     * @param identityDocumentSearcher the identityDocumentSearcher to set
     */
    public void setIdentityDocumentSearcher(IdentityDocumentSearcher identityDocumentSearcher) {
        this.identityDocumentSearcher = identityDocumentSearcher;
    }

    /**
     * @return the identityDocumentId
     */
    public Short getIdentityDocumentId() {
        return identityDocumentId;
    }

    /**
     * @param identityDocumentId the identityDocumentId to set
     */
    public void setIdentityDocumentId(Short identityDocumentId) {
        this.identityDocumentId = identityDocumentId;
    }

    /**
     * @return the identityNumber
     */
    public String getIdentityNumber() {
        return identityNumber;
    }

    /**
     * @param identityNumber the identityNumber to set
     */
    public void setIdentityNumber(String identityNumber) {
        this.identityNumber = identityNumber;
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
     * @return the customer
     */
    public Boolean getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(Boolean customer) {
        this.customer = customer;
    }

    /**
     * @return the supplier
     */
    public Boolean getSupplier() {
        return supplier;
    }

    /**
     * @param supplier the supplier to set
     */
    public void setSupplier(Boolean supplier) {
        this.supplier = supplier;

    }

    /**
     * @return the synchroDataWithWeb
     */
    public AsynchroUpdate getSynchroDataWithWeb() {
        return synchroDataWithWeb;
    }

    /**
     * @param synchroDataWithWeb the synchroDataWithWeb to set
     */
    public void setSynchroDataWithWeb(AsynchroUpdate synchroDataWithWeb) {
        this.synchroDataWithWeb = synchroDataWithWeb;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="IdentityDocumentSearcher">
    public class IdentityDocumentSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = identityDocumentService.getBasicData();
        }

        /**
         * @return the data
         */
        public List<Object[]> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }

    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="AsynchroUpdate">
    public class AsynchroUpdate extends AsynchronousTask {

        protected List<String> errors;
        protected Long totalCount;
        protected Long currentCount;
        protected boolean valid;
        protected boolean finalize;
        protected String captcha;
        protected IdentitySearch identitySearch;
        protected String image64;

        public AsynchroUpdate(String openScriptOnLoad) {
            super(openScriptOnLoad);
        }

        public void beforeBegin() {
            if (identitySearch == null) {
                identitySearch = new IdentitySearch() {
                    @Override
                    public void init(int length) {
                        super.init(length);
                        if (imageCaptcha != null) {
                            image64 = new String(Base64.getEncoder().encode(imageCaptcha));
                        } else {
                            image64 = null;
                        }
                    }

                };
            }
            errors = new ArrayList();
            finalize = false;
            refresh();
        }

        public void stop() {
            finalize = true;
        }

        public void refresh() {
            captcha = "";
            valid = false;
            identitySearch.init(8);
        }

        @Override
        protected boolean isFinished() {
            return finalize;
        }

        @Override
        @Asynchronous
        public void begin() {
            valid = true;
            totalCount = actorService.countIdentityDataForSynchro("1");
            List<Object[]> data = actorService.getIdentityDataForSynchro("1");
            currentCount = 0L;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    for (Object[] item : data) {
                        if (finalize) {
                            break;
                        }
                        try {
                            search((String) item[0], (String) item[1]);
                        } catch (CaptchaFailedException ex) {
                            PNotifyMessage.errorMessage("Texto de imagen incorrecto");
                            valid = false;
                            captcha = "";
                            ex.printStackTrace();
                        } catch (IdentityException ex) {
                            errors.add(item[0] + " : " + ex.getMessage());
                            ex.printStackTrace();
                        } catch (SessionException ex) {
                            PNotifyMessage.errorMessage("Sesion expirada");
                            refresh();
                            ex.printStackTrace();
                        } catch (Exception ex) {
                            errors.add(item[0] + " : " + ex.getMessage());
                            ex.printStackTrace();
                        }
                        currentCount++;
                    }
                    finalize = true;
                }
            };
            executor.submit(task);
        }

        private void search(String identityNumber, String identityDocumentCode) throws Exception {
            switch (identityDocumentCode) {
                case "1":
                    String name = (String) identitySearch.search(identityNumber, captcha);
                    if (name == null) {
                        throw new Exception("El DNI no existe");
                    }
                    actorService.updateNameByIdentityDocument(identityNumber, name);
                    valid = true;
                    break;
                default:
                    break;
            }

        }

        public Long getCurrentCount() {
            return currentCount;
        }

        public void setCurrentCount(Long currentCount) {
            this.currentCount = currentCount;
        }

        public Long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Long totalCount) {
            this.totalCount = totalCount;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public String getCaptcha() {
            return captcha;
        }

        public void setCaptcha(String captcha) {
            this.captcha = captcha;
        }

        public void setIdentitySearch(IdentitySearch identitySearch) {
            this.identitySearch = identitySearch;
        }

        public void setImage64(String image64) {
            this.image64 = image64;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public IdentitySearch getIdentitySearch() {
            return identitySearch;
        }

        public String getImage64() {
            return image64;
        }

        /**
         * @return the finalize
         */
        public boolean isFinalize() {
            return finalize;
        }

        /**
         * @param finalize the finalize to set
         */
        public void setFinalize(boolean finalize) {
            this.finalize = finalize;
        }
    }

    //</editor-fold>
}
