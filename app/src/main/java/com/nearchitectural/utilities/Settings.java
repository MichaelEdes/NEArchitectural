package com.nearchitectural.utilities;

import java.io.Serializable;
import java.util.HashSet;

/* Author:  Joel Bell-Wilding, Kristiyan Doykov
 * Since:   15/01/20
 * Version: 1.1
 * Purpose: Settings singleton class which stores and allows manipulation of application-wide settings
 */
public class Settings implements Serializable {

    private static volatile Settings soleInstance; // The single instance of the settings singleton
    private static TagMapper activeTags; // A TagMapper which stores the states of each tag
    private static int fontSize; // An integer representing the selected font-size
    private static DistanceUnit distanceUnit; // The selected distance unit
    private static boolean locationPermissionsGranted; // Boolean for location permissions
    private static double maxDistance; // The maximum distance within which locations will be shown
    private static int maxDistanceSliderVal; // The UI slider value for the selected max distance
    private static HashSet<String> likedLocations; // A set containing the IDs of all liked locations
    // A boolean representing if application has been initialised (i.e. if settings have loaded)
    private static boolean settingsLoaded = false;

    // Enumerator to represent the available distance units
    public enum DistanceUnit {

        KILOMETER(1000, "kilometers"),
        MILE(1609, "miles");

        private final int conversionRate; // The conversion rate from meters
        private final String displayName; // The name to be displayed in the UI

        DistanceUnit(int conversionRate, String displayName) {
            this.conversionRate = conversionRate;
            this.displayName = displayName;
        }

        public int getConversionRate() {
            return conversionRate;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    //private constructor
    private Settings() {
        activeTags = new TagMapper();
        distanceUnit = DistanceUnit.KILOMETER;
        //Prevent form the reflection api.
        if (soleInstance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    // Used to get an instance of the Singleton and change/retrieve settings
    public static Settings getInstance() {
        if (soleInstance == null) { //if there is no instance available... create new one
            synchronized (Settings.class) {
                if (soleInstance == null) soleInstance = new Settings();
            }
        }
        return soleInstance;
    }

    // Set settings loaded to true once settings are initially retrieved
    void setSettingsLoaded() {
        settingsLoaded = true;
    }

    public boolean isSettingsLoaded() {
        return settingsLoaded;
    }

    public TagMapper getTagMapper() {
        return activeTags;
    }

    // Determines if a tag is currently active for application-wide filtering
    public boolean getTagValue(TagID tag) {
        return activeTags.getTagValuesMap().get(tag);
    }

    // Set a tag value in the internal TagMapper
    public void setTagValue(TagID tag, boolean isActive) {
        Settings.activeTags.addTagToMapper(tag, isActive);
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        Settings.fontSize = fontSize;
    }

    /* Make singleton free from serialize and deserialize operation. In other words guard against
    multiple instances of this class */
    protected Settings readResolve() {
        return getInstance();
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    // Sets the max distance and corresponding slider value for a given distance unit
    public void setMaxDistance(double newMaxDistance) {
        maxDistance = newMaxDistance;
        maxDistanceSliderVal = (int) maxDistance/distanceUnit.conversionRate;
        if (maxDistanceSliderVal < 0 || maxDistanceSliderVal > 10)
            maxDistanceSliderVal = 0;
    }

    public int getMaxDistanceSliderVal() {
        return maxDistanceSliderVal;
    }

    public void setDistanceUnit(DistanceUnit newDistanceUnit) {
        distanceUnit = newDistanceUnit;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    // Used for data binding radio buttons when Settings used as model in settings layout XML
    public String getKilometerString() {
        return DistanceUnit.KILOMETER.displayName;
    }

    public String getMilesString() {
        return DistanceUnit.MILE.displayName;
    }

    public boolean locationPermissionsAreGranted() {
        return locationPermissionsGranted;
    }

    public void setLocationPermissionsGranted(boolean locationPermissionsAreGranted) {
       locationPermissionsGranted = locationPermissionsAreGranted;
    }

    public HashSet<String> getLikedLocations() {
        return likedLocations;
    }

    // Sets the initial value of liked locations when read in from the device
    void setLikedLocations(HashSet<String> initialLikedLocations) {
        likedLocations = initialLikedLocations;
    }

    public void addLikedLocation(String likedLocationID) {
        likedLocations.add(likedLocationID);
    }

    public void removeLikedLocation(String likedLocationID) {
        likedLocations.remove(likedLocationID);
    }

    public boolean locationIsLiked(String locationID) {
        return likedLocations.contains(locationID);
    }
}