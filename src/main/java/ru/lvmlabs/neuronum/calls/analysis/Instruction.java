package ru.lvmlabs.neuronum.calls.analysis;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static ru.lvmlabs.neuronum.calls.analysis.constants.TelephonyConstants.*;

public final class Instruction {
    public static Builder builder() {
        return new Builder();
    }

    @Slf4j
    public static class Builder {
        private final StringBuilder instructionBuilder = new StringBuilder(LLM_TASK);

        private final List<String> keysInResult = new ArrayList<>();

        public Builder complaintWithAttention() {
            if (!keysInResult.contains("complaint")) {
                keysInResult.add("complaint");
            }
            return append(COMPLAINT_ATTENTION);
        }

        public Builder complaintWithPatient() {
            if (!keysInResult.contains("complaint")) {
                keysInResult.add("complaint");
            }
            return append(COMPLAINT_PATIENT);
        }

        public Builder complaintWithClinic() {
            if (!keysInResult.contains("complaint")) {
                keysInResult.add("complaint");
            }
            return append(COMPLAINT_CLINIC);
        }

        public Builder record() {
            keysInResult.add("record");
            return append(RECORD);
        }

        public Builder doctor() {
            keysInResult.add("doctor");
            return append(DOCTOR);
        }

        public Builder whyNo() {
            keysInResult.add("whyNo");
            return append(WHY_NO);
        }

        public Builder administratorName() {
            keysInResult.add("administratorName");
            return append(ADMINISTRATOR_NAME);
        }

        public Builder clientName() {
            keysInResult.add("clientName");
            return append(CLIENT_NAME);
        }

        public Builder wasBefore() {
            keysInResult.add("wasBefore");
            return append(WAS_BEFORE);
        }

        public Builder analysis() {
            keysInResult.add("analysis");
            return append(ANALYSIS);
        }

        public Builder lateMarker() {
            keysInResult.add("lateMarker");
            return append(LATE_MARKER);
        }

        public Builder adminQuality() {
            keysInResult.add("adminQuality");
            return append(ADMIN_QUALITY);
        }

        public String build() {
            String jsonFormat = LLM_RESPONSE_AS_JSON + "";

            for (int i = 0; i < keysInResult.size() - 1; i++) {
                jsonFormat = jsonFormat.replace("%JSON%", "'" + keysInResult.get(i) + "': '...',\n%JSON%");
            }
            jsonFormat = jsonFormat.replace("%JSON%", "'" + keysInResult.getLast() + "': '...'");

            String result = append(jsonFormat.replace("%KEYS%", String.valueOf(keysInResult.size()))).toString();
            return result;
        }

        @Override
        public String toString() {
            return instructionBuilder.toString();
        }

        private Builder append(String toAppend) {
            instructionBuilder.append("\n").append(toAppend);
            return this;
        }
    }
}
