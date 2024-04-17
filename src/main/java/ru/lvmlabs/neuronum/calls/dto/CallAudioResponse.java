package ru.lvmlabs.neuronum.calls.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallAudioResponse {
    private byte[] mp3;

    private String fileName;
}
