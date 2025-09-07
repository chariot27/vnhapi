// src/main/kotlin/br/ars/vnhapi/config/DataSourceConfig.kt
package br.ars.vnhapi.config

import br.ars.vnhapi.shared.constants.DbAccess
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    fun dataSource(): DataSource {
        // Env > fallback para constantes
        val url  = System.getenv("DB_URL")  ?: DbAccess.DB_STRING
        val user = System.getenv("DB_USER") ?: DbAccess.DB_USER
        val pass = System.getenv("DB_PASS") ?: DbAccess.DB_PASS

        val cfg = HikariConfig().apply {
            jdbcUrl = url
            username = user
            password = pass
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
        }
        return HikariDataSource(cfg)
    }
}
