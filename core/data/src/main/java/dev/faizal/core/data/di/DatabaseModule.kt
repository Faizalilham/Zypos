package dev.faizal.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.faizal.core.common.pdf.PdfReportGenerator
import dev.faizal.core.data.database.AppDatabase
import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.dao.MenuDao
import dev.faizal.core.data.datasource.dao.OrderDao
import dev.faizal.core.data.repository.CategoryRepositoryImpl
import dev.faizal.core.data.repository.MenuRepositoryImpl
import dev.faizal.core.data.repository.OrderRepositoryImpl
import dev.faizal.core.domain.repository.CategoryRepository
import dev.faizal.core.domain.repository.MenuRepository
import dev.faizal.core.domain.repository.OrderRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.Companion.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideMenuDao(database: AppDatabase): MenuDao {
        return database.menuDao()
    }

    @Provides
    @Singleton
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao
    ): CategoryRepository {
        return CategoryRepositoryImpl(categoryDao)
    }

    @Provides
    @Singleton
    fun provideMenuRepository(
        menuDao: MenuDao,
        categoryDao: CategoryDao,
        @ApplicationContext context: Context
    ): MenuRepository {
        return MenuRepositoryImpl(menuDao, categoryDao, context)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        orderDao: OrderDao,
        pdfGenerator: PdfReportGenerator
    ): OrderRepository {
        return OrderRepositoryImpl(orderDao, pdfGenerator)
    }

    @Provides
    @Singleton
    fun providePdfReportGenerator(): PdfReportGenerator {
        return PdfReportGenerator()
    }
}