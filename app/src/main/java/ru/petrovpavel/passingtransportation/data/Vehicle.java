package ru.petrovpavel.passingtransportation.data;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Vehicle {

    private Long id;

    private Capacity capacity;

    private Delay delay;

    private Integer earnings;

    private List<Cargo> cargoList = new ArrayList<>();

    private List<Cargo> cargoHistory = new ArrayList<>();

    private Fuel fuelInfo;

}
