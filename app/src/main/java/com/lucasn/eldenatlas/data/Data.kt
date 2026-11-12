package com.lucasn.eldenatlas.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

data class Boss(
 val id: String,
 val name: String?,
 val image: String?,
 val description: String?,
 val location: String?,
 val healthPoints: String?,
 val drops: List<String>?
)

data class BossResponse(
 val success: Boolean,
 val data: List<Boss>?
)

data class BossDetailResponse(
 val success: Boolean,
 val data: Boss?
)

@Entity(primaryKeys = ["owner", "id"])
data class Favorite(
 val owner: String,
 val id: String,
 val json: String
)

@Dao
interface FavoriteDao {
 @Query("SELECT * FROM Favorite WHERE owner = :owner")
 fun observe(owner: String): Flow<List<Favorite>>

 @Insert(onConflict = OnConflictStrategy.REPLACE)
 suspend fun save(favorite: Favorite)

 @Query("DELETE FROM Favorite WHERE owner = :owner AND id = :id")
 suspend fun remove(owner: String, id: String)
}

@Database(
 entities = [Favorite::class, Usuario::class],
 version = 2,
 exportSchema = false
)
abstract class AtlasDatabase : RoomDatabase() {
 abstract fun favorites(): FavoriteDao
 abstract fun usuarios(): UsuarioDao
}
@Entity(indices = [androidx.room.Index(value = ["email"], unique = true)])
data class Usuario(@androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0, val email: String, val senhaHash: String)
@Dao
interface UsuarioDao {
 @Query("SELECT * FROM Usuario WHERE email = :email LIMIT 1") suspend fun find(email: String): Usuario?
 @Insert suspend fun insert(usuario: Usuario): Long
}
