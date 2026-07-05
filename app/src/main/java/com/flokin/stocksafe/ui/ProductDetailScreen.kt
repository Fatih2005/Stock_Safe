package com.flokin.stocksafe.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.Arrangement
import com.flokin.stocksafe.ui.theme.StockIn
import com.flokin.stocksafe.ui.theme.StockLow
import com.flokin.stocksafe.ui.theme.StockOut
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flokin.stocksafe.model.Product
import com.flokin.stocksafe.model.StockStatus
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    productViewModel: ProductViewModel = viewModel(),
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit
) {
    val detailState    by productViewModel.detailState.collectAsState()
    val operationState by productViewModel.operationState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStockInDialog  by remember { mutableStateOf(false) }
    var showStockOutDialog by remember { mutableStateOf(false) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var restockQuantity by remember { mutableStateOf("") }
    var restockNote by remember { mutableStateOf("") }
    var restockError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        productViewModel.getProductById(productId)
    }

    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is Result.Success -> {
                snackbarHostState.showSnackbar("Operasi berhasil")
                productViewModel.resetOperationState()
                onDeleted()
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
                        title          = { Text("Detail Produk") },
                        navigationIcon = {
                            IconButton(onClick = {
                                productViewModel.resetDetailState()
                                onBack()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Hapus",
                                    tint               = colorScheme.error
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor         = colorScheme.primary,
                            titleContentColor      = colorScheme.onPrimary,
                            actionIconContentColor = colorScheme.onPrimary,
                            navigationIconContentColor = colorScheme.onPrimary
                        ),
                        modifier = Modifier.zIndex(2f)
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

                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = detailState) {
                        is Result.Loading -> CircularProgressIndicator(color = colorScheme.primary)

                        is Result.Error -> Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                        ) {
                            Text(state.message, color = colorScheme.error)
                            TextButton(onClick = {
                                productViewModel.getProductById(productId)
                            }) { Text("Coba Lagi") }
                        }

                        is Result.Success -> {
                            ProductDetailContent(
                                product          = state.data,
                                onStockIn        = { showStockInDialog  = true },
                                onStockOut       = { showStockOutDialog = true },
                                onRestock        = { showRestockDialog = true },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        null -> {}
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title            = { Text("Hapus Produk?") },
                text             = { Text("Data produk ini akan dihapus secara permanen.") },
                confirmButton    = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        productViewModel.deleteProduct(productId)
                    }) {
                        Text("Hapus", color = colorScheme.error)
                    }
                },
                dismissButton    = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        if (showStockInDialog) {
            StockInputDialog(
                title       = "Stok Masuk",
                buttonColor = StockIn,
                onConfirm   = { quantity, note ->
                    showStockInDialog = false
                    productViewModel.stockIn(productId, quantity, note)
                },
                onDismiss   = { showStockInDialog = false }
            )
        }

        if (showStockOutDialog) {
            StockInputDialog(
                title       = "Stok Keluar",
                buttonColor = StockOut,
                onConfirm   = { quantity, note ->
                    showStockOutDialog = false
                    productViewModel.stockOut(productId, quantity, note)
                },
                onDismiss   = { showStockOutDialog = false }
            )
        }

        if (showRestockDialog && detailState is Result.Success) {
            val currentStock = (detailState as Result.Success<Product>).data.stock
            AlertDialog(
                onDismissRequest = { showRestockDialog = false },
                title = { Text("Isi Ulang Stok") },
                text = {
                    Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Stok sekarang: $currentStock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = restockQuantity,
                            onValueChange = {
                                restockQuantity = it.filter { c -> c.isDigit() }
                                restockError = when {
                                    restockQuantity.isBlank() -> "Jumlah wajib diisi"
                                    (restockQuantity.toIntOrNull() ?: 0) <= 0 -> "Jumlah harus lebih dari 0"
                                    else -> null
                                }
                            },
                            label = { Text("Jumlah Tambahan *") },
                            isError = restockError != null,
                            supportingText = {
                                if (restockError != null) Text(restockError!!, color = colorScheme.error)
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = restockNote,
                            onValueChange = { restockNote = it },
                            label = { Text("Keterangan (opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val qty = restockQuantity.toIntOrNull() ?: 0
                        if (qty > 0) {
                            showRestockDialog = false
                            productViewModel.stockIn(productId, qty, restockNote)
                            restockQuantity = ""
                            restockNote = ""
                            restockError = null
                        } else {
                            restockError = "Jumlah harus lebih dari 0"
                        }
                    }, shape = RoundedCornerShape(12.dp)) {
                        Text("Konfirmasi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRestockDialog = false
                        restockQuantity = ""
                        restockNote = ""
                        restockError = null
                    }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun ProductDetailContent(
    product: Product,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onRestock: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null
) {
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    val stockStatus  = product.getStockStatus()
    val statusColor  = when (stockStatus) {
        StockStatus.OK  -> StockIn
        StockStatus.LOW -> StockLow
        StockStatus.OUT -> StockOut
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = colorScheme.primary.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Warehouse,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = product.category.ifBlank { "Tanpa Kategori" },
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                DivideLine()
                DetailRow("SKU", product.sku)
                DetailRow("Supplier", product.supplier.ifEmpty { "-" })
                DetailRow("Harga", rupiahFormat.format(product.price))
                DetailRow("Stok Min", "${product.stockMin} ${product.unit}")
            }
        }

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = statusColor.copy(alpha = 0.08f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text  = "Status Stok Saat Ini",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "${product.stock} ${product.unit}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = statusColor,
                    fontWeight = FontWeight.ExtraBold
                )
                val statusLabel = when (stockStatus) {
                    StockStatus.OK  -> "Stok Aman"
                    StockStatus.LOW -> "Stok Menipis!"
                    StockStatus.OUT -> "Stok Habis!"
                }
                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text  = statusLabel,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick  = onStockIn,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockIn,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text("+ Stok Masuk", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick  = onStockOut,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                enabled = product.stock > 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (product.stock > 0) StockOut else colorScheme.onSurfaceVariant
                )
            ) {
                Text("- Stok Keluar", fontWeight = FontWeight.Bold)
            }
        }

        if (stockStatus == StockStatus.OUT) {
            Button(
                onClick = onRestock,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.secondary,
                    contentColor = colorScheme.onSecondary
                )
            ) {
                Text("Isi Ulang Stok", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DivideLine() {
    Spacer(modifier = Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colorScheme.outlineVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text  = ": $value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )
    }
}

@Composable
fun StockInputDialog(
    title: String,
    buttonColor: androidx.compose.ui.graphics.Color,
    onConfirm: (quantity: Int, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity    by remember { mutableStateOf("") }
    var note        by remember { mutableStateOf("") }
    var quantityError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text(title) },
        text             = {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = quantity,
                    onValueChange = {
                        quantity      = it.filter { c -> c.isDigit() }
                        quantityError = when {
                            quantity.isBlank() -> "Jumlah wajib diisi"
                            (quantity.toIntOrNull() ?: 0) <= 0 -> "Jumlah harus lebih dari 0"
                            else -> null
                        }
                    },
                    label           = { Text("Jumlah *") },
                    isError         = quantityError != null,
                    supportingText  = {
                        if (quantityError != null) {
                            Text(quantityError!!, color = colorScheme.error)
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value         = note,
                    onValueChange = { note = it },
                    label         = { Text("Keterangan (opsional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (qty > 0) onConfirm(qty, note)
                    else quantityError = "Jumlah harus lebih dari 0"
                },
                colors   = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                enabled  = quantity.isNotBlank() && quantityError == null,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Konfirmasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
