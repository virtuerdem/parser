package com.ttgint.library.nativeQuery;

import com.ttgint.library.record.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.StoredProcedureQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class NativeQuery {

    private final EntityManager entityManager;

    public NativeQuery(ApplicationContext applicationContext) {
        this.entityManager = applicationContext.getBean(EntityManager.class);
    }

    @Transactional
    public List<Object[]> getResultList(String query, String queryTag) {
        List<Object[]> resultList = new ArrayList<>();
        try {
            resultList = entityManager.createNativeQuery(query).getResultList();
        } catch (Exception exception) {
            log.error("{} has exception ", queryTag, exception);
        }
        return resultList;
    }

    @Transactional
    public List<String> getResultListForSingleColumn(String query, String queryTag) {
        return Arrays.stream(getResultList(query, queryTag).toArray()).map(e -> (String) e).toList();
    }

    @Transactional
    public Boolean executeQuery(String query, String queryTag) {
        log.info("{} has started ", queryTag);
        boolean isSuccess = false;
        EntityManager entityManager = this.entityManager.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.createNativeQuery(query).executeUpdate();
            transaction.commit();
            isSuccess = true;
            log.info("{} has done ", queryTag);
        } catch (Exception exception) {
            log.error("{} has exception ", queryTag, exception);
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackException) {
                    log.error("{} has rollbackException ", queryTag, rollbackException);
                }
            }
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }

        return isSuccess;
    }

    @Transactional
    public Boolean executeStoredProcedure(String storedProcedureCommand,
                                          Map<Integer, StoredProcedureParamRecord> params,
                                          String storedProcedureTag) {
        log.info("{} has started ", storedProcedureTag);
        boolean isSuccess = false;
        EntityManager entityManager = this.entityManager.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();
            StoredProcedureQuery storedProcedureQuery
                    = entityManager.createStoredProcedureQuery(storedProcedureCommand);
            params.keySet()
                    .forEach(e -> storedProcedureQuery
                            .registerStoredProcedureParameter(
                                    e,
                                    params.get(e).getClassType(),
                                    params.get(e).getParameterMode()
                            )
                            .setParameter(e, params.get(e).getParameter())
                    );
            storedProcedureQuery.execute();
            transaction.commit();
            isSuccess = true;
            log.info("{} has done ", storedProcedureTag);
        } catch (Exception exception) {
            log.error("{} has exception ", storedProcedureTag, exception);
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackException) {
                    log.error("{} has rollbackException ", storedProcedureTag, rollbackException);
                }
            }
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }

        return isSuccess;
    }

 /*
    public void sendMail(String header, String body, String procedureTag) {
        TreeMap<Integer, StoredProcedureParamRecord> params = new TreeMap<>();
        params.put(1, new StoredProcedureParamRecord(String.class, ParameterMode.IN, "northi.system@ttgint.com"));
        params.put(2, new StoredProcedureParamRecord(String.class, ParameterMode.IN, header == null ? "" : header));
        params.put(3, new StoredProcedureParamRecord(String.class, ParameterMode.IN, body == null ? "" : body));
        executeStoredProcedure("NORTHI.SEND_MAIL", params, procedureTag);
    }
*/

    public abstract List<String> getExistsTables(String schemaName);

    public abstract Boolean isTableExists(String schemaName, String tableName);

    public abstract List<String> getExistsColumns(String schemaName, String tableName);

    public abstract Boolean isColumnExists(String schemaName, String tableName, String columnName);

    public abstract Boolean generateTable(GenerateTableRecord record);

    public abstract Boolean generatePartition(GeneratePartitionRecord record);

    public abstract Boolean generateIndex(GenerateIndexRecord record);

    public abstract Boolean generateColumn(GenerateColumnRecord record);

}
