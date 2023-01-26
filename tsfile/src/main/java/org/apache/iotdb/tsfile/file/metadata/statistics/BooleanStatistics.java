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
package org.apache.iotdb.tsfile.file.metadata.statistics;

import org.apache.iotdb.tsfile.exception.filter.StatisticsClassException;
import org.apache.iotdb.tsfile.file.metadata.enums.TSDataType;
import org.apache.iotdb.tsfile.utils.ReadWriteIOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BooleanStatistics extends Statistics<Boolean> {

  private boolean firstValue;
  private boolean lastValue;
  private long sumValue;

  static final int BOOLEAN_STATISTICS_FIXED_RAM_SIZE = 56;

  @Override
  public TSDataType getType() {
    return TSDataType.BOOLEAN;
  }

  @Override
  public int getStatsSize() {
    return 10;
  }

  @Override
  public void setMinInfo(MinMaxInfo minInfo) {}

  @Override
  public void setMaxInfo(MinMaxInfo maxInfo) {}
  /**
   * initialize boolean Statistics.
   *
   * @param firstValue first boolean value
   * @param lastValue last boolean value
   */
  public void initializeStats(boolean firstValue, boolean lastValue, long sum) {
    this.firstValue = firstValue;
    this.lastValue = lastValue;
    this.sumValue = sum;
  }

  private void updateStats(boolean lastValue, long sum) {
    this.lastValue = lastValue;
    this.sumValue += sum;
  }

  private void updateStats(
      boolean firstValue, boolean lastValue, long startTime, long endTime, long sum) {
    // only if endTime greater or equals to the current endTime need we update the last value
    // only if startTime less or equals to the current startTime need we update the first value
    // otherwise, just ignore
    if (startTime <= this.getStartTime()) {
      this.firstValue = firstValue;
    }
    if (endTime >= this.getEndTime()) {
      this.lastValue = lastValue;
    }
    this.sumValue += sum;
  }

  @Override
  void updateStats(boolean value) {
    if (isEmpty) {
      initializeStats(value, value, value ? 1 : 0);
      isEmpty = false;
    } else {
      updateStats(value, value ? 1 : 0);
    }
  }

  @Override
  void updateStats(boolean[] values, int batchSize) {
    for (int i = 0; i < batchSize; i++) {
      updateStats(values[i]);
    }
  }

  @Override
  public long calculateRamSize() {
    return BOOLEAN_STATISTICS_FIXED_RAM_SIZE;
  }

  //  @Override
  //  public void setMinMaxFromBytes(byte[] minBytes, byte[] maxBytes) {}
  /** @author Yuyuan Kang */
  @Override
  public MinMaxInfo<Boolean> getMinInfo() {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "min"));
  }

  /** @author Yuyuan Kang */
  @Override
  public MinMaxInfo<Boolean> getMaxInfo() {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "max"));
  }

  /** @author Yuyuan Kang */
  @Override
  public Boolean getMinValue() {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "min"));
  }

  /** @author Yuyuan Kang */
  @Override
  public Boolean getMaxValue() {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "max"));
  }

  /** @author Yuyuan Kang */
  @Override
  public long getBottomTimestamp() {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "min"));
  }

  /** @author Yuyuan Kang */
  @Override
  public long getTopTimestamp() {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "max"));
  }

  @Override
  public Boolean getFirstValue() {
    return firstValue;
  }

  @Override
  public Boolean getLastValue() {
    return lastValue;
  }

  /** @author Yuyuan Kang */
  @Override
  public double getSumDoubleValue() {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "double sum"));
  }

  @Override
  public long getSumLongValue() {
    return sumValue;
  }

  @Override
  protected void mergeStatisticsValue(Statistics stats) {
    BooleanStatistics boolStats = (BooleanStatistics) stats;
    if (isEmpty) {
      initializeStats(boolStats.getFirstValue(), boolStats.getLastValue(), boolStats.sumValue);
      isEmpty = false;
    } else {
      updateStats(
          boolStats.getFirstValue(),
          boolStats.getLastValue(),
          stats.getStartTime(),
          stats.getEndTime(),
          boolStats.sumValue);
    }
  }

  /** @author Yuyuan Kang */
  @Override
  public void updateMinInfo(Boolean val, long timestamp) {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "min"));
  }

  /** @author Yuyuan Kang */
  @Override
  public void updateMaxInfo(Boolean val, long timestamp) {
    throw new StatisticsClassException(
        String.format(OPERATION_NOT_SUPPORT_FORMAT, "Boolean", "max"));
  }

  @Override
  public int serializeStats(OutputStream outputStream) throws IOException {
    int byteLen = 0;
    byteLen += ReadWriteIOUtils.write(firstValue, outputStream);
    byteLen += ReadWriteIOUtils.write(lastValue, outputStream);
    byteLen += ReadWriteIOUtils.write(sumValue, outputStream);
    return byteLen;
  }

  @Override
  public void deserialize(InputStream inputStream) throws IOException {
    this.firstValue = ReadWriteIOUtils.readBool(inputStream);
    this.lastValue = ReadWriteIOUtils.readBool(inputStream);
    this.sumValue = ReadWriteIOUtils.readLong(inputStream);
  }

  @Override
  public void deserialize(ByteBuffer byteBuffer) {
    this.firstValue = ReadWriteIOUtils.readBool(byteBuffer);
    this.lastValue = ReadWriteIOUtils.readBool(byteBuffer);
    this.sumValue = ReadWriteIOUtils.readLong(byteBuffer);
  }

  @Override
  public String toString() {
    return super.toString()
        + " [firstValue="
        + firstValue
        + ", lastValue="
        + lastValue
        + ", sumValue="
        + sumValue
        + ']';
  }
}
