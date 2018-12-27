/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.optimizer.impl.ddl.dialect.mysql;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.parsing.antlr.optimizer.impl.ddl.AlterTableOptimizer;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnAfterPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnFirstPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;

import java.util.Iterator;
import java.util.List;

/**
 * Alter table statement optimizer for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLAlterTableOptimizer extends AlterTableOptimizer {
    
    @Override
    protected void adjustColumnDefinition(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> newColumnMetaData) {
        for (ColumnPositionSegment each : alterTableStatement.getChangedPositionColumns()) {
            if (each instanceof ColumnFirstPositionSegment) {
                adjustFirst(newColumnMetaData, (ColumnFirstPositionSegment) each);
            } else {
                adjustAfter(newColumnMetaData, (ColumnAfterPositionSegment) each);
            }
        }
    }
    
    private void adjustFirst(final List<ColumnMetaData> newColumnMetaData, final ColumnFirstPositionSegment columnFirstPositionSegment) {
        ColumnMetaData firstMetaData = null;
        Iterator<ColumnMetaData> iterator = newColumnMetaData.iterator();
        while (iterator.hasNext()) {
            ColumnMetaData each = iterator.next();
            if (each.getColumnName().equals(columnFirstPositionSegment.getColumnName())) {
                firstMetaData = each;
                iterator.remove();
                break;
            }
        }
        if (null != firstMetaData) {
            newColumnMetaData.add(0, firstMetaData);
        }
    }
    
    private void adjustAfter(final List<ColumnMetaData> newColumnMetaData, final ColumnAfterPositionSegment columnAfterPositionSegment) {
        int afterIndex = -1;
        int adjustColumnIndex = -1;
        for (int i = 0; i < newColumnMetaData.size(); i++) {
            if (newColumnMetaData.get(i).getColumnName().equals(columnAfterPositionSegment.getColumnName())) {
                adjustColumnIndex = i;
            }
            if (newColumnMetaData.get(i).getColumnName().equals(columnAfterPositionSegment.getAfterColumnName())) {
                afterIndex = i;
            }
            if (adjustColumnIndex >= 0 && afterIndex >= 0) {
                break;
            }
        }
        if (adjustColumnIndex >= 0 && afterIndex >= 0 && adjustColumnIndex != afterIndex + 1) {
            ColumnMetaData adjustColumnMetaData = newColumnMetaData.remove(adjustColumnIndex);
            if (afterIndex < adjustColumnIndex) {
                afterIndex = afterIndex + 1;
            }
            newColumnMetaData.add(afterIndex, adjustColumnMetaData);
        }
    }
}
