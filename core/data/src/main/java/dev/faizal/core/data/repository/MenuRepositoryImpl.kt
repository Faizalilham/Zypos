package dev.faizal.core.data.repository


import javax.inject.Inject
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dev.faizal.core.data.datasource.dao.CategoryDao
import dev.faizal.core.data.datasource.dao.MenuDao
import dev.faizal.core.data.datasource.entity.MenuEntity
import dev.faizal.core.data.mapper.MenuMapper.toDomain
import dev.faizal.core.domain.model.menu.Menu
import dev.faizal.core.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MenuRepositoryImpl @Inject constructor(
    private val menuDao: MenuDao,
    private val categoryDao: CategoryDao,
    private val context: Context
) : MenuRepository {

    override fun getAllMenus(): Flow<List<Menu>> =
        menuDao.getAllMenus().map { entities ->
            entities.mapNotNull { entity ->
                val category = categoryDao.getCategoryById(entity.categoryId)
                category?.let { entity.toDomain(it) }
            }
        }

    override fun getActiveMenus(): Flow<List<Menu>> =
        menuDao.getActiveMenus().map { entities ->
            entities.mapNotNull { entity ->
                val category = categoryDao.getCategoryById(entity.categoryId)
                category?.let { entity.toDomain(it) }
            }
        }

    override fun getMenusByCategory(categoryId: Int): Flow<List<Menu>> =
        menuDao.getMenusByCategory(categoryId).map { entities ->
            entities.mapNotNull { entity ->
                val category = categoryDao.getCategoryById(entity.categoryId)
                category?.let { entity.toDomain(it) }
            }
        }

    override suspend fun getMenuById(id: Int): Menu? {
        val entity = menuDao.getMenuById(id) ?: return null
        val category = categoryDao.getCategoryById(entity.categoryId) ?: return null
        return entity.toDomain(category)
    }

    override fun searchMenus(query: String): Flow<List<Menu>> =
        menuDao.searchMenus(query).map { entities ->
            entities.mapNotNull { entity ->
                val category = categoryDao.getCategoryById(entity.categoryId)
                category?.let { entity.toDomain(it) }
            }
        }

    override suspend fun insertMenu(
        name: String,
        categoryId: Int,
        basePrice: Double,
        imageUri: Uri?,
        isActive: Boolean
    ): Result<Long> {
        return try {
            val savedImagePath = imageUri?.let { saveImageToInternalStorage(it) }

            val menuEntity = MenuEntity(
                name = name,
                categoryId = categoryId,
                basePrice = basePrice,
                imageUri = savedImagePath,
                isActive = isActive
            )

            val id = menuDao.insertMenu(menuEntity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMenu(
        id: Int,
        name: String,
        categoryId: Int,
        basePrice: Double,
        imageUri: Uri?,
        isActive: Boolean
    ): Result<Unit> {
        return try {
            val existingMenu = menuDao.getMenuById(id)
                ?: return Result.failure(Exception("Menu not found"))

            val imagePath = if (imageUri != null) {
                existingMenu.imageUri?.let { deleteImageFromInternalStorage(it) }
                saveImageToInternalStorage(imageUri)
            } else {
                existingMenu.imageUri
            }

            val updatedMenu = existingMenu.copy(
                name = name,
                categoryId = categoryId,
                basePrice = basePrice,
                imageUri = imagePath,
                isActive = isActive,
                updatedAt = System.currentTimeMillis()
            )

            menuDao.updateMenu(updatedMenu)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMenu(id: Int): Result<Unit> {
        return try {
            val menu = menuDao.getMenuById(id)
            menu?.imageUri?.let { deleteImageFromInternalStorage(it) }
            menuDao.deleteMenuById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMenuStatus(id: Int, isActive: Boolean): Result<Unit> {
        return try {
            menuDao.updateMenuStatus(id, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMenuCount(): Int = menuDao.getMenuCount()

    override suspend fun getMenuCountByCategory(categoryId: Int): Int =
        menuDao.getMenuCountByCategory(categoryId)

    // Image handling functions
    private fun saveImageToInternalStorage(uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val filename = "menu_${System.currentTimeMillis()}.jpg"
            val directory = File(context.filesDir, "menu_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, filename)
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun deleteImageFromInternalStorage(imagePath: String) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}