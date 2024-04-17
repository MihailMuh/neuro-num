package ru.lvmlabs.neuronum.familia.calls;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.baseconfigs.utils.RestClientWrap;
import ru.lvmlabs.neuronum.calls.model.Call;
import ru.lvmlabs.neuronum.calls.service.extension.CallsBroker;
import ru.lvmlabs.neuronum.calls.service.extension.CallsExtension;
import ru.lvmlabs.neuronum.users.enums.Clinic;

import java.util.Date;

@Slf4j
@Service
@CallsExtension(
        administratorNames = {
                "Лариса", "Ирина", "Дания", "Ольга", "Алена", "Карина", "Елена", "Галина", "Дарья", "Татьяна"
        },
        doctorSpecialties = {
                "лор оториноларинголог отоларинголог лыков максим сергеевич браим наталья георгиевна десятка максим эдуардович егорушкина анна владимировна коркина эльвира эдуардовна салимгараева алия айратовна тарасевич татьяна николаевна",
                "сурдолог браим наталья георгиевна",
                "педиатр серикова виктория михайловна",
                "дерматолог венеролог елтышева дарья николаевна",
                "косметолог малахова ольга анатольевна",
                "психиатр лыгденов тумэн батожабович",
                "терапевт максимов дмитрий михайлович соколова анна вячеславовна цориев ян андреевич",
                "невролог кошелева ольга александровна",
                "гинеколог ларионов владислав алексеевич",
                "офтальмолог кунис валерий давидович",
                "узи врач ультразвуковой диагностики ломакина ирина петровна минимухаметова анна сергеевна",
                "кардиолог кардиоориентолог холтер саранская вилена анатольевна"
        }
)
public class FamiliaCallsService extends RestClientWrap {
    public CallsBroker callsBroker;

    public void create(SeleniumCallParsing seleniumCall) {
        Call call = new Call();
        call.setDate(seleniumCall.getDate());
        call.setTime(seleniumCall.getTime());
        call.setPhoneNumber(seleniumCall.getPhoneNumber());
        call.setVirtualNumber(seleniumCall.getVirtualNumber());
        call.setIncoming(seleniumCall.isIncoming());
        call.setText(seleniumCall.getText());
        call.setClinic(Clinic.familia);

        byte[] audio = restClient.get()
                .uri(seleniumCall.getAudioDownloadUrl())
                .retrieve()
                .body(byte[].class);

        call.setMp3(audio);

        callsBroker.create(call);
    }

    public boolean existsByTimeAndPhoneNumber(Date time, String phoneNumber) {
        return callsBroker.existsByTimeAndPhoneNumber(time, phoneNumber);
    }
}
