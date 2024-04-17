package ru.lvmlabs.neuronum.calls.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.filters.Filter;
import ws.schild.jave.filters.FilterChain;
import ws.schild.jave.filters.FilterGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Slf4j
class FFmpegService {
    private static final EncodingAttributes encodingAttributes;

    static {
        VideoAttributes videoAttributes = new VideoAttributes();
        videoAttributes.setComplexFiltergraph(new FilterGraph("-af", new FilterChain(new Filter("loudnorm"))));

        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setChannels(1);
        audioAttributes.setSamplingRate(8_000);

        encodingAttributes = new EncodingAttributes();
        encodingAttributes.setOutputFormat("mp3");
        encodingAttributes.setAudioAttributes(audioAttributes);
        encodingAttributes.setVideoAttributes(videoAttributes);
    }

    @Nullable
    public static byte[] convertToMp3(byte[] content) {
        if (content == null || content.length == 0) {
            log.warn("Empty content!");
            return null;
        }

        log.debug("Converting audio to mp3 format...");

        final Encoder encoder = new Encoder();
        byte[] convertedMp3 = null;
        File tempOriginal = null;
        File tempConverted = null;

        try {
            tempOriginal = File.createTempFile("neuronum-audio-orig", "");
            tempConverted = File.createTempFile("neuronum-audio-converted", ".mp3");

            try (FileOutputStream originalStream = new FileOutputStream(tempOriginal)) {
                originalStream.write(content);
                originalStream.flush();
            }

            encoder.encode(new MultimediaObject(tempOriginal), tempConverted, encodingAttributes);

            try (FileInputStream convertedStream = new FileInputStream(tempConverted)) {
                convertedMp3 = convertedStream.readAllBytes();
            }

            log.debug("Successfully!");
        } catch (Exception exception) {
            log.error("Can't convert audio to mp3!");
            log.error(encoder.getUnhandledMessages().toString());
            exception.printStackTrace();
        }

        if (tempOriginal != null) tempOriginal.delete();
        if (tempConverted != null) tempConverted.delete();

        return convertedMp3;
    }
}
