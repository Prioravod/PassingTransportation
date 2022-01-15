package ru.petrovpavel.passingtransportation.data;

import lombok.Data;

@Data
public class MapPoint {

    private String alias;

    private Double latitude;

    private Double longitude;
}
