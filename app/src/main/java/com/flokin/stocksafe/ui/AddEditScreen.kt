package com.flokin.stocksafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flokin.stocksafe.model.Product
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    productViewModel: ProductViewModel = viewModel(),
    productId: Int? = null,
    onBack: () -> Unit
) {
    val isEditMode     = productId != null
    val detailState    by productViewModel.detailState.collectAsState()
    val operationState by productViewModel.operationState.collectAsState()

    var name     by rememberSaveable { mutableStateOf("") }
    var sku      by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var supplier by rememberSaveable { mutableStateOf("") }
    var price    by rememberSaveable { mutableStateOf("") }
    var stock    by rememberSaveable { mutableStateOf("") }
    var stockMin by rememberSaveable { mutableStateOf("5") }
    var unit     by rememberSaveable { mutableStateOf("pcs") }

    var nameError  by remember { mutableStateOf<String?>(null) }
    var skuError   by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }

    var isFormInit    by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Ambil data produk saat mode edit
    LaunchedEffect(productId) {
        if (isEditMode && productId != null) {
            productViewModel.getProductById(productId)
        }
    }

    // Isi form otomatis saat mode edit
    LaunchedEffect(detailState) {
        if (isEditMode && detailState is Result.Success && !isFormInit) {
            val product = (detailState as Result.Success<Product>).data
            name     = product.name
            sku      = product.sku
            category = product.category
            supplier = product.supplier
            price    = product.price.toLong().toString()
            stock    = product.stock.toString()
            stockMin = product.stockMin.toString()
            unit     = product.unit
            isFormInit = true
        }
    }

    // Handle hasil operasi simpan
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is Result.Success -> {
                productViewModel.resetOperationState()
                // onBack() telah dihapus di sini untuk mencegah bug layar putih
            }
            is Result.Error -> {
                snackbarHostState.showSnackbar(state.message)
                productViewModel.resetOperationState()
            }
            else -> {}
        }
    }

    // Validasi semua field
    fun validateAll(): Boolean {
        nameError = when {
            name.isBlank()         -> "Nama produk wajib diisi"
            name.trim().length < 2 -> "Nama minimal 2 karakter"
            else                   -> null
        }
        skuError = when {
            sku.isBlank()         -> "SKU wajib diisi"
            sku.trim().length < 3 -> "SKU minimal 3 karakter"
            else                  -> null
        }
        priceError = when {
            price.isBlank()                  -> null
            (price.toLongOrNull() ?: -1) < 0 -> "Harga tidak boleh negatif"
            else                             -> null
        }
        stockError = when {
            // Saat mode tambah, stok wajib diisi
            !isEditMode && stock.isBlank()    -> "Stok awal wajib diisi"
            (stock.toIntOrNull() ?: -1) < 0   -> "Stok tidak boleh negatif"
            else                              -> null
        }
        return listOf(nameError, skuError, priceError, stockError).all { it == null }
    }

    val isLoading = operationState is Result.Loading

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Produk" else "Tambah Produk")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors  = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.zIndex(2f)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor   = MaterialTheme.colorScheme.onSurface,
                    shape          = RoundedCornerShape(12.dp)
                ) {
                    Text(it.visuals.message)
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(22.dp)),
                    shape  = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header card
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape    = RoundedCornerShape(10.dp),
                                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector        = Icons.Filled.Warehouse,
                                        contentDescription = null,
                                        modifier           = Modifier.size(18.dp),
                                        tint               = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text       = if (isEditMode) "Edit Data Produk" else "Tambah Produk Baru",
                                style      = MaterialTheme.typography.titleMedium,
                                color      = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Field Nama Produk
                        AddEditField(
                            value         = name,
                            onValueChange = {
                                name      = it
                                nameError = when {
                                    it.isBlank()         -> "Nama produk wajib diisi"
                                    it.trim().length < 2 -> "Nama minimal 2 karakter"
                                    else                 -> null
                                }
                            },
                            label = "Nama Produk *",
                            error = nameError
                        )

                        // Field SKU
                        AddEditField(
                            value         = sku,
                            onValueChange = {
                                sku      = it.uppercase()
                                skuError = when {
                                    it.isBlank()         -> "SKU wajib diisi"
                                    it.trim().length < 3 -> "SKU minimal 3 karakter"
                                    else                 -> null
                                }
                            },
                            label = "SKU *",
                            error = skuError
                        )

                        // Field Kategori
                        AddEditField(
                            value         = category,
                            onValueChange = { category = it },
                            label         = "Kategori",
                            error         = null
                        )

                        // Field Supplier
                        AddEditField(
                            value         = supplier,
                            onValueChange = { supplier = it },
                            label         = "Supplier",
                            error         = null
                        )

                        // Field Harga
                        AddEditField(
                            value         = price,
                            onValueChange = {
                                price      = it.filter { c -> c.isDigit() }
                                priceError = if ((price.toLongOrNull() ?: 0) < 0)
                                    "Harga tidak boleh negatif" else null
                            },
                            label        = "Harga (Rp)",
                            error        = priceError,
                            keyboardType = KeyboardType.Number
                        )

                        // Field Stok
                        AddEditField(
                            value         = stock,
                            onValueChange = {
                                stock      = it.filter { c -> c.isDigit() }
                                stockError = if ((stock.toIntOrNull() ?: 0) < 0)
                                    "Stok tidak boleh negatif" else null
                            },
                            label        = if (isEditMode) "Stok Saat Ini (ubah via Stok Masuk/Keluar)" else "Stok Awal *",
                            error        = stockError,
                            enabled      = !isEditMode,
                            keyboardType = KeyboardType.Number
                        )

                        // Field Stok Minimum
                        AddEditField(
                            value         = stockMin,
                            onValueChange = { stockMin = it.filter { c -> c.isDigit() } },
                            label         = "Stok Minimum",
                            error         = null,
                            keyboardType  = KeyboardType.Number
                        )

                        // Field Satuan
                        AddEditField(
                            value         = unit,
                            onValueChange = { unit = it },
                            label         = "Satuan (pcs, unit, rim, dll)",
                            error         = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tombol Simpan
                Button(
                    onClick = {
                        if (validateAll()) {
                            val product = Product(
                                id       = 0,
                                name     = name.trim(),
                                sku      = sku.trim(),
                                category = category.trim(),
                                supplier = supplier.trim(),
                                price    = price.toDoubleOrNull()    ?: 0.0,
                                stock    = stock.toIntOrNull()        ?: 0,
                                stockMin = stockMin.toIntOrNull()     ?: 5,
                                unit     = unit.trim().ifEmpty { "pcs" }
                            )
                            if (isEditMode && productId != null) {
                                productViewModel.updateProduct(productId, product) { onBack() }
                            } else {
                                productViewModel.createProduct(product) { onBack() }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading,
                    shape   = RoundedCornerShape(16.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color    = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text  = if (isEditMode) "Simpan Perubahan" else "Tambah Produk",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun AddEditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        enabled        = enabled,
        label          = { Text(label) },
        isError        = error != null,
        supportingText = {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine      = true,
        modifier        = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp)
    )
}