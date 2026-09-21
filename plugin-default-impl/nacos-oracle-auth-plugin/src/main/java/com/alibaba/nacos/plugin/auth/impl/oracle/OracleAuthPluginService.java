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

package com.alibaba.nacos.plugin.auth.impl.oracle;

import com.alibaba.nacos.api.plugin.ConfigItemDefinition;
import com.alibaba.nacos.api.plugin.ConfigItemEffectMode;
import com.alibaba.nacos.api.plugin.ConfigItemType;
import com.alibaba.nacos.plugin.auth.impl.NacosAuthPluginService;
import com.alibaba.nacos.plugin.auth.impl.configuration.NacosAuthPluginConfig;
import com.alibaba.nacos.plugin.auth.impl.constant.AuthConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Oracle adapted auth plugin service. Identity model, login and authorization behaviour are the
 * same as the default Nacos auth plugin; this plugin type additionally activates the
 * Oracle-adapted permission persistence (see {@code OracleAuthPluginAutoConfig}) because
 * {@code resource} is a reserved word on Oracle and the permission column is named
 * {@code resources} there.
 *
 * <p>Activate with {@code nacos.plugin.auth.type=oracle} together with
 * {@code nacos.plugin.datasource-dialect.type=oracle}. All configuration items mirror
 * {@code auth:nacos} and additionally accept the {@code nacos.plugin.auth.nacos.*} keys as
 * aliases, so deployments only need one set of keys.</p>
 *
 * @author liam
 */
public class OracleAuthPluginService extends NacosAuthPluginService {

    public static final String AUTH_PLUGIN_TYPE = "oracle";

    private static final List<ConfigItemDefinition> CONFIG_DEFINITIONS =
        buildConfigDefinitions();

    private static List<ConfigItemDefinition> buildConfigDefinitions() {
        ConfigItemDefinition secret = new ConfigItemDefinition.Builder(
            NacosAuthPluginConfig.TOKEN_SECRET_KEY, "Token secret key", ConfigItemType.STRING)
            .description("Base64-encoded key used to sign Nacos authentication tokens")
            .defaultValue(AuthConstants.DEFAULT_TOKEN_SECRET_KEY)
            .aliases(Arrays.asList(AuthConstants.TOKEN_SECRET_KEY,
                "nacos.plugin.auth.nacos.token.secret.key"))
            .sensitive(true).effectMode(ConfigItemEffectMode.RESTART).build();
        ConfigItemDefinition expiration = new ConfigItemDefinition.Builder(
            NacosAuthPluginConfig.TOKEN_EXPIRE_SECONDS, "Token expiration", ConfigItemType.NUMBER)
            .description("Token validity period in seconds")
            .defaultValue(AuthConstants.DEFAULT_TOKEN_EXPIRE_SECONDS.toString())
            .aliases(Arrays.asList(AuthConstants.TOKEN_EXPIRE_SECONDS,
                "nacos.plugin.auth.nacos.token.expire.seconds"))
            .effectMode(ConfigItemEffectMode.RUNTIME).build();
        ConfigItemDefinition tokenCache = new ConfigItemDefinition.Builder(
            NacosAuthPluginConfig.TOKEN_CACHE_ENABLE, "Token cache", ConfigItemType.BOOLEAN)
            .description("Cache issued and parsed authentication tokens")
            .defaultValue(Boolean.FALSE.toString())
            .aliases(Arrays.asList(AuthConstants.TOKEN_CACHE_ENABLE,
                "nacos.plugin.auth.nacos.token.cache.enable"))
            .effectMode(ConfigItemEffectMode.RUNTIME).build();
        ConfigItemDefinition authCache = new ConfigItemDefinition.Builder(
            NacosAuthPluginConfig.CACHING_ENABLED, "Authorization cache", ConfigItemType.BOOLEAN)
            .description("Cache users, roles and permissions")
            .defaultValue(Boolean.TRUE.toString())
            .aliases(Arrays.asList(AuthConstants.NACOS_CORE_AUTH_CACHING_ENABLED,
                "nacos.plugin.auth.nacos.caching.enabled"))
            .effectMode(ConfigItemEffectMode.RUNTIME).build();
        ConfigItemDefinition anonymous = new ConfigItemDefinition.Builder(
            NacosAuthPluginConfig.ANONYMOUS_AI_ENABLED, "Anonymous AI access",
            ConfigItemType.BOOLEAN)
            .description("Allow anonymous access to explicitly opted-in AI endpoints")
            .defaultValue(Boolean.FALSE.toString())
            .aliases(Arrays.asList(AuthConstants.NACOS_CORE_AUTH_NACOS_ANONYMOUS_AI_ENABLED,
                "nacos.plugin.auth.nacos.anonymous.ai.enabled"))
            .effectMode(ConfigItemEffectMode.RUNTIME).build();
        return Collections.unmodifiableList(
            Arrays.asList(secret, expiration, tokenCache, authCache, anonymous));
    }

    @Override
    public String getAuthServiceName() {
        return AUTH_PLUGIN_TYPE;
    }

    @Override
    public List<ConfigItemDefinition> getConfigDefinitions() {
        return CONFIG_DEFINITIONS;
    }

    @Override
    public synchronized void applyConfig(Map<String, String> effectiveConfig) {
        // effective config for auth:oracle is resolved through the same keys as auth:nacos
        // (via aliases); the default parsing and token handling apply unchanged.
        super.applyConfig(effectiveConfig);
    }
}
