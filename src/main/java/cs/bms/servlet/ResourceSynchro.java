/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.servlet;

import cs.bms.model.User;
import cs.bms.service.interfac.IActorService;
import cs.bms.service.interfac.ICompanyService;
import cs.bms.service.interfac.IDocumentNumberingService;
import cs.bms.service.interfac.IIdentityDocumentService;
import cs.bms.service.interfac.IInternalStockMovementService;
import cs.bms.service.interfac.IPaymentProofService;
import cs.bms.service.interfac.IProductCostPriceService;
import cs.bms.service.interfac.IProductSalePriceService;
import cs.bms.service.interfac.IProductService;
import cs.bms.service.interfac.IRolService;
import cs.bms.service.interfac.IStockService;
import cs.bms.service.interfac.IStockTypeService;
import cs.bms.service.interfac.IUoMService;
import cs.bms.service.interfac.IUserService;
import cs.bms.util.AESKeys;
import gkfire.util.AES;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author Jhoan Brayam
 */
@SessionScoped
public class ResourceSynchro implements java.io.Serializable {

    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

    @Autowired
    @Qualifier("actorService")
    protected IActorService actorService;
    @Autowired
    @Qualifier("companyService")
    protected ICompanyService companyService;
    @Autowired
    @Qualifier("productService")
    protected IProductService productService;
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

    @GET
    @Path("synchro_all.dkn")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject searchById(@QueryParam("q") String aes) throws Exception {
        //0 : date_request
        //1 : username
        //2 : password
        //3 : last_update
        //4 : id_company
        //5 : ruc_company

        String[] data = AES.decrypt(aes, AESKeys.SYNCHRO_TRANSFERENCE).split(",");
        Date currentDate = new Date();
        double minutes = new Long(currentDate.getTime() - Long.parseLong(data[0])).doubleValue() / 60000.0;
        if (minutes > 3) {
            Response.status(Response.Status.REQUEST_TIMEOUT);
            return null;
        }

        if (userService.verifyAuthenthication(data[1], data[2])) {
            JsonObjectBuilder allBuilder = Json.createObjectBuilder();
            Date lastUpdate = new Date();
            lastUpdate.setTime(Long.parseLong(data[3]));

            String company = data[4];

            //<editor-fold defaultstate="collapsed" desc="Tipos de comprobantes de pago">
            allBuilder.add("create_pp",
                    createArrayBuilderFromList(
                            paymentProofService.getCreateByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_pp",
                    createArrayBuilderFromList(
                            paymentProofService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Numeración de series">            
            allBuilder.add("document_number",
                    createArrayBuilderFromList(
                            documentNumberingService.getListData()
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Tipos de identificaciones">
            allBuilder.add("create_idd",
                    createArrayBuilderFromList(
                            identityDocumentService.getCreateByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_idd",
                    createArrayBuilderFromList(
                            identityDocumentService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Actores">
            allBuilder.add("create_actors",
                    createArrayBuilderFromList(
                            actorService.getCreatedByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_actors",
                    createArrayBuilderFromList(
                            actorService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Roles">
            allBuilder.add("create_rol",
                    createArrayBuilderFromList(
                            rolService.getCreateByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_rol",
                    createArrayBuilderFromList(
                            rolService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Sedes">
            allBuilder.add("create_company",
                    createArrayBuilderFromList(
                            companyService.getCreatedByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_company",
                    createArrayBuilderFromList(
                            companyService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>   
            //<editor-fold defaultstate="collapsed" desc="Usuarios">
            allBuilder.add("create_user",
                    createArrayBuilderFromList(
                            userService.getCreateByAfterDate(lastUpdate, company)
                    )
            );
            allBuilder.add("edit_user",
                    createArrayBuilderFromList(
                            userService.getEditedByAfterDate(lastUpdate, company, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Unidades de Medida">
            allBuilder.add("create_uom",
                    createArrayBuilderFromList(
                            uomService.getCreatedByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_uom",
                    createArrayBuilderFromList(
                            uomService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Tipos de existencia">
            allBuilder.add("create_stt",
                    createArrayBuilderFromList(
                            stockTypeService.getCreatedByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_stt",
                    createArrayBuilderFromList(
                            stockTypeService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Productos">
            allBuilder.add("create_product",
                    createArrayBuilderFromList(
                            productService.getCreatedByAfterDate(lastUpdate)
                    )
            );
            allBuilder.add("edit_product",
                    createArrayBuilderFromList(
                            productService.getEditedByAfterDate(lastUpdate, false)
                    )
            );
            //</editor-fold>      
            //<editor-fold defaultstate="collapsed" desc="Precios de venta">
            allBuilder.add("psp",
                    createArrayBuilderFromList(
                            productSalePriceService.getCreatedByAfterDate(lastUpdate, company)
                    )
            );
            //</editor-fold>            
            //<editor-fold defaultstate="collapsed" desc="Costos de productos">
            allBuilder.add("create_pcp",
                    createArrayBuilderFromList(
                            productCostPriceService.getCreatedByAfterDate(lastUpdate, company)
                    )
            );
            allBuilder.add("edit_pcp",
                    createArrayBuilderFromList(
                            productCostPriceService.getEditedByAfterDate(lastUpdate, company, false)
                    )
            );
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Existencias">
            allBuilder.add("create_stock",
                    createArrayBuilderFromList(
                            stockService.getCreatedByAfterDate(lastUpdate, company)
                    )
            );
            allBuilder.add("edit_stock",
                    createArrayBuilderFromList(
                            stockService.getEditedByAfterDate(lastUpdate, company, false)
                    )
            );
            //</editor-fold>     
            //<editor-fold defaultstate="collapsed" desc="Movimientos Internos">
            allBuilder.add("create_stock",
                    createArrayBuilderFromList(
                            internalStockMovementService.getCreatedByAfterDate(lastUpdate, company)
                    )
            );
            allBuilder.add("edit_stock",
                    createArrayBuilderFromList(
                            internalStockMovementService.getEditedByAfterDate(lastUpdate, company, false)
                    )
            );

            //</editor-fold>
            return allBuilder.build();
        } else {
            Response.status(Response.Status.UNAUTHORIZED);
            return null;
        }
    }

    @POST
    @Path("points.dkn")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray updatePoints(JsonObject jsonInput) throws Exception {

        Long time = jsonInput.getJsonNumber("time").longValue();

        Date currentDate = new Date();
        double minutes = new Long(currentDate.getTime() - time).doubleValue() / 60000.0;
        if (minutes > 3) {
            Response.status(Response.Status.REQUEST_TIMEOUT);
            return null;
        }
        String username = AES.decrypt(jsonInput.getString("username"), AESKeys.SYNCHRO_TRANSFERENCE);
        String password = AES.decrypt(jsonInput.getString("password"), AESKeys.SYNCHRO_TRANSFERENCE);
        User user =  userService.login(username, password);
        if (user != null && user.getActive()) {
            JsonArray data = jsonInput.getJsonArray("data");
            JsonArrayBuilder responseBuilder = Json.createArrayBuilder();
            data.getValuesAs(JsonObject.class).forEach((item) -> {
                Long id = actorService.getIdByIdentityNumber(item.getString("identityNumber"));
                if(id != null){
                    actorService.addPoints(id, item.getJsonNumber("points").longValue(), user);                        
                }else{
                    responseBuilder.add(item.getJsonNumber("saleId").longValue());
                }
            });
            return responseBuilder.build();
        } else {
            Response.status(Response.Status.UNAUTHORIZED);
            return null;
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
