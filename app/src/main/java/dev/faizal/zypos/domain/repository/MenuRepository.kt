package dev.faizal.zypos.domain.repository



import android.net.Uri
import dev.faizal.zypos.domain.model.menu.Menu
import kotlinx.coroutines.flow.Flow


interface MenuRepository {

    fun getAllMenus(): Flow<List<Menu>>

    fun getActiveMenus(): Flow<List<Menu>>

    fun getMenusByCategory(categoryId: Int): Flow<List<Menu>>

    suspend fun getMenuById(id: Int): Menu?

    fun searchMenus(query: String): Flow<List<Menu>>

    suspend fun insertMenu(
        name: String,
        categoryId: Int,
        basePrice: Double,
        imageUri: Uri?,
        isActive: Boolean = true
    ): Result<Long>

    suspend fun updateMenu(
        id: Int,
        name: String,
        categoryId: Int,
        basePrice: Double,
        imageUri: Uri?,
        isActive: Boolean
    ): Result<Unit>

    suspend fun deleteMenu(id: Int): Result<Unit>

    suspend fun updateMenuStatus(id: Int, isActive: Boolean): Result<Unit>

    suspend fun getMenuCount(): Int

    suspend fun getMenuCountByCategory(categoryId: Int): Int
}