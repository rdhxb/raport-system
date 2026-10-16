package rdhxb.common.krs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KRSNumberModel {
    private UUID id;
    private String KrsNum;
}
