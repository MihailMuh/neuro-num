package ru.lvmlabs.neuronum.familia.calls;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SeleniumCallParsing {
    private Date date;

    private Date time;

    private String phoneNumber;

    private String virtualNumber;

    private boolean isIncoming;

    private String audioDownloadUrl;

    private String text;

    private String validateNumber(String phoneNumber) {
        if (phoneNumber == null) return "";

        phoneNumber = phoneNumber.replace(" ", "").replace("-", "");

        if (phoneNumber.startsWith("7") && phoneNumber.length() == 11) {
            phoneNumber = "+" + phoneNumber;
        }

        return phoneNumber;
    }

    public void setPhoneNumber(String number) {
        phoneNumber = validateNumber(number);
    }

    public void setVirtualNumber(String number) {
        virtualNumber = validateNumber(number);
    }
}
