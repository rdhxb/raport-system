package rdhxb.krsservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestModel {
    private UUID id;
    private String testMess;
    private int testInt;
}
