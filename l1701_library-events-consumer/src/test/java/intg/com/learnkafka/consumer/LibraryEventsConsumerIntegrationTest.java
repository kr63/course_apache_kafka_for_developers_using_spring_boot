package com.learnkafka.consumer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
        , "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventsConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumerSpy;

    // @SpyBean
    // LibraryEventsService libraryEventsServiceSpy;

    // @Autowired
    // LibraryEventsRepository libraryEventsRepository;
    //
    // @Autowired
    // ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }


    @Test
    void loadContext() {
        Assertions.assertTrue(true);
    }

    // @AfterEach
    // void tearDown() {
    //     libraryEventsRepository.deleteAll();
    // }

    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        String json =
                " {\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
        // verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        // List<LibraryEvent> libraryEventList = (List<LibraryEvent>) libraryEventsRepository.findAll();
        // assert libraryEventList.size() == 1;
        // libraryEventList.forEach(libraryEvent -> {
        //     assert libraryEvent.getLibraryEventId() != null;
        //     assertEquals(456, libraryEvent.getBook().getBookId());
        // });

    }

    // @Test
    // void publishUpdateLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
    //     //given
    //     String json =
    //             "{\"libraryEventId\":null,\"libraryEventType\":\"ADD\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    //     LibraryEvent libraryEvent = objectMapper.readValue(json, LibraryEvent.class);
    //     libraryEvent.getBook().setLibraryEvent(libraryEvent);
    //     libraryEventsRepository.save(libraryEvent);
    //     //publish the update LibraryEvent
    //
    //     Book updatedBook = Book.builder().
    //             bookId(456).bookName("Kafka Using Spring Boot 2.x").bookAuthor("Dilip").build();
    //     libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
    //     libraryEvent.setBook(updatedBook);
    //     String updatedJson = objectMapper.writeValueAsString(libraryEvent);
    //     kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updatedJson).get();
    //
    //     //when
    //     CountDownLatch latch = new CountDownLatch(1);
    //     latch.await(3, TimeUnit.SECONDS);
    //
    //     //then
    //     verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
    //     verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
    //     LibraryEvent persistedLibraryEvent = libraryEventsRepository.findById(libraryEvent.getLibraryEventId()).get();
    //     assertEquals("Kafka Using Spring Boot 2.x", persistedLibraryEvent.getBook().getBookName());
    // }
    //
    // @Test
    // void publishModifyLibraryEvent_Not_A_Valid_LibraryEventId() throws JsonProcessingException, InterruptedException, ExecutionException {
    //     //given
    //     Integer libraryEventId = 123;
    //     String json = "{\"libraryEventId\":" + libraryEventId +
    //             ",\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    //     System.out.println(json);
    //     kafkaTemplate.sendDefault(libraryEventId, json).get();
    //     //when
    //     CountDownLatch latch = new CountDownLatch(1);
    //     latch.await(3, TimeUnit.SECONDS);
    //
    //
    //     verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
    //     verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
    //
    //     Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(libraryEventId);
    //     assertFalse(libraryEventOptional.isPresent());
    // }
    //
    // @Test
    // void publishModifyLibraryEvent_Null_LibraryEventId() throws JsonProcessingException, InterruptedException, ExecutionException {
    //     //given
    //     Integer libraryEventId = null;
    //     String json = "{\"libraryEventId\":" + libraryEventId +
    //             ",\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    //     kafkaTemplate.sendDefault(libraryEventId, json).get();
    //     //when
    //     CountDownLatch latch = new CountDownLatch(1);
    //     latch.await(3, TimeUnit.SECONDS);
    //     // then
    //     verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
    //     verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));
    // }
    //
    // @Test
    // void publishModifyLibraryEvent_0_LibraryEventId() throws JsonProcessingException, InterruptedException, ExecutionException {
    //     //given
    //     Integer libraryEventId = 0;
    //     String json = "{\"libraryEventId\":" + libraryEventId +
    //             ",\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":456,\"bookName\":\"Kafka Using Spring Boot\",\"bookAuthor\":\"Dilip\"}}";
    //     //when
    //     kafkaTemplate.sendDefault(libraryEventId, json).get();
    //     CountDownLatch latch = new CountDownLatch(1);
    //     latch.await(3, TimeUnit.SECONDS);
    //     // then
    //     // verify(libraryEventsConsumerSpy, times(3)).onMessage(isA(ConsumerRecord.class));
    //     verify(libraryEventsServiceSpy, times(4)).processLibraryEvent(isA(ConsumerRecord.class));
    //     verify(libraryEventsServiceSpy, times(1)).handleRecovery(isA(ConsumerRecord.class));
    // }
}
