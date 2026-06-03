package com.yaumi.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yaumi.app.azan.ui.AzanRoute
import com.yaumi.app.doa.ui.DoaRoute
import com.yaumi.app.hadis.ui.HadisRoute
import com.yaumi.app.hijriah.ui.HijriahRoute
import com.yaumi.app.home.FeatureKey
import com.yaumi.app.home.HomeScreen
import com.yaumi.app.qibla.ui.QiblaRoute
import com.yaumi.app.quran.ui.QuranRoute
import com.yaumi.app.settings.ui.SettingsRoute
import com.yaumi.app.tasbih.ui.TasbihRoute
import com.yaumi.app.tadabur.ui.TadaburRoute
import com.yaumi.app.ui.components.RouteHeader

object Routes {
    const val HOME = "home"
    const val QURAN = "quran"
    const val HADIS = "hadis"
    const val DOA = "doa"
    const val AZAN = "azan"
    const val QIBLA = "qibla"
    const val TASBIH = "tasbih"
    const val HIJRIAH = "hijriah"
    const val SETTINGS = "settings"
    const val TADABUR = "tadabur"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(navController = navController, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(contentPadding = PaddingValues()) { key ->
                    when (key) {
                        FeatureKey.QURAN -> navController.navigate(Routes.QURAN)
                        FeatureKey.HADIS -> navController.navigate(Routes.HADIS)
                        FeatureKey.DOA -> navController.navigate(Routes.DOA)
                        FeatureKey.AZAN -> navController.navigate(Routes.AZAN)
                        FeatureKey.QIBLA -> navController.navigate(Routes.QIBLA)
                        FeatureKey.TASBIH -> navController.navigate(Routes.TASBIH)
                        FeatureKey.HIJRIAH -> navController.navigate(Routes.HIJRIAH)
                        FeatureKey.TADABUR -> navController.navigate(Routes.TADABUR)
                        FeatureKey.SETTINGS -> navController.navigate(Routes.SETTINGS)
                    }
                }
            }

            composable(Routes.QURAN) {
                FeatureRoute(
                    title = "Al-Qur'an",
                    subtitle = "Baca, dengarkan, dan tandai ayat",
                    onBack = { navController.popBackStack() }
                ) { padding -> QuranRoute(contentPadding = padding) }
            }

            composable(Routes.HADIS) {
                FeatureRoute(
                    title = "Hadis",
                    subtitle = "Kumpulan hadis pilihan",
                    onBack = { navController.popBackStack() }
                ) { padding -> HadisRoute(contentPadding = padding) }
            }

            composable(Routes.DOA) {
                FeatureRoute(
                    title = "Doa Harian",
                    subtitle = "Doa dari Al-Qur'an dan sunnah",
                    onBack = { navController.popBackStack() }
                ) { padding -> DoaRoute(contentPadding = padding) }
            }

            composable(Routes.AZAN) {
                FeatureRoute(
                    title = "Azan",
                    subtitle = "Jadwal & pengingat sholat",
                    onBack = { navController.popBackStack() }
                ) { padding -> AzanRoute(contentPadding = padding) }
            }

            composable(Routes.QIBLA) {
                FeatureRoute(
                    title = "Kiblat",
                    subtitle = "Arah kiblat dari lokasi Anda",
                    onBack = { navController.popBackStack() }
                ) { padding -> QiblaRoute(contentPadding = padding) }
            }

            composable(Routes.TASBIH) {
                FeatureRoute(
                    title = "Tasbih Digital",
                    subtitle = "Hitung zikir harian",
                    onBack = { navController.popBackStack() }
                ) { padding -> TasbihRoute(contentPadding = padding) }
            }

            composable(Routes.HIJRIAH) {
                FeatureRoute(
                    title = "Kalender Hijriah",
                    subtitle = "Tanggal Hijriah hari ini",
                    onBack = { navController.popBackStack() }
                ) { padding -> HijriahRoute(contentPadding = padding) }
            }

            composable(Routes.SETTINGS) {
                FeatureRoute(
                    title = "Pengaturan",
                    subtitle = null,
                    onBack = { navController.popBackStack() }
                ) { padding -> SettingsRoute(contentPadding = padding) }
            }

            composable(Routes.TADABUR) {
                FeatureRoute(
                    title = "Tadabur",
                    subtitle = "Renungkan ayat harian",
                    onBack = { navController.popBackStack() }
                ) { padding -> TadaburRoute(contentPadding = padding, onComplete = { navController.popBackStack() }) }
            }
        }
    }
}

@Composable
private fun FeatureRoute(
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RouteHeader(title = title, subtitle = subtitle, onBack = onBack)
        val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            content(PaddingValues(top = 4.dp, bottom = navBar + 24.dp))
        }
    }
}
