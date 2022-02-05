package ru.petrovpavel.passingtransportation.listener;

import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import ru.petrovpavel.passingtransportation.data.Vehicle;
import ru.petrovpavel.passingtransportation.data.memory.VehicleHolder;

@RequiredArgsConstructor
public class OnSearchPassingRoutesCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

    private final TextInputEditText capacityValue;
    private final TextInputEditText delayValue;

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isEnabled) {
        VehicleHolder vehicleHolder = VehicleHolder.getInstance();
        vehicleHolder.setEnabled(isEnabled);
        vehicleHolder.setAvailableCapacity(getNumericValue(capacityValue));
        vehicleHolder.setDelay(getNumericValue(delayValue));
    }

    private Integer getNumericValue(TextInputEditText delayValue) {
        return Optional.ofNullable(delayValue)
                .map(AppCompatEditText::getText)
                .map(Object::toString)
                .map(Integer::parseInt)
                .orElse(null);
    }
}
