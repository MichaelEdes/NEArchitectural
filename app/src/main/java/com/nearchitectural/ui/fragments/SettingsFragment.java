package com.nearchitectural.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;
import com.nearchitectural.R;
import com.nearchitectural.databinding.FragmentSettingsBinding;
import com.nearchitectural.ui.activities.MapsActivity;
import com.nearchitectural.utilities.Settings;
import com.nearchitectural.utilities.SettingsManager;

/* Author:  Joel Bell-Wilding, Kristiyan Doykov
 * Since:   10/12/19
 * Version: 1.1
 * Purpose: Act as an interface between the user and the internal
 *          applications settings, allowing settings to be modified
 *          and saved to the device where desired.
 */
public class SettingsFragment extends Fragment {

    // Binding between location fragment and layout
    private FragmentSettingsBinding settingsBinding;

    // LAYOUT ELEMENTS
    private RadioGroup fontSizeSelection;
    private Switch locationEnabledSwitch;
    private RadioGroup distanceUnitSelection;
    private Slider maxDistanceSlider;
    private Button tagSelectButton;
    private TextView distanceSliderText;

    public static final String TAG = "SettingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Settings Manager class to handle retrieval or saving of settings when altered
        final SettingsManager settingsManager = new SettingsManager(getContext());
        // An instance of the Settings singleton through which settings can be read and modified
        final Settings userSettings = Settings.getInstance();

        // Data binding
        settingsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        settingsBinding.setSettings(Settings.getInstance()); // Set settings as data binding model

        // Bind all layout elements
        fontSizeSelection = settingsBinding.fontSizeChoices;
        locationEnabledSwitch = settingsBinding.locationEnabledSwitch;
        distanceSliderText = settingsBinding.distanceSliderText;
        maxDistanceSlider = settingsBinding.maxDistanceSlider;
        tagSelectButton = settingsBinding.selectTagsButton;
        distanceUnitSelection = settingsBinding.distanceUnitChoice;

        // Toggle the radiobutton of the font size which is currently active in settings
        switch (userSettings.getFontSize()) {
            case R.style.FontStyle_Small:
                settingsBinding.smallButton.toggle();
                break;
            case R.style.FontStyle_Large:
                settingsBinding.largeButton.toggle();
                break;
            default:
                settingsBinding.mediumButton.toggle();
                break;
        }

        // Listener for selection of button in font size Radio Group
        fontSizeSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Set the font size for the corresponding selected button
                if (checkedId == settingsBinding.smallButton.getId()) {
                    userSettings.setFontSize(R.style.FontStyle_Small);
                } else if (checkedId == settingsBinding.largeButton.getId()) {
                    userSettings.setFontSize(R.style.FontStyle_Large);
                } else {
                    userSettings.setFontSize(R.style.FontStyle_Medium);
                }
                settingsManager.saveSettings();

                // Refreshes the MapActivity UI to update the font-size once changed
                MapsActivity parentActivity = (MapsActivity) getActivity();
                assert parentActivity != null;
                parentActivity.getTheme().applyStyle(userSettings.getFontSize(), true);
                // Reopens the Settings page after the refresh
                Intent openSettingsIntent = new Intent(getContext(), MapsActivity.class);
                openSettingsIntent.putExtra("openFragment", "Settings");
                parentActivity.finish();
                // Removes animation from transition to appear smoother when changing font
                parentActivity.overridePendingTransition(0, 0);
                parentActivity.startActivity(openSettingsIntent);
                parentActivity.overridePendingTransition(0, 0);
            }
        });

        // Listener for enabling/disabling location switch
        locationEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Handle the distance slider (since it is tied to location services)
                    handleSliderValue(userSettings.getMaxDistanceSliderVal());
                    // Request location permissions via parent Maps Activity
                    MapsActivity parentActivity = (MapsActivity) getActivity();
                    assert parentActivity != null;
                    parentActivity.requestLocationPermissions();
                    // If location services aren't enabled, permissions cannot be granted
                    if (!parentActivity.locationServicesEnabled()) {
                        isChecked = false;
                        locationEnabledSwitch.setChecked(false);
                    }
                } else {
                    distanceSliderText.setText(R.string.slider_disabled);
                }
                // Handle distance slider and save updated value of location permissions in settings
                maxDistanceSlider.setEnabled(isChecked);
                userSettings.setLocationPermissionsGranted(isChecked);
                settingsManager.saveSettings();
            }
        });

        // Listener for changing of slider
        maxDistanceSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                // Handle the slider value and save settings
                handleSliderValue((int) slider.getValue());
                settingsManager.saveSettings();
            }
        });

        // Listener for select tags button
        tagSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTagSelector(settingsManager);
            }
        });

        // Listener for selection of Distance Unit Radio Group
        distanceUnitSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Update setting with selected distance unit and save settings
                if (checkedId == settingsBinding.kilometersButton.getId()) {
                    userSettings.setDistanceUnit(Settings.DistanceUnit.KILOMETER);
                } else if (checkedId == settingsBinding.milesButton.getId()) {
                    userSettings.setDistanceUnit(Settings.DistanceUnit.MILE);
                }
                settingsManager.saveSettings();
            }
        });

        return settingsBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapsActivity parentActivity = (MapsActivity) this.getActivity();
        assert parentActivity != null;
        parentActivity.getNavigationView().getMenu().findItem(R.id.nav_settings).setChecked(true);
        parentActivity.setActionBarTitle("Settings");

        // Enabled/distable the distance slider based on whether location permissions are granted
        maxDistanceSlider.setEnabled(Settings.getInstance().locationPermissionsAreGranted());
    }

    /* Handle the popup for tag selection */
    private void openTagSelector(final SettingsManager settingsManager) {
        // Create an instance of the tag selector fragment and show it
        TagSelectorFragment dialogFragment = new TagSelectorFragment(Settings.getInstance().getTagMapper());
        dialogFragment.setDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                settingsManager.saveSettings();
            }
        });
        assert getFragmentManager() != null;
        dialogFragment.show(getFragmentManager(), "TagSelectorFragment");
    }

    // Updates the max distance setting with the provided slider value
    private void handleSliderValue(int distanceSelected) {

        // Sets the appropriate message for the associated text view
        if (distanceSelected == 0 || distanceSelected == 10) {
            distanceSliderText.setText(R.string.slider_enabled);
        } else {
            distanceSliderText.setText(String.format(getString(R.string.slider_distance),
                    distanceSelected,
                    Settings.getInstance().getDistanceUnit().getDisplayName()));
        }

        // If slider value is 0 (i.e not set) or location permissions not granted show all locations
        if (distanceSelected == 0 || !Settings.getInstance().locationPermissionsAreGranted()) {
            Settings.getInstance().setMaxDistance(Double.MAX_VALUE);
        } else {
            // else show locations within selected distance
            Settings.getInstance().setMaxDistance(distanceSelected * Settings.getInstance().getDistanceUnit().getConversionRate());
        }
    }

    // Method to handle the result of a location permissions request
    public void locationPermissionsResult(boolean result) {
        locationEnabledSwitch.setChecked(result);
    }
}
