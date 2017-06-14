/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean;

import cs.bms.model.Actor;
import cs.bms.model.IdentityDocument;
import cs.bms.model.Product;
import cs.bms.model.ProductCostPrice;
import cs.bms.model.ProductSalePrice;
import cs.bms.model.Seller;
import cs.bms.model.Stock;
import cs.bms.model.UoM;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IProductCostPriceService;
import cs.bms.service.interfac.IProductLineService;
import cs.bms.service.interfac.IProductSalePriceService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.ISellerService;
import cs.bms.service.interfac.IStockService;
import cs.bms.service.interfac.IStockTypeService;
import cs.bms.service.interfac.IUoMService;
import gkfire.auditory.Auditory;
import gkfire.hibernate.AliasList;
import gkfire.hibernate.CriterionList;
import gkfire.hibernate.OrderFactory;
import gkfire.hibernate.OrderList;
import gkfire.util.ImportUtils;
import gkfire.web.bean.ABasicBean;
import gkfire.web.util.AbstractImport;
import gkfire.web.util.BeanUtil;
import gkfire.web.util.Pagination;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.postgresql.util.PSQLException;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class ProductBean extends ABasicBean<Long> {

    @ManagedProperty(value = "#{sessionBean}")
    protected SessionBean sessionBean;
    @ManagedProperty(value = "#{productService}")
    protected IProductService productService;
    @ManagedProperty(value = "#{productSalePriceService}")
    protected IProductSalePriceService productSalePriceService;
    @ManagedProperty(value = "#{productCostPriceService}")
    protected IProductCostPriceService productCostPriceService;
    @ManagedProperty(value = "#{uomService}")
    protected IUoMService uomService;
    @ManagedProperty(value = "#{stockTypeService}")
    protected IStockTypeService stockTypeService;
    @ManagedProperty(value = "#{stockService}")
    protected IStockService stockService;
    @ManagedProperty(value = "#{productLineService}")
    protected IProductLineService productLineService;
    @ManagedProperty(value = "#{sellerService}")
    protected ISellerService sellerService;
    @ManagedProperty(value = "#{actorService}")
    protected IActorService actorService;
    @ManagedProperty(value = "#{identityDocumentService}")
    protected IIdentityDocumentService identityDocumentService;

    protected AbstractImport disabledImport;
    protected AbstractImport priceImport;
    protected AbstractImport supplierAssigmentImport;

    protected StockTypeSearcher stockTypeSearcher;
    protected ProductLineSearcher productLineSearcher;
    protected Product selected;
    protected Map<String, Object> otherData;

    protected Short stockTypeId;
    protected Integer productLineId;
    protected String barcode;
    protected String name;

    @PostConstruct
    public void init() {
        pagination = new Pagination<>(productService);
        orderFactory = new OrderFactory(new OrderList());
        orderFactory.setDefaultOrder(Order.desc("id"));
        stockTypeSearcher = new StockTypeSearcher();
        productLineSearcher = new ProductLineSearcher();
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        stockTypeSearcher.update();
        productLineSearcher.update();
        refresh();
    }

    @Override
    protected void clearFields() {
        barcode = "";
        name = "";
        stockTypeId = null;
        productLineId = null;
    }

    @Override
    protected void initImport() {
        //<editor-fold defaultstate="collapsed" desc="Initialize supplierAssigmentImport">
        supplierAssigmentImport = new AbstractImport() {
            @Override
            public void beginThread(int rowBegin, int trama) {
                for (int i = rowBegin; i < rowBegin + trama; i++) {
                    if (i > totalRecords) {
                        break;
                    }
                    Object[] o = null;
                    try {
                        o = ImportUtils.readRow(fileObject, i, 5);
                    } catch (Exception e) {
                        addError(i, " Contenido no legible :  " + e.getMessage());
                        continue;
                    }

                    if (o[1] == null) {
                        addError(i, "NOMBRE  :  NO SE HA COLOCADO EL NOMBRE");
                        continue;
                    }

                    String barcode = null;
                    try {
                        if (o[0] == null) {
                            barcode = null;
                        } else if (o[0] instanceof Number) {
                            barcode = String.valueOf(((Double) o[0]).longValue());
                        } else if (o[0] instanceof String) {
                            if (NumberUtils.isDigits((String) o[0])) {
                                barcode = (String) o[0];
                            } else {
                                throw new Exception();
                            }
                        } else {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        addError(i, "CODIGO DE BARRAS  :  CODIGO INVALIDO");
                        continue;
                    }

                    String name = null;
                    try {
                        name = (String) o[1];
                    } catch (ClassCastException exception) {
                        addError(i, "NOMBRE  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }
                    String identityNumberSupplier = null;
                    try {
                        if (o[4] == null) {
                            addError(i, "IDENTIFICACION PROV  :  CAMPO VACIO");
                            continue;
                        } else if (o[4] instanceof Number) {
                            identityNumberSupplier = String.valueOf(((Double) o[4]).longValue());
                        } else if (o[4] instanceof String) {
                            if (NumberUtils.isDigits((String) o[4])) {
                                identityNumberSupplier = (String) o[4];
                            } else {
                                throw new ClassCastException();
                            }
                        } else {
                            throw new ClassCastException();
                        }
                        if (identityNumberSupplier.length() != 8 && identityNumberSupplier.length() != 11) {
                            addError(i, "IDENTIFICACION PROV  :  DEBE SER UN DNI/RUC");
                            continue;
                        }
                    } catch (ClassCastException exception) {
                        addError(i, "IDENTIFICACION PROV  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }
                    String supplierName = null;
                    try {
                        if (o[3] == null) {
                            addError(i, "NOMBRE PROV :  CAMPO VACIO");
                            continue;
                        } else {
                            supplierName = (String) o[3];
                        }
                    } catch (ClassCastException exception) {
                        addError(i, "NOMBRE PROV  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }
                    String selleDesc = null;
                    try {
                        selleDesc = (String) o[2];
                    } catch (ClassCastException exception) {
                        addError(i, "NOMBRE VENDEDOR  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }

                    Product product = null;
                    if (barcode != null) {
                        product = productService.getByBarcode(barcode);
                    }
                    if (product == null) {
                        product = new Product();
                        product.setStockType(stockTypeService.getByCode("01"));
                        product.setUom(new UoM(uomService.getIdByCode("NIU")));
                    }
                    product.setBarcode(barcode);
                    product.setName(name);
                    product.setSellers(new ArrayList());
                    Actor actor = actorService.getByIdentityNumber(identityNumberSupplier);
                    if (actor == null) {
                        actor = new Actor();
                        actor.setIdentityNumber(identityNumberSupplier);
                        actor.setIdentityDocument(new IdentityDocument(identityDocumentService.getIdByLength(identityNumberSupplier.length())));
                        actor.setName(supplierName);
                        Auditory.make(actor, getSessionBean().getCurrentUser());
                        actorService.saveOrUpdate(actor);
                    }
                    List<Long> sellersId = sellerService.getIdsByProduct(product);
                    for (Long id : sellersId) {
                        product.getSellers().add(new Seller(id));
                    }
                    Seller seller;
                    if (selleDesc != null) {
                        seller = sellerService.getByDescSupplier(selleDesc, actor);
                        if (seller == null) {
                            seller = new Seller();
                            seller.setDefault_(false);
                            seller.setDescription(selleDesc.toUpperCase());
                            seller.setSupplier(actor);
                            Auditory.make(seller, sessionBean.getCurrentUser());
                            sellerService.saveOrUpdate(seller);
                        }
                    } else {
                        seller = sellerService.getByDefaulSupplier(actor);
                        if (seller == null) {
                            seller = new Seller();
                            seller.setDefault_(true);
                            seller.setDescription("-");
                            seller.setSupplier(actor);
                            Auditory.make(seller, sessionBean.getCurrentUser());
                            sellerService.saveOrUpdate(seller);
                        }
                    }
                    if (product.getSellers().contains(seller)) {
                        addError(i, " Error en guardado : Vendedor ya contenido");
                        continue;
                    }
                    product.getSellers().add(seller);

                    Auditory.make(product, getSessionBean().getCurrentUser());

                    try {
                        getProductService().saveOrUpdate(product);
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
        //<editor-fold defaultstate="collapsed" desc="Initialize disabledImport">
        disabledImport = new AbstractImport() {
            @Override
            public void beginThread(int rowBegin, int trama) {
                for (int i = rowBegin; i < rowBegin + trama; i++) {
                    if (i > totalRecords) {
                        break;
                    }
                    Object[] o = null;
                    try {
                        o = ImportUtils.readRow(fileObject, i, 3);
                    } catch (Exception e) {
                        addError(i, " Contenido no legible :  " + e.getMessage());
                        continue;
                    }

                    if (o[1] == null) {
                        addError(i, "NOMBRE  :  NO SE HA COLOCADO EL NOMBRE");
                        continue;
                    }
                    if (o[2] == null) {
                        addError(i, "UNIDAD  :  NO SE HA COLOCADO EL CODIGO DE UNIDAD");
                        continue;
                    }

                    String barcode = null;
                    try {
                        if (o[0] == null) {
                            barcode = null;
                        } else if (o[0] instanceof Number) {
                            barcode = String.valueOf(((Double) o[0]).longValue());
                        } else if (o[0] instanceof String) {
                            if (NumberUtils.isDigits((String) o[0])) {
                                barcode = (String) o[0];
                            } else {
                                throw new Exception();
                            }
                        } else {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        addError(i, "CODIGO DE BARRAS  :  CODIGO INVALIDO");
                        continue;
                    }

                    String name = null;
                    try {
                        name = (String) o[1];
                    } catch (ClassCastException exception) {
                        addError(i, "NOMBRE  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }

                    UoM uom = null;
                    try {
                        String codeUnit = (String) o[2];
                        Integer unitId = uomService.getIdByCode(codeUnit);
                        if (unitId == null) {
                            addError(i, "UNIDAD DE MEDIDA  :  EL CODIGO DE UNIDAD NO EXISTE");
                            continue;
                        }
                        uom = new UoM(unitId);
                    } catch (Exception exception) {
                        addError(i, "UNIDAD DE MEDIDA  :  FORMATO DE CODIGO INVALIDO");
                        continue;
                    }

                    Product product = null;
                    if (barcode != null) {
                        product = productService.getByBarcode(barcode);
                    }
                    if (product == null) {
                        product = new Product();
                        product.setBarcode(barcode);
                        product.setName(name);
                        product.setStockType(stockTypeService.getByCode("01"));
                        product.setUom(uom);
                    } else {
                        product.setBarcode(barcode);
                        product.setName(name);
                        product.setStockType(stockTypeService.getByCode("01"));
                        product.setUom(uom);
                    }
                    product.setActive(false);
                    Auditory.make(product, getSessionBean().getCurrentUser());

                    try {
                        getProductService().saveOrUpdate(product);
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
        //<editor-fold defaultstate="collapsed" desc="Initialize import_">
        import_ = new AbstractImport() {
            @Override
            public void beginThread(int rowBegin, int trama) {
                for (int i = rowBegin; i < rowBegin + trama; i++) {
                    if (i > totalRecords) {
                        break;
                    }
                    Object[] o = null;
                    try {
                        o = ImportUtils.readRow(fileObject, i, 5);
                    } catch (Exception e) {
                        addError(i, " Contenido no legible :  " + e.getMessage());
                        continue;
                    }

                    if (o[1] == null) {
                        addError(i, "NOMBRE  :  NO SE HA COLOCADO EL NOMBRE");
                        continue;
                    }
                    if (o[2] == null) {
                        addError(i, "UNIDAD  :  NO SE HA COLOCADO EL CODIGO DE UNIDAD");
                        continue;
                    }

                    String barcode = null;
                    try {
                        if (o[0] == null) {
                            barcode = null;
                        } else if (o[0] instanceof Number) {
                            barcode = String.valueOf(((Double) o[0]).longValue());
                        } else if (o[0] instanceof String) {
                            if (NumberUtils.isDigits((String) o[0])) {
                                barcode = (String) o[0];
                            } else {
                                throw new Exception();
                            }
                        } else {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        addError(i, "CODIGO DE BARRAS  :  CODIGO INVALIDO");
                        continue;
                    }

                    String name = null;
                    try {
                        name = (String) o[1];
                    } catch (ClassCastException exception) {
                        addError(i, "NOMBRE  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }

                    UoM uom = null;
                    try {
                        String codeUnit = (String) o[2];
                        Integer unitId = uomService.getIdByCode(codeUnit);
                        if (unitId == null) {
                            addError(i, "UNIDAD DE MEDIDA  :  EL CODIGO DE UNIDAD NO EXISTE");
                            continue;
                        }
                        uom = new UoM(unitId);
                    } catch (Exception exception) {
                        addError(i, "UNIDAD DE MEDIDA  :  FORMATO DE CODIGO INVALIDO");
                        continue;
                    }

                    BigDecimal stockQuantity;
                    try {
                        if (o[3] == null) {
                            o[3] = "0";
                        }
                        stockQuantity = new BigDecimal(o[3].toString(), new MathContext(2, RoundingMode.HALF_UP));
                    } catch (ClassCastException exception) {
                        addError(i, "COSTO BASICO :  FORMATO DE COSTO INVALIDO");
                        continue;
                    }

                    BigDecimal cost = null;
                    try {
                        cost = new BigDecimal((Double) o[4]).setScale(4, RoundingMode.HALF_UP);
                    } catch (ClassCastException exception) {
                        addError(i, "COSTO BASICO :  FORMATO DE COSTO INVALIDO");
                        continue;
                    }

                    Product product = null;
                    if (barcode != null) {
                        product = productService.getByBarcode(barcode);
                    }
                    if (product == null) {
                        product = new Product();
                        product.setBarcode(barcode);
                        product.setName(name);
                        product.setStockType(stockTypeService.getByCode("01"));
                        product.setUom(uom);
                        Auditory.make(product, getSessionBean().getCurrentUser());
                    } else {
                        product.setBarcode(barcode);
                        product.setName(name);
                        product.setStockType(stockTypeService.getByCode("01"));
                        product.setUom(uom);
                    }

                    ProductCostPrice productCostPrice = new ProductCostPrice();
                    productCostPrice.setId(productCostPriceService.getIdByCompanyProduct(sessionBean.getCurrentCompany(), product));
                    productCostPrice.setCompany(sessionBean.getCurrentCompany());
                    productCostPrice.setCost(cost);
                    productCostPrice.setProduct(product);
                    
                    Auditory.make(product, getSessionBean().getCurrentUser());

                    try {
                        getProductService().saveOrUpdate(product);
                        addSaved(i, "Exito");
                        productSalePriceService.deleteByCompanyProduct(sessionBean.getCurrentCompany(), product);
                        Stock stock = new Stock(stockService.getIdByCompanyProduct(sessionBean.getCurrentCompany(), product));
                        stock.setProduct(product);
                        stock.setQuantity(stockQuantity.intValue() < 0 ? BigDecimal.ZERO : stockQuantity);
                        stock.setCompany(sessionBean.getCurrentCompany());
                        stockService.saveOrUpdate(stock);
                        productCostPriceService.saveOrUpdate(productCostPrice);
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
        //<editor-fold defaultstate="collapsed" desc="Initialize priceImport">
        priceImport = new AbstractImport() {
            @Override
            public void beginThread(int rowBegin, int trama) {
                for (int i = rowBegin; i < rowBegin + trama; i++) {
                    if (i > totalRecords) {
                        break;
                    }
                    Object[] o = null;
                    try {
                        o = ImportUtils.readRow(fileObject, i, 10);
                    } catch (Exception e) {
                        addError(i, " Contenido no legible :  " + e.getMessage());
                        continue;
                    }

                    if (o[1] == null) {
                        addError(i, "NOMBRE  :  NO SE HA COLOCADO EL NOMBRE");
                        continue;
                    }

                    String barcode = null;
                    try {
                        if (o[0] == null) {
                            barcode = null;
                        } else if (o[0] instanceof Number) {
                            barcode = String.valueOf(((Double) o[0]).longValue());
                        } else if (o[0] instanceof String) {
                            if (NumberUtils.isDigits((String) o[0])) {
                                barcode = (String) o[0];
                            } else {
                                throw new Exception();
                            }
                        } else {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        addError(i, "CODIGO DE BARRAS  :  CODIGO INVALIDO");
                        continue;
                    }

                    String name = null;
                    try {
                        name = (String) o[1];
                    } catch (ClassCastException exception) {
                        addError(i, "NOMBRE  :  FORMATO DE CAMPO INCORRECTO, DEBE SER TEXTO");
                        continue;
                    }

                    Product product = null;
                    if (barcode != null) {
                        product = productService.getByBarcode(barcode);
                    }
                    if (product == null) {
                        product = new Product();
                        product.setBarcode(barcode);
                        product.setName(name);
                        product.setStockType(stockTypeService.getByCode("01"));
                        product.setUom(new UoM(uomService.getIdByCode("NIU")));
                        Auditory.make(product, getSessionBean().getCurrentUser());
                    } else {
                        product.setBarcode(barcode);
                        product.setName(name);
                    }

                    List<ProductSalePrice> productSalePrices = new ArrayList<>();
                    try {
                        makeProductSalePrice(o[2], o[3], product, productSalePrices);
                        makeProductSalePrice(o[4], o[5], product, productSalePrices);
                        makeProductSalePrice(o[6], o[7], product, productSalePrices);
                        makeProductSalePrice(o[8], o[9], product, productSalePrices);
                    } catch (Exception e) {
                        addError(i, e.getMessage());
                        continue;
                    }
                    Auditory.make(product, getSessionBean().getCurrentUser());

                    try {
                        getProductService().saveOrUpdate(product);
                        productSalePriceService.deleteByCompanyProduct(sessionBean.getCurrentCompany(), product);
                        for (ProductSalePrice psp : productSalePrices) {
                            getProductSalePriceService().saveForGroup(psp.getPrice(), psp.getQuantity(), sessionBean.getCurrentCompany(), product,sessionBean.getCurrentUser());
                        }
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

            private void makeProductSalePrice(Object o1, Object o2, Product product, List<ProductSalePrice> list) throws Exception {
                if (o1 == null || o2 == null) {
                    return;
                }
                if ((o1 == null && o2 != null) || (o1 != null && o2 == null)) {
                    throw new Exception("PRECIO Y CANTIDAD :  UNO DE ESTOS CAMPOS ESTA VACIO");
                }
                Integer quantity;
                BigDecimal price;
                try {
                    quantity = new Double(o1.toString()).intValue();
                } catch (ClassCastException e) {
                    throw new Exception("CANTIDAD  : FORMATO INVALIDO");
                } catch (NumberFormatException e) {
                    throw new Exception("CANTIDAD  : FORMATO INVALIDO");
                }

                if (quantity == 0) {
                    return;
                }
                try {
                    price = new BigDecimal(o2.toString()).setScale(2, RoundingMode.HALF_UP);
                } catch (ClassCastException e) {
                    throw new Exception("PRECIO  : FORMATO INVALIDO");
                }
                if (price.doubleValue() <= 0) {
                    return;
                }

                ProductSalePrice psp = new ProductSalePrice();
                for (ProductSalePrice productSalePrice : list) {
                    if (productSalePrice.getQuantity().intValue() == quantity) {
                        productSalePrice.setQuantity(quantity);
                        productSalePrice.setPrice(price);
                        return;
                    }
                }
                psp.setCompany(sessionBean.getCurrentCompany());
                psp.setQuantity(quantity);
                psp.setPrice(price);
                psp.setProduct(product);
                list.add(psp);
            }
        };
        //</editor-fold>
    }

    public void load() {
        selected = productService.getById(id);
        otherData = new HashMap();
        Object[] auditory = productService.getAuditoryData(id);
        otherData.put("createUser", auditory[0]);
        otherData.put("editUser", auditory[2]);
        List<Object[]> salePrices = productSalePriceService.getBasicDataByCompanyProduct(sessionBean.getCurrentCompany(), selected);
        otherData.put("salePrices", salePrices);
        BigDecimal cost = productCostPriceService.getCostByCompanyProduct(sessionBean.getCurrentCompany(), selected);
        otherData.put("basicCost", cost);
        BigDecimal lastCost = productCostPriceService.getLastAcquisitionCost(sessionBean.getCurrentCompany(), selected);
        otherData.put("lastCost", lastCost);
        List<Object[]> sellers = productService.getSellersBasicData(id);
        otherData.put("sellers", sellers);
    }

    @Override
    public void search() {
        barcode = barcode.trim();
        name = name.trim();
        ProjectionList projectionList = Projections.projectionList()
                .add(Projections.id())
                .add(Projections.property("image"))
                .add(Projections.property("barcode"))
                .add(Projections.property("name"))
                .add(Projections.sqlProjection("(SELECT COUNT(*) FROM Kardex k WHERE k.id_product = {alias}.id) as kard", new String[]{"kard"}, new Type[]{LongType.INSTANCE}))
                .add(Projections.property("active"));
        CriterionList criterionList = new CriterionList();
        criterionList.add(Restrictions.eq("active", true));
        AliasList aliasList = new AliasList();
        if (stockTypeId != null) {
            aliasList.add("stockType", "st");
            criterionList.add(Restrictions.eq("st.id", stockTypeId));
        }
        if (productLineId != null) {
            aliasList.add("productLine", "pl");
            criterionList.add(Restrictions.eq("pl.id", productLineId));
        }
        if (barcode.length() != 0) {
            criterionList.add(Restrictions.like("barcode", barcode, MatchMode.ANYWHERE));
        }
        if (name.length() != 0) {
            criterionList.add(Restrictions.like("name", name, MatchMode.ANYWHERE).ignoreCase());
        }
        pagination.search(1, projectionList, criterionList, aliasList, orderFactory.make());
    }
    //<editor-fold defaultstate="collapsed" desc="Gets & Sets">

    /**
     * @return the selected
     */
    public Product getSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(Product selected) {
        this.selected = selected;
    }

    /**
     * @return the otherData
     */
    public Map<String, Object> getOtherData() {
        return otherData;
    }

    /**
     * @param otherData the otherData to set
     */
    public void setOtherData(Map<String, Object> otherData) {
        this.otherData = otherData;
    }

    /**
     * @return the sellerService
     */
    public ISellerService getSellerService() {
        return sellerService;
    }

    /**
     * @param sellerService the sellerService to set
     */
    public void setSellerService(ISellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * @return the supplierAssigmentImport
     */
    public AbstractImport getSupplierAssigmentImport() {
        return supplierAssigmentImport;
    }

    /**
     * @param supplierAssigmentImport the supplierAssigmentImport to set
     */
    public void setSupplierAssigmentImport(AbstractImport supplierAssigmentImport) {
        this.supplierAssigmentImport = supplierAssigmentImport;
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
     * @return the productService
     */
    public IProductService getProductService() {
        return productService;
    }

    /**
     * @param productService the productService to set
     */
    public void setProductService(IProductService productService) {
        this.productService = productService;
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
     * @return the stockTypeSearcher
     */
    public StockTypeSearcher getStockTypeSearcher() {
        return stockTypeSearcher;
    }

    /**
     * @param stockTypeSearcher the stockTypeSearcher to set
     */
    public void setStockTypeSearcher(StockTypeSearcher stockTypeSearcher) {
        this.stockTypeSearcher = stockTypeSearcher;
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

    /**
     * @return the stockService
     */
    public IStockService getStockService() {
        return stockService;
    }

    /**
     * @param stockService the stockService to set
     */
    public void setStockService(IStockService stockService) {
        this.stockService = stockService;
    }

    /**
     * @return the disabledImport
     */
    public AbstractImport getDisabledImport() {
        return disabledImport;
    }

    /**
     * @param disabledImport the disabledImport to set
     */
    public void setDisabledImport(AbstractImport disabledImport) {
        this.disabledImport = disabledImport;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="StockTypeSearcher">
    public class StockTypeSearcher implements java.io.Serializable {

        private List<Object[]> data;

        public void update() {
            data = stockTypeService.getBasicData();
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

        public void update() {
            data = productLineService.getBasicData();
        }

        public List<Object[]> getData() {
            return data;
        }
    }
    //</editor-fold>

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
     * @return the priceImport
     */
    public AbstractImport getPriceImport() {
        return priceImport;
    }

    /**
     * @param priceImport the priceImport to set
     */
    public void setPriceImport(AbstractImport priceImport) {
        this.priceImport = priceImport;
    }

}
