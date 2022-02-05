package ru.petrovpavel.passingtransportation.listener;

import androidx.annotation.NonNull;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VehicleSliderChangeListener implements Slider.OnChangeListener{

    private final TextInputLayout editTextIL;
    private final TextInputEditText textInputEditText;

    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        int roundedValue = Math.round(value);
        editTextIL.setErrorEnabled(false);
        textInputEditText.setText(String.valueOf(roundedValue));
    }
}
