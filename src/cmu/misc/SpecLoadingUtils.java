package cmu.misc;

import cmu.subsystems.BaseSubsystem;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * @author tomatopaste
 * used to load various spec from .json and .csv
 */
public final class SpecLoadingUtils {
    private static HashMap<String, BaseSubsystem.SubsystemData> subsystemData;

    private static final List<String> subsystemHotkeyPriority = new ArrayList<>();

    public static void loadSubsystemData() throws JSONException, IOException {
        SettingsAPI settings = Global.getSettings();
        JSONArray subsystems = settings.getMergedSpreadsheetDataForMod("id","data/subsystems/subsystems.csv", "cmutils");

        subsystemData = new HashMap<>();

        for (int i = 0; i < subsystems.length(); i++) {
            JSONObject row = subsystems.getJSONObject(i);
            String id = row.getString("id");

            float inTime = catchJsonFloatDefaultZero(row, "inTime");
            float activeTime = catchJsonFloatDefaultZero(row, "activeTime");
            float outTime = catchJsonFloatDefaultZero(row, "outTime");
            float cooldownTime = catchJsonFloatDefaultZero(row, "cooldownTime");

            //float floatData = (float) row.getDouble("floatDataKey");

            boolean isToggle = catchJsonBooleanDefaultFalse(row, "isToggle");
            if (isToggle) activeTime = Float.MAX_VALUE;

            float fluxPerSecondPercentMaxCapacity = catchJsonFloatDefaultZero(row, "fluxPerSecondPercentMaxCapacity");
            float fluxPerSecondFlat = catchJsonFloatDefaultZero(row, "fluxPerSecondFlat");

            String hotkey = row.getString("hotkey");
            if (hotkey.isEmpty()) hotkey = null;

            BaseSubsystem.SubsystemData data = new BaseSubsystem.SubsystemData(
                    hotkey,
                    id,
                    row.getString("name"),
                    inTime,
                    activeTime,
                    outTime,
                    cooldownTime,
                    isToggle,
                    fluxPerSecondPercentMaxCapacity,
                    fluxPerSecondFlat
            );

            subsystemData.put(id, data);
        }

        subsystemHotkeyPriority.add(Global.getSettings().getString("cmu_DefaultKeybind1"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("cmu_DefaultKeybind2"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("cmu_DefaultKeybind3"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("cmu_DefaultKeybind4"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("cmu_DefaultKeybind5"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("cmu_DefaultKeybind6"));
    }

    public static List<String> getSubsystemHotkeyPriority() {
        return subsystemHotkeyPriority;
    }

    public static BaseSubsystem.SubsystemData getSubsystemData(String id) {
        return subsystemData.get(id);
    }

    public static float catchJsonFloatDefaultZero(JSONObject row, String id) {
        float value;
        try {
            value = (float) row.getDouble(id);
        } catch (JSONException e) {
            value = 0f;
        }
        return value;
    }

    public static boolean catchJsonBooleanDefaultFalse(JSONObject row, String id) {
        boolean value;
        try {
            value = row.getBoolean(id);
        } catch (JSONException e) {
            value = false;
        }
        return value;
    }
}