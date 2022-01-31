package ru.petrovpavel.passingtransportation.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.petrovpavel.passingtransportation.data.Route;
import ru.petrovpavel.passingtransportation.data.RouteStatus;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteWrapper extends Route {

    private RouteStatus status;
}

