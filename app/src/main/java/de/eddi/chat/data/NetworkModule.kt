package de.eddi.chat.data

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    val okHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .build()
            val response = chain.proceed(request)

            if (response.code == 302 || response.code == 301) {
                val location = response.header("Location") ?: ""
                if (location.contains("login")) {
                    return@addInterceptor response.newBuilder()
                        .code(401)
                        .message("Unauthorized (Redirected to login: $location)")
                        .build()
                }
            }
            response
        }
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ChatApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ChatApiService::class.java)
}
