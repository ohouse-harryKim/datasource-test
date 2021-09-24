package com.datasourcetest

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionSynchronizationManager
import com.datasourcetest.RoutingDataSource.*
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

val BASE_CLASS_PATH: String = DatasourcetestApplication::class.java.packageName

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "routingTransactionManager"
)
class DataSourceConfiguration {

    fun HibernateProperties.toVendorProperties(properties: JpaProperties): MutableMap<String, Any> =
        LinkedHashMap(this.determineHibernateProperties(properties.properties, HibernateSettings()))

    @Bean
    fun entityManagerFactory(
        @Qualifier("mainDataSource") dataSource: DataSource,
        properties: JpaProperties,
        hibernateProperties: HibernateProperties,
        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
    ): LocalContainerEntityManagerFactoryBean = entityManagerFactoryBuilder
        .dataSource(dataSource)
        .packages(BASE_CLASS_PATH)
        .properties(hibernateProperties.toVendorProperties(properties))
        .mappingResources(*properties.mappingResources.toTypedArray())
        .build()
        .also {
            println("#BASE_CLASS_PATH: $BASE_CLASS_PATH")
        }

        ?: throw ExceptionInInitializerError()

    @Bean
    @Primary
    fun routingTransactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager =
        JpaTransactionManager().apply {
            this.entityManagerFactory = entityManagerFactory
        }

    @Bean
    fun routingDataSource(
        @Qualifier("writerDataSource") writerDataSource: DataSource,
        @Qualifier("readerDataSource") readerDataSource: DataSource,
    ): DataSource = RoutingDataSource().apply {
        setTargetDataSources(
            mapOf<Any, Any>( //casting required
                DataSourceType.WRITER to writerDataSource,
                DataSourceType.READER to readerDataSource,
            )
        )
    }

    @Bean
    @Primary
    fun mainDataSource(@Qualifier("routingDataSource") routingDataSource: DataSource) =
        LazyConnectionDataSourceProxy(routingDataSource)

}

class RoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any =
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly())
            DataSourceType.READER
        else
            DataSourceType.WRITER

    enum class DataSourceType {
        WRITER,
        READER;
    }
}
