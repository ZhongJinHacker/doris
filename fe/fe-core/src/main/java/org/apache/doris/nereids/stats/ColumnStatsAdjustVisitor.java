// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.stats;

import org.apache.doris.nereids.trees.expressions.Cast;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.expressions.visitor.ExpressionVisitor;
import org.apache.doris.statistics.ColumnStatistic;
import org.apache.doris.statistics.Statistics;

/**
 * table: T(A, B)
 * T.stats = (rows=10,
 *            {
 *                A->ndv=10, rows=10
 *                B->...
 *            }
 *           )
 * after node: filter(cast(A as double)=1.0)
 * filter.stats = (rows = 1
 *          {
 *           A->ndv=m, rows=1
 *           B->ndv=m, rows=1
 *           cast(A as double) -> ndv=1, rows=1
 *          }
 *         )
 *
 * m is computed by function computeNdv()
 *
 * filter.stats should be adjusted.
 * A.columnStats should be equal to "cast(A as double)".columnStats
 * for other expressions(except cast), we also need to adjust their input column stats.
 *
 */
public class ColumnStatsAdjustVisitor extends ExpressionVisitor<ColumnStatistic, Statistics> {
    @Override
    public ColumnStatistic visit(Expression expr, Statistics context) {
        expr.children().forEach(child -> child.accept(this, context));
        return null;
    }

    public ColumnStatistic visitCast(Cast cast, Statistics context) {
        ColumnStatistic colStats = context.findColumnStatistics(cast);
        if (colStats != null) {
            context.addColumnStats(cast.child(), colStats);
        }
        return null;
    }
}
