package com.frank.eventsourced.app.api.support;

import com.frank.eventsourced.app.api.bean.WidgetBean;
import com.frank.eventsourced.commands.platform.app.AddWidget;
import com.frank.eventsourced.commands.platform.app.CancelApp;
import com.frank.eventsourced.commands.platform.app.CreateApp;
import com.frank.eventsourced.commands.platform.app.Widget;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

import static com.frank.eventsourced.app.utils.KeyBuilder.key;

@Component
public class Command2Avro {

    public Command2AvroResult createCommand(String tenantId, String userId) {

        String operationId = UUID.randomUUID().toString();
        CreateApp createApp = CreateApp.newBuilder().setTenantId(tenantId)
                .setEventId(UUID.randomUUID().toString())
                .setOperationId(operationId)
                .setKey(key(tenantId, userId))
                .setTenantId(tenantId)
                .setUserId(userId)
                .setTimestampMs(Clock.systemUTC().millis())
                .setVersion(0)
                .build();
        return new Command2AvroResult(createApp, operationId);
    }

    public Command2AvroResult addWidgetCommand(String tenantId, String userId, WidgetBean widgetBean) {

        String operationId = UUID.randomUUID().toString();
        Widget widget = Widget.newBuilder()
                .setWidgetId(widgetBean.getWidgetId())
                .setData(widgetBean.getData())
                .setMeta(widgetBean.getMeta())
                .build();
        AddWidget addWidget = AddWidget.newBuilder()
                .setKey(key(tenantId, userId))
                .setVersion(widgetBean.getVersion())
                .setTenantId(tenantId)
                .setUserId(userId)
                .setEventId(UUID.randomUUID().toString())
                .setOperationId(operationId)
                .setTimestampMs(Clock.systemUTC().millis())
                .setWidget(widget)
                .build();
        return new Command2AvroResult(addWidget, operationId);
    }

    public Command2AvroResult cancelCommand(String tenantId, String userId, int version) {

        String operationId = UUID.randomUUID().toString();
        CancelApp createApp = CancelApp.newBuilder().setTenantId(tenantId)
                .setEventId(UUID.randomUUID().toString())
                .setOperationId(operationId)
                .setKey(key(tenantId, userId))
                .setTenantId(tenantId)
                .setUserId(userId)
                .setVersion(version)
                .setTimestampMs(Clock.systemUTC().millis())
                .build();
        return new Command2AvroResult(createApp, operationId);
    }
}
