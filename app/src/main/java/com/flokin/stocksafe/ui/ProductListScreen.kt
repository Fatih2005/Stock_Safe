package com.flokin.stocksafe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Arrangement
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flokin.stocksafe.model.Product
import com.flokin.stocksafe.model.StockStatus
import com.flokin.stocksafe.ui.theme.StockIn
import com.flokin.stocksafe.ui.theme.StockLow
import com.flokin.stocksafe.ui.theme.StockOut
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.viewmodel.AuthViewModel
import com.flokin.stocksafe.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    productViewModel: ProductViewModel = viewModel(),
    authViewModel: AuthViewModel       = viewModel(),
    onAddClick: () -> Unit,
    onDetailClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val username        = authViewModel.getUsername()
    val productsState   by productViewModel.productsState.collectAsState()
    val operationState  by productViewModel.operationState.collectAsState()

    var searchQuery      by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var productToDelete  by remember { mutableStateOf<Product?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // SOLUSI: Hapus pengecekan 'if'.
    // Sekarang, setiap kali layar List ini muncul (termasuk saat kembali dari layar hapus/detail),
    // aplikasi akan memaksa me-refresh data terbaru dari database.
    LaunchedEffect(Unit) {
        productViewModel.getAllProducts()
    }

    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is Result.Success -> {
                val name    = productViewModel.lastActionName.value
                val message = if (name.isNotEmpty())
                    "$name berhasil diproses" else "Operasi berhasil"
                snackbarHostState.showSnackbar(message)
                productViewModel.resetOperationState()
            }
            is Result.Error -> {
                snackbarHostState.showSnackbar(state.message)
                productViewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.04f),
                            colorScheme.surface,
                            colorScheme.surface
                        )
                    )
                )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "StockSafe",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = username ?: "Karyawan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colorScheme.onPrimary.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = colorScheme.onPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor         = colorScheme.primary,
                            titleContentColor      = colorScheme.onPrimary,
                            actionIconContentColor = colorScheme.onPrimary
                        ),
                        modifier = Modifier.zIndex(2f)
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = onAddClick,
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Tambah Produk"
                            )
                        },
                        text = { Text("Tambah") }
                    )
                },
                snackbarHost = {
                    SnackbarHost(snackbarHostState) {
                        Snackbar(
                            containerColor = colorScheme.surface,
                            contentColor = colorScheme.onSurface,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(it.visuals.message)
                        }
                    }
                }
            ) { paddingValues ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            productViewModel.searchWithDebounce(it)
                        },
                        label         = { Text("Cari produk...") },
                        leadingIcon   = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon  = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    productViewModel.getAllProducts()
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine    = true,
                        modifier      = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = productsState) {
                        is Result.Loading -> {
                            Box(
                                modifier          = Modifier.fillMaxSize(),
                                contentAlignment  = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                        }

                        is Result.Error -> {
                            Box(
                                modifier          = Modifier.fillMaxSize(),
                                contentAlignment  = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(72.dp),
                                        shape = CircleShape,
                                        color = colorScheme.errorContainer.copy(alpha = 0.6f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                modifier = Modifier.size(36.dp),
                                                tint = colorScheme.error
                                            )
                                        }
                                    }
                                    Text(
                                        text  = state.message,
                                        color = colorScheme.error,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    TextButton(onClick = {
                                        productViewModel.getAllProducts()
                                    }) {
                                        Text("Coba Lagi")
                                    }
                                }
                            }
                        }

                        is Result.Success -> {
                            val products = state.data

                            if (products.isEmpty()) {
                                Box(
                                    modifier         = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text  = if (searchQuery.isNotEmpty())
                                            "Tidak ada hasil untuk \"$searchQuery\""
                                        else
                                            "Belum ada produk",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                StatsRow(products = products)

                                Spacer(modifier = Modifier.height(8.dp))

                                if (productViewModel.isRefreshing.collectAsState().value) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = products,
                                        key   = { it.id }
                                    ) { product ->
                                        ProductListItem(
                                            product  = product,
                                            onClick  = { onDetailClick(product.id) },
                                            onDelete = { productToDelete = product }
                                        )
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title            = { Text("Logout?") },
                text             = { Text("Anda yakin ingin keluar dari aplikasi?") },
                confirmButton    = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        authViewModel.logout(onLogout)
                    }) {
                        Text("Keluar", color = colorScheme.error)
                    }
                },
                dismissButton    = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        productToDelete?.let { product ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title            = { Text("Hapus Produk?") },
                text             = {
                    Text("Data \"${product.name}\" akan dihapus secara permanen.")
                },
                confirmButton    = {
                    TextButton(onClick = {
                        productViewModel.deleteProduct(product.id)
                        productToDelete = null
                        // Tambahkan pemanggilan ini agar saat dihapus dari List, layarnya juga langsung update
                        productViewModel.getAllProducts()
                    }) {
                        Text("Hapus", color = colorScheme.error)
                    }
                },
                dismissButton    = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatsRow(products: List<Product>) {
    val okCount = products.count { it.getStockStatus() == StockStatus.OK }
    val lowCount = products.count { it.getStockStatus() == StockStatus.LOW }
    val outCount = products.count { it.getStockStatus() == StockStatus.OUT }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MiniStat(title = "Total", value = "${products.size}", modifier = Modifier.weight(1f))
        MiniStat(
            title = "Aman",
            value = "$okCount",
            modifier = Modifier.weight(1f),
            dotColor = StockIn
        )
        MiniStat(
            title = "Menipis",
            value = "$lowCount",
            modifier = Modifier.weight(1f),
            dotColor = StockLow
        )
        MiniStat(
            title = "Habis",
            value = "$outCount",
            modifier = Modifier.weight(1f),
            dotColor = StockOut
        )
    }
}

@Composable
private fun MiniStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    dotColor: androidx.compose.ui.graphics.Color? = null
) {
    Surface(
        modifier = modifier.shadow(1.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    color = dotColor ?: colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (dotColor != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = dotColor
                    ) {}
                }
            }
        }
    }
}

@Composable
fun ProductListItem(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val rupiahFormat  = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val stockStatus   = product.getStockStatus()
    val statusColor   = when (stockStatus) {
        StockStatus.OK  -> StockIn
        StockStatus.LOW -> StockLow
        StockStatus.OUT -> StockOut
    }
    val statusLabel   = when (stockStatus) {
        StockStatus.OK  -> "Aman"
        StockStatus.LOW -> "Menipis"
        StockStatus.OUT -> "Habis"
    }

    Card(
        onClick    = onClick,
        modifier   = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text  = "SKU: ${product.sku} • ${product.category.ifBlank { "Tanpa Kategori" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text  = rupiahFormat.format(product.price),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Badge(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        contentColor = statusColor
                    ) {
                        Text(text = statusLabel, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text  = "${product.stock} ${product.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (stockStatus != StockStatus.OK) statusColor else colorScheme.onSurface,
                        fontWeight = if (stockStatus != StockStatus.OK) FontWeight.Bold else FontWeight.Normal
                    )
                    if (stockStatus != StockStatus.OK) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector        = Icons.Filled.Warning,
                            contentDescription = "Stok menipis",
                            tint               = statusColor,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = colorScheme.errorContainer.copy(alpha = 0.6f)
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        tint               = colorScheme.error,
                        modifier           = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}