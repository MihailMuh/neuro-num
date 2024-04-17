package ru.lvmlabs.neuronum.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CrmAccountDto {
    private String password;

    private String login;

    private Date newestDate;

    private Date oldestDate;

    private int analysisPeriod;
}
