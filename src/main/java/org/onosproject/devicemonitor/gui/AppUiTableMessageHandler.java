/*
 * Copyright 2016 Open Networking Laboratory
 *
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
 */
package org.onosproject.devicemonitor.gui;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.devicemonitor.api.DeviceMonitorService;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DeviceId;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.table.TableModel;
import org.onosproject.ui.table.TableRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Override;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Skeletal ONOS UI Table-View message handler.
 */
public class AppUiTableMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_TABLE_DATA_REQ = "sampleTableDataRequest";
    private static final String SAMPLE_TABLE_DATA_RESP = "sampleTableDataResponse";
    private static final String SAMPLE_TABLES = "sampleTables";

    private static final String SAMPLE_TABLE_DETAIL_REQ = "sampleTableDetailsRequest";
    private static final String SAMPLE_TABLE_DETAIL_RESP = "sampleTableDetailsResponse";
    private static final String DETAILS = "details";

    private static final String NO_ROWS_MESSAGE = "No items found";

    private static final String ID = "id";
    private static final String LABEL = "label";
    private static final String CODE = "code";
    private static final String COMMENT = "comment";
    private static final String RESULT = "result";

    private static final String[] COLUMN_IDS = { ID, LABEL, CODE };

    private final Logger log = LoggerFactory.getLogger(getClass());


    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleTableDataRequestHandler(),
                new SampleTableDetailRequestHandler()
        );
    }

    // handler for sample table requests
    private final class SampleTableDataRequestHandler extends TableRequestHandler {

        private SampleTableDataRequestHandler() {
            super(SAMPLE_TABLE_DATA_REQ, SAMPLE_TABLE_DATA_RESP, SAMPLE_TABLES);
        }

        // if necessary, override defaultColumnId() -- if it isn't "id"

        @Override
        protected String[] getColumnIds() {
            return COLUMN_IDS;
        }

        // if required, override createTableModel() to set column formatters / comparators

        @Override
        protected String noRowsMessage(ObjectNode payload) {
            return NO_ROWS_MESSAGE;
        }

        @Override
        protected void populateTable(TableModel tm, ObjectNode payload) {
            // === NOTE: the table model supplied here will have been created
            // via  a call to createTableModel(). To assign non-default
            // cell formatters or comparators to the table model, override
            // createTableModel() and set them there.

            // === retrieve table row items from some service...
            // SomeService ss = get(SomeService.class);
            // List<Item> items = ss.getItems()

            // fake data for demonstration purposes...
            List<Item> items = getItems();
            for (Item item: items) {
                populateRow(tm.addRow(), item);
            }
        }

        private void populateRow(TableModel.Row row, Item item) {
            row.cell(ID, item.id())
                    .cell(LABEL, item.label())
                    .cell(CODE, item.code());
        }
    }


    // handler for sample item details requests
    private final class SampleTableDetailRequestHandler extends RequestHandler {

        private SampleTableDetailRequestHandler() {
            super(SAMPLE_TABLE_DETAIL_REQ);
        }

        @Override
        public void process(long sid, ObjectNode payload) {
            String id = string(payload, ID, "(none)");

            // SomeService ss = get(SomeService.class);
            // Item item = ss.getItemDetails(id)

            // fake data for demonstration purposes...
            Item item = getItem(id);

            ObjectNode rootNode = objectNode();
            ObjectNode data = objectNode();
            rootNode.set(DETAILS, data);

            if (item == null) {
                rootNode.put(RESULT, "Item with id '" + id + "' not found");
                log.warn("attempted to get item detail for id '{}'", id);

            } else {
                rootNode.put(RESULT, "Found item with id '" + id + "'");

                data.put(ID, item.id());
                data.put(LABEL, item.label());
                data.put(CODE, item.code());
                data.put(COMMENT, "The device is a forbidden for too many up/down.");
            }

            sendMessage(SAMPLE_TABLE_DETAIL_RESP, 0, rootNode);
        }
    }


    // ===================================================================
    // NOTE: The code below this line is to create fake data for this
    //       sample code. Normally you would use existing services to
    //       provide real data.

    // Lookup a single item.
    private static Item getItem(String id) {
        // We realize this code is really inefficient, but
        // it suffices for our purposes of demonstration...
        for (Item item : getItems()) {
            if (item.id().equals(id)) {
                return item;
            }
        }
        return null;
    }

    // Produce a list of items.
    private static List<Item> getItems() {
        DeviceMonitorService deviceMonitorService = AbstractShellCommand.get(DeviceMonitorService.class);
        Set<DeviceId> forbiddenSet = deviceMonitorService.getForbiddenDevices();
        List<Item> items = new ArrayList<>();
        int count = 0;
        for (DeviceId deviceId : forbiddenSet) {
            items.add(new Item(String.valueOf(++count), deviceId.toString(), deviceMonitorService.getDeviceCount(deviceId)));
        }
        return items;
    }

    // Simple model class to provide sample data
    private static class Item {
        private final String id;
        private final String label;
        private final long code;

        Item(String id, String label, long code) {
            this.id = id;
            this.label = label;
            this.code = code;
        }

        String id() { return id; }
        String label() { return label; }
        long code() { return code; }
    }
}