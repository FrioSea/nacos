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

import com.alibaba.nacos.persistence.constants.PersistenceConstant;
import com.alibaba.nacos.plugin.auth.impl.persistence.ExternalPermissionPersistServiceImpl;
import jakarta.annotation.PostConstruct;

/**
 * Oracle adapted permission persistence. {@code resource} is a reserved word on Oracle, so the
 * physical column is named {@code resources} and must be selected with a quoted
 * {@code "resource"} alias (see {@link ExternalPermissionPersistServiceImpl#resolveDialectColumns()}).
 * This implementation forces the Oracle column names instead of resolving them from the
 * datasource type, and fails fast when the datasource dialect is not Oracle.
 *
 * @author liam
 */
public class OraclePermissionPersistServiceImpl extends ExternalPermissionPersistServiceImpl {
    
    @PostConstruct
    @Override
    protected void init() {
        super.init();
        String dataSourceType = getDataSourceType();
        if (!PersistenceConstant.ORACLE.equalsIgnoreCase(dataSourceType)) {
            throw new IllegalStateException(
                "nacos.plugin.auth.type=oracle requires nacos.plugin.datasource-dialect.type=oracle, "
                    + "but the current datasource dialect is '" + dataSourceType
                    + "'. Please align the two properties or use the default nacos auth type.");
        }
    }
    
    @Override
    protected void resolveDialectColumns() {
        resourceColumn = "resources";
        resourceAlias = "\"resource\"";
    }
}
