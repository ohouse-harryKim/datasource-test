package com.datasourcetest

import javax.persistence.*

@Entity
@Table(name="test_table")
data class TestEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    val value: String,
)
