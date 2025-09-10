package br.ars.vnhapi.shared.constants

object DbAccess {
    // credenciais do banco de dados
    const val DB_STRING = "jdbc:postgresql://ep-rough-mud-afhxri74-pooler.c-2.us-west-2.aws.neon.tech/neondb?user=neondb_owner&password=npg_1WHDAnfGRo0u&sslmode=require&channelBinding=require"
    const val DB_USER = "neondb_owner"
    const val DB_PASS = "npg_1WHDAnfGRo0u"

    // complemento dos servidores e distruibuição
    const val NORTH_AMERICA_SERVER = "" // estados unidos
    const val SOUTH_AMERICA_SERVER = "" // brazil, argentina, paraguai
    const val AFRICA_SERVER = "" // africa do sul
    const val EUROPE_SERVER = "" // inglaterra, espanha, portugal, italia, frança, alemanha
    const val ASIA_SERVER = ""  // china, japão, russia
    const val OCEAN_SERVER = "" //australia, nova zelandia
}