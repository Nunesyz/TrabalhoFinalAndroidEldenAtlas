package com.lucasn.eldenatlas.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lucasn.eldenatlas.network.ApiProvider
import java.util.Locale

class AtlasRepository(context: Context) {
 private val db = Room.databaseBuilder(context.applicationContext, AtlasDatabase::class.java, "atlas.db")
  .addMigrations(object : Migration(1, 2) {
   override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL("CREATE TABLE IF NOT EXISTS `Usuario` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `senhaHash` TEXT NOT NULL)")
    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Usuario_email` ON `Usuario` (`email`)")
   }
  }).build()
 private val prefs = context.getSharedPreferences("sessao_local", Context.MODE_PRIVATE)
 val session: String? get() = prefs.getString("usuario", null)
 val email: String get() = prefs.getString("email", "").orEmpty()
 suspend fun login(email: String, password: String, register: Boolean): String {
  val normalized = email.trim().lowercase(Locale.ROOT)
  require(android.util.Patterns.EMAIL_ADDRESS.matcher(normalized).matches() && password.length >= 6) { "Informe um e-mail válido e uma senha com pelo menos 6 caracteres." }
  val hash = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
  val user = db.usuarios().find(normalized)
  val id = if (register) {
   require(user == null) { "Este e-mail já está cadastrado." }
   db.usuarios().insert(Usuario(email = normalized, senhaHash = hash))
  } else {
   require(user != null && user.senhaHash == hash) { "E-mail ou senha inválidos." }
   user.id
  }
  val owner = "local:$id"
  prefs.edit().putString("usuario", owner).putString("email", normalized).apply()
  return owner
 }
 fun logout() { prefs.edit().clear().apply() }
 suspend fun bosses(page: Int, name: String?) = ApiProvider.api.bosses(page, name)
 suspend fun boss(id: String) = ApiProvider.api.boss(id)
 fun observe(owner: String) = db.favorites().observe(owner)
 suspend fun save(favorite: Favorite) = db.favorites().save(favorite)
 suspend fun remove(owner: String, id: String) = db.favorites().remove(owner, id)
}
