package se.fk.rimfrost.framework.regel.maskinell;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.framework.kundbehovsflode.adapter.KundbehovsflodeAdapter;
import se.fk.rimfrost.framework.regel.integration.config.RegelConfigProviderYaml;
import se.fk.rimfrost.framework.regel.integration.kafka.RegelKafkaProducer;
import se.fk.rimfrost.framework.regel.logic.ImmutableProcessRegelResponse;
import se.fk.rimfrost.framework.regel.logic.RegelMapper;
import se.fk.rimfrost.framework.regel.logic.RegelServiceInterface;
import se.fk.rimfrost.framework.regel.logic.dto.ImmutableRegelDataRequest;
import se.fk.rimfrost.framework.regel.logic.dto.RegelDataRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Ersattning;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@QuarkusTestResource.List(
        {
                @QuarkusTestResource(WireMockTestResource.class)
        })
public class RegelMaskinellTest {

    @Inject
    @Connector("smallrye-in-memory")
    InMemoryConnector inMemoryConnector;
    private static final String regelRequestsChannel = "regel-requests";

    private static WireMockServer wiremockServer;

/*    @Inject
    KundbehovsflodeAdapter kundbehovsflodeAdapter;

    @Inject
    RegelMapper regelMapper;

    @Inject
    RegelConfigProviderYaml regelConfigProvider;

    @Inject
    RegelKafkaProducer regelKafkaProducer;

    @Inject
    RegelServiceInterface regelService;*/

//    @BeforeEach
//    void setupProcessRegelMock() {
//        when(regelService.processRegel(any()))
//                .thenReturn(
//                        ImmutableProcessRegelResponse.builder()
//                                .ersattningar(List.of())
//                                .underlag(List.of())
//                                .build()
//                );
//    }

    @Inject
    RegelMaskinellTestRequestHandler regelMaskinellRequestHandler;

    @BeforeAll
    static void setup() {
        setupWiremock();
    }

    static void setupWiremock() {
        wiremockServer = WireMockTestResource.getWireMockServer();
    }

    private RegelDataRequest testRegelDataRequest(String kundbehovsflodeId) {
        return ImmutableRegelDataRequest
                .builder()
                .id(UUID.fromString("99994567-89ab-4cde-9012-3456789abcde"))
                .kundbehovsflodeId(UUID.fromString(kundbehovsflodeId))
                .kogitorootprocid("123456")
                .kogitorootprociid(UUID.fromString("77774567-89ab-4cde-9012-3456789abcde"))
                .kogitoparentprociid(UUID.fromString("88884567-89ab-4cde-9012-3456789abcde"))
                .kogitoprocid("234567")
                .kogitoprocinstanceid(UUID.fromString("66664567-89ab-4cde-9012-3456789abcde"))
                .kogitoprocist("345678")
                .kogitoprocversion("111")
                .type(regelRequestsChannel)
                .build();
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "5367f6b8-cc4a-11f0-8de9-199901011234, 19990101-1234, Ja, JA" //,
                    // "5367f6b8-cc4a-11f0-8de9-199901013333, 19990101-3333, Utredning, FU",
                    // "5367f6b8-cc4a-11f0-8de9-199901012222, 19990101-2222, Ja, JA",
                    //"5367f6b8-cc4a-11f0-8de9-199901014444, 19990101-4444, Nej, NEJ"
            })
    void TestRegelMaskinell(String kundbehovsflodeId,
                            String persnr,
                            String expectedUtfall, Ersattning.BeslutsutfallEnum expectedBeslutsutfall) {


        var regelDataRequest = testRegelDataRequest(kundbehovsflodeId);
        regelMaskinellRequestHandler.handleRegelRequest(regelDataRequest);
        var x = 1;
    }

}
