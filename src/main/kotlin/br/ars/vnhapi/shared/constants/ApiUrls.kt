package br.ars.vnhapi.shared.constants

object ApiUrls {

    // urls de server e homolog
    const val BASE_URL = ""
    const val LOCAL_URL = "localhost:8080"

    // complemento dos servidores e distruibuição
    const val NORTH_AMERICA_SERVER = "" // estados unidos
    const val SOUTH_AMERICA_SERVER = "" // brazil, argentina, paraguai
    const val AFRICA_SERVER = "" // africa do sul
    const val EUROPE_SERVER = "" // inglaterra, espanha, portugal, italia, frança, alemanha
    const val ASIA_SERVER = ""  // china, japão, russia
    const val OCEAN_SERVER = "" //australia, nova zelandia

    // rotas da API
    const val USER_ROUTE = "/api/users"
    const val COUNTRY_ROUTE = "/api/countries"
    const val PROFILE_ROUTE = "/api/profile-user-countries"
    const val LOGGER_ROUTE = "/api/logs"
}
