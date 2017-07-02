/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs.bms.bean.electronicBook;

import cs.bms.service.interfac.ISaleService;
import gkfire.util.Month;
import gkfire.web.util.BeanUtil;
import java.util.ArrayList;
import java.util.Arrays;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Jhoan Brayam
 */
@ManagedBean
@SessionScoped
public class Bean141 extends ABookBean implements java.io.Serializable {

    private ISaleService saleService;

    @Override
    public void generate() {
        saleService.listHQL("SELECT "
                + "s.id,"
                + "s.dateIssue,"
                + "s.paymentProof.code,"
                + "s.serie,"
                + "s.documentNumber, "
                + "idd.code,"
                + "c.identityNumber,"
                + "s.customerName,"
                + "s.subtotal,"
                + "s.igv,"
                + "s.subtotalDiscount,"
                + "s.igvDiscount,"
                + "s.active "
                + "FROM SALE s left join s.customer as c left join c.identityDocument as idd "
                + "WHERE "
                + "COALESCE(s.editDate,s.dateIssue) < (?::timestamp with time zone) AND"
                + ""
                + "ORDER BY COALESCE(s.editDate,s.dateIssue)",
                ("01/" + (month.ordinal() + 2) + "/" + year));
    }

    @Override
    public void onLoad(boolean allowAjax) {
        if (BeanUtil.isAjaxRequest() && !allowAjax) {
            return;
        }
        refresh();
    }

    public void beginCreate() {
        createYear = year;
        createMonth = Month.byOrdinal((Number) accountingRecordService.getByHQL("SELECT MAX(ar.month) FROM AccountingRecord ar WHERE ar.year = ?", createYear));
        createMonths = new ArrayList<>();
        if (createMonth == null) {
            createMonths.addAll(Arrays.asList(Month.values()));
        } else {
            for (int i = createMonth.ordinal(); i < 12; i++) {
                createMonths.add(Month.byOrdinal(i));
            }
        }
    }

    @Override
    public void search() {
    }

}
