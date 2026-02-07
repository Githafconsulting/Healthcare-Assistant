package com.afya.assistant.di

import android.content.Context
import com.afya.assistant.data.local.AppDatabase
import com.afya.assistant.messaging.PlaceholderReminderGateway
import com.afya.assistant.messaging.ReminderGateway
import com.afya.assistant.sync.SyncApi
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
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSyncApi(okHttpClient: OkHttpClient): SyncApi {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")  // Configure in BuildConfig
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SyncApi::class.java)
    }

    /** For pilot: use PlaceholderReminderGateway; replace with real gateway when SMS API is configured. */
    @Provides
    @Singleton
    fun provideReminderGateway(placeholder: PlaceholderReminderGateway): ReminderGateway = placeholder
}
