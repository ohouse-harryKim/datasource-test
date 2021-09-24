package com.datasourcetest

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DataSourcePropertiesConfiguration {

    companion object {
        const val READER_DATASOURCE_CONFIG_PREFIX = "spring.datasources.reader"
        const val WRITER_DATASOURCE_CONFIG_PREFIX = "spring.datasources.writer"
    }

    @Bean
    @ConfigurationProperties(prefix = READER_DATASOURCE_CONFIG_PREFIX)
    fun readerDataSourceProperties() = DataSourceProperties()

    @Bean
    @ConfigurationProperties(prefix = WRITER_DATASOURCE_CONFIG_PREFIX)
    fun writerDataSourceProperties() = DataSourceProperties()

    @Bean
    fun readerDataSource(
        @Qualifier("readerDataSourceProperties") dataSourceProperties: DataSourceProperties
    ): DataSource = dataSourceProperties.createDataSource().apply {
        isReadOnly = true
    }

    @Bean
    fun writerDataSource(
        @Qualifier("writerDataSourceProperties") dataSourceProperties: DataSourceProperties
    ): DataSource = dataSourceProperties.createDataSource()


    open class DataSourceProperties {
        lateinit var url : String
        lateinit var username : String
        lateinit var password : String
        lateinit var driverClassName: String

        fun createDataSource(): HikariDataSource = DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .url(this.url)
            .username(this.username)
            .password(this.password)
            .driverClassName(this.driverClassName)
            .build()
    }
}
