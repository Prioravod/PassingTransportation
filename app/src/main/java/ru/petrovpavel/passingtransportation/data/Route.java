package ru.petrovpavel.passingtransportation.data;

import java.text.MessageFormat;
import java.util.Optional;

import lombok.Data;

@Data
public class Route {

    private static final String DISPLAY_TEMPLATE = "{0} -> {1} {3} кг {4} р";

    private MapPoint destination;

    private MapPoint origin;

    private Integer payment;

    private Integer weight;

    @Override
    public String toString() {
        String from = Optional.ofNullable(origin)
                .map(MapPoint::getAlias)
                .orElse("");
        String to = Optional.ofNullable(destination)
                .map(MapPoint::getAlias)
                .orElse("");
        return MessageFormat.format(DISPLAY_TEMPLATE, from, to, weight.toString(), payment.toString());
    }
}
