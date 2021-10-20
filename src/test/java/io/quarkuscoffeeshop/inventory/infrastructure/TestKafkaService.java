package io.quarkuscoffeeshop.inventory.infrastructure;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkuscoffeeshop.inventory.domain.Item;
import io.quarkuscoffeeshop.inventory.domain.RestockInventoryCommand;
import io.quarkuscoffeeshop.inventory.domain.RestockItemCommand;
import io.quarkuscoffeeshop.inventory.domain.StockRoom;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest @QuarkusTestResource(KafkaTestResource.class)
public class TestKafkaService {

    static final Logger logger = LoggerFactory.getLogger(TestKafkaService.class);

    Jsonb jsonb = JsonbBuilder.create();

    String KAKFA_TOPIC = "inventory-in";

    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    KafkaService kafkaService;

    @InjectSpy
    StockRoom stockRoom;

    @BeforeEach
    public void setUp() {
        doAnswer(invocationOnMock -> new CompletableFuture<RestockItemCommand>()).when(stockRoom).handleRestockItemCommand(any(Item.class));
    }

    @Test
    public void testIncoming() {

        RestockInventoryCommand restockInventoryCommand = new RestockInventoryCommand(Item.COFFEE_BLACK);
        InMemorySource<RestockInventoryCommand> ordersIn = connector.source(KAKFA_TOPIC);
        ordersIn.send(restockInventoryCommand);
        await().atLeast(2, TimeUnit.SECONDS);
        verify(stockRoom, times(1)).handleRestockItemCommand(Item.COFFEE_BLACK);
    }
}