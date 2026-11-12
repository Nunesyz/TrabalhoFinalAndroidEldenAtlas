# Elden Atlas
Aplicativo para conhecer os bosses de Elden Ring e salvar os favoritos no aparelho.

## API utilizada
Elden Ring FanAPI: https://eldenring.fanapis.com/api/
Usamos os endpoints `bosses` e `bosses/{id}` para nome, imagem, descrição, localização, vida e recompensas. Busca por nome e paginação usam Retrofit e coroutines.
A aprovação desta API deve ser confirmada com o professor.

## Como executar
Abra esta pasta no Android Studio, use JDK 17 e SDK Android 35, sincronize o Gradle e execute em Android 7 ou superior. Kotlin 2.0.21 e Navigation Compose 2.8.5 foram mantidos. Firebase não é mais necessário.
Cadastre uma conta no próprio aplicativo. O login é local, conforme a atividade: usuários no Room, senha SHA-256 e sessão em SharedPreferences. A conta funciona sem internet; o catálogo precisa de conexão.

## Telas


## Organização
- network: interface e configuração Retrofit.
- data: modelos, usuários, favoritos, Room e Repository.
- viewmodel: estados e ações das telas.
- userinterface: telas, navegação e MainActivity.

## Prints
Pendente antes da entrega: inserir de 4 a 6 capturas reais do aplicativo executando (login, cadastro, lista, detalhe, favoritos e erro). Não foram criadas imagens simuladas.

## Divisão de tarefas
Lucas Santiago: API e listagem dos bosses
Roger: Login e favoritos no Room

| Integrante | O que fez |
|---|-----------|
| Lucas Nunes Araújo | API e listagem de bosses |
| Lucas Santiago | Telas e navegação |
| Roger | Login e favoritos no Room |

## O que aprendemos
Lucas Nunes: Aprendi a buscar e mostrar os dados de uma API
Lucas Santiago: Aprendi a criar telas e navegar entre elas
Roger: Login e favoritos no Room

## Atualização de uma instalação antiga
A migração do Room da versão 1 para 2 preserva a tabela de favoritos e adiciona usuários locais. Contas Firebase não se transformam em contas locais: cadastre uma nova conta. Favoritos antigos continuam no banco com o UID antigo e não aparecem nas novas contas, evitando misturar dados entre usuários.

## Conferência antes da entrega
- Cadastrar e-mail válido com senha de seis caracteres ou mais.
- Rejeitar e-mail inválido, senha curta e cadastro duplicado (inclusive variações de maiúsculas).
- Tentar senha incorreta e verificar mensagem genérica.
- Navegar nas três abas e abrir o boss correto.
- Buscar e mudar páginas.
- Salvar favorito, fechar completamente e reabrir: sessão e favorito devem permanecer.
- Sair, criar outra conta e confirmar que os favoritos não se misturam.
- Voltar à primeira conta e remover o favorito.
- Em modo avião, fazer uma busca e observar erro; abrir favorito salvo.
- Inserir prints, relatos e divisão real; todos devem ter seus próprios commits.
- Cada integrante deve preencher o feedback individual do PDF.

## Validação desta revisão
Revisão estática dos requisitos, imports, rotas e migração SQL realizada. A tentativa de `assembleDebug` foi bloqueada pelo ambiente ao baixar o Gradle (Network is unreachable), antes de compilar. Não houve execução em emulador e não se afirma que os testes práticos passaram. Execute a checklist no Android Studio.
