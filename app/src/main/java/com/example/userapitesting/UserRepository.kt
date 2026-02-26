import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Interfaces y Clases de ayuda necesarias para que el código compile
data class User(val id: Int, val name: String)
interface ApiService { fun getUsers(): Call<List<User>> }
interface UserDao { fun insertUsers(users: List<User>?) }

class UserRepository(private val apiService: ApiService, private val userDao: UserDao) {

    fun getUsers(): LiveData<List<User>> {
        val usersLiveData = MutableLiveData<List<User>>()

        // Obtenemos los datos de la API utilizando Retrofit
        val call: Call<List<User>> = apiService.getUsers()

        call.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    // Almacenamos los datos localmente en la base de datos
                    userDao.insertUsers(response.body())
                    // Emitimos los datos almacenados a través del LiveData
                    usersLiveData.value = response.body()
                } else {
                    // Manejar el estado de error
                    usersLiveData.value = emptyList()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                // Manejar el error de conexión
                usersLiveData.value = emptyList()
            }
        })
        return usersLiveData
    }
}