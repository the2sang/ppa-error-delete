package com.kepco.ppa.web.batch.reader;

import com.kepco.ppa.web.batch.domain.TaxEmailBillInfoRowMapper;
import com.kepco.ppa.web.batch.domain.TaxEmailBillInfoVO;
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

    public JdbcPagingItemReader<TaxEmailBillInfoVO> getPagingReader() {
        JdbcPagingItemReader<TaxEmailBillInfoVO> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setPageSize(READER_FETCH_SIZE);
        reader.setRowMapper(new TaxEmailBillInfoRowMapper());

        reader.setQueryProvider(createQuery(this.batchIds));

        return reader;
    }

    private OraclePagingQueryProvider createQuery(String batchIds) {
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("ISSUE_ID", Order.ASCENDING);

        OraclePagingQueryProvider queryProvider = new OraclePagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("from TAX_EMAIL_BILL_INFO");
        queryProvider.setWhereClause(getWhereClause());
        queryProvider.setSortKeys(sortKeys);
        return queryProvider;
    }

    private String getWhereClause() {
        StringBuilder sb = new StringBuilder();

        sb.append("MAIL_STATUS_CODE ='88'"); //메일진행상태(null:DEFAULT,'01':작성중및회계처리중,'02'회계처리완료,'98':공급받는자반려, '88': ENC 국.승인번호 중복처리
        sb.append(" AND ID IN ('" + batchIds + "')");

        return sb.toString();
    }
}
