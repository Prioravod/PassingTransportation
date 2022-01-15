package ru.petrovpavel.passingtransportation.data;

import lombok.Data;

@Data
public class Route {

    private MapPoint origin;

    private MapPoint destination;

    private Double weight;

    private Double payment;
}
