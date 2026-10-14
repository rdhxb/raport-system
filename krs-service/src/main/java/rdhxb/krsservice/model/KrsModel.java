package rdhxb.krsservice.model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class KrsModel {
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String krsNumber;

//    ... other information will implement in a while this will be entity
}
