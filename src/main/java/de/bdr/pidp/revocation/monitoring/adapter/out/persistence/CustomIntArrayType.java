/*
 * Copyright 2024-2026 Bundesdruckerei GmbH
 * For the license see the accompanying file LICENSE.MD.
 */
package de.bdr.pidp.revocation.monitoring.adapter.out.persistence;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class CustomIntArrayType implements UserType<Integer[]> {
    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public Integer[] nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        var array = rs.getArray(position);
        return array != null ? (Integer[]) array.getArray() : null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Integer[] value, int position, WrapperOptions options) throws SQLException {
        if (st != null) {
            if (value != null) {
                var connection = st.getConnection();
                var array = connection.createArrayOf("int", value);
                st.setArray(position, array);
            } else {
                st.setNull(position, Types.ARRAY);
            }
        }
    }

    @Override
    public Class<Integer[]> returnedClass() {
        return Integer[].class;
    }

    @Override
    public Integer[] deepCopy(Integer[] value) {
        return value == null ? null : Arrays.copyOf(value, value.length);
    }

    @Override
    public boolean isMutable() {
        return false;
    }
}
