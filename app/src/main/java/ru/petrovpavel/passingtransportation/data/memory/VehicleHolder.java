package ru.petrovpavel.passingtransportation.data.memory;

import android.util.Log;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ru.petrovpavel.passingtransportation.data.Capacity;
import ru.petrovpavel.passingtransportation.data.Cargo;
import ru.petrovpavel.passingtransportation.data.Delay;
import ru.petrovpavel.passingtransportation.data.Vehicle;

public class VehicleHolder {

    public static final Integer UNDEFINED_CAPACITY = -1;
    public static final Integer UNDEFINED_DELAY = -1;
    private static final String TAG = "VehicleHolder";
    private boolean isEnabled = false;

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    private final Vehicle vehicle = new Vehicle();

    private static final VehicleHolder holder = new VehicleHolder();

    public static VehicleHolder getInstance() {
        return holder;
    }

    public int getAvailableCapacity() {
        return Optional.ofNullable(vehicle.getCapacity())
                .map(Capacity::getWeight)
                .orElse(VehicleHolder.UNDEFINED_CAPACITY);
    }

    public void setAvailableCapacity(Integer capacityWeight) {
        Capacity capacity = buildCapacity(capacityWeight); //TODO: rewrite to use Capacity argument
        vehicle.setCapacity(capacity);
    }

    public int getDelay() {
        return Optional.ofNullable(vehicle.getDelay())
                .map(Delay::getMinutesValue)
                .orElse(VehicleHolder.UNDEFINED_DELAY);
    }

    public void setDelay(Integer delayMinutes) {
        Delay delay = buildDelay(delayMinutes); //TODO: rewrite to use Delay argument
        vehicle.setDelay(delay);
    }

    public void loadCargo(Integer cargoWeight) {
        Cargo cargo = buildCargo(cargoWeight);//TODO: rewrite to use Cargo argument
        this.getCargoList().add(cargo);
    }

    public void unloadCargo(Integer cargoId) {
        Cargo cargo = this.getCargoList().get(cargoId); //TODO: rewrite to use Cargo argument
        this.getCargoHistory().add(cargo);
        Log.d(TAG, MessageFormat.format("unload cargo [id = %0]", cargo));
    }

    private Cargo buildCargo(Integer cargoWeight) {
        Cargo cargo = new Cargo();
        cargo.setWeight(cargoWeight);
        return cargo;
    }

    private Capacity buildCapacity(Integer capacityWeight) {
        Capacity capacity = new Capacity();
        capacity.setWeight(capacityWeight);
        return capacity;
    }

    private Delay buildDelay(Integer delayMinutes) {
        Delay delay = new Delay();
        delay.setMinutesValue(delayMinutes);
        return delay;
    }

    private List<Cargo> getCargoList() {
        if (vehicle.getCargoList() == null) {
            vehicle.setCargoList(new ArrayList<>());
        }
        return vehicle.getCargoList();
    }

    private List<Cargo> getCargoHistory() {
        if (vehicle.getCargoList() == null) {
            vehicle.setCargoList(new ArrayList<>());
        }
        return vehicle.getCargoHistory();
    }
}
