package ru.petrovpavel.passingtransportation.data;

import lombok.Getter;

@Getter
public enum RouteStatus {

    NEW,
    VIEWED,
    CONFIRMED,
    REJECTED,
    PASSED,
    RATED
}
