package ru.lvmlabs.neuronum.baseconfigs.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "NeuroNum Backend Api",
                version = "1.0.0",
                contact = @Contact(
                        name = "Михаил Алексеевич",
                        email = "mukhortovm2004@mail.ru"
                )
        ),
        servers = @Server(
                url = "${neuronum.url}"
        )
)
public class OpenApiConfiguration {
}
