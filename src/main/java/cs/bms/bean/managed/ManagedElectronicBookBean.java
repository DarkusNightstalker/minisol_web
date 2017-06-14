/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.ElectronicBook;
import cs.bms.service.interfac.IElectronicBookCategoryService;
import cs.bms.service.interfac.IElectronicBookService;
import gkfire.auditory.Auditory;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Johan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedElectronicBookBean extends AManagedBean<ElectronicBook, IElectronicBookService> implements ILoadable {

    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{electronicBookService}")
    private IElectronicBookService mainService;
    @ManagedProperty(value = "#{electronicBookCategoryService}")
    private IElectronicBookCategoryService electronicBookCategoryService;
    @ManagedProperty(value = "#{navigationBean}")
    private NavigationBean navigationBean;
    @ManagedProperty(value = "#{managedElectronicBookCategoryBean}")
    private ManagedElectronicBookCategoryBean managedElectronicBookCategoryBean;

    private CategorySearcher categorySearcher;

    private Short elecronicBookCategoryId;
    private String code;
    private String name;
    private String metaData;
    private String summaryData;

    @PostConstruct
    public void init() {
        categorySearcher = new CategorySearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        categorySearcher.update();
    }

    @Override
    public boolean save() {
        if (mainService.existCode(code.trim(), selected.getId())) {
            PNotifyMessage.errorMessage("El codigo \"" + code + "\" ya esta registrado");
            saved = false;
        } else {
            try {
                String content = getSelected().getId() != null ? "Se ha actualizado los datos" : "Se ha registrado un nuevo tipo de libro electrónico";
                saved = super.save();
                PNotifyMessage.saveMessage(content);
            } catch (Exception e) {
                PNotifyMessage.errorMessage("Consulte el log de la app CODE : " + sessionBean.addError(e));
                saved = false;
            }
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        code = selected.getCode();
        name = selected.getName();
//        metaData = selected.getMetaData();
//        summaryData = selected.getMetaData();
        try {
            elecronicBookCategoryId = selected.getElectronicBookCategory().getId();
        } catch (Exception e) {
            elecronicBookCategoryId = null;
        }
        onLoad(true);
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        code = code.trim();
        name = name.trim();
        selected.setCode(code);
        selected.setName(name);
        try {
            selected.setElectronicBookCategory(electronicBookCategoryService.getById(elecronicBookCategoryId));
        } catch (Exception e) {
            selected.setElectronicBookCategory(null);
        }
        Auditory.make(selected, sessionBean.getCurrentUser());
    }

    //<editor-fold desc="Getters & Setters" defaultstate="collapsed">
    /**
     * @return the categorySearcher
     */
    public CategorySearcher getCategorySearcher() {
        return categorySearcher;
    }

    /**
     * @param categorySearcher the categorySearcher to set
     */
    public void setCategorySearcher(CategorySearcher categorySearcher) {
        this.categorySearcher = categorySearcher;
    }

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
     * @return the navigationBean
     */
    public NavigationBean getNavigationBean() {
        return navigationBean;
    }

    /**
     * @param navigationBean the navigationBean to set
     */
    public void setNavigationBean(NavigationBean navigationBean) {
        this.navigationBean = navigationBean;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
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
     * @return the mainService
     */
    public IElectronicBookService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IElectronicBookService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the electronicBookCategoryService
     */
    public IElectronicBookCategoryService getElectronicBookCategoryService() {
        return electronicBookCategoryService;
    }

    /**
     * @param electronicBookCategoryService the electronicBookCategoryService to
     * set
     */
    public void setElectronicBookCategoryService(IElectronicBookCategoryService electronicBookCategoryService) {
        this.electronicBookCategoryService = electronicBookCategoryService;
    }

    /**
     * @return the elecronicBookCategoryId
     */
    public Short getElecronicBookCategoryId() {
        return elecronicBookCategoryId;
    }

    /**
     * @param elecronicBookCategoryId the elecronicBookCategoryId to set
     */
    public void setElecronicBookCategoryId(Short elecronicBookCategoryId) {
        this.elecronicBookCategoryId = elecronicBookCategoryId;
    }

    /**
     * @return the managedElectronicBookCategoryBean
     */
    public ManagedElectronicBookCategoryBean getManagedElectronicBookCategoryBean() {
        return managedElectronicBookCategoryBean;
    }

    /**
     * @param managedElectronicBookCategoryBean the
     * managedElectronicBookCategoryBean to set
     */
    public void setManagedElectronicBookCategoryBean(ManagedElectronicBookCategoryBean managedElectronicBookCategoryBean) {
        this.managedElectronicBookCategoryBean = managedElectronicBookCategoryBean;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="CategorySearcher">
    public class CategorySearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getElectronicBookCategoryService().getBasicData();
        }

        public List<Object[]> getData() {
            return data;
        }

        public void initManaged() {
            getManagedElectronicBookCategoryBean().create();
        }

        public void save() {
            if (getManagedElectronicBookCategoryBean().isSaved()) {
                update();
            }
            setElecronicBookCategoryId(getManagedElectronicBookCategoryBean().getSelected().getId());
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }

    }

//</editor-fold>
    /**
     * @return the metaData
     */
    public String getMetaData() {
        return metaData;
    }

    /**
     * @param metaData the metaData to set
     */
    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    /**
     * @return the summaryData
     */
    public String getSummaryData() {
        return summaryData;
    }

    /**
     * @param summaryData the summaryData to set
     */
    public void setSummaryData(String summaryData) {
        this.summaryData = summaryData;
    }

}
