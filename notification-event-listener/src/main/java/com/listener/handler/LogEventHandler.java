package com.listener.handler;

import static com.listener.beans.NotificationType.INSERT;
import static com.listener.beans.NotificationType.UPDATE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntPredicate;

import javax.annotation.PostConstruct;

import com.listener.dao.ColumnsDao;
import com.listener.services.Producer;
import org.springframework.stereotype.Component;

import com.listener.beans.NotificationDataRow;
import com.listener.beans.NotificationEvent;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.EventHeader;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventHandler implements BinaryLogClient.EventListener {

    private final Map<String, Long> tableMap = new HashMap<>();
    private final Producer producer;
    private final ColumnsDao columnsDao;
    /**
     * Predicate to check whether column ordinal position has a corresponding column name associated
     * or not
     */
    private IntPredicate hasColPosToName;

    /**
     * Function to get column name associated with ordinal position
     */
    private Function<Integer, String> colPosToNameFn;

    /**
     * Initialize column metadata for "JOBINSTANCEBEAN" table
     */
    @PostConstruct
    private void init() {
        hasColPosToName = position -> columnsDao.columnNameForOrdinalPosition(ColumnsDao.PRODUCT_DISTRIBUTION,
                                                                              position)
                                                .isPresent();
        colPosToNameFn = i -> columnsDao.columnNameForOrdinalPosition(ColumnsDao.PRODUCT_DISTRIBUTION, i).get();

    }

    @Override
    public void onEvent(Event event) {

        EventData data = event.getData();

        final EventHeader header = event.getHeader();
        log.info("Event Header : {}", header);
        log.debug("Event Data : {}", data);

        switch (header.getEventType()) {
            case TABLE_MAP:
                TableMapEventData tableData =
                                (TableMapEventData) requireNonNull(data,
                                                                   "Empty binary log event data");;
                tableMap.put(tableData.getTable(), tableData.getTableId());
                break;
            case EXT_DELETE_ROWS:
                log.info("Ext Delete Rows Event Ignored");
                break;
            case EXT_UPDATE_ROWS:
                UpdateRowsEventData updatedData =
                                (UpdateRowsEventData) requireNonNull(data,
                                                                     "Empty binary log event data");
                if (updatedData.getTableId() == tableMap.getOrDefault(ColumnsDao.PRODUCT_DISTRIBUTION, 0L)) {

                    final int oldColumnSize = updatedData.getIncludedColumnsBeforeUpdate().length();
                    final int newColumnSize = updatedData.getIncludedColumns().length();

                    NotificationEvent notificationEvent = new NotificationEvent();
                    notificationEvent.setNotificationType(UPDATE);
                    notificationEvent.setUtcTimestamp(header.getTimestamp());

                    updatedData.getRows().parallelStream().map(entry -> {
                        NotificationDataRow dataRow = new NotificationDataRow();
                        mapValuesToDataRow(newColumnSize,
                                           dataRow::setNewValues).apply(entry.getValue());
                        return mapValuesToDataRow(oldColumnSize,
                                                  dataRow::setOldValues).apply(entry.getKey());
                    }).forEach(notificationEvent.getNotificationData()::add);

                    log.info("Event Type: {}, No of inserted rows: {}, included columns before : {}, after : {}",
                             header.getEventType().name(), updatedData.getRows().size(),
                             oldColumnSize, newColumnSize);
                    producer.produceMsg(notificationEvent);
                }
                break;
            case EXT_WRITE_ROWS:
                WriteRowsEventData insertedData =
                                (WriteRowsEventData) requireNonNull(data,
                                                                    "Empty binary log event data");;
                if (insertedData.getTableId() == tableMap.getOrDefault(ColumnsDao.PRODUCT_DISTRIBUTION, 0L)) {

                    final int noOfIncludedColumns = insertedData.getIncludedColumns().length();

                    log.info("Event Type : {}, No of inserted rows: {}, included columns : {}",
                             header.getEventType().name(), insertedData.getRows().size(),
                             noOfIncludedColumns);

                    NotificationEvent notificationEvent = new NotificationEvent();
                    notificationEvent.setNotificationType(INSERT);
                    notificationEvent.setUtcTimestamp(header.getTimestamp());

                    insertedData.getRows()
                                .parallelStream()
                                .map(mapValuesToDataRow(noOfIncludedColumns,
                                                        NotificationDataRow::new))
                                .forEach(notificationEvent.getNotificationData()::add);


                    producer.produceMsg(notificationEvent);
                }
                break;
            case UNKNOWN:
            case START_V3:
            case QUERY:
            case STOP:
            case XA_PREPARE:
            case VIEW_CHANGE:
            case TRANSACTION_CONTEXT:
            case PREVIOUS_GTIDS:
            case ANONYMOUS_GTID:
            case GTID:
            case ROWS_QUERY:
            case IGNORABLE:
            case HEARTBEAT:
            case INCIDENT:
            case DELETE_ROWS:
            case UPDATE_ROWS:
            case WRITE_ROWS:
            case PRE_GA_DELETE_ROWS:
            case PRE_GA_UPDATE_ROWS:
            case PRE_GA_WRITE_ROWS:
            case EXECUTE_LOAD_QUERY:
            case BEGIN_LOAD_QUERY:
            case XID:
            case FORMAT_DESCRIPTION:
            case USER_VAR:
            case RAND:
            case NEW_LOAD:
            case DELETE_FILE:
            case EXEC_LOAD:
            case APPEND_BLOCK:
            case CREATE_FILE:
            case SLAVE:
            case LOAD:
            case INTVAR:
            case ROTATE:
            default:
                break;

        }
    }

    /**
     * Method to map column values from the binary log data to {@link NotificationDataRow}. This
     * will skip columns with {@code null} values
     *
     * @param noOfIncludedColumns no of included columns in the event
     * @param dataRowFunction Function to add column values to data row
     *
     * @return instance of {@link NotificationDataRow}
     */
    private Function<Serializable[], NotificationDataRow> mapValuesToDataRow(int noOfIncludedColumns,
                                                                             Function<Map<String, ? extends Serializable>, NotificationDataRow> dataRowFunction) {
        return mapRowValuesToColumns(noOfIncludedColumns).andThen(dataRowFunction);
    }

    /**
     * Method to map column values from the binary log data to corresponding column names. This will
     * skip columns with {@code null} values
     *
     * @param noOfIncludedColumns no of included columns in the event
     *
     * @return map of column name to corresponding value
     */
    private Function<Serializable[], Map<String, ? extends Serializable>> mapRowValuesToColumns(int noOfIncludedColumns) {
        log.info("No of columns : {}", noOfIncludedColumns);
        return columns -> range(0,
                                noOfIncludedColumns).parallel()
                                                    .filter(hasColPosToName.and(i -> columns[i] != null))
                                                    .boxed()
                                                    .collect(toMap(colPosToNameFn, i -> columns[i],
                                                                   (a, b) -> b));
    }


}
