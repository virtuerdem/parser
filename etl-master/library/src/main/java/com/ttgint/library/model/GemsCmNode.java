package com.ttgint.library.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;

@Table(schema = "cm", name = "node")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GemsCmNode implements Serializable {

    @Id
    private Long nodeId;
    private String nodeName;

}
