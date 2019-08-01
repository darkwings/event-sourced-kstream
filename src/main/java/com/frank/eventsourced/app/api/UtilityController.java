package com.frank.eventsourced.app.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frank.eventsourced.common.eventsourcing.EventSourcingService;
import com.frank.eventsourced.app.utils.KeyBuilder;
import com.frank.eventsourced.common.interactivequeries.HostStoreInfo;
import com.frank.eventsourced.model.app.App;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author ftorriani
 */
@RestController
@Slf4j
public class UtilityController {

    @Autowired
    @Qualifier("appService")
    private EventSourcingService<App> appService;

    private static final ObjectMapper mapper = new ObjectMapper();

    @GetMapping(value = "/store/info/{tenantId}/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> get( @PathVariable("tenantId") String tenantId,
                                  @PathVariable("userId") String userId ) {

        try {
            String key = KeyBuilder.key( tenantId, userId );
            HostStoreInfo info = appService.hostForKey( key ).
                    orElseThrow( () -> new Exception( "Missing key " + key ) );

            return ResponseEntity.ok().body( mapper.writeValueAsString( info ) );
        }
        catch ( Exception e ) {
            log.error( "Error", e );
            return ResponseEntity.status( 500 ).body( "{}" );
        }
    }

    @GetMapping(value = "/store/info", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAll() {

        try {
            List<HostStoreInfo> info = appService.allInfo();
            return ResponseEntity.ok().body( mapper.writeValueAsString( info ) );
        }
        catch ( Exception e ) {
            log.error( "Error", e );
            return ResponseEntity.status( 500 ).body( "{}" );
        }
    }
}
