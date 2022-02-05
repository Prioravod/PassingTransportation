package ru.petrovpavel.passingtransportation.listener;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VehicleTextInputEditTextListener implements View.OnKeyListener{

    private final TextInputLayout editTextIL;
    private final Slider slider;

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        // If the event is a key-down event on the "enter" button
        if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            onValueEntered(view, keyCode, keyEvent);
            return true;
        }
        return false;
    }

    private void onValueEntered(View view, int keyCode, KeyEvent keyEvent) {
        final int MIN_SLIDER_VALUE = Math.round(slider.getValueFrom());
        final int MAX_SLIDER_VALUE = Math.round(slider.getValueTo());

        String enteredValue = Optional.ofNullable(editTextIL)
                .map(TextInputLayout::getEditText)
                .map(EditText::getText)
                .map(Object::toString)
                .orElse(StringUtils.EMPTY);

        if (enteredValue.equals(StringUtils.EMPTY) || enteredValue.contains(StringUtils.LF)) {
            editTextIL.setError("Cannot be blank");
            editTextIL.setErrorEnabled(true);
        }
        else {
            int enteredIntValue = Integer.parseInt(enteredValue);

            if (isValueInRange(enteredIntValue, MIN_SLIDER_VALUE, MAX_SLIDER_VALUE)) {
                Snackbar.make(view, enteredValue, Snackbar.LENGTH_SHORT).show();
                editTextIL.setErrorEnabled(false);
                slider.setValue(enteredIntValue);
            } else {
                editTextIL.getEditText().setText(StringUtils.EMPTY);
                editTextIL.setError("Number must be between " + MIN_SLIDER_VALUE + "-" + MAX_SLIDER_VALUE);
                editTextIL.setErrorEnabled(true);
            }
        }
    }

    private boolean isValueInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

}
