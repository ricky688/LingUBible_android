package com.lingubible.app.data.remote

import android.content.Context
import com.lingubible.app.di.AppConfig
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Functions
import io.appwrite.services.Storage

class AppwriteClientProvider(
    context: Context? = null,
    val appConfig: AppConfig = AppConfig()
) {
    val client: Client? = context?.let {
        val appwriteClient = Client(it)
            .setEndpoint(appConfig.endpoint)
            .setProject(appConfig.projectId)
            .setSelfSigned(true)
            .addHeader("origin", "https://lingubible.com")

        try {
            val field = Client::class.java.getDeclaredField("http")
            field.isAccessible = true
            val okHttpClient = field.get(appwriteClient) as? okhttp3.OkHttpClient
            if (okHttpClient != null) {
                val customOkHttp = okHttpClient.newBuilder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .callTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                field.set(appwriteClient, customOkHttp)
            }
        } catch (e: Exception) {
            android.util.Log.w("AppwriteClient", "Could not customize OkHttpClient timeouts: ${e.message}")
        }

        appwriteClient
    }

    val account: Account? = client?.let { Account(it) }
    val databases: Databases? = client?.let { Databases(it) }
    val functions: Functions? = client?.let { Functions(it) }
    val tablesDB: io.appwrite.services.TablesDB? = client?.let { io.appwrite.services.TablesDB(it) }
    val storage: Storage? = client?.let { Storage(it) }
    val databaseId: String = appConfig.databaseId

    init {
        android.util.Log.d("AppwriteClient", "Initialized: client=${client != null}, databases=${databases != null}, endpoint=${appConfig.endpoint}, projectId=${appConfig.projectId}")
    }
}
