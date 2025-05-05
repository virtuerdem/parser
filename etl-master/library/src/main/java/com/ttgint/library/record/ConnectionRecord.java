package com.ttgint.library.record;

import com.ttgint.library.enums.*;
import com.ttgint.library.model.Connection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionRecord {

    private Long flowId;
    private String flowCode;
    private String flowProcessCode;
    private Long connectionId;
    private String connectionHistoryCode;

    @Enumerated(EnumType.STRING)
    private ConnectionProtocol conProtocolType;
    private String ip;
    private String userName;
    private String userPass;
    private Integer port;
    private String remotePath;

    @Enumerated(EnumType.STRING)
    private TransferWorkType transferWorkType;
    @Enumerated(EnumType.STRING)
    private PathWalkMethod pathWalkMethod;
    @Enumerated(EnumType.STRING)
    private FileTimeFilter readFileTimeFilter;
    @Enumerated(EnumType.STRING)
    private FileTimeFilter transferFileTimeFilter;

    private Integer transferTryCountMinLimit;
    private Integer transferTryCountMaxLimit;
    @Enumerated(EnumType.STRING)
    private TimeUnit fragmentTimeMinLimitUnit;
    private Long fragmentTimeMinLimit;
    @Enumerated(EnumType.STRING)
    private TimeUnit fragmentTimeMaxLimitUnit;
    private Long fragmentTimeMaxLimit;

    private OffsetDateTime lastModifiedTime;
    private Boolean isConnected;
    private String connectionTag;

    public static ConnectionRecord getRecord(Connection connection, String flowCode, String flowProcessCode) {
        ConnectionRecord record = new ConnectionRecord();
        record.setFlowId(connection.getFlowId());
        record.setFlowCode(flowCode);
        record.setFlowProcessCode(flowProcessCode);
        record.setConnectionId(connection.getId());
        record.setConnectionHistoryCode(
                OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + String.format("%1$" + 6 + "s", connection.getId()).replace(' ', '0'));
        record.setConProtocolType(connection.getServer().getConnectionProtocol());
        record.setIp(connection.getServer().getIp());
        record.setUserName(connection.getServer().getUserName());
        record.setUserPass(connection.getServer().getUserPass());
        record.setPort(connection.getServer().getConnectionPort());
        record.setRemotePath(connection.getPath().getRemotePath());
        record.setTransferWorkType(connection.getTransferWorkType());
        record.setPathWalkMethod(connection.getPathWalkMethod());
        record.setReadFileTimeFilter(connection.getReadFileTimeFilter());
        record.setTransferFileTimeFilter(connection.getTransferFileTimeFilter());
        record.setTransferTryCountMinLimit(connection.getTransferTryCountMinLimit());
        record.setTransferTryCountMaxLimit(connection.getTransferTryCountMaxLimit());
        record.setFragmentTimeMinLimitUnit(connection.getFragmentTimeMinLimitUnit());
        record.setFragmentTimeMinLimit(connection.getFragmentTimeMinLimit());
        record.setFragmentTimeMaxLimitUnit(connection.getFragmentTimeMaxLimitUnit());
        record.setFragmentTimeMaxLimit(connection.getFragmentTimeMaxLimit());
        record.setLastModifiedTime(connection.getLastModifiedTime());
        record.setIsConnected(false);
        record.setConnectionTag(connection.getConnectionTag());
        return record;
    }

}
