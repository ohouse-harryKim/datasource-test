package com.datasourcetest

import org.springframework.data.jpa.repository.JpaRepository

interface TestEntityRepository: JpaRepository<TestEntity, Int> {
}
