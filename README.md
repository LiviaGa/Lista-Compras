# 📱 Lista de Compras - Aplicativo Android

Este projeto é um exemplo de aplicativo Android que permite criar e remover itens de uma lista de compras. Ele utiliza os principais componentes modernos do Android como:

- **RecyclerView** para listagem dinâmica
- **Room Database** para persistência local
- **ViewModel** para manter os dados mesmo com mudanças de configuração
- **Coroutines** para operações assíncronas

---

Antes de adicionar um item na lista:

![WhatsApp Image 2025-05-16 at 10 41 00](https://github.com/user-attachments/assets/0c2aa41c-7ed5-4ee0-873e-110775b64631)

Depois de adicionar o item:


![WhatsApp Image 2025-05-16 at 10 42 25](https://github.com/user-attachments/assets/714e3f5f-4dcd-46bd-835c-b3d2082ced23)

Excluindo o item "Playstation 5" da lista:


![WhatsApp Image 2025-05-16 at 10 42 45](https://github.com/user-attachments/assets/38798a19-d9d8-4dac-bef5-8fedc524d13a)



## 🧱 Estrutura do Projeto

```
├── model/
│   └── ItemModel.kt
├── data/
│   ├── ItemDao.kt
│   └── ItemDatabase.kt
├── viewmodel/
│   └── ItemsViewModel.kt
├── adapter/
│   └── ItemsAdapter.kt
├── MainActivity.kt
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   └── item_layout.xml
│   └── drawable/
│       └── ic_delete.xml
└── AndroidManifest.xml
```

---

## 🧩 Conceitos Envolvidos

### 🔁 RecyclerView

- Estrutura para exibir listas de forma eficiente e reaproveitável.
- Cada item é um objeto (`ItemModel`) com seu próprio layout.
- Quando a lista é rolada, o RecyclerView reaproveita os elementos da tela, melhorando a performance.

### 💾 Room Database

- Abstração moderna sobre o SQLite.
- Cria tabelas a partir de `@Entity` e permite acesso via `@Dao`.

### 🧠 ViewModel + LiveData

- Separa lógica de UI dos dados.
- LiveData permite observar alterações nos dados.
- ViewModel sobrevive a mudanças de configuração (como rotação de tela).

### 🧵 Coroutines

- Operações em background (acesso ao banco, por exemplo) são feitas com `Dispatchers.IO`.
- Evita travamento da interface ao manipular dados.

---

## 🛠️ Dependências (build.gradle)

```kotlin
implementation("androidx.room:room-ktx:2.4.1")
kapt("androidx.room:room-compiler:2.4.1")

implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
implementation("androidx.appcompat:appcompat:1.4.1")
implementation("androidx.activity:activity-ktx:1.7.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
```

---

## 📦 Banco de Dados

### `ItemModel.kt`

```kotlin
@Entity
data class ItemModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
```

@Entity: Essa linha diz para o Room Database (seu caderno inteligente) que ItemModel é algo que ele deve guardar como uma linha na tabela.
@PrimaryKey(autoGenerate = true): Imagina que cada item da sua lista precisa de um número de identificação único. Essa linha diz que o id é esse número e que o banco de dados vai gerá-lo automaticamente para você (começando do 0 e aumentando).
val name: String: Isso define que cada item terá um nome (que é um texto).

### `ItemDao.kt`

```kotlin
@Dao
interface ItemDao {
    @Query("SELECT * FROM ItemModel")
    fun getAll(): LiveData<List<ItemModel>>

    @Insert
    fun insert(item: ItemModel)

    @Delete
    fun delete(item: ItemModel)
}
```
@Dao: Diz que esta é a pessoa que sabe como interagir com o banco de dados.
@Query("SELECT * FROM ItemModel"): Essa é uma "pergunta" que você faz ao banco de dados: "Me traga TUDO (*) que está na tabela ItemModel".
fun getAll(): LiveData<List<ItemModel>>: Essa função vai te dar uma lista de todos os seus itens (List<ItemModel>), e essa lista é "observável" (LiveData), ou seja, se algo mudar nela, a tela será avisada.
@Insert e fun insert(item: ItemModel): É a instrução para adicionar um novo item (item: ItemModel) no banco de dados.
@Delete e fun delete(item: ItemModel): É a instrução para remover um item (item: ItemModel) do banco de dados.


### `ItemDatabase.kt`

```kotlin
@Database(entities = [ItemModel::class], version = 1)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}
```
@Database(entities = [ItemModel::class], version = 1): Essa linha diz que este é o seu banco de dados. Ela especifica quais tipos de "coisas" (entities = [ItemModel::class]) serão guardadas nele e qual a "versão" do seu banco (a version = 1 indica que é a primeira versão dele).
abstract fun itemDao(): ItemDao: Isso diz que o seu banco de dados tem uma "interface" para você interagir com ele, que é o seu ItemDao.

---

## 🧠 ViewModel (Cérebro da operação)

### `ItemsViewModel.kt`

```kotlin
class ItemsViewModel(application: Application) : AndroidViewModel(application) {

    private val itemDao: ItemDao

    init {
        val database = Room.databaseBuilder(
            application,
            ItemDatabase::class.java,
            "items_database"
        ).build()

        itemDao = database.itemDao()
    }

    fun addItem(item: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = ItemModel(name = item)
            itemDao.insert(newItem)
        }
    }

    fun removeItem(item: ItemModel) {
        viewModelScope.launch(Dispatchers.IO) {
            itemDao.delete(item)
        }
    }
}
```
class ItemsViewModel(...) : AndroidViewModel(...): Basicamente, está dizendo que este é o "gerente de palco" para os itens da lista e que ele tem acesso a coisas básicas do aplicativo (como o contexto para criar o banco de dados).
init { ... }: Esse bloco é executado quando o ViewModel é criado. É onde ele "configura" o acesso ao seu banco de dados (Room.databaseBuilder(...)) para poder guardar e pegar as informações. O "items_database" é o nome do seu arquivo de banco de dados.
fun addItem(item: String): Essa função é a que chama para adicionar um novo item à sua lista.
viewModelScope.launch(Dispatchers.IO) { ... }: Aqui entram as Coroutines (launch). Essa parte diz: "Vou fazer essa tarefa de adicionar o item em segundo plano (Dispatchers.IO), sem travar o aplicativo principal".
val newItem = ItemModel(name = item): Cria um novo item com o nome que foi digitado.
itemDao.insert(newItem): Pede para o ItemDao (a pessoa que sabe interagir com o banco) guardar esse novo item.
fun removeItem(item: ItemModel): Essa função é para remover um item da lista, também fazendo isso em segundo plano para não travar o app.
---

## 🔄 ItemsAdapter

```kotlin
class ItemsAdapter(private val onItemRemoved: (ItemModel) -> Unit)
    : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>() {

    private var items = listOf<ItemModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.textView)
        private val button: Button = view.findViewById(R.id.deleteButton)

        fun bind(item: ItemModel) {
            textView.text = item.name
            button.setOnClickListener {
                onItemRemoved(item)
            }
        }
    }
}
```
class ItemsAdapter(...) : RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>(): Basicamente, este é o "garçom" que trabalha com o RecyclerView (o mural inteligente). Ele sabe como pegar os dados e mostrar na tela.
O (private val onItemRemoved: (ItemModel) -> Unit) é uma forma de dizer que, quando um item for removido, ele vai "avisar" alguém (que no caso é o ViewModel para apagar do banco).
private var items = listOf<ItemModel>(): É a lista de itens que o garçom está carregando na bandeja para mostrar.
override fun onCreateViewHolder(...): É quando o garçom precisa criar um novo espaço na bandeja para colocar um item. Ele "infla" (transforma em algo visível) o desenho do item_layout.xml.
override fun getItemCount(): Int = items.size: O garçom está te dizendo quantos itens ele tem na bandeja.
override fun onBindViewHolder(...): É o momento em que o garçom pega um item específico da lista (items[position]) e o coloca no espaço da bandeja (holder), preenchendo as informações dele.
inner class ItemViewHolder(...): Essa é a "caneta" do garçom. É com ela que ele escreve o nome do item e coloca o botão de apagar em cada espaço da bandeja.
textView.text = item.name: Coloca o nome do item no local certo na tela.
button.setOnClickListener { onItemRemoved(item) }: Quando clica no botão de apagar, ele chama a função onItemRemoved (que foi passada para o Adapter) e diz qual item deve ser removido.
---

## 🖼️ Interface XML

- `activity_main.xml`: Contém o campo de texto (EditText), botão de adicionar e RecyclerView.
- `item_layout.xml`: Layout de cada item com texto e botão de exclusão.

---

## 🧪 Inspecionar o Banco

1. Acesse `View > Tool Windows > App Inspection`
2. Vá em:
```
Device Explorer > data > data > pacote_do_app > database > items_database
```
3. Visualize e edite os dados diretamente.

---

## ✅ Observações Extras

- Se o botão de adicionar for clicado com o campo vazio, um erro aparece no EditText.
- O `RecyclerView` calcula automaticamente quantos itens cabem na tela.
- O botão de remover chama `onItemRemoved`, que por sua vez chama `removeItem()` no ViewModel.
