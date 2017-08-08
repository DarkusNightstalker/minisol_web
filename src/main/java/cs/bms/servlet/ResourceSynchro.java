/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.servlet;

import cs.bms.model.Actor;
import cs.bms.model.Company;
import cs.bms.model.IdentityDocument;
import cs.bms.model.InternalStockMovement;
import cs.bms.model.InternalStockMovementDetail;
import cs.bms.model.OperationType;
import cs.bms.model.PaymentProof;
import cs.bms.model.Product;
import cs.bms.model.Purchase;
import cs.bms.model.PurchaseDetail;
import cs.bms.model.PurchasePayment;
import cs.bms.model.Sale;
import cs.bms.model.SaleDetail;
import cs.bms.model.SalePayment;
import cs.bms.model.StockReturnSupplier;
import cs.bms.model.StockReturnSupplierDetail;
import cs.bms.model.Ubigeo;
import cs.bms.model.UoM;
import cs.bms.model.User;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDistrictService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IInternalStockMovementDetailService;
import cs.bms.service.interfac.IInternalStockMovementService;
import cs.bms.service.interfac.IOperationTypeService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IPaymentVoucherService;
import cs.bms.service.interfac.IProductCostPriceService;
import cs.bms.service.interfac.IProductSalePriceService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IPurchaseDetailService;
import cs.bms.service.interfac.IPurchasePaymentService;
import cs.bms.service.interfac.IPurchaseService;
import cs.bms.service.interfac.IRolService;
import cs.bms.service.interfac.ISaleDetailService;
import cs.bms.service.interfac.ISalePaymentService;
import cs.bms.service.interfac.ISaleService;
import cs.bms.service.interfac.IStockReturnSupplierDetailService;
import cs.bms.service.interfac.IStockReturnSupplierService;
import cs.bms.service.interfac.IStockService;
import cs.bms.service.interfac.IStockTypeService;
import cs.bms.service.interfac.IUoMService;
import cs.bms.service.interfac.IUserService;
import cs.bms.util.AESKeys;
import gkfire.util.AES;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Jhoan Brayam
 */
@Path("sincronization")
@javax.enterprise.context.RequestScoped
public class ResourceSynchro implements java.io.Serializable {

    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
    private final static SimpleDateFormat FULL_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS a");
    private final static SimpleDateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

    //<editor-fold defaultstate="collapsed" desc="Services">    
    @Autowired
    @Qualifier("actorService")
    protected IActorService actorService;
    @Autowired
    @Qualifier("districtService")
    protected IDistrictService districtService;
    @Autowired
    @Qualifier("internalStockMovementDetailService")
    protected IInternalStockMovementDetailService internalStockMovementDetailService;
    @Autowired
    @Qualifier("operationTypeService")
    protected IOperationTypeService operationTypeService;
    @Autowired
    @Qualifier("companyService")
    protected ICompanyService companyService;
    @Autowired
    @Qualifier("purchaseService")
    protected IPurchaseService purchaseService;
    @Autowired
    @Qualifier("stockReturnSupplierService")
    protected IStockReturnSupplierService stockReturnSupplierService;
    @Autowired
    @Qualifier("stockReturnSupplierDetailService")
    protected IStockReturnSupplierDetailService stockReturnSupplierDetailService;
    @Autowired
    @Qualifier("productService")
    protected IProductService productService;
    @Autowired
    @Qualifier("purchasePaymentService")
    protected IPurchasePaymentService purchasePaymentService;
    @Autowired
    @Qualifier("saleService")
    protected ISaleService saleService;
    @Autowired
    @Qualifier("saleDetailService")
    protected ISaleDetailService saleDetailService;
    @Autowired
    @Qualifier("purchaseDetailService")
    protected IPurchaseDetailService purchaseDetailService;
    @Autowired
    @Qualifier("salePaymentService")
    protected ISalePaymentService salePaymentService;
    @Autowired
    @Qualifier("productSalePriceService")
    protected IProductSalePriceService productSalePriceService;
    @Autowired
    @Qualifier("productCostPriceService")
    protected IProductCostPriceService productCostPriceService;
    @Autowired
    @Qualifier("paymentProofService")
    protected IPaymentProofService paymentProofService;
    @Autowired
    @Qualifier("identityDocumentService")
    protected IIdentityDocumentService identityDocumentService;
    @Autowired
    @Qualifier("documentNumberingService")
    protected IDocumentNumberingService documentNumberingService;
    @Autowired
    @Qualifier("internalStockMovementService")
    private IInternalStockMovementService internalStockMovementService;
    @Autowired
    @Qualifier("stockService")
    private IStockService stockService;
    @Autowired
    @Qualifier("rolService")
    private IRolService rolService;
    @Autowired
    @Qualifier("uomService")
    private IUoMService uomService;
    @Autowired
    @Qualifier("stockTypeService")
    private IStockTypeService stockTypeService;
    @Autowired
    @Qualifier("userService")
    private IUserService userService;
    @Autowired
    @Qualifier("paymentVoucherService")
    private IPaymentVoucherService paymentVoucherService;
    //</editor-fold>

    @POST
    @Path("fast.dkn")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject searchById(JsonObject jsonInput) throws Exception {

        JsonObjectBuilder allBuilder = Json.createObjectBuilder();
        Date lastUpdate = new Date();
        lastUpdate.setTime(jsonInput.getJsonNumber("companyCode").longValue());

        String company = jsonInput.getString("companyCode");
        //<editor-fold defaultstate="collapsed" desc="Actores">    
        JsonArray current = jsonInput.getJsonArray("actors");
        for (int i = 0; i < current.size(); i++) {
            JsonArray item = current.getJsonArray(i);
            Actor actor = new Actor();
            actor.setId(actorService.getIdByIdentityNumber(item.getString(1)));
            actor.setIdentityDocument(new IdentityDocument(identityDocumentService.getIdByCode(item.getString(0))));
            actor.setIdentityNumber(item.getString(1));
            actor.setName(item.getString(2));
            actor.setOther(item.getString(3));
            actor.setAddress(item.getString(4));
            actor.setCustomer(item.getBoolean(5));
            actor.setSupplier(item.getBoolean(6));
            actor.setPoints(item.getJsonNumber(7).longValue());
            actor.setRepresentative(item.getString(8));
            actor.setSynchronized_(item.getBoolean(9));
            if (item.getJsonNumber(10) != null) {
                actor.setUbigeo(new Ubigeo(districtService.getIdByUbigeo(item.getString(10))));
            }
            actor.setUploaded(true);
            actor.setCreateUser(new User(userService.getIdByUsername(item.getString(11))));
            actor.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(12)));
            actor.setEditUser(new User(1));
            actor.setEditDate(new Date());
            actor.setActive(item.getBoolean(15));
            Long points = saleService.getNotUploadedPointsByIdentityNumber(actor.getIdentityNumber());
            actor.setPoints(actor.getPoints() + points);
            actorService.saveOrUpdate(actor);
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="puntos">
        current = jsonInput.getJsonArray("puntos");
        for (int i = 0; i < jsonInput.size(); i++) {
            JsonObject object = current.getJsonObject(i);
            actorService.addPoints(
                    object.getString("identityNumber"),
                    object.getJsonNumber("points").longValue(),
                    new User(1)
            );
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Vales de consumo">
        current = jsonInput.getJsonArray("pv");
        for (int i = 0; i < current.size(); i++) {
            paymentVoucherService.useVoucher(current.getString(i), new User(1));
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Movimientos internos">
        current = jsonInput.getJsonArray("ism");
        for (int i = 0; i < current.size(); i++) {
            JsonArray item = current.getJsonArray(i);
            InternalStockMovement ism = new InternalStockMovement();

            ism.setTargetCompany(new Company(companyService.getIdByCode(item.getString(0))));
            ism.setSourceCompany(new Company(companyService.getIdByCode(item.getString(1))));
            ism.setPaymentProof(new PaymentProof(paymentProofService.getIdByCode(item.getString(2))));
            ism.setSerie(item.getString(3));
            ism.setDocumentNumber(item.getString(4));
            ism.setId(internalStockMovementService.getIdByFullDocument(ism.getPaymentProof(), ism.getSerie(), ism.getDocumentNumber()));
            if (!item.isNull(5)) {
                ism.setCarrier(new Actor(actorService.getIdByIdentityNumber(item.getString(5))));
            }
            ism.setDateArrival(SHORT_DATE_FORMATTER.parse(item.getString(6)));
            if (!item.isNull(7)) {
                ism.setDateRealArrival(FULL_DATE_FORMATTER.parse(item.getString(7)));
            }
            ism.setDateShipping(SHORT_DATE_FORMATTER.parse(item.getString(8)));
            ism.setDriverLicense(item.getString(9));
            ism.setElectronic(item.getBoolean(10));
            ism.setOperationTypeSource(new OperationType(operationTypeService.getISMOutTypeId()));
            ism.setOperationTypeTarget(new OperationType(operationTypeService.getISMInTypeId()));
            ism.setSent(item.getBoolean(13));
            ism.setTransportDescription(item.getString(14));
            ism.setUploaded(item.getBoolean(15));

            ism.setElectronic(item.getBoolean(16));
            ism.setSent(item.getBoolean(17));

            ism.setCreateUser(new User(userService.getIdByUsername(item.getString(18))));
            ism.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(19)));
            ism.setEditUser(new User(1));
            ism.setEditDate(new Date());
            ism.setActive(item.getBoolean(22));
            ism.setServerExist(true);

            boolean isCreated = ism.getId() == null;
            internalStockMovementService.saveOrUpdate(ism);

            JsonArray jsonDetails = item.getJsonArray(23);
            List<Long> detaillsId = new ArrayList();
            for (int j = 0; j < jsonDetails.size(); j++) {
                JsonArray detail = jsonDetails.getJsonArray(j);
                InternalStockMovementDetail ismd = new InternalStockMovementDetail();
                ismd.setProduct(new Product(productService.getIdByBarcode(detail.getString(0))));
                ismd.setId(internalStockMovementDetailService.getIdByISMProduct(ism, ismd.getProduct()));
                ismd.setUom(new UoM(uomService.getIdByCode(detail.getString(1))));
                ismd.setProductName(detail.getString(2));
                ismd.setUnitCost(detail.getJsonNumber(3).bigDecimalValue());
                ismd.setQuantity(detail.getJsonNumber(4).bigDecimalValue());
                if (!detail.isNull(5)) {
                    ismd.setWeight(detail.getJsonNumber(5).bigDecimalValue());
                }
                if (!detail.isNull(6)) {
                    ismd.setWeightUoM(new UoM(uomService.getIdByCode(detail.getString(6))));
                }
                ismd.setInternalStockMovement(ism);
                internalStockMovementDetailService.saveOrUpdate(ismd);
                if (!isCreated) {
                    detaillsId.add(ismd.getId());
                }
            }
            if (!isCreated) {
                internalStockMovementDetailService.deleteByExcludeIds(ism, detaillsId);
            }
        }        
        //</editor-fold>        
        
        Date currentDateUpdate = new Date();
        //<editor-fold defaultstate="collapsed" desc="Tipos de comprobantes de pago">
        allBuilder.add("create_pp",
                createArrayBuilderFromList(
                        paymentProofService.getCreateByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_pp",
                createArrayBuilderFromList(
                        paymentProofService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Numeración de series">            
        allBuilder.add("dn",
                createArrayBuilderFromList(
                        documentNumberingService.getListData()
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Tipos de identificaciones">
        allBuilder.add("create_idd",
                createArrayBuilderFromList(
                        identityDocumentService.getCreateByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_idd",
                createArrayBuilderFromList(
                        identityDocumentService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Actores">
        allBuilder.add("create_actors",
                createArrayBuilderFromList(
                        actorService.getCreatedByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_actors",
                createArrayBuilderFromList(
                        actorService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Roles">
        allBuilder.add("create_rol",
                createArrayBuilderFromList(
                        rolService.getCreateByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_rol",
                createArrayBuilderFromList(
                        rolService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Sedes">
        allBuilder.add("create_company",
                createArrayBuilderFromList(
                        companyService.getCreatedByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_company",
                createArrayBuilderFromList(
                        companyService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>   
        //<editor-fold defaultstate="collapsed" desc="Usuarios">
        allBuilder.add("create_user",
                createArrayBuilderFromList(
                        userService.getCreateByAfterDate(lastUpdate,currentDateUpdate, company)
                )
        );
        allBuilder.add("edit_user",
                createArrayBuilderFromList(
                        userService.getEditedByAfterDate(lastUpdate,currentDateUpdate, company, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Unidades de Medida">
        allBuilder.add("create_uom",
                createArrayBuilderFromList(
                        uomService.getCreatedByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_uom",
                createArrayBuilderFromList(
                        uomService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Tipos de existencia">
        allBuilder.add("create_stt",
                createArrayBuilderFromList(
                        stockTypeService.getCreatedByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_stt",
                createArrayBuilderFromList(
                        stockTypeService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Productos">
        allBuilder.add("create_product",
                createArrayBuilderFromList(
                        productService.getCreatedByAfterDate(lastUpdate,currentDateUpdate)
                )
        );
        allBuilder.add("edit_product",
                createArrayBuilderFromList(
                        productService.getEditedByAfterDate(lastUpdate,currentDateUpdate, false)
                )
        );
        //</editor-fold>      
        //<editor-fold defaultstate="collapsed" desc="Precios de venta">
        allBuilder.add("psp",
                createArrayBuilderFromList(
                        productSalePriceService.getCreatedByAfterDate(lastUpdate,currentDateUpdate, company)
                )
        );
        //</editor-fold>            
        //<editor-fold defaultstate="collapsed" desc="Costos de productos">
        allBuilder.add("create_pcp",
                createArrayBuilderFromList(
                        productCostPriceService.getCreatedByAfterDate(lastUpdate,currentDateUpdate, company)
                )
        );
        allBuilder.add("edit_pcp",
                createArrayBuilderFromList(
                        productCostPriceService.getEditedByAfterDate(lastUpdate,currentDateUpdate, company, false)
                )
        );
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Existencias">
        allBuilder.add("create_stock",
                createArrayBuilderFromList(
                        stockService.getCreatedByAfterDate(lastUpdate,currentDateUpdate, company)
                )
        );
        allBuilder.add("edit_stock",
                createArrayBuilderFromList(
                        stockService.getEditedByAfterDate(lastUpdate,currentDateUpdate, company, false)
                )
        );
        //</editor-fold>     
        //<editor-fold defaultstate="collapsed" desc="Movimientos Internos">
        allBuilder.add("create_stock",
                createArrayBuilderFromList(
                        internalStockMovementService.getCreatedByAfterDate(lastUpdate,currentDateUpdate, company)
                )
        );
        allBuilder.add("edit_stock",
                createArrayBuilderFromList(
                        internalStockMovementService.getEditedByAfterDate(lastUpdate,currentDateUpdate, company, false)
                )
        );

        //</editor-fold>
        return allBuilder.build();
    }

    @POST
    @Path("verification.dkn")
    @Consumes(MediaType.APPLICATION_JSON)
    public void verification(JsonObject jsonInput) throws Exception {
        Long time = jsonInput.getJsonNumber("time").longValue();
        Date currentDate = new Date();
        double minutes = new Long(currentDate.getTime() - time).doubleValue() / 60000.0;
        if (minutes > 3) {
            Response.status(Response.Status.REQUEST_TIMEOUT);
            return;
        }
        String username = AES.decrypt(jsonInput.getString("username"), AESKeys.SYNCHRO_TRANSFERENCE);
        String password = AES.decrypt(jsonInput.getString("password"), AESKeys.SYNCHRO_TRANSFERENCE);
        if (userService.authorize(username, password, "M_SINC")) {
            Response.status(Response.Status.OK);
        } else {
            Response.status(Response.Status.UNAUTHORIZED);
        }
    }


    @POST
    @Path("/slow.dkn")
    @Consumes(MediaType.APPLICATION_JSON)
    public void uploadSlow(JsonObject jsonInput) throws Exception {
        Long time = jsonInput.getJsonNumber("time").longValue();
        Date currentDate = new Date();
        double minutes = new Long(currentDate.getTime() - time).doubleValue() / 60000.0;
        if (minutes > 3) {
            Response.status(Response.Status.REQUEST_TIMEOUT);
            return;
        }
        String username = AES.decrypt(jsonInput.getString("username"), AESKeys.SYNCHRO_TRANSFERENCE);
        String password = AES.decrypt(jsonInput.getString("password"), AESKeys.SYNCHRO_TRANSFERENCE);
        if (userService.authorize(username, password, "M_SINC")) {
            User user = userService.login(username, password);
            Company company = new Company(companyService.getIdByCode(jsonInput.getString("company")));
            JsonArray jsonActors = jsonInput.getJsonArray("c_actor");
            //<editor-fold defaultstate="collapsed" desc="Actores">    
            if (!jsonActors.isEmpty()) {
                synchronized (this) {
                    for (int i = 0; i < jsonActors.size(); i++) {
                        JsonArray item = jsonActors.getJsonArray(i);
                        if (!actorService.verifyIdentityNumber(item.getString(1), null)) {
                            Actor actor = new Actor();
                            actor.setIdentityDocument(new IdentityDocument(identityDocumentService.getIdByCode(item.getString(0))));
                            actor.setIdentityNumber(item.getString(1));
                            actor.setName(item.getString(2));
                            actor.setOther(item.getString(3));
                            actor.setAddress(item.getString(4));
                            actor.setCustomer(item.getBoolean(5));
                            actor.setSupplier(item.getBoolean(6));
                            actor.setPoints(item.getJsonNumber(7).longValue());
                            actor.setRepresentative(item.getString(8));
                            actor.setSynchronized_(item.getBoolean(9));
                            if (item.getJsonNumber(10) != null) {
                                actor.setUbigeo(new Ubigeo(item.getJsonNumber(10).intValue()));
                            }
                            actor.setUploaded(true);
                            actor.setCreateUser(new User(userService.getIdByUsername(item.getString(11))));
                            actor.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(12)));
                            actor.setEditUser(user);
                            actor.setEditDate(new Date());
                            actorService.saveOrUpdate(actor);
                        } else {
                            actorService.addPoints(actorService.getIdByIdentityNumber(item.getString(1)), item.getJsonNumber(7).longValue(), user);
                        }
                    }
                }
            }
            //</editor-fold>
            JsonArray jsonSales = jsonInput.getJsonArray("sales");
            //<editor-fold defaultstate="collapsed" desc="Ventas">            
            for (int i = 0; i < jsonSales.size(); i++) {
                JsonArray item = jsonSales.getJsonArray(i);
                Sale sale = new Sale();
                sale.setCompany(company);
                sale.setPaymentProof(new PaymentProof(paymentProofService.getIdByCode(item.getString(0))));
                sale.setSerie(item.getString(1));
                sale.setDocumentNumber(item.getString(2));
                sale.setCustomerName(item.getString(3));
                if (item.getString(4) != null) {
                    sale.setCustomer(new Actor(actorService.getIdByIdentityNumber(item.getString(4))));
                }
                sale.setCustomerPoints(item.getJsonNumber(5).longValue());
                sale.setPoints(item.getJsonNumber(6).longValue());
                sale.setSpendPoints(item.getInt(7));
                sale.setCredit(item.getBoolean(8));

                sale.setDateIssue(FULL_DATE_FORMATTER.parse(item.getString(9)));
                if (item.getString(10) != null) {
                    sale.setDateDue(SHORT_DATE_FORMATTER.parse(item.getString(10)));
                }
                sale.setElectronic(item.getBoolean(11));
                sale.setIgv(item.getJsonNumber(12).bigDecimalValue());
                sale.setIgvDiscount(item.getJsonNumber(13).bigDecimalValue());
                sale.setSubtotal(item.getJsonNumber(14).bigDecimalValue());
                sale.setSubtotalDiscount(item.getJsonNumber(15).bigDecimalValue());
                sale.setSent(item.getBoolean(16));
                sale.setVerified(item.getBoolean(17));

                sale.setCreateUser(new User(item.getInt(18)));
                sale.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(19)));
                sale.setEditUser(user);
                sale.setEditDate(new Date());
                sale.setActive(item.getBoolean(20));

                saleService.saveOrUpdate(sale);

                JsonArray jsonDetails = item.getJsonArray(21);
                for (int j = 0; j < jsonDetails.size(); j++) {
                    JsonArray detail = jsonDetails.getJsonArray(j);
                    SaleDetail saleDetail = new SaleDetail();
                    saleDetail.setProduct(new Product(productService.getIdByBarcode(detail.getString(0))));
                    saleDetail.setUom(new UoM(uomService.getIdByCode(detail.getString(1))));
                    saleDetail.setProductName(detail.getString(2));
                    saleDetail.setUnitCost(detail.getJsonNumber(3).bigDecimalValue());
                    saleDetail.setUnitPrice(detail.getJsonNumber(4).bigDecimalValue());
                    saleDetail.setPointsPrice(detail.getJsonNumber(5).bigDecimalValue());
                    saleDetail.setQuantity(detail.getJsonNumber(6).bigDecimalValue());
                    saleDetail.setSale(sale);
                    saleDetail.setSubtotal(detail.getJsonNumber(7).bigDecimalValue());
                    saleDetailService.saveOrUpdate(saleDetail);
                    if (sale.getActive()) {
                        stockService.substractQuantity(
                                saleDetail.getQuantity(),
                                saleDetail.getProduct(),
                                sale.getCompany(),
                                user
                        );
                    }
                }

                JsonArray jsonPayments = item.getJsonArray(22);
                for (int j = 0; j < jsonPayments.size(); j++) {
                    JsonArray payment = jsonPayments.getJsonArray(j);
                    SalePayment salePayment = new SalePayment();
                    salePayment.setDatePayment(FULL_DATE_FORMATTER.parse(payment.getString(0)));
                    salePayment.setQuantity(item.getJsonNumber(1).bigDecimalValue());
                    salePayment.setSale(sale);
                    salePayment.setVisa(item.getBoolean(2));
                    salePaymentService.saveOrUpdate(salePayment);
                }

            }
            //</editor-fold>
            JsonArray jsonPurchases = jsonInput.getJsonArray("purchases");
            //<editor-fold defaultstate="collapsed" desc="Compras">            
            for (int i = 0; i < jsonPurchases.size(); i++) {
                JsonArray item = jsonPurchases.getJsonArray(i);
                Purchase purchase = new Purchase();
                purchase.setCompanyArrival(company);

                purchase.setDateIssue(SHORT_DATE_FORMATTER.parse(item.getString(0)));
                purchase.setDateDue(SHORT_DATE_FORMATTER.parse(item.getString(1)));
                if (!item.isNull(2)) {
                    purchase.setSupplier(new Actor(actorService.getIdByIdentityNumber(item.getString(2))));
                }
                purchase.setSupplierName(item.getString(3));

                purchase.setPaymentProof(new PaymentProof(paymentProofService.getIdByCode(item.getString(4))));
                purchase.setSerie(item.getString(5));
                purchase.setDocumentNumber(item.getString(6));
                purchase.setElectronic(item.getBoolean(7));

                purchase.setIgv(item.getJsonNumber(8).bigDecimalValue());
                purchase.setIgvDiscount(item.getJsonNumber(9).bigDecimalValue());
                purchase.setSubtotal(item.getJsonNumber(10).bigDecimalValue());
                purchase.setSubtotalDiscount(item.getJsonNumber(11).bigDecimalValue());

                purchase.setCreateUser(new User(userService.getIdByUsername(item.getString(12))));
                purchase.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(13)));
                purchase.setEditUser(user);
                purchase.setEditDate(currentDate);
                purchase.setActive(item.getBoolean(14));
                purchaseService.saveOrUpdate(purchase);

                JsonArray jsonDetails = item.getJsonArray(21);
                for (int j = 0; j < jsonDetails.size(); j++) {
                    JsonArray detail = jsonDetails.getJsonArray(j);
                    PurchaseDetail pd = new PurchaseDetail();
                    pd.setProduct(new Product(productService.getIdByBarcode(detail.getString(0))));
                    pd.setUom(new UoM(uomService.getIdByCode(detail.getString(1))));
                    pd.setProductName(detail.getString(2));
                    pd.setUnitPrice(detail.getJsonNumber(3).bigDecimalValue());
                    pd.setQuantity(detail.getJsonNumber(4).bigDecimalValue());
                    pd.setPurchase(purchase);
                    pd.setSubtotal(detail.getJsonNumber(5).bigDecimalValue());
                    pd.setIgv(detail.getJsonNumber(6).bigDecimalValue());

                    if (purchase.getActive()) {
                        //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">
                        Double initialStock, detailStock, finalStock;
                        Double initialCost, detailCost, finalCost;
                        detailCost = pd.getUnitPrice().doubleValue();
                        detailStock = pd.getQuantity().doubleValue();
                        try {
                            initialStock = stockService.getGroupQuantityByCompanyProduct(purchase.getCompanyArrival(), pd.getProduct()).doubleValue();
                            initialCost = productCostPriceService.getCostByCompanyProduct(purchase.getCompanyArrival(), pd.getProduct()).doubleValue();

                            finalStock = initialStock + detailStock;
                            finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
                        } catch (NullPointerException e) {
                            finalCost = detailCost;
                        }

                        //</editor-fold>
                        productCostPriceService.updateGroupCostByCompanyProduct(
                                new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                                purchase.getCompanyArrival(),
                                pd.getProduct(),
                                user
                        );

                        stockService.addQuantity(
                                pd.getQuantity(),
                                pd.getProduct(),
                                purchase.getCompanyArrival(),
                                user
                        );
                    }
                    purchaseDetailService.saveOrUpdate(pd);
                }

                JsonArray jsonPayments = item.getJsonArray(22);
                for (int j = 0; j < jsonPayments.size(); j++) {
                    JsonArray payment = jsonPayments.getJsonArray(j);
                    PurchasePayment pp = new PurchasePayment();
                    pp.setDatePayment(FULL_DATE_FORMATTER.parse(payment.getString(0)));
                    pp.setQuantity(item.getJsonNumber(1).bigDecimalValue());
                    pp.setDescription(item.getString(2));
                    pp.setCompanyDisbursement(company);
                    pp.setCreateUser(new User(userService.getIdByUsername(item.getString(3))));
                    pp.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(4)));
                    pp.setEditUser(user);
                    pp.setEditDate(currentDate);
                    pp.setActive(item.getBoolean(5));
                    purchasePaymentService.saveOrUpdate(pp);
                }
            }
            //</editor-fold>
            JsonArray jsonSupplierReturn = jsonInput.getJsonArray("suplier_returns");
            //<editor-fold defaultstate="collapsed" desc="Devolucion de Proveedores">            
            for (int i = 0; i < jsonSupplierReturn.size(); i++) {
                JsonArray item = jsonSupplierReturn.getJsonArray(i);
                StockReturnSupplier srs = new StockReturnSupplier();

                srs.setDateIssue(SHORT_DATE_FORMATTER.parse(item.getString(1)));
                srs.setPurchase(new Purchase(purchaseService.getIdByDocument(item.getString(2), item.getString(3), item.getString(4), item.getString(5))));
                srs.setPaymentProof(new PaymentProof(paymentProofService.getIdByCode(item.getString(6))));
                srs.setSerie(item.getString(7));
                srs.setDocumentNumber(item.getString(8));
                srs.setElectronic(item.getBoolean(9));
                srs.setIgv(item.getJsonNumber(10).bigDecimalValue());
                srs.setOperationType(new OperationType(operationTypeService.getSupplierReturnTypeId()));
                srs.setRepayment(item.getJsonNumber(11).bigDecimalValue());

                srs.setCreateUser(new User(userService.getIdByUsername(item.getString(12))));
                srs.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(13)));
                srs.setEditUser(user);
                srs.setEditDate(new Date());

                srs.setActive(item.getBoolean(14));
                stockReturnSupplierService.saveOrUpdate(srs);
                Company srsCompany = new Company(companyService.getIdByCode(item.getString(0)));
                JsonArray jsonDetails = item.getJsonArray(13);
                for (int j = 0; j < jsonDetails.size(); j++) {
                    JsonArray detail = jsonDetails.getJsonArray(j);
                    StockReturnSupplierDetail srsd = new StockReturnSupplierDetail();
                    srsd.setProduct(new Product(productService.getIdByBarcode(detail.getString(0))));
                    srsd.setUom(new UoM(uomService.getIdByCode(detail.getString(1))));
                    srsd.setProductName(detail.getString(2));
                    srsd.setUnitCost(detail.getJsonNumber(3).bigDecimalValue());
                    srsd.setQuantity(detail.getJsonNumber(4).bigDecimalValue());
                    srsd.setStockReturnSupplier(srs);
                    if (srs.getActive()) {
                        //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">
                        Double initialStock, detailStock, finalStock;
                        Double initialCost, detailCost, finalCost;
                        detailCost = srsd.getUnitCost().doubleValue();
                        detailStock = srsd.getQuantity().doubleValue();
                        try {
                            initialStock = stockService.getGroupQuantityByCompanyProduct(
                                    srsCompany,
                                    srsd.getProduct()).doubleValue();
                            initialCost = productCostPriceService.getCostByCompanyProduct(srsCompany, srsd.getProduct()).doubleValue();

                            finalStock = initialStock - detailStock;
                            finalCost = ((initialCost * initialStock) - (detailCost * detailStock)) / finalStock;
                        } catch (NullPointerException e) {
                            finalCost = detailCost;
                        }
                        //</editor-fold>
                        productCostPriceService.updateGroupCostByCompanyProduct(
                                new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                                srsCompany,
                                srsd.getProduct(),
                                user
                        );

                        stockService.addQuantity(
                                srsd.getQuantity(),
                                srsd.getProduct(),
                                srsCompany,
                                user
                        );
                    }
                    stockReturnSupplierDetailService.saveOrUpdate(srsd);
                }

                JsonArray jsonPayments = item.getJsonArray(22);
                for (int j = 0; j < jsonPayments.size(); j++) {
                    JsonArray payment = jsonPayments.getJsonArray(j);
                    PurchasePayment pp = new PurchasePayment();
                    pp.setDatePayment(FULL_DATE_FORMATTER.parse(payment.getString(0)));
                    pp.setQuantity(item.getJsonNumber(1).bigDecimalValue());
                    pp.setDescription(item.getString(2));
                    pp.setCompanyDisbursement(company);
                    pp.setCreateUser(new User(userService.getIdByUsername(item.getString(3))));
                    pp.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(4)));
                    pp.setEditUser(user);
                    pp.setEditDate(currentDate);
                    pp.setActive(item.getBoolean(5));
                    purchasePaymentService.saveOrUpdate(pp);
                }
            }
            //</editor-fold>
            JsonArray jsonCreatedISM = jsonInput.getJsonArray("c_ism");
            //<editor-fold defaultstate="collapsed" desc="Movimientos creados">            
            for (int i = 0; i < jsonCreatedISM.size(); i++) {
                JsonArray item = jsonCreatedISM.getJsonArray(i);
                InternalStockMovement ism = new InternalStockMovement();

                ism.setTargetCompany(new Company(companyService.getIdByCode(item.getString(0))));
                ism.setSourceCompany(new Company(companyService.getIdByCode(item.getString(1))));
                ism.setPaymentProof(new PaymentProof(paymentProofService.getIdByCode(item.getString(2))));
                ism.setSerie(item.getString(3));
                ism.setDocumentNumber(item.getString(4));
                if (!item.isNull(5)) {
                    ism.setCarrier(new Actor(actorService.getIdByIdentityNumber(item.getString(5))));
                }
                ism.setDateArrival(SHORT_DATE_FORMATTER.parse(item.getString(6)));
                if (!item.isNull(7)) {
                    ism.setDateRealArrival(FULL_DATE_FORMATTER.parse(item.getString(7)));
                }
                ism.setDateShipping(SHORT_DATE_FORMATTER.parse(item.getString(8)));
                ism.setDriverLicense(item.getString(9));
                ism.setElectronic(item.getBoolean(10));
                ism.setOperationTypeSource(new OperationType(operationTypeService.getISMOutTypeId()));
                ism.setOperationTypeTarget(new OperationType(operationTypeService.getISMInTypeId()));
                ism.setSent(item.getBoolean(13));
                ism.setTransportDescription(item.getString(14));
                ism.setUploaded(Boolean.TRUE);

                ism.setElectronic(item.getBoolean(15));
                ism.setSent(item.getBoolean(16));

                ism.setCreateUser(new User(item.getInt(17)));
                ism.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(18)));
                ism.setEditUser(user);
                ism.setEditDate(new Date());
                ism.setActive(item.getBoolean(19));

                internalStockMovementService.saveOrUpdate(ism);

                JsonArray jsonDetails = item.getJsonArray(21);
                for (int j = 0; j < jsonDetails.size(); j++) {
                    JsonArray detail = jsonDetails.getJsonArray(j);
                    InternalStockMovementDetail ismd = new InternalStockMovementDetail();
                    ismd.setProduct(new Product(productService.getIdByBarcode(detail.getString(0))));
                    ismd.setUom(new UoM(uomService.getIdByCode(detail.getString(1))));
                    ismd.setProductName(detail.getString(2));
                    ismd.setUnitCost(detail.getJsonNumber(3).bigDecimalValue());
                    ismd.setQuantity(detail.getJsonNumber(4).bigDecimalValue());
                    if (!detail.isNull(5)) {
                        ismd.setWeight(detail.getJsonNumber(5).bigDecimalValue());
                    }
                    if (!detail.isNull(6)) {
                        ismd.setWeightUoM(new UoM(uomService.getIdByCode(detail.getString(6))));
                    }
                    ismd.setInternalStockMovement(ism);
                    if (ism.getActive()) {
                        if (ism.getDateArrival() != null) {
                            //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">

                            Double initialStock, detailStock, finalStock;
                            Double initialCost, detailCost, finalCost;
                            detailCost = ismd.getUnitCost().doubleValue();
                            detailStock = ismd.getQuantity().doubleValue();
                            try {
                                initialStock = stockService.getGroupQuantityByCompanyProduct(
                                        ism.getTargetCompany(),
                                        ismd.getProduct()
                                ).doubleValue();
                                initialCost = productCostPriceService.getCostByCompanyProduct(
                                        ism.getTargetCompany(),
                                        ismd.getProduct()
                                ).doubleValue();

                                finalStock = initialStock + detailStock;
                                finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
                            } catch (NullPointerException e) {
                                finalCost = detailCost;
                            }
                            //</editor-fold>
                            productCostPriceService.updateGroupCostByCompanyProduct(
                                    new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                                    ism.getTargetCompany(),
                                    ismd.getProduct(),
                                    user);
                            stockService.addQuantity(
                                    ismd.getQuantity(),
                                    ismd.getProduct(),
                                    ism.getTargetCompany(),
                                    user
                            );
                        }
                        stockService.substractQuantity(
                                ismd.getQuantity(),
                                ismd.getProduct(),
                                ism.getSourceCompany(),
                                user
                        );
                    }
                    internalStockMovementDetailService.saveOrUpdate(ismd);
                }
            }
            //</editor-fold>
            JsonArray jsonEditISM = jsonInput.getJsonArray("e_ism");
            //<editor-fold defaultstate="collapsed" desc="Movimientos modificados">
            if (!jsonEditISM.isEmpty()) {
                synchronized (this) {
                    for (int i = 0; i < jsonEditISM.size(); i++) {
                        JsonArray item = jsonEditISM.getJsonArray(i);
                        InternalStockMovement ism = new InternalStockMovement();

                        ism.setTargetCompany(new Company(companyService.getIdByCode(item.getString(0))));
                        ism.setSourceCompany(new Company(companyService.getIdByCode(item.getString(1))));
                        ism.setPaymentProof(new PaymentProof(paymentProofService.getIdByCode(item.getString(2))));
                        ism.setSerie(item.getString(3));
                        ism.setDocumentNumber(item.getString(4));
                        ism.setId(internalStockMovementService.getIdByFullDocument(ism.getPaymentProof(), ism.getSerie(), ism.getDocumentNumber()));
                        if (!item.isNull(5)) {
                            ism.setCarrier(new Actor(actorService.getIdByIdentityNumber(item.getString(5))));
                        }
                        ism.setDateArrival(SHORT_DATE_FORMATTER.parse(item.getString(6)));
                        if (!item.isNull(7)) {
                            ism.setDateRealArrival(FULL_DATE_FORMATTER.parse(item.getString(7)));
                        }
                        ism.setDateShipping(SHORT_DATE_FORMATTER.parse(item.getString(8)));
                        ism.setDriverLicense(item.getString(9));
                        ism.setElectronic(item.getBoolean(10));
                        ism.setOperationTypeSource(new OperationType(operationTypeService.getISMOutTypeId()));
                        ism.setOperationTypeTarget(new OperationType(operationTypeService.getISMInTypeId()));
                        ism.setSent(item.getBoolean(13));
                        ism.setTransportDescription(item.getString(14));
                        ism.setUploaded(Boolean.TRUE);

                        ism.setElectronic(item.getBoolean(15));
                        ism.setSent(item.getBoolean(16));

                        ism.setCreateUser(new User(item.getInt(17)));
                        ism.setCreateDate(FULL_DATE_FORMATTER.parse(item.getString(18)));
                        ism.setEditUser(user);
                        ism.setEditDate(new Date());
                        ism.setActive(item.getBoolean(19));

                        internalStockMovementService.saveOrUpdate(ism);

                        JsonArray jsonDetails = item.getJsonArray(21);
                        for (int j = 0; j < jsonDetails.size(); j++) {
                            JsonArray detail = jsonDetails.getJsonArray(j);
                            InternalStockMovementDetail ismd = new InternalStockMovementDetail();
                            ismd.setProduct(new Product(productService.getIdByBarcode(detail.getString(0))));
                            ismd.setUom(new UoM(uomService.getIdByCode(detail.getString(1))));
                            ismd.setProductName(detail.getString(2));
                            ismd.setUnitCost(detail.getJsonNumber(3).bigDecimalValue());
                            ismd.setQuantity(detail.getJsonNumber(4).bigDecimalValue());
                            if (!detail.isNull(5)) {
                                ismd.setWeight(detail.getJsonNumber(5).bigDecimalValue());
                            }
                            if (!detail.isNull(6)) {
                                ismd.setWeightUoM(new UoM(uomService.getIdByCode(detail.getString(6))));
                            }
                            ismd.setInternalStockMovement(ism);

                            Double initialStock, detailStock, finalStock;
                            Double initialCost, detailCost, finalCost;
                            if (ismd.getId() != null) {
                                BigDecimal quantity = internalStockMovementDetailService.getQuantityById(ismd.getId());

                                if (ism.getDateRealArrival() != null) {
                                    //<editor-fold defaultstate="collapsed" desc="Formula de Costo Inicial">
                                    finalStock = stockService.getGroupQuantityByCompanyProduct(ism.getTargetCompany(), ismd.getProduct()).doubleValue();
                                    finalCost = productCostPriceService.getCostByCompanyProduct(ism.getTargetCompany(), ismd.getProduct()).doubleValue();
                                    detailCost = internalStockMovementDetailService.getUnitCostById(ismd.getId()).doubleValue();
                                    detailStock = quantity.doubleValue();

                                    initialStock = finalStock - detailStock;
                                    initialCost = ((finalCost * finalStock) - (detailStock * detailCost)) / initialStock;
                                    //</editor-fold>

                                    //<editor-fold defaultstate="collapsed" desc="Formula de Costo final">
                                    detailCost = ismd.getUnitCost().doubleValue();
                                    detailStock = ismd.getQuantity().doubleValue();
                                    finalStock = initialStock + detailStock;

                                    finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
                                    //</editor-fold>

                                    productCostPriceService.updateGroupCostByCompanyProduct(
                                            new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                                            ism.getTargetCompany(),
                                            ismd.getProduct(),
                                            user
                                    );

                                    stockService.substractQuantity(
                                            quantity,
                                            ismd.getProduct(),
                                            ism.getTargetCompany(),
                                            user
                                    );
                                }
                                stockService.addQuantity(quantity, ismd.getProduct(), ism.getSourceCompany(), user);
                            }

                            if (ism.getDateArrival() != null) {
                                //<editor-fold defaultstate="collapsed" desc="Formula de Costo Final">

                                detailCost = ismd.getUnitCost().doubleValue();
                                detailStock = ismd.getQuantity().doubleValue();
                                try {
                                    initialStock = stockService.getGroupQuantityByCompanyProduct(
                                            ism.getTargetCompany(),
                                            ismd.getProduct()
                                    ).doubleValue();
                                    initialCost = productCostPriceService.getCostByCompanyProduct(
                                            ism.getTargetCompany(),
                                            ismd.getProduct()
                                    ).doubleValue();

                                    finalStock = initialStock + detailStock;
                                    finalCost = ((initialCost * initialStock) + (detailCost * detailStock)) / finalStock;
                                } catch (NullPointerException e) {
                                    finalCost = detailCost;
                                }
                                //</editor-fold>
                                productCostPriceService.updateGroupCostByCompanyProduct(
                                        new BigDecimal(finalCost).setScale(4, RoundingMode.HALF_UP),
                                        ism.getTargetCompany(),
                                        ismd.getProduct(),
                                        user);
                                stockService.addQuantity(
                                        ismd.getQuantity(),
                                        ismd.getProduct(),
                                        ism.getTargetCompany(),
                                        user
                                );
                            }
                            stockService.substractQuantity(
                                    ismd.getQuantity(),
                                    ismd.getProduct(),
                                    ism.getSourceCompany(),
                                    user
                            );
                            internalStockMovementDetailService.saveOrUpdate(ismd);
                        }
                    }
                }
            }
            //</editor-fold>
        } else {
            Response.status(Response.Status.UNAUTHORIZED);
        }
    }

    private JsonArrayBuilder createArrayBuilderFromList(List data) {
        JsonArrayBuilder listBuilder, arrayBuilder;
        listBuilder = Json.createArrayBuilder();
        for (Object item : data) {

            if (item instanceof Object[]) {
                arrayBuilder = Json.createArrayBuilder();
                for (Object object : (Object[]) item) {
                    formatter(arrayBuilder, object);
                }
                listBuilder.add(arrayBuilder);
            } else {
                formatter(listBuilder, item);
            }
        }
        return listBuilder;
    }

    private void formatter(JsonArrayBuilder arrayBuilder, Object object) {
        if (object == null) {
            arrayBuilder.addNull();
        } else {
            if (object instanceof String) {
                arrayBuilder.add((String) object);
            } else if (object instanceof BigDecimal) {
                arrayBuilder.add((BigDecimal) object);
            } else if (object instanceof Double) {
                arrayBuilder.add((BigDecimal) object);
            } else if (object instanceof Integer) {
                arrayBuilder.add((Integer) object);
            } else if (object instanceof Long) {
                arrayBuilder.add((Long) object);
            } else if (object instanceof Boolean) {
                arrayBuilder.add((Boolean) object);
            } else if (object instanceof Date) {
                arrayBuilder.add(DATE_FORMATTER.format((Date) object));
            } else if (object instanceof List) {
                arrayBuilder.add(createArrayBuilderFromList((List) object));
            }
        }
    }
}
