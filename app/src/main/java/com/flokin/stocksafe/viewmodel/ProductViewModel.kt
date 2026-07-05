package com.flokin.stocksafe.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flokin.stocksafe.model.Product
import com.flokin.stocksafe.repository.ProductRepository
import com.flokin.stocksafe.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola state operasi CRUD produk dan stok.
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    // State untuk daftar produk
    private val _productsState = MutableStateFlow<Result<List<Product>>>(Result.Loading)
    val productsState: StateFlow<Result<List<Product>>> = _productsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // State untuk detail satu produk
    private val _detailState = MutableStateFlow<Result<Product>?>(null)
    val detailState: StateFlow<Result<Product>?> = _detailState.asStateFlow()

    // State untuk hasil operasi (create, update, delete, stock)
    private val _operationState = MutableStateFlow<Result<Unit>?>(null)
    val operationState: StateFlow<Result<Unit>?> = _operationState.asStateFlow()

    // Menyimpan nama produk terakhir yang dioperasikan (untuk Snackbar)
    private val _lastActionName = MutableStateFlow("")
    val lastActionName: StateFlow<String> = _lastActionName.asStateFlow()

    // Job untuk debounce search
    private var searchJob: Job? = null

    // Ambil semua produk saat ViewModel pertama kali dibuat
    init {
        getAllProducts()
    }

    // Public refresh that does NOT clobber list with a hard Loading state.
    fun getAllProducts(search: String = "", category: String = "") {
        viewModelScope.launch {
            _isRefreshing.value = true
            Log.d("ProductVM", "getAllProducts start search=$search")
            val result = repository.getAllProducts(search, category)

            // Paksa membuat list baru dengan .toList() agar StateFlow mendeteksi perubahan
            _productsState.value = when (result) {
                is Result.Success -> Result.Success(result.data.toList())
                else -> result
            }

            Log.d(
                "ProductVM",
                "getAllProducts result=" + when (result) {
                    is Result.Success -> "Success size=${result.data.size}"
                    is Result.Error   -> "Error msg=${result.message}"
                    else              -> "Loading"
                }
            )

            _isRefreshing.value = false
        }
    }

    /**
     * Pencarian dengan debounce 500ms.
     * Mencegah request API setiap kali user mengetik 1 huruf.
     */
    fun searchWithDebounce(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            getAllProducts(query)
        }
    }

    /**
     * Mendapatkan detail satu produk berdasarkan ID.
     */
    fun getProductById(id: Int) {
        viewModelScope.launch {
            _detailState.value = Result.Loading
            val result = repository.getProductById(id)
            _detailState.value = result
        }
    }

    /**
     * Menambahkan produk baru.
     */
    fun createProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _lastActionName.value = product.name
            _operationState.value = Result.Loading

            Log.d("ProductVM", "createProduct start name=${product.name}")
            val result = repository.createProduct(product)

            val transformedResult: Result<Unit> = when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error   -> Result.Error(result.message)
                is Result.Loading -> Result.Loading
            }

            if (transformedResult is Result.Success) {
                Log.d("ProductVM", "createProduct success -> getAllProducts + onSuccess")
                getAllProducts()
                onSuccess()
            }

            _operationState.value = transformedResult
        }
    }

    /**
     * Mengupdate data produk.
     */
    fun updateProduct(id: Int, product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _lastActionName.value = product.name
            _operationState.value = Result.Loading

            Log.d("ProductVM", "updateProduct start id=$id name=${product.name}")
            val result = repository.updateProduct(id, product)

            if (result is Result.Success) {
                Log.d("ProductVM", "updateProduct success -> getAllProducts + onSuccess")
                getAllProducts()
                onSuccess()
            }

            _operationState.value = result
        }
    }

    /**
     * Menghapus produk.
     * ✅ Perbaikan: langsung panggil getAllProducts() untuk refresh daftar dari database,
     *    sehingga item langsung hilang dari tampilan tanpa harus keluar layar.
     */
    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _operationState.value = Result.Loading

            Log.d("ProductVM", "deleteProduct start id=$id")
            val result = repository.deleteProduct(id)

            val transformedResult: Result<Unit> = when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error   -> Result.Error(result.message)
                is Result.Loading -> Result.Loading
            }

            if (transformedResult is Result.Success) {
                Log.d("ProductVM", "deleteProduct success -> refresh full list")
                getAllProducts() // 🔄 langsung muat ulang data dari database
            }

            _operationState.value = transformedResult
        }
    }

    /**
     * Input stok masuk.
     */
    fun stockIn(productId: Int, quantity: Int, note: String = "") {
        viewModelScope.launch {
            _operationState.value = Result.Loading

            val result = repository.stockIn(productId, quantity, note)

            val transformedResult: Result<Unit> = when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error   -> Result.Error(result.message)
                is Result.Loading -> Result.Loading
            }

            if (transformedResult is Result.Success) {
                getAllProducts()
                if (_detailState.value != null) {
                    getProductById(productId)
                }
            }

            _operationState.value = transformedResult
        }
    }

    /**
     * Input stok keluar.
     */
    fun stockOut(productId: Int, quantity: Int, note: String = "") {
        viewModelScope.launch {
            _operationState.value = Result.Loading

            val result = repository.stockOut(productId, quantity, note)

            val transformedResult: Result<Unit> = when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error   -> Result.Error(result.message)
                is Result.Loading -> Result.Loading
            }

            if (transformedResult is Result.Success) {
                getAllProducts()
                if (_detailState.value != null) {
                    getProductById(productId)
                }
            }

            _operationState.value = transformedResult
        }
    }

    /**
     * Reset state operasi (dipanggil setelah Snackbar ditampilkan).
     */
    fun resetOperationState() {
        _operationState.value = null
    }

    /**
     * Reset state detail (dipanggil saat keluar dari screen detail).
     */
    fun resetDetailState() {
        _detailState.value = null
    }
}