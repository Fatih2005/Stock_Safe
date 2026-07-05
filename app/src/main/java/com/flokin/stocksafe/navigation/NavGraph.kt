package com.flokin.stocksafe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flokin.stocksafe.ui.AddEditScreen
import com.flokin.stocksafe.ui.LoginScreen
import com.flokin.stocksafe.ui.ProductDetailScreen
import com.flokin.stocksafe.ui.ProductListScreen
import com.flokin.stocksafe.ui.RegisterScreen
import com.flokin.stocksafe.util.SecureStorage
import com.flokin.stocksafe.viewmodel.AuthViewModel
import com.flokin.stocksafe.viewmodel.ProductViewModel

object Routes {
    const val LOGIN          = "login"
    const val REGISTER       = "register"
    const val PRODUCT_LIST   = "product_list"
    const val ADD_PRODUCT    = "add_product"
    const val DETAIL_PRODUCT = "product_detail/{productId}"
    const val EDIT_PRODUCT   = "product_edit/{productId}"

    fun detailRoute(id: Int) = "product_detail/$id"
    fun editRoute(id: Int)   = "product_edit/$id"
}

@Composable
fun AppNavGraph() {
    val context              = LocalContext.current
    val navController        = rememberNavController()
    val authViewModel: AuthViewModel       = viewModel()
    val productViewModel: ProductViewModel = viewModel()

    val isLoggedIn       = SecureStorage.isLoggedIn(context) &&
                           !SecureStorage.isTokenExpired(context)
    val startDestination = if (isLoggedIn) Routes.PRODUCT_LIST else Routes.LOGIN

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ================================================
        // SCREEN LOGIN
        // ================================================
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel        = authViewModel,
                onLoginSuccess       = {
                    navController.navigate(Routes.PRODUCT_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // ================================================
        // SCREEN REGISTER
        // ================================================
        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel     = authViewModel,
                onRegisterSuccess = {
                    // Setelah register berhasil → kembali ke login
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onBackToLogin     = {
                    navController.popBackStack()
                }
            )
        }

        // ================================================
        // SCREEN DAFTAR PRODUK
        // ================================================
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                productViewModel = productViewModel,
                authViewModel    = authViewModel,
                onAddClick       = {
                    navController.navigate(Routes.ADD_PRODUCT)
                },
                onDetailClick    = { productId ->
                    navController.navigate(Routes.detailRoute(productId))
                },
                onLogout         = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ================================================
        // SCREEN TAMBAH PRODUK
        // ================================================
        composable(Routes.ADD_PRODUCT) {
            AddEditScreen(
                productViewModel = productViewModel,
                productId        = null,
                onBack           = { navController.popBackStack() }
            )
        }

        // ================================================
        // SCREEN DETAIL PRODUK
        // ================================================
        composable(
            route     = Routes.DETAIL_PRODUCT,
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailScreen(
                productId        = productId,
                productViewModel = productViewModel,
                onBack           = { navController.popBackStack() },
                onEdit           = {
                    navController.navigate(Routes.editRoute(productId))
                },
                onDeleted        = { navController.popBackStack() }
            )
        }

        // ================================================
        // SCREEN EDIT PRODUK
        // ================================================
        composable(
            route     = Routes.EDIT_PRODUCT,
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            AddEditScreen(
                productViewModel = productViewModel,
                productId        = productId,
                onBack           = { navController.popBackStack() }
            )
        }
    }
}