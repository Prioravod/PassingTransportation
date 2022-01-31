package ru.petrovpavel.passingtransportation.data;

import java.text.MessageFormat;
import java.util.Optional;

import lombok.Data;

@Data
public class Route {

    private static final String DISPLAY_TEMPLATE = "{0} -> {1} {3} кг {4, number, double} р";

    private MapPoint origin;

    private MapPoint destination;

    private Double weight;

    private Double payment;

    @Override
    public String toString() {
        String from = Optional.ofNullable(origin)
                .map(MapPoint::getAlias)
                .orElse("");
        String to = Optional.ofNullable(destination)
                .map(MapPoint::getAlias)
                .orElse("");
        return MessageFormat.format(DISPLAY_TEMPLATE, from, to, weight, payment);
    }
}
