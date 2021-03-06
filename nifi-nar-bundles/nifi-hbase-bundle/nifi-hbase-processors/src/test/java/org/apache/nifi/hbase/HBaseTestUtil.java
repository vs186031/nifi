/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.hbase;

import org.apache.nifi.hbase.put.PutColumn;
import org.apache.nifi.hbase.put.PutFlowFile;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class HBaseTestUtil {

    public static void verifyPut(final String row, final String columnFamily, final Map<String,String> columns, final List<PutFlowFile> puts) {
        boolean foundPut = false;

        for (final PutFlowFile put : puts) {
            if (!row.equals(put.getRow())) {
                continue;
            }

            if (put.getColumns() == null || put.getColumns().size() != columns.size()) {
                continue;
            }

            // start off assuming we have all the columns
            boolean foundAllColumns = true;

            for (Map.Entry<String, String> entry : columns.entrySet()) {
                // determine if we have the current expected column
                boolean foundColumn = false;
                for (PutColumn putColumn : put.getColumns()) {
                    final String colVal = new String(putColumn.getBuffer(), StandardCharsets.UTF_8);
                    if (columnFamily.equals(putColumn.getColumnFamily()) && entry.getKey().equals(putColumn.getColumnQualifier())
                            && entry.getValue().equals(colVal)) {
                        foundColumn = true;
                        break;
                    }
                }

                // if we didn't have the current expected column we know we don't have all expected columns
                if (!foundColumn) {
                    foundAllColumns = false;
                    break;
                }
            }

            // if we found all the expected columns this was a match so we can break
            if (foundAllColumns) {
                foundPut = true;
                break;
            }
        }

        assertTrue(foundPut);
    }

    public static void verifyEvent(final List<ProvenanceEventRecord> events, final String uri, final ProvenanceEventType eventType) {
        boolean foundEvent = false;
        for (final ProvenanceEventRecord event : events) {
            if (event.getTransitUri().equals(uri) && event.getEventType().equals(eventType)) {
                foundEvent = true;
                break;
            }
        }
        assertTrue(foundEvent);
    }
}
