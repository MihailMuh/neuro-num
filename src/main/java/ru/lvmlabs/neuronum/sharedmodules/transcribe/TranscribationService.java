package ru.lvmlabs.neuronum.sharedmodules.transcribe;

import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

public interface TranscribationService {
    @NonNull
    String transcribe(String downloadUrl);

    @NonNull
    String transcribe(Resource fileResource);
}
