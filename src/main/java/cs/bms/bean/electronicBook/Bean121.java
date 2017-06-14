/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.electronicBook;

import cs.bms.enumerated.EBDetailState;
import cs.bms.model.AccountingRecord;
import cs.bms.model.AccountingRecordDetail;
import cs.bms.service.interfac.IKardexService;
import gkfire.web.util.BeanUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class Bean121 extends ABookBean {

    protected IKardexService kardexService;


    @Override
    public void onLoad(boolean allowAjax) {    
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }
    
    @Override
    public void generate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<Object[]> list = kardexService.listHQL("SELECT "
                + "k.company.code,"
                + "k.product.catalog.code,"
                + "k.product.stockType.code,"
                + "k.product.barcode,"
                + "k.date,"
                + "COALESCE(pp.code,'00'),"
                + "COALESCE(k.serie,'0'),"
                + "COALESCE(k.documentNumber,'0'),"
                + "k.operationType.code,"
                + "k.product.name,"
                + "k.product.uom.code,"
                + "COALESCE(k.quantityIn,0.00),"
                + "COALESCE(k.quantityOut,0.00),"
                + "k.idReference,"
                + "k.entityName,"
                + "k.active "
                + "FROM Kardex k"
                + "LEFT JOIN k.paymentProof as pp WHERE "
                + "k.date < ? AND "
                + "( )"
                + "ORDER BY ");
        AccountingRecord accountingRecord = new AccountingRecord();
        accountingRecord.setDay(null);
        accountingRecord.setMonth(getMonth());
        accountingRecord.setYear(getYear());
        accountingRecord.setRuc(getSessionBean().getCurrentCompany().getRuc());
        accountingRecord.setFile(null);
        accountingRecord.setSent(Boolean.FALSE);
        accountingRecord.setSentDate(null);
        accountingRecord.setElectronicBook(getElectronicBook());
        accountingRecord.setSummaryData(getElectronicBook().getMetaData());
        getAccountingRecordService().saveOrUpdate(accountingRecord);
        
        for (Long i = 0L; i < list.size(); i++) {
            Object[] item = list.get(i.intValue());
            String prefix = null;
            if (1 == 0) {
                prefix = "A";
            } else if (i == list.size() - 1) {
                prefix = "C";
            } else {
                prefix = "M";
            }
            boolean active = (Boolean) item[15];
            EBDetailState state = null;
            try {
                state = EBDetailState.calculate((Date) item[4], getYear(), getMonth(), active);
            } catch (ParseException ex) {
                Logger.getLogger(Bean121.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            AccountingRecordDetail detail = new AccountingRecordDetail();
            detail.setCuo(i);
            detail.setIdReference(((Number) item[13]).longValue());
            detail.setEntityName((String) item[14]);
            detail.setPrefix(prefix);
            detail.setState(state);
            detail.setAccountingRecord(accountingRecord);
            String data = getYear() + getMonth().getNumber() + "00" + "|"
                    + i + "|"                   //1
                    + prefix + i + "|"          //2
                    + item[0] + "|"             //3
                    + item[1] + "|"             //4
                    + item[2] + "|"             //5
                    + item[3] + "|"             //6
                    + "|"                       //7
                    + sdf.format((Date) item[4]) + "|" //8
                    + item[5] + "|" //9
                    + item[6] + "|" //10
                    + item[7] + "|" //11
                    + item[8] + "|" //12
                    + item[9] + "|" //13
                    + item[10] + "|" //14
                    + item[11] + "|" //15
                    + item[12] + "|" //16
                    + state.getNumber() + "|";                     //17        
            detail.setData(data);
            getAccountingRecordDetailService().save(detail);
        }

    }

    @Override
    public void search() {
    }
}
