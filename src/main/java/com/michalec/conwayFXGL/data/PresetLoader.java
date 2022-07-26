package com.michalec.conwayFXGL.data;

import com.michalec.conwayFXGL.valueObject.Preset;

import java.util.ArrayList;
import java.util.List;

public class PresetLoader {
    private List<Preset> presets = new ArrayList<>();
    public PresetLoader() {
        presets.add(new EmptyPreset());
        presets.add(new GliderPreset());
        //presets.add(new DynamicPreset());
    }
    public List<Preset> getPresets() {
        return presets;
    }
}
