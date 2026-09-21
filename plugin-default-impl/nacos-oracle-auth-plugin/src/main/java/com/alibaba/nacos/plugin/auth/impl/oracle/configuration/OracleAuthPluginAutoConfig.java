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

package com.alibaba.nacos.plugin.auth.impl.oracle.configuration;

import com.alibaba.nacos.plugin.auth.impl.condition.ConditionOnInnerDatasource;
import com.alibaba.nacos.plugin.auth.impl.oracle.OracleAuthPluginService;
import com.alibaba.nacos.plugin.auth.impl.oracle.persistence.OraclePermissionPersistServiceImpl;
import com.alibaba.nacos.plugin.auth.impl.persistence.PermissionPersistService;
import com.alibaba.nacos.persistence.configuration.condition.ConditionOnExternalStorage;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * Auto configuration of the Oracle auth plugin. Activated by
 * {@code nacos.plugin.auth.type=oracle} on server deployments with external storage; registers the
 * Oracle-adapted {@link PermissionPersistService} before the default auth plugin assembly so the
 * default bean backs off via {@code @ConditionalOnMissingBean}.
 *
 * @author liam
 */
@AutoConfigureBefore(name = {
    "com.alibaba.nacos.plugin.auth.impl.configuration.autoconfiguration.NacosAuthPluginInnerAutoConfig",
    "com.alibaba.nacos.plugin.auth.impl.configuration.autoconfiguration.NacosAuthPluginRemoteAutoConfig"})
@ConditionalOnProperty(name = "nacos.plugin.auth.type",
    havingValue = OracleAuthPluginService.AUTH_PLUGIN_TYPE)
@Conditional(value = {ConditionOnInnerDatasource.class, ConditionOnExternalStorage.class})
public class OracleAuthPluginAutoConfig {
    
    @Bean
    public PermissionPersistService permissionPersistService() {
        return new OraclePermissionPersistServiceImpl();
    }
}
