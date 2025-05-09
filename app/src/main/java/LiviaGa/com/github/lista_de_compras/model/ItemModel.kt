package LiviaGa.com.github.lista_de_compras.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class ItemModel(


    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    val name: String
)