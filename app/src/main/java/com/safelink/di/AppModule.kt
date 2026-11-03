package com.safelink.di

import android.content.Context
import androidx.room.Room
import com.safelink.BuildConfig
import com.safelink.data.local.AppDatabase
import com.safelink.data.remote.AuthInterceptor
import com.safelink.data.remote.SafeLinkApi
import com.safelink.engine.ImpactAreaEngine
import com.safelink.sync.SignatureVerifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun db(@ApplicationContext c: Context): AppDatabase =
        Room.databaseBuilder(c, AppDatabase::class.java, "safelink.db").build()

    @Provides fun rulesDao(db: AppDatabase) = db.rules()
    @Provides fun historyDao(db: AppDatabase) = db.history()
    @Provides fun guideDao(db: AppDatabase) = db.guides()
    @Provides fun settingsDao(db: AppDatabase) = db.settings()

    @Provides @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(BuildConfig.APP_TOKEN))
        .callTimeout(1800, TimeUnit.MILLISECONDS)   // C-03
        .build()

    @Provides @Singleton
    fun api(client: OkHttpClient): SafeLinkApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SafeLinkApi::class.java)

    @Provides @Singleton fun engine() = ImpactAreaEngine()   // TODO: load rules from Room
    @Provides fun verifier() = SignatureVerifier(BuildConfig.RULES_PUBLIC_KEY_B64)
}
