package ru.petrovpavel.passingtransportation.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.RouteStatus;

@Data
@AllArgsConstructor
public class RouteWrapper {

    private Route route;

    private RouteStatus status;
}

