package ru.petrovpavel.passingtransportation.listener;

import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import ru.petrovpavel.passingtransportation.data.Vehicle;
import ru.petrovpavel.passingtransportation.data.memory.VehicleHolder;

@RequiredArgsConstructor
public class OnSearchPassingRoutesCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

    private final TextInputEditText capacityValue;

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isEnabled) {
        VehicleHolder vehicleHolder = VehicleHolder.getInstance();
        vehicleHolder.setEnabled(isEnabled);
    }
}
