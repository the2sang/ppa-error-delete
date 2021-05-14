package com.kepco.ppa.web.batch.reader;

import com.kepco.ppa.web.batch.domain.TaxEmailBillInfoRowMapper;
import com.kepco.ppa.web.batch.domain.TaxEmailBillInfoVO;
import com.kepco.ppa.web.batch.domain.TbTaxBillInfoEncRowMapper;
import com.kepco.ppa.web.batch.domain.TbTaxBillInfoEncVO;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.OraclePagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;

public class TaxEmailBillInfoDataReader {

    @Autowired
    @Setter
    private DataSource dataSource;

    public final int READER_FETCH_SIZE = 50;

    @Setter
    @Getter
    public String batchIds;

    public TaxEmailBillInfoDataReader(String batchIds) {
        this.batchIds = batchIds;
    }

    public JdbcPagingItemReader<TbTaxBillInfoEncVO> getPagingReader() {
        JdbcPagingItemReader<TbTaxBillInfoEncVO> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setPageSize(READER_FETCH_SIZE);
        reader.setRowMapper(new TbTaxBillInfoEncRowMapper());

        reader.setQueryProvider(createQuery(this.batchIds));

        return reader;
    }

    private OraclePagingQueryProvider createQuery(String batchIds) {
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("B_ISSUE_ID", Order.ASCENDING);

        OraclePagingQueryProvider queryProvider = new OraclePagingQueryProvider();
        queryProvider.setSelectClause("A.*, B.ISSUE_ID AS B_ISSUE_ID");
        queryProvider.setFromClause("from EXEDI.TB_TAX_BILL_INFO A INNER JOIN TAX_EMAIL_BILL_INFO B ON A.ESERO_ISSUE_ID = B.ISSUE_ID");
        queryProvider.setWhereClause(getWhereClause());
        queryProvider.setSortKeys(sortKeys);
        return queryProvider;
    }

    private String getWhereClause() {
        StringBuilder sb = new StringBuilder();

        sb.append(" B.MAIL_STATUS_CODE = '88' "); //메일진행상태(null:DEFAULT,'01':작성중및회계처리중,'02'회계처리완료,'98':공급받는자반려, '88': ENC 국.승인번호 중복처리
        sb.append(" AND B.ID IN ('" + batchIds + "')");

        return sb.toString();
    }
}
