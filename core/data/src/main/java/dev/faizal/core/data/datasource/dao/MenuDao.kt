package dev.faizal.core.data.datasource.dao

import androidx.room.*
import dev.faizal.core.data.datasource.entity.MenuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {

    @Query("SELECT * FROM menu_items ORDER BY createdAt DESC")
    fun getAllMenus(): Flow<List<MenuEntity>>

    @Query("SELECT * FROM menu_items WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveMenus(): Flow<List<MenuEntity>>

    @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId AND isActive = 1")
    fun getMenusByCategory(categoryId: Int): Flow<List<MenuEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id")
    suspend fun getMenuById(id: Int): MenuEntity?

    @Query("SELECT * FROM menu_items WHERE name LIKE '%' || :query || '%' AND isActive = 1")
    fun searchMenus(query: String): Flow<List<MenuEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenu(menu: MenuEntity): Long

    @Update
    suspend fun updateMenu(menu: MenuEntity)

    @Delete
    suspend fun deleteMenu(menu: MenuEntity)

    @Query("UPDATE menu_items SET isActive = :isActive WHERE id = :id")
    suspend fun updateMenuStatus(id: Int, isActive: Boolean)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuById(id: Int)

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun getMenuCount(): Int

    @Query("SELECT COUNT(*) FROM menu_items WHERE categoryId = :categoryId")
    suspend fun getMenuCountByCategory(categoryId: Int): Int
}