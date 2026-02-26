import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.Response

class UserRepositoryTest {

    // Establece la regla para ejecutar tareas de LiveData de forma sincrona
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockApiService: ApiService

    @Mock
    private lateinit var mockUserDao: UserDao

    @Mock
    private lateinit var mockCall: Call<List<User>>

    private lateinit var userRepository: UserRepository
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        userRepository = UserRepository(mockApiService, mockUserDao)
    }

    @Test
    fun getUsers_SuccessfulResponse_DataStoredLocally() = runBlocking {
        // Creamos una instancia de MockWebServer
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val baseUrl = mockWebServer.url("/").toString()

        val dummyUserList = listOf(User(1, "Test User"))
        val successResponse = Response.success(dummyUserList)

        // Configuramos el mockApiService para que simule una respuesta exitosa
        Mockito.`when`(mockApiService.getUsers()).thenReturn(mockCall)
        Mockito.doAnswer { invocation ->
            val callback = invocation.getArgument<retrofit2.Callback<List<User>>>(0)
            callback.onResponse(mockCall, successResponse)
            null
        }.`when`(mockCall).enqueue(Mockito.any())

        // Llamamos al método getUsers() del UserRepository
        val resultLiveData: LiveData<List<User>> = userRepository.getUsers()

        // Observamos el LiveData para esperar cambios
        val latch = CountDownLatch(1)
        var emittedData: List<User>? = null

        val observer = object : Observer<List<User>> {
            override fun onChanged(value: List<User>) {
                emittedData = value
                latch.countDown()
            }
        }

        resultLiveData.observeForever(observer)

        // Esperamos a que se emita algún dato en el LiveData
        latch.await(2, TimeUnit.SECONDS)
        resultLiveData.removeObserver(observer)

        // Verificamos que se haya llamado a la API
        Mockito.verify(mockApiService).getUsers()

        // Verificamos que los datos se hayan almacenado localmente en la base de datos
        Mockito.verify(mockUserDao).insertUsers(dummyUserList)

        // Verificamos que el LiveData haya emitido datos
        assertNotNull(emittedData)

        // Detenemos el MockWebServer
        mockWebServer.shutdown()
    }

    @Test
    fun getUsers_ApiError_NoDataStoredLocally() = runBlocking {
        // Creamos una instancia de MockWebServer
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Configuramos el mockApiService para que retorne un error
        Mockito.`when`(mockApiService.getUsers()).thenReturn(mockCall)
        Mockito.doAnswer { invocation ->
            val callback = invocation.getArgument<retrofit2.Callback<List<User>>>(0)
            callback.onFailure(mockCall, Exception("Internal Server Error"))
            null
        }.`when`(mockCall).enqueue(Mockito.any())

        // Llamamos al método getUsers() del UserRepository
        val resultLiveData: LiveData<List<User>> = userRepository.getUsers()

        // Observamos el LiveData para esperar cambios
        val latch = CountDownLatch(1)
        var emittedData: List<User>? = null

        val observer = object : Observer<List<User>> {
            override fun onChanged(value: List<User>) {
                emittedData = value
                latch.countDown()
            }
        }

        resultLiveData.observeForever(observer)

        // Esperamos a que se emita algún dato en el LiveData
        latch.await(2, TimeUnit.SECONDS)
        resultLiveData.removeObserver(observer)

        // Verificamos que se haya llamado a la API
        Mockito.verify(mockApiService).getUsers()

        // Verificamos que no se hayan almacenado datos localmente en la base de datos
        Mockito.verify(mockUserDao, Mockito.never()).insertUsers(Mockito.anyList())

        // Detenemos el MockWebServer
        mockWebServer.shutdown()
    }
}