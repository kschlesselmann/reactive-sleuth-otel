package com.example.sleuth

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import java.util.UUID

private val logger = KotlinLogging.logger {}

@SpringBootApplication
class SleuthApplication

@Document
data class Test(
    @Id val id: UUID = UUID.randomUUID(),
)

interface TestRepository : ReactiveCrudRepository<Test, UUID>

@RestController
@RequestMapping("/tests")
class TestController(
    private val testRepository: TestRepository,
    private val webClientBuilder: WebClient.Builder,
) {

    @GetMapping
    fun findAll(): Flux<Test> = Flux.just(Test())
        .flatMap { testRepository.save(it) }
        .doFinally { logger.info { "Finished" } }

    @PostMapping
    fun triggerSomething(): Flux<Test> = Flux.range(1, 10)
        .flatMap {
            webClientBuilder
                .baseUrl("http://localhost:8080")
                .build()
                .get()
                .uri("/tests")
                .retrieve()
                .bodyToFlux<Test>()
        }
        .doFinally { logger.info { "Processed Response" } }
}

fun main(args: Array<String>) {
    runApplication<SleuthApplication>(*args)
}
