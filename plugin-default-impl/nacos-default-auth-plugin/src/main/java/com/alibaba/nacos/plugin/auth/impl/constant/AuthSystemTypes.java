/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.constant;

/**
 * Types of all auth implementations.
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
public enum AuthSystemTypes {
    
    /**
     * Nacos builtin auth system.
     */
    NACOS,
    /**
     * LDAP.
     */
    LDAP,
    /**
     * Oracle adapted auth system: same identity model as {@link #NACOS} (local users table with
     * bcrypt passwords), selected to activate a database-specific auth plugin such as
     * nacos-oracle-auth-plugin.
     */
    ORACLE;
    
    /**
     * Whether the given auth type uses the Nacos local-user identity model (users table with
     * bcrypt passwords), so login and admin initialization are supported the same way as
     * {@link #NACOS}.
     *
     * @param authSystemType auth type name, case-insensitive
     * @return {@code true} if the type is NACOS or ORACLE
     */
    public static boolean isNacosCompatible(String authSystemType) {
        return NACOS.name().equalsIgnoreCase(authSystemType)
            || ORACLE.name().equalsIgnoreCase(authSystemType);
    }
}
