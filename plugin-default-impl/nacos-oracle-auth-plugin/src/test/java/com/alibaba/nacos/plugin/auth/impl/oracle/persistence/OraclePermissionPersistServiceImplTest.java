/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.auth.impl.oracle.persistence;

import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.persistence.configuration.DatasourceConfiguration;
import com.alibaba.nacos.persistence.datasource.DataSourceService;
import com.alibaba.nacos.persistence.datasource.DynamicDataSource;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OraclePermissionPersistServiceImplTest {
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @Mock
    private DataSourceService dataSourceService;
    
    private boolean embeddedStorageCache;
    
    private DataSourceService dataSourceServiceCache;
    
    @BeforeEach
    void setUp() throws Exception {
        when(dataSourceService.getJdbcTemplate()).thenReturn(jdbcTemplate);
        embeddedStorageCache = DatasourceConfiguration.isEmbeddedStorage();
        DatasourceConfiguration.setEmbeddedStorage(false);
        Field datasourceField = DynamicDataSource.class.getDeclaredField("basicDataSourceService");
        datasourceField.setAccessible(true);
        dataSourceServiceCache =
            (DataSourceService) datasourceField.get(DynamicDataSource.getInstance());
        datasourceField.set(DynamicDataSource.getInstance(), dataSourceService);
    }
    
    @AfterEach
    void tearDown() throws NoSuchFieldException, IllegalAccessException {
        DatasourceConfiguration.setEmbeddedStorage(embeddedStorageCache);
        Field datasourceField = DynamicDataSource.class.getDeclaredField("basicDataSourceService");
        datasourceField.setAccessible(true);
        datasourceField.set(DynamicDataSource.getInstance(), dataSourceServiceCache);
    }
    
    @Test
    void testInitFailsFastWhenDialectIsNotOracle() {
        when(dataSourceService.getDataSourceType()).thenReturn("mysql");
        
        assertThrows(IllegalStateException.class,
            () -> new OraclePermissionPersistServiceImpl().init());
    }
    
    @Test
    void testInsertAndDeleteUseResourcesColumn() {
        when(dataSourceService.getDataSourceType()).thenReturn("oracle");
        OraclePermissionPersistServiceImpl service = new OraclePermissionPersistServiceImpl();
        service.init();
        
        service.addPermission("role", "resource", "action");
        verify(jdbcTemplate).update(
            "INSERT INTO permissions (role, resources, action) VALUES (?, ?, ?)",
            "role", "resource", "action");
        
        service.deletePermission("role", "resource", "action");
        verify(jdbcTemplate)
            .update("DELETE FROM permissions WHERE role=? AND resources=? AND action=?", "role",
                "resource", "action");
    }
    
    @Test
    void testSelectUsesQuotedResourceAliasWithOraclePaging() {
        when(dataSourceService.getDataSourceType()).thenReturn("oracle");
        when(jdbcTemplate.queryForObject(any(), any(), eq(Integer.class))).thenReturn(1);
        when(jdbcTemplate.query(any(String.class), any(Object[].class), any(RowMapper.class)))
            .thenReturn(Collections.emptyList());
        OraclePermissionPersistServiceImpl service = new OraclePermissionPersistServiceImpl();
        service.init();
        
        Page<PermissionInfo> page = service.getPermissions("role", 1, 10);
        
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(Object[].class), any(RowMapper.class));
        assertTrue(sqlCaptor.getValue().contains("resources AS \"resource\""));
        assertTrue(sqlCaptor.getValue().contains("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"));
    }
    
    @Test
    void testInsertUsesResourcesEvenIfDialectReportsMysql() {
        // resolveDialectColumns is overridden unconditionally; the dialect mismatch itself is
        // rejected by init(), this test documents the column choice isolation.
        Mockito.doReturn("oracle").when(dataSourceService).getDataSourceType();
        OraclePermissionPersistServiceImpl service = new OraclePermissionPersistServiceImpl();
        service.init();
        service.addPermission("role", "resource", "action");
        verify(jdbcTemplate)
            .update("INSERT INTO permissions (role, resources, action) VALUES (?, ?, ?)", "role",
                "resource", "action");
    }
}
