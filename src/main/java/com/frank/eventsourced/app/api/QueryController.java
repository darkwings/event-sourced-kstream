package com.frank.eventsourced.app.api;

import com.frank.eventsourced.app.service.AppService;
import com.frank.eventsourced.app.utils.KeyBuilder;
import com.frank.eventsourced.common.utils.AvroJsonConverter;
import com.frank.eventsourced.model.app.App;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author ftorriani
 */
@RestController
@Log4j2
public class QueryController {

    @Autowired
    private AppService appService;

    private static final AvroJsonConverter<App> converter = AvroJsonConverter.fromClass(App.class);

    @GetMapping(value = "/app/{tenantId}/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<String> get(@PathVariable("tenantId") String tenantId,
                                      @PathVariable("userId") String userId) {

        try {
            String key = KeyBuilder.key(tenantId, userId);
            Optional<String> appOpt = appService.stateAsJson(key);
            if (appOpt.isPresent()) {
                log.info("Returning data of App [tenant {}, user {}]", tenantId, userId);
                return ResponseEntity.ok().body(appOpt.get());
            } else {
                log.warn("App not found for tenant {} and user {}", tenantId, userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseEntity.status(500).body("{}");
        }
    }
}
