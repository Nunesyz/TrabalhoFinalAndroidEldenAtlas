package com.lucasn.eldenatlas.viewmodel
import com.lucasn.eldenatlas.data.*

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AtlasModel(app: Application) : AndroidViewModel(app) {
 private val gson = Gson()
 private val repository = AtlasRepository(app)
 var user by mutableStateOf(repository.session); private set
 val email: String get() = repository.email
 var bosses by mutableStateOf<List<Boss>>(emptyList()); private set
 var favorites by mutableStateOf<List<Boss>>(emptyList()); private set
 var loading by mutableStateOf(false); private set
 var authenticating by mutableStateOf(false); private set
 var error by mutableStateOf<String?>(null); private set
 var authError by mutableStateOf<String?>(null); private set
 var page by mutableStateOf(0); private set
 var hasNext by mutableStateOf(false); private set
 var query by mutableStateOf(""); private set
 private var observation: Job? = null
 private var request: Job? = null
 private var detailRequest: Job? = null
 init { observeFavorites() }
 private fun observeFavorites() {
  observation?.cancel(); favorites = emptyList()
  val uid = user ?: return
  observation = viewModelScope.launch { repository.observe(uid).collect { rows -> favorites = rows.map { gson.fromJson(it.json, Boss::class.java) } } }
 }
 fun login(email: String, password: String, register: Boolean) {
  if (email.isBlank() || password.length < 6) { authError = "Informe seu e-mail e uma senha de pelo menos 6 caracteres."; return }
  if (authenticating) return
  viewModelScope.launch {
   authenticating = true; authError = null
   try {
    user = repository.login(email, password, register); observeFavorites()
   } catch (e: CancellationException) { throw e } catch (e: Exception) { authError = if (e is IllegalArgumentException) e.message else "Não foi possível acessar a conta. Tente novamente." }
   finally { authenticating = false }
  }
 }
 fun logout() { detailRequest?.cancel(); detail = null; request?.cancel(); repository.logout(); user = null; observeFavorites(); bosses = emptyList(); error = null; authError = null; query = ""; page = 0 }
 fun load(search: String = query, targetPage: Int = 0) {
  request?.cancel()
  request = viewModelScope.launch {
   loading = true; error = null
   try {
    val result = repository.bosses(targetPage, search.trim().ifEmpty { null })
    check(result.success)
    bosses = result.data.orEmpty(); page = targetPage; query = search; hasNext = bosses.size == 20
   } catch (e: CancellationException) { throw e } catch (e: Exception) { error = "Não foi possível carregar a API. Confira a internet e tente novamente." }
   finally { loading = false }
  }
 }
 var detailLoading by mutableStateOf(false); private set
 var detail by mutableStateOf<Boss?>(null); private set
 fun loadDetail(id: String) {
  detailRequest?.cancel()
  detail = null; detailLoading = true; error = null
  detailRequest = viewModelScope.launch {
   try { val response = repository.boss(id); if (response.success) detail = response.data }
   catch (e: CancellationException) { throw e }
   catch (e: Exception) { error = "Não foi possível carregar os detalhes. Volte ao catálogo e tente novamente." }
   finally { detailLoading = false }
  }
 }
 fun toggle(boss: Boss) {
  val uid = user ?: return
  viewModelScope.launch {
   try { if (favorites.any { it.id == boss.id }) repository.remove(uid, boss.id) else repository.save(Favorite(uid, boss.id, gson.toJson(boss))) }
   catch (e: Exception) { error = "Não foi possível salvar o favorito." }
  }
 }
}