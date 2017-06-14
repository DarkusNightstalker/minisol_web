/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.managed;

import cs.bms.bean.NavigationBean;
import cs.bms.bean.SessionBean;
import cs.bms.bean.util.PNotifyMessage;
import cs.bms.model.Product;
import cs.bms.model.ProductLine;
import cs.bms.model.StockType;
import cs.bms.model.UoM;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IProductCostPriceService;
import cs.bms.service.interfac.IProductLineService;
import cs.bms.service.interfac.IProductSalePriceService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IPurchaseService;
import cs.bms.service.interfac.IStockTypeService;
import cs.bms.service.interfac.IUoMService;
import gkfire.auditory.Auditory;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderList;
import gkfire.web.bean.AManagedBean;
import gkfire.web.bean.ILoadable;
import gkfire.web.util.BeanUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ManagedProductBean extends AManagedBean<Product, IProductService> implements ILoadable {

    @ManagedProperty(value = "#{productService}")
    protected IProductService mainService;
    @ManagedProperty(value = "#{stockTypeService}")
    protected IStockTypeService stockTypeService;
    @ManagedProperty(value = "#{productLineService}")
    protected IProductLineService productLineService;
    @ManagedProperty(value = "#{uomService}")
    protected IUoMService uomService;
    @ManagedProperty(value = "#{companyService}")
    protected ICompanyService companyService;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{purchaseService}")
    protected IPurchaseService purchaseService;
    @ManagedProperty(value = "#{productSalePriceService}")
    protected IProductSalePriceService productSalePriceService;
    @ManagedProperty(value = "#{productCostPriceService}")
    protected IProductCostPriceService productCostPriceService;
    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{navigationBean}")
    protected NavigationBean navigationBean;
    @ManagedProperty(value = "#{managedProductLineBean}")
    protected ManagedProductLineBean managedProductLineBean;
    @ManagedProperty(value = "#{managedUoMBean}")
    protected ManagedUoMBean managedUoMBean;

    // private StockTypeSearcher stockTypeSearcher;
    protected ProductLineSearcher productLineSearcher;
    protected ManagedSupplier managedSupplier;
    protected UoMSearcher uomSearcher;
    protected ManagedProductPrice managedProductPrice;

    protected String barcode;
    protected String name;
    protected BigDecimal basicCost;
    protected BigDecimal lastCost;
    protected String description;
    protected Integer productLineId;
    protected Short stockTypeId;
    protected Integer uomCategoryId;

    @PostConstruct
    public void init() {
        managedSupplier = new ManagedSupplier();
        productLineSearcher = new ProductLineSearcher();
        managedProductPrice = new ManagedProductPrice();
        uomSearcher = new UoMSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        managedSupplier.updateSearch();
        productLineSearcher.update();
        uomSearcher.update();
    }

    @Override
    public boolean save() {
        if (mainService.existBarCode(barcode, selected.getId())) {
            PNotifyMessage.errorMessage("El código de barras \"" + barcode + "\" ya esta registrado");
            saved = false;
        } else {
            if (managedProductPrice.quantity != null && managedProductPrice.price != null) {
                for (int i = 0; i < managedProductPrice.data.size(); i++) {
                    if (managedProductPrice.quantity == ((Integer) managedProductPrice.data.get(i)[0]).intValue()) {
                        PNotifyMessage.errorMessage("Esta combinacion de valores ya estan definidas");
                        saved = false;
                        return saved;
                    }
                }
                managedProductPrice.data.add(new Object[]{
                    managedProductPrice.quantity,
                    managedProductPrice.price
                });
            } else if (managedProductPrice.quantity != null || managedProductPrice.price != null) {
                if (managedProductPrice.quantity == null) {
                    PNotifyMessage.errorMessage("Precio de venta vacio");
                    saved = false;
                }
                if (managedProductPrice.price == null) {
                    PNotifyMessage.errorMessage("Cantidad de venta vacio");
                    saved = false;
                }
            }
            String content = getSelected().getId() != null ? "Se ha actualizado los datos" : "Se ha creado un nuevo producto";
            saved = super.save(); 
            productSalePriceService.deleteByCompanyProduct(sessionBean.getCurrentCompany(), selected);
            try {
                managedProductPrice.getData().stream().forEach(item -> {
                    productSalePriceService.saveForGroup((BigDecimal) item[1],(Integer) item[0],sessionBean.getCurrentCompany(),selected,sessionBean.getCurrentUser());
                });
                PNotifyMessage.saveMessage(content);
            } catch (Exception ex) {
                PNotifyMessage.systemError(ex, sessionBean);
                saved = false;
            }
        }
        return saved;
    }

    @Override
    protected void fillFields() {
        barcode = selected.getBarcode();
        description = selected.getDescription();
        name = selected.getName();
        try {
            productLineId = selected.getProductLine().getId();
        } catch (NullPointerException npe) {
            productLineId = null;
        }
        try {
            stockTypeId = stockTypeService.getIdByCode("01");
        } catch (NullPointerException npe) {
            stockTypeId = null;
        }
        try {
            uomCategoryId = uomService.getIdByCode("NIU");
        } catch (NullPointerException npe) {
            uomCategoryId = null;
        }
        if (selected.getId() != null) {
            basicCost = productCostPriceService.getCostByCompanyProduct(sessionBean.getCurrentCompany(), selected);
            lastCost = productCostPriceService.getLastAcquisitionCost(sessionBean.getCurrentCompany(), selected);
        } else {
            lastCost = BigDecimal.ZERO.setScale(2);
            basicCost = BigDecimal.ZERO;
        }
        managedSupplier.update();
        onLoad(true);

        managedProductPrice.update();
        createAgain = selected.getId() == null;
    }

    @Override
    protected void clearFields() {
    }

    @Override
    protected void fillSelected() {
        selected.setBarcode(barcode);
        selected.setDescription(description);
        selected.setName(name);
        if (productLineId != null) {
            selected.setProductLine(new ProductLine(productLineId));
        } else {
            selected.setProductLine(null);
        }
        if (stockTypeId != null) {
            selected.setStockType(new StockType(stockTypeId));
        } else {
            selected.setStockType(null);
        }
        if (uomCategoryId != null) {
            selected.setUom(new UoM(uomCategoryId));
        } else {
            selected.setUom(null);
        }
        Auditory.make(selected, sessionBean.getCurrentUser());
    }
    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">

    /**
     * @return the mainService
     */
    public IProductService getMainService() {
        return mainService;
    }

    /**
     * @param mainService the mainService to set
     */
    public void setMainService(IProductService mainService) {
        this.mainService = mainService;
    }

    /**
     * @return the stockTypeService
     */
    public IStockTypeService getStockTypeService() {
        return stockTypeService;
    }

    /**
     * @param stockTypeService the stockTypeService to set
     */
    public void setStockTypeService(IStockTypeService stockTypeService) {
        this.stockTypeService = stockTypeService;
    }

    /**
     * @return the productLineService
     */
    public IProductLineService getProductLineService() {
        return productLineService;
    }

    /**
     * @param productLineService the productLineService to set
     */
    public void setProductLineService(IProductLineService productLineService) {
        this.productLineService = productLineService;
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
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * @param barcode the barcode to set
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
     * @return the basicCost
     */
    public BigDecimal getBasicCost() {
        return basicCost;
    }

    /**
     * @param basicCost the basicCost to set
     */
    public void setBasicCost(BigDecimal basicCost) {
        this.basicCost = basicCost;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the productLineId
     */
    public Integer getProductLineId() {
        return productLineId;
    }

    /**
     * @param productLineId the productLineId to set
     */
    public void setProductLineId(Integer productLineId) {
        this.productLineId = productLineId;
    }

    /**
     * @return the stockTypeId
     */
    public Short getStockTypeId() {
        return stockTypeId;
    }

    /**
     * @param stockTypeId the stockTypeId to set
     */
    public void setStockTypeId(Short stockTypeId) {
        this.stockTypeId = stockTypeId;
    }

    /**
     * @return the uomCategoryId
     */
    public Integer getUomCategoryId() {
        return uomCategoryId;
    }

    /**
     * @param uomCategoryId the uomCategoryId to set
     */
    public void setUomCategoryId(Integer uomCategoryId) {
        this.uomCategoryId = uomCategoryId;
    }

    /**
     * @return the companyService
     */
    public ICompanyService getCompanyService() {
        return companyService;
    }

    /**
     * @param companyService the companyService to set
     */
    public void setCompanyService(ICompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * @return the productLineSearcher
     */
    public ProductLineSearcher getProductLineSearcher() {
        return productLineSearcher;
    }

    /**
     * @param productLineSearcher the productLineSearcher to set
     */
    public void setProductLineSearcher(ProductLineSearcher productLineSearcher) {
        this.productLineSearcher = productLineSearcher;
    }

    /**
     * @return the uomService
     */
    public IUoMService getUomService() {
        return uomService;
    }

    /**
     * @param uomService the uomService to set
     */
    public void setUomService(IUoMService uomService) {
        this.uomService = uomService;
    }

    /**
     * @return the productSalePriceService
     */
    public IProductSalePriceService getProductSalePriceService() {
        return productSalePriceService;
    }

    /**
     * @param productSalePriceService the productSalePriceService to set
     */
    public void setProductSalePriceService(IProductSalePriceService productSalePriceService) {
        this.productSalePriceService = productSalePriceService;
    }

    /**
     * @return the uomSearcher
     */
    public UoMSearcher getUomSearcher() {
        return uomSearcher;
    }

    /**
     * @param uomSearcher the uomSearcher to set
     */
    public void setUomSearcher(UoMSearcher uomSearcher) {
        this.uomSearcher = uomSearcher;
    }

    /**
     * @return the managedProductPrice
     */
    public ManagedProductPrice getManagedProductPrice() {
        return managedProductPrice;
    }

    /**
     * @param managedProductPrice the managedProductPrice to set
     */
    public void setManagedProductPrice(ManagedProductPrice managedProductPrice) {
        this.managedProductPrice = managedProductPrice;
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
     * @return the managedSupplier
     */
    public ManagedSupplier getManagedSupplier() {
        return managedSupplier;
    }

    /**
     * @param managedSupplier the managedSupplier to set
     */
    public void setManagedSupplier(ManagedSupplier managedSupplier) {
        this.managedSupplier = managedSupplier;
    }

    /**
     * @return the managedProductLineBean
     */
    public ManagedProductLineBean getManagedProductLineBean() {
        return managedProductLineBean;
    }

    /**
     * @param managedProductLineBean the managedProductLineBean to set
     */
    public void setManagedProductLineBean(ManagedProductLineBean managedProductLineBean) {
        this.managedProductLineBean = managedProductLineBean;
    }

    /**
     * @return the managedUoMBean
     */
    public ManagedUoMBean getManagedUoMBean() {
        return managedUoMBean;
    }

    /**
     * @param managedUoMBean the managedUoMBean to set
     */
    public void setManagedUoMBean(ManagedUoMBean managedUoMBean) {
        this.managedUoMBean = managedUoMBean;
    }

    /**
     * @return the purchaseService
     */
    public IPurchaseService getPurchaseService() {
        return purchaseService;
    }

    /**
     * @param purchaseService the purchaseService to set
     */
    public void setPurchaseService(IPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /**
     * @return the lastCost
     */
    public BigDecimal getLastCost() {
        return lastCost;
    }

    /**
     * @param lastCost the lastCost to set
     */
    public void setLastCost(BigDecimal lastCost) {
        this.lastCost = lastCost;
    }

    /**
     * @return the productCostPriceService
     */
    public IProductCostPriceService getProductCostPriceService() {
        return productCostPriceService;
    }

    /**
     * @param productCostPriceService the productCostPriceService to set
     */
    public void setProductCostPriceService(IProductCostPriceService productCostPriceService) {
        this.productCostPriceService = productCostPriceService;
    }
    
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="StockTypeSearcher">
    public class StockTypeSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = getStockTypeService().listHQL("SELECT st.id,st.name FROM StockType st where st.active = true");
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
    //<editor-fold defaultstate="collapsed" desc="ProductLineSearcher">

    public class ProductLineSearcher implements java.io.Serializable {

        private List<Object[]> data;
        private boolean valid;

        public void update() {
            data = productLineService.getBasicData();
        }

        public List<Object[]> getData() {
            return data;
        }

        public void initManaged() {
            valid = false;
            getManagedProductLineBean().create();
        }

        public void save() {
            valid = getManagedProductLineBean().save();
            if (valid) {
                update();
            }
            setProductLineId(getManagedProductLineBean().getSelected().getId());
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }

        /**
         * @return the valid
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * @param valid the valid to set
         */
        public void setValid(boolean valid) {
            this.valid = valid;
        }

    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="UoMCategoryearcher">
    public class UoMSearcher implements java.io.Serializable {

        private List<Object[]> data;
        private boolean valid;

        public void update() {
            data = uomService.getBasicData();
        }

        public void change() {
            getManagedProductPrice().update();
            getUomSearcher().update();
        }

        public void initManaged() {
            valid = false;
            getManagedUoMBean().create();
        }

        public void save() {
            valid = getManagedUoMBean().save();
            if (valid) {
                update();
            }
            setUomCategoryId(getManagedUoMBean().getSelected().getId());
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }

        public List<Object[]> getData() {
            return data;
        }

        /**
         * @return the valid
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * @param valid the valid to set
         */
        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="ManagedSupplier">
    public class ManagedSupplier implements java.io.Serializable {

        private List<Object[]> data;
        private List<Object[]> searchData;
        private Long selectedSupplier;

        public void update() {
            data = new ArrayList<>();
            if (selected.getId() != null) {
                data.addAll(getMainService().getSuppliersBasicData(selected.getId()));
            }
        }

        public void updateSearch() {
            ProjectionList projectionList = Projections.projectionList()
                    .add(Projections.id())
                    .add(Projections.property("idd.abbr"))
                    .add(Projections.property("identityNumber"))
                    .add(Projections.property("name"));
            AliasList aliasList = new AliasList();
            aliasList.add("identityDocument", "idd");
            CriterionList criterionList = new CriterionList()
                    ._add(Restrictions.eq("active", true))
                    ._add(Restrictions.eq("supplier", true));
            data.forEach(item -> {
                criterionList.add(Restrictions.ne("id", item[0]));
            });
            OrderList orderList = new OrderList();
            orderList.add(Order.asc("name"));
            searchData = getActorService().addRestrictionsVariant(projectionList, criterionList, aliasList, orderList);
        }

        public void addItem() {
            for (int i = 0; i < searchData.size(); i++) {
                if (((Long) searchData.get(i)[0]).longValue() == selectedSupplier) {
                    data.add(searchData.get(i));
                    searchData.remove(i);
                    selectedSupplier = null;
                    return;
                }
            }
            selectedSupplier = null;
        }

        public void removedItem(int index) {
            searchData.add(data.get(index));
            data.remove(index);
        }

        public List<Object[]> getSearchData() {
            return searchData;
        }

        public List<Object[]> getData() {
            return data;
        }

        /**
         * @param data the data to set
         */
        public void setData(List<Object[]> data) {
            this.data = data;
        }

        /**
         * @param searchData the searchData to set
         */
        public void setSearchData(List<Object[]> searchData) {
            this.searchData = searchData;
        }

        /**
         * @return the selectedSupplier
         */
        public Long getSelectedSupplier() {
            return selectedSupplier;
        }

        /**
         * @param selectedSupplier the selectedSupplier to set
         */
        public void setSelectedSupplier(Long selectedSupplier) {
            this.selectedSupplier = selectedSupplier;
        }
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="ManagedProductPrice">

    public class ManagedProductPrice implements java.io.Serializable {

        private Integer selectedCompanyId;
        private List<Object[]> data;
        private Integer quantity;
        private BigDecimal price;

        public void update() {
            selectedCompanyId = getSessionBean().getCurrentCompany().getId();
            data = new ArrayList();
            if (selected.getId() != null) {
                data.addAll(productSalePriceService.getBasicDataByCompanyProduct(sessionBean.getCurrentCompany(), selected));
            }
        }

        public void addItem() {
            List<Object[]> list = data;
            if (quantity == null || price == null) {
                PNotifyMessage.errorMessage("Falta rellenar algunos campos");
                return;
            }
            for (Object[] item : list) {
                if (quantity.intValue() == (Integer) item[0]) {
                    PNotifyMessage.errorMessage("Esto ya esta declarado");
                    return;
                }
            }
            list.add(new Object[]{quantity, price});
            quantity = null;
            price = null;
        }

        public void remove(int index) {
            data.remove(index);
        }

        public void updateItem() {
            Integer updateIndex = Integer.parseInt(BeanUtil.getParameter("index"));
            String updateLastValue = BeanUtil.getParameter("last_value");
            List<Object[]> list = data;
            Object[] updatedItem = list.get(updateIndex);
            for (int i = 0; i < list.size(); i++) {
                if (updateIndex == i) {
                    continue;
                }
                if ((Integer) updatedItem[0] == ((Integer) list.get(i)[0]).intValue()) {
                    PNotifyMessage.errorMessage("Esta combinacion de valores ya estan definidas");
                    String[] values = updateLastValue.split(",");
                    updatedItem[0] = new Integer(values[0]);
                    updatedItem[1] = new BigDecimal(values[1]).setScale(2, RoundingMode.HALF_UP);
                    return;
                }
            }
        }

        /**
         * @return the selectedCompanyId
         */
        public Integer getSelectedCompanyId() {
            return selectedCompanyId;
        }

        /**
         * @param selectedCompanyId the selectedCompanyId to set
         */
        public void setSelectedCompanyId(Integer selectedCompanyId) {
            this.selectedCompanyId = selectedCompanyId;
        }

        /**
         * @return the quantity
         */
        public Integer getQuantity() {
            return quantity;
        }

        /**
         * @param quantity the quantity to set
         */
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        /**
         * @return the price
         */
        public BigDecimal getPrice() {
            return price;
        }

        /**
         * @param price the price to set
         */
        public void setPrice(BigDecimal price) {
            this.price = price;
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
}
