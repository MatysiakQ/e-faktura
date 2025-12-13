package com.example.e_faktura.ui.onboarding

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// A simple data class to hold the content for each onboarding slide
data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector
)

// --- DataStore Logic to check if onboarding has been completed ---
// This would typically be in your data layer, but for simplicity, we add it here.
// You should create a new file for this logic in a real app, e.g., `data/repository/UserPreferencesRepository.kt`
private fun setOnboardingCompleted(context: Context) {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    with(sharedPrefs.edit()) {
        putBoolean("onboarding_completed", true)
        apply()
    }
}

fun hasOnboardingBeenCompleted(context: Context): Boolean {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPrefs.getBoolean("onboarding_completed", false)
}
// ----------------------------------------------------------------

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val slides = listOf(
        OnboardingSlide("Zarządzaj Firmami", "Wszystkie dane Twoich firm w jednym, bezpiecznym miejscu.", Icons.Filled.List),
        OnboardingSlide("Śledź Dochody", "Monitoruj swoje finanse i miej wszystko pod kontrolą.", Icons.Filled.Star),
        OnboardingSlide("Bezpiecznie i Szybko", "Twoje dane są chronione i dostępne natychmiast.", Icons.Filled.Check)
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) {
            page -> OnboardingSlideContent(slide = slides[page])
        }

        if (pagerState.currentPage == slides.size - 1) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        setOnboardingCompleted(context)
                        navController.navigate("home") {
                            // Clear the back stack so the user cannot go back to onboarding
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text("Rozpocznij")
            }
        } else {
            // Placeholder for indicators or a 'Next' button if you want one
            Spacer(modifier = Modifier.height(66.dp)) // Matches button height + padding
        }
    }
}

@Composable
private fun OnboardingSlideContent(slide: OnboardingSlide) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                imageVector = slide.icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = slide.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = slide.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}