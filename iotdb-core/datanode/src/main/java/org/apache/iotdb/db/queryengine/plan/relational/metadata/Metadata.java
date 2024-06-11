/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.queryengine.plan.relational.metadata;

import java.util.Map;
import org.apache.iotdb.commons.partition.DataPartition;
import org.apache.iotdb.commons.partition.DataPartitionQueryParam;
import org.apache.iotdb.db.queryengine.common.MPPQueryContext;
import org.apache.iotdb.db.queryengine.common.SessionInfo;
import org.apache.iotdb.db.queryengine.plan.analyze.schema.ISchemaComputationWithAutoCreation;
import org.apache.iotdb.db.queryengine.plan.relational.function.OperatorType;
import org.apache.iotdb.db.queryengine.plan.relational.security.AccessControl;
import org.apache.iotdb.db.queryengine.plan.relational.sql.ast.Expression;
import org.apache.iotdb.db.queryengine.plan.relational.type.TypeNotFoundException;
import org.apache.iotdb.db.queryengine.plan.relational.type.TypeSignature;

import org.apache.tsfile.read.common.type.Type;

import java.util.List;
import java.util.Optional;

public interface Metadata {

  boolean tableExists(QualifiedObjectName name);

  /**
   * Return table schema definition for the specified table handle. Table schema definition is a set
   * of information required by semantic analyzer to analyze the query.
   *
   * @throws RuntimeException if table handle is no longer valid
   */
  Optional<TableSchema> getTableSchema(SessionInfo session, QualifiedObjectName name);

  Type getOperatorReturnType(OperatorType operatorType, List<? extends Type> argumentTypes)
      throws OperatorNotFoundException;

  Type getFunctionReturnType(String functionName, List<? extends Type> argumentTypes);

  boolean isAggregationFunction(
      SessionInfo session, String functionName, AccessControl accessControl);

  Type getType(TypeSignature signature) throws TypeNotFoundException;

  boolean canCoerce(Type from, Type to);

  /**
   * get all device ids and corresponding attributes from schema region
   *
   * @param tableName qualified table name
   * @param expressionList device filter in conj style, need to remove all the deviceId filter after
   *     index scanning
   * @param attributeColumns attribute column names
   */
  List<DeviceEntry> indexScan(
      QualifiedObjectName tableName,
      List<Expression> expressionList,
      List<String> attributeColumns);

  /**
   * Fetch and compute the schema of target timeseries, with device and measurement defined in given
   * schemaComputationWithAutoCreation. The computation defined in given
   * schemaComputationWithAutoCreation will be executed during scanning the fetched schema. If some
   * target timeseries doesn't exist, they will be auto created.
   *
   * @param schemaComputationWithAutoCreationList define the target devices, measurements and
   *     computation
   */
  default void fetchAndComputeSchemaWithAutoCreate(
      List<? extends ISchemaComputationWithAutoCreation> schemaComputationWithAutoCreationList,
      MPPQueryContext context) {
    throw new UnsupportedOperationException();
  }

  /**
   * Get or create data partition, used in cluster write scenarios. if enableAutoCreateSchema is
   * true and database/series/time slots not exists, then automatically create.
   *
   * @param dataPartitionQueryParams the list of DataPartitionQueryParams
   * @param userName
   */
  default DataPartition getOrCreateDataPartition(
      List<DataPartitionQueryParam> dataPartitionQueryParams, String userName) {
    throw new UnsupportedOperationException();
  }
}
