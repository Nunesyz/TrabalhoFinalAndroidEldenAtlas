package com.lucasn.eldenatlas.userinterface
import com.lucasn.eldenatlas.data.*
import com.lucasn.eldenatlas.viewmodel.AtlasModel
import androidx.navigation.toRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFC6AB68), background = Color(0xFF141412), surface = Color(0xFF22221E))) {
    Surface(Modifier.fillMaxSize()) { Atlas() }
   }
  }
 }
}

@Composable
fun Atlas(model: AtlasModel = viewModel()) {
 if (model.user == null) { Login(model); return }
 key(model.user) {
  val nav = rememberNavController()
  LaunchedEffect(Unit) { model.load() }
  val entry by nav.currentBackStackEntryAsState()
  Scaffold(bottomBar = {
   NavigationBar {
    NavigationBarItem(selected = entry?.destination?.hasRoute<CatalogRoute>() == true, onClick = { nav.navigate(CatalogRoute) { popUpTo<CatalogRoute>(); launchSingleTop = true } }, icon = { Text("⚔") }, label = { Text("Bosses") })
    NavigationBarItem(selected = entry?.destination?.hasRoute<FavoritesRoute>() == true, onClick = { nav.navigate(FavoritesRoute) { popUpTo<CatalogRoute>(); launchSingleTop = true } }, icon = { Text("☆") }, label = { Text("Favoritos") })
    NavigationBarItem(selected = entry?.destination?.hasRoute<ProfileRoute>() == true, onClick = { nav.navigate(ProfileRoute) { popUpTo<CatalogRoute>(); launchSingleTop = true } }, icon = { Text("○") }, label = { Text("Perfil") })
   }
  }) { padding ->
  NavHost(nav, startDestination = CatalogRoute, modifier = Modifier.padding(padding)) {
   composable<ProfileRoute> {
    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
     Text("Sua jornada.", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
     Text(model.email)
     Text("Favoritos salvos: ${model.favorites.size}")
     Button(onClick = model::logout) { Text("Sair") }
    }
   }
   composable<CatalogRoute> {
    Catalog(model, false, { nav.navigate(FavoritesRoute) { launchSingleTop = true } }, { nav.navigate(DetailRoute(it.id)) })
   }
   composable<FavoritesRoute> {
    Catalog(model, true, { nav.popBackStack() }, { nav.navigate(DetailRoute(it.id)) })
   }
   composable<DetailRoute> { entry ->
    val id = entry.toRoute<DetailRoute>().id
    val boss = (model.bosses + model.favorites).firstOrNull { it.id == id } ?: model.detail?.takeIf { it.id == id }
    LaunchedEffect(id) { if (boss == null) model.loadDetail(id) }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
     item { TextButton(onClick = { nav.popBackStack() }) { Text("Voltar") } }
     if (boss == null) {
      item { if (model.detailLoading) CircularProgressIndicator() else Text(model.error ?: "Boss não encontrado.") }
     } else {
      item { Text(boss.name ?: "Boss", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge) }
      item { BossImage(boss) }
      item { Text(boss.description ?: "Descrição não informada.") }
      item { Text("Local: ${boss.location ?: "Não informado"}") }
      item { Text("Vida: ${boss.healthPoints ?: "Não informada"}") }
      item { Text("Recompensas", style = MaterialTheme.typography.titleLarge) }
      item { Text(boss.drops?.joinToString("\n")?.ifBlank { "Não informadas" } ?: "Não informadas") }
      item { Button(onClick = { model.toggle(boss) }) { Text(if (model.favorites.any { it.id == boss.id }) "Remover dos favoritos" else "Salvar favorito") } }
      item { model.error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
     }
    }
   }
  }
 }
}

}

@Serializable object CatalogRoute
@Serializable object FavoritesRoute
@Serializable object ProfileRoute
@Serializable data class DetailRoute(val id: String)

@Composable
private fun Login(model: AtlasModel) {
 var email by rememberSaveable { mutableStateOf("") }
 var password by remember { mutableStateOf("") }
 var register by rememberSaveable { mutableStateOf(false) }
 LazyColumn(Modifier.fillMaxSize().imePadding().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
  item { Spacer(Modifier.height(32.dp)); Text("ELDEN ATLAS", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary) }
  item { Text("Conheça seus adversários. Guarde sua próxima jornada.") }
  item { Text(if (register) "Criar conta" else "Entrar", style = MaterialTheme.typography.titleLarge) }
  item { OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
  item { OutlinedTextField(password, { password = it }, label = { Text("Senha") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()) }
  item { model.authError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
  item { Button(onClick = { model.login(email, password, register) }, enabled = !model.authenticating, modifier = Modifier.fillMaxWidth()) { Text(if (model.authenticating) "Aguarde..." else if (register) "Cadastrar" else "Entrar") } }
  item { TextButton(onClick = { register = !register }, enabled = !model.authenticating) { Text(if (register) "Já tenho conta" else "Criar uma conta") } }
 }
}

@Composable
private fun Catalog(model: AtlasModel, saved: Boolean, switch: () -> Unit, open: (Boss) -> Unit) {
 var search by rememberSaveable { mutableStateOf(model.query) }
 val list = if (saved) model.favorites else model.bosses
 LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
  item { Text(if (saved) "Guarde o essencial." else "Diante da névoa.", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary) }
  item { Row { TextButton(onClick = switch) { Text(if (saved) "Voltar ao catálogo" else "Favoritos (${model.favorites.size})") }; TextButton(onClick = model::logout) { Text("Sair") } } }
  if (!saved) {
   item { OutlinedTextField(search, { search = it }, label = { Text("Nome do boss (em inglês)") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
   item { Button(onClick = { model.load(search) }, enabled = !model.loading) { Text("Buscar") } }
   item {
    if (model.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    model.error?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(onClick = { model.load(search) }) { Text("Tentar novamente") } }
   }
  }
  if (list.isEmpty() && (saved || !model.loading)) item { Text(if (saved) "Nenhum favorito ainda. Abra um boss e toque em Salvar favorito." else "Nenhum boss encontrado.") }
  items(list, key = { it.id }) { boss ->
   Card(onClick = { open(boss) }, modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
     AsyncImage(model = boss.image, contentDescription = boss.name, modifier = Modifier.fillMaxWidth().height(130.dp), contentScale = ContentScale.Crop)
     Text(boss.name ?: "Boss", fontFamily = FontFamily.Serif, style = MaterialTheme.typography.titleLarge)
     Text(boss.location ?: "Local não informado")
     Text("Ver detalhes →", color = MaterialTheme.colorScheme.primary)
    }
   }
  }
  if (!saved) item {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    TextButton(onClick = { model.load(targetPage = model.page - 1) }, enabled = !model.loading && model.page > 0) { Text("Anterior") }
    Text("Página ${model.page + 1}", modifier = Modifier.padding(top = 12.dp))
    TextButton(onClick = { model.load(targetPage = model.page + 1) }, enabled = !model.loading && model.hasNext) { Text("Próxima") }
   }
  }
 }
}

@Composable
private fun BossImage(boss: Boss) {
 var failed by remember(boss.id) { mutableStateOf(false) }
 if (boss.image.isNullOrBlank() || failed) Text("Imagem indisponível")
 else AsyncImage(model = boss.image, contentDescription = boss.name, modifier = Modifier.fillMaxWidth().height(220.dp), onError = { failed = true })
}
