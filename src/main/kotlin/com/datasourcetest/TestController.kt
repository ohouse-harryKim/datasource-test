package com.datasourcetest

import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class TestController(
    private val testEntityRepository: TestEntityRepository
) {

    @GetMapping("/")
    fun hello(): String = "hello, world!"

    @GetMapping("/test/writer/{id}")
    @Transactional(readOnly = false)
    fun writer(@PathVariable id: Int): TestEntity =
        testEntityRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

    @GetMapping("/test/reader/{id}")
    @Transactional(readOnly = true)
    fun readonly(@PathVariable id: Int): TestEntity =
        testEntityRepository.findById(id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
}
