package ru.lvmlabs.neuronum.calls.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Data
public class LLmParsingResponse {
    private String complaint;

    private String record;

    private String doctor;

    private String whyNo;

    private String clientName;

    private String administratorName;

    private String wasBefore;

    private String lateMarker;

    private String adminQuality;

    private String analysis;

    private String text;

    @JsonIgnore
    public boolean isEmpty() {
        return (analysis == null || analysis.isBlank()) || (adminQuality == null || adminQuality.isBlank());
    }

    @SafeVarargs
    public static LLmParsingResponse getInstance(Map<String, String>... maps) {
        Map<String, String> mainMap = new HashMap<>();
        for (val map : maps) {
            if (map != null && !map.isEmpty()) {
                mainMap.putAll(map);
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(mainMap, LLmParsingResponse.class);
    }
}
