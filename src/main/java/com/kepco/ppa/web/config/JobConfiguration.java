package com.kepco.ppa.web.config;

import com.kepco.ppa.web.batch.domain.TaxEmailBillInfoVO;
import com.kepco.ppa.web.batch.domain.TbTaxBillInfoEncVO;
import com.kepco.ppa.web.batch.reader.TaxEmailBillInfoDataReader;
import com.kepco.ppa.web.common.YAMLConfig;
import com.kepco.ppa.web.web.rest.CreateBachIdsJobParameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * Created By K.H.C (Hanjun KDN)
 * @since 2021.03.10
 * @version 1.0
 */

@RequiredArgsConstructor
@Slf4j
@EnableBatchProcessing
@Configuration
public class JobConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    JobExplorer jobExplorer;

    @Autowired
    @Qualifier("etaxdatasource")
    public DataSource etaxDataSource;

    @Autowired
    @Qualifier("exedidatasource")
    public DataSource exediDataSource;

    @Autowired
    private YAMLConfig dbencConfig;

    @Autowired
    private CreateBachIdsJobParameter jobParameter;

    @Bean
    @JobScope // (2)
    public CreateBachIdsJobParameter jobParameter() {
        return new CreateBachIdsJobParameter();
    }

    @Bean
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<TbTaxBillInfoEncVO> pagingTaxEmailBillInfoItemReader() {
        Map<String, Object> params = new HashMap<>();
        params.put("time", jobParameter.getTime());
        params.put("batchIds", jobParameter.getBatchIds());
        log.info(">>>>>>>>>>> time={}, batchIds={}", jobParameter.getTime(), jobParameter.getBatchIds());

        TaxEmailBillInfoDataReader dataReader = new TaxEmailBillInfoDataReader(jobParameter.getBatchIds());
        dataReader.setDataSource(this.etaxDataSource);

        return dataReader.getPagingReader();
    }

    ///////////////////////////////////////////////
    // TB_TAX_BILL_INFO_ENC
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> tbTaxBillInfoEncWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM TB_TAX_BILL_INFO_ENC WHERE REGIST_ID = 'ppauser' AND ESERO_ISSUE_ID = :eseroIssueId")
            .build();
    }

    // IF_TAX_BILL_RESULT_INFO
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> ifTaxBillResultInfoWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM KEY_POWEREDI.IF_TAX_BILL_RESULT_INFO WHERE MANAGE_ID = :svcManageId")
            .build();
    }

    // TB_STATUS_HIST
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> tbStatusHistWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM TB_STATUS_HIST WHERE REGIST_ID = 'ppauser' AND BIZ_MANAGE_ID = :bizManageId")
            .build();
    }

    // ETS_TAX_META_INFO_TB
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> etsTaxMetaInfoTbWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM ETS_TAX_META_INFO_TB WHERE SVC_MANAGE_ID = :svcManageId AND UUID = :uuid")
            .build();
    }

    // ETS_TAX_MAIN_INFO_TB
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> etsTaxMainInfoTbWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM ETS_TAX_MAIN_INFO_TB WHERE BUYER_CONTACTOR_NAME ='BATCH' AND UUID = :uuid")
            .build();
    }

    // IF_TAX_BILL_INFO
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> ifTaxBillInfoWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM KEY_POWEREDI.IF_TAX_BILL_INFO WHERE REGIST_ID = 'ppauser' AND MANAGE_ID = :svcManageId")
            .build();
    }

    ///////////////////////////////////////////////
    // TB_TRADE_ITEM_LIST
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> tbTradeItemListWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM TB_TRADE_ITEM_LIST WHERE BIZ_MANAGE_ID = :bizManageId")
            .build();
    }

    // ETS_TAX_LINE_INFO_TB
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> etsTaxLineInfoTbWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM ETS_TAX_LINE_INFO_TB WHERE UUID = :uuid")
            .build();
    }

    // IF_TAX_BILL_ITEM_LIST
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> ifTaxBillItemListWriter() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(exediDataSource)
            .beanMapped()
            .sql("DELETE FROM KEY_POWEREDI.IF_TAX_BILL_ITEM_LIST WHERE MANAGE_ID = :svcManageId")
            .build();
    }

    ///////////////////////////////////////////////
    @Bean
    JdbcBatchItemWriter<TbTaxBillInfoEncVO> taxEmailItemListEndingUpdate() {
        return new JdbcBatchItemWriterBuilder<TbTaxBillInfoEncVO>()
            .dataSource(etaxDataSource)
            .beanMapped()
            .sql("UPDATE TAX_EMAIL_BILL_INFO SET MAIL_STATUS_CODE = '66' WHERE ISSUE_ID = :eseroIssueId")
            .build();
    }

    @Bean
    public CompositeItemWriter<TbTaxBillInfoEncVO> compositeItemWriter() {
        CompositeItemWriter<TbTaxBillInfoEncVO> compositeItemWriter = new CompositeItemWriter<>();

        compositeItemWriter.setDelegates(
            Arrays.asList(
                tbStatusHistWriter(),
                etsTaxMainInfoTbWriter(),
                etsTaxMetaInfoTbWriter(),
                tbTradeItemListWriter(),
                etsTaxLineInfoTbWriter(),
                ifTaxBillResultInfoWriter(),
                ifTaxBillItemListWriter(),
                ifTaxBillInfoWriter(),
                tbTaxBillInfoEncWriter(),
                taxEmailItemListEndingUpdate()
            )
        );

        return compositeItemWriter;
    }

    @JobScope
    @Bean
    public Step step1() {
        log.info("STEP-1 시작...");
        return stepBuilderFactory
            .get("step1")
            .<TbTaxBillInfoEncVO, TbTaxBillInfoEncVO>chunk(20)
            .reader(pagingTaxEmailBillInfoItemReader())
            .writer(compositeItemWriter())
            .build();
    }

    @Bean(name = "ppaBatchJob")
    public Job ppaBatchJob() {
        return jobBuilderFactory.get("ppaBatchJob").preventRestart().incrementer(new RunIdIncrementer()).start(step1()).build();
    }
}
