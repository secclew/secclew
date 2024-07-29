dependencies {
    implementation "androidx.work:work-runtime-ktx:2.7.1"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9"
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.3.1'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.google.android.gms:play-services-maps:17.0.0'
    implementation 'com.google.maps:google-maps-services:0.15.0'
    implementation 'com.tomtom.online:sdk-maps:2.4.937'
    implementation 'com.tomtom.online:sdk-search:2.4.937'
    implementation 'com.tomtom.online:sdk-traffic:2.4.937'
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:3.11.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
}

object RetrofitInstance {
    private const val BASE_URL = "http://api.olhovivo.sptrans.com.br/v2.1/"

    val api: OlhoVivoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OlhoVivoApi::class.java)
    }
}

interface OlhoVivoApi {
    @POST("Login/Autenticar")
    suspend fun autenticar(@Query("token") token: String): Response<Boolean>

    @GET("Posicao")
    suspend fun getPosicaoVeiculos(@Query("codigoLinha") codigoLinha: Int): Response<PosicaoVeiculosResponse>

    @GET("Linha/Buscar")
    suspend fun getLinhas(@Query("termosBusca") termosBusca: String): Response<List<Linha>>

    @GET("Parada/Buscar")
    suspend fun getParadas(@Query("termosBusca") termosBusca: String): Response<List<Parada>>

    @GET("Previsao/Parada")
    suspend fun getPrevisaoParada(@Query("codigoParada") codigoParada: Int): Response<PrevisaoParadaResponse>
}

<fragment
    android:id="@+id/map"
    android:name="com.google.android.gms.maps.SupportMapFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>

    class MapActivity : AppCompatActivity(), OnMapReadyCallback {
        private lateinit var mMap: GoogleMap

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_map)
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }

        override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap

            // Obter posições dos veículos e marcá-los no mapa
            viewModel.veiculos.observe(this, { veiculos ->
                veiculos.forEach { veiculo ->
                    val position = LatLng(veiculo.latitude, veiculo.longitude)
                    mMap.addMarker(MarkerOptions().position(position).title("Veículo ${veiculo.prefixo}"))
                }
            })
        }
    }
    class LinhaAdapter(private val linhas: List<Linha>) : RecyclerView.Adapter<LinhaAdapter.LinhaViewHolder>() {
        // Implementação do Adapter...
    }

    class ParadaAdapter(private val paradas: List<Parada>) : RecyclerView.Adapter<ParadaAdapter.ParadaViewHolder>() {
        // Implementação do Adapter...
    }
    class PrevisaoActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_previsao)

            // Obter código da parada e buscar previsão
            val codigoParada = intent.getIntExtra("codigoParada", 0)
            viewModel.getPrevisaoParada(codigoParada).observe(this, { previsao ->
                // Exibir previsão de chegada
            })
        }
    }
    class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            val searchView = findViewById<SearchView>(R.id.searchView)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        viewModel.searchLinhas(query)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }
    }

    import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class DataUpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Lógica para atualizar dados
        // Exemplo: Chamar a API para buscar as últimas posições dos veículos
        // viewModel.fetchVeiculos()

        return Result.success()
    }
}

// Agendar a tarefa periódica
fun scheduleDataUpdate(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<DataUpdateWorker>(15, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduleDataUpdate(this)
    }
}

import com.google.maps.GeoApiContext
import com.google.maps.DirectionsApi
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode

fun calculateRoute(apiKey: String, origin: String, destination: String): DirectionsResult {
    val context = GeoApiContext.Builder()
        .apiKey(apiKey)
        .build()

    return DirectionsApi.newRequest(context)
        .mode(TravelMode.TRANSIT)
        .origin(origin)
        .destination(destination)
        .await()
}

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val apiKey = "YOUR_API_KEY"

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val origin = "SOME_ORIGIN"
        val destination = "SOME_DESTINATION"
        val directionsResult = calculateRoute(apiKey, origin, destination)

        // Processar e desenhar a rota no mapa
        val path = directionsResult.routes[0].overviewPolyline.decodePath()
        val polylineOptions = PolylineOptions().addAll(path).color(Color.BLUE).width(5f)
        mMap.addPolyline(polylineOptions)
    }
}

import com.tomtom.online.sdk.traffic.api.TrafficApi
import com.tomtom.online.sdk.traffic.api.TrafficApiFactory
import com.tomtom.online.sdk.traffic.api.incident.TrafficIncidentList

fun getTrafficData(apiKey: String, location: LatLng): TrafficIncidentList {
    val context = TomTomContext.Builder()
        .apiKey(apiKey)
        .build()

    val trafficApi = TrafficApiFactory.create(context)
    return trafficApi.getTrafficIncidents(location)
}

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.transporteapi.viewmodel.TransporteViewModel
import com.example.transporteapi.repository.TransporteRepository
import com.example.transporteapi.model.Veiculo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class TransporteViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: TransporteRepository

    private lateinit var viewModel: TransporteViewModel

    @Mock
    private lateinit var observer: Observer<List<Veiculo>>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = TransporteViewModel(repository)
        viewModel.veiculos.observeForever(observer)
    }

    @Test
    fun fetchVeiculosSuccess() {
        val veiculos = listOf(Veiculo(1, "Bus 1", "123", 10.0, 20.0))
        `when`(repository.getVeiculos()).thenReturn(veiculos)

        viewModel.fetchVeiculos()

        verify(observer).onChanged(veiculos)
    }
}


# TransporteSP

## Descrição

Aplicativo Android que exibe dados sobre o transporte público da cidade de São Paulo, utilizando a API Olho Vivo.

## Funcionalidades

- Exibição de posições dos veículos no mapa.
- Informações sobre linhas de ônibus.
- Exibição de pontos de parada no mapa.
- Previsão de chegada dos veículos em paradas.
- Pesquisa e filtros para dados exibidos.
- Atualização automática dos dados.
- Cálculo de rotas utilizando a API do Google Directions.
- Exibição de velocidade das vias (TomTom Traffic API).

## Requisitos

- Android 5.0 (Lollipop) ou superior.
- Conexão com a internet.

## Instalação

1. Clone o repositório:
    ```bash
    git clone https://github.com/seu-usuario/TransporteSP.git
    ```

2. Abra o projeto no Android Studio.

3. Adicione suas chaves de API para Olho Vivo, Google Directions e TomTom nas respectivas configurações.

4. Compile e execute o aplicativo em um dispositivo ou emulador Android.

## Configuração de Chaves de API

Crie um arquivo `local.properties` na raiz do projeto e adicione suas chaves de API:






