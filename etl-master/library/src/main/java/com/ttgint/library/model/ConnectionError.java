package com.ttgint.library.model;

import com.ttgint.library.record.ConnectionRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_connection_error")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionError implements Serializable {

    @Id
    @SequenceGenerator(name = "t_connection_error_seq_id", sequenceName = "t_connection_error_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_connection_error_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private Long connectionId;
    private String connectionHistoryCode;
    private String errorCode;
    private String errorSource;
    private String errorMessage;
    private String errorDetail;
    private OffsetDateTime errorTime;

    public static ConnectionError getEntity(ConnectionRecord connectionRecord,
                                            String errorCode,
                                            String errorSource,
                                            String errorMessage) {
        ConnectionError result = new ConnectionError();
        result.setFlowId(connectionRecord.getFlowId());
        result.setFlowProcessCode(connectionRecord.getFlowProcessCode());
        result.setConnectionId(connectionRecord.getConnectionId());
        result.setConnectionHistoryCode(connectionRecord.getConnectionHistoryCode());
        result.setErrorCode(errorCode);
        result.setErrorSource(errorSource);
        result.setErrorMessage(errorMessage);
        result.setErrorTime(OffsetDateTime.now());
        return result;
    }
}