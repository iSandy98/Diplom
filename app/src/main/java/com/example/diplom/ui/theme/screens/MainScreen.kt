package com.example.diplom.ui.theme.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.diplom.utils.CurrentRegionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private sealed class MainRoute(
    val route: String,
    val label: String
) {
    data object Home : MainRoute("main_home", "Главная")
    data object Map : MainRoute("main_map", "Карта")
    data object Profile : MainRoute("main_profile", "Профиль")

    data object Districts : MainRoute("districts", "Районы")
    data object DistrictDetail : MainRoute("district_detail/{districtId}", "Район")
    data object Stories : MainRoute("stories", "История")
    data object PlaceDetail : MainRoute("place_detail/{placeId}", "Место")
    data object EditProfile : MainRoute("edit_profile", "Редактировать")

    data object StoryDetail : MainRoute("story_detail/{storyId}", "История")
}

@Composable
fun MainScreen(
    isGuest: Boolean = false,
    onPlayAudio: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context =
        LocalContext.current

    val currentRegionManager =

        remember {

            CurrentRegionManager(
                context
            )
        }

    val offlineRepository =

        remember {

            com.example.diplom.network
                .OfflineRepository(
                    context
                )
        }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""

    val gold = Color(0xFFFFD382)

    val isHomeSelected =

        currentRoute in listOf(

            MainRoute.Home.route,

            MainRoute.Districts.route,

            MainRoute.Stories.route,

            MainRoute.EditProfile.route

        ) ||

                currentRoute.startsWith(
                    "district_detail"
                ) ||

                currentRoute.startsWith(
                    "place_detail"
                ) ||

                currentRoute.startsWith(
                    "story_detail"
                )

    val isMapSelected = currentRoute == MainRoute.Map.route
    val isProfileSelected = currentRoute == MainRoute.Profile.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White
            ) {
                NavigationBarItem(
                    selected = isHomeSelected,
                    onClick = {

                        navController.navigate(
                            MainRoute.Home.route
                        ){

                            popUpTo(
                                navController.graph
                                    .findStartDestination().id
                            ){

                                saveState= false
                                inclusive = false
                            }

                            launchSingleTop=true

                            restoreState=true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Home,
                            contentDescription = "Главная"
                        )
                    },
                    label = { Text("Главная") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1F1F1F),
                        selectedTextColor = Color(0xFF1F1F1F),
                        indicatorColor = gold,
                        unselectedIconColor = Color(0xFF49454F),
                        unselectedTextColor = Color(0xFF49454F)
                    )
                )

                NavigationBarItem(
                    selected = isMapSelected,
                    onClick = {

                        navController.navigate(
                            MainRoute.Map.route
                        ) {

                            popUpTo(
                                navController.graph
                                    .findStartDestination().id
                            ) {

                                saveState = false
                                inclusive = false
                            }

                            launchSingleTop = true

                            restoreState = false
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Map,
                            contentDescription = "Карта"
                        )
                    },
                    label = { Text("Карта") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1F1F1F),
                        selectedTextColor = Color(0xFF1F1F1F),
                        indicatorColor = gold,
                        unselectedIconColor = Color(0xFF49454F),
                        unselectedTextColor = Color(0xFF49454F)
                    )
                )

                NavigationBarItem(
                    selected = isProfileSelected,
                    onClick = {

                        navController.navigate(
                            MainRoute.Profile.route
                        ) {

                            popUpTo(
                                navController.graph
                                    .findStartDestination().id
                            ) {

                                saveState = false
                                inclusive = false
                            }

                            launchSingleTop = true

                            restoreState = false
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Профиль"
                        )
                    },
                    label = { Text("Профиль") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1F1F1F),
                        selectedTextColor = Color(0xFF1F1F1F),
                        indicatorColor = gold,
                        unselectedIconColor = Color(0xFF49454F),
                        unselectedTextColor = Color(0xFF49454F)
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(MainRoute.Home.route) {
                HomeScreen(
                    onOpenProfile = {
                        navController.navigate(MainRoute.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenDistrict = { districtId ->

                        CoroutineScope(
                            Dispatchers.IO
                        ).launch {

                            val region =

                                offlineRepository
                                    .getRegions()
                                    .firstOrNull {

                                        it.id.toString() ==
                                                districtId
                                    }

                            region?.name?.let {

                                currentRegionManager
                                    .saveRegion(it)
                            }
                        }

                        navController.navigate(
                            MainRoute.Home.route
                        ) {

                            popUpTo(
                                MainRoute.Home.route
                            ) {

                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    },
                    onOpenDistrictsList = {
                        navController.navigate(MainRoute.Districts.route)
                    },
                    onOpenMap = {
                        navController.navigate(MainRoute.Map.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenStories = {
                        navController.navigate(MainRoute.Stories.route)
                    },
                    onOpenPlace = { placeId ->
                        navController.navigate("place_detail/$placeId")
                    },
                    onPlayAudio = onPlayAudio
                )
            }

            composable(MainRoute.Map.route) {
                MapScreen(
                    onBack = {
                        navController.navigate(MainRoute.Home.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenPlace = { placeId ->
                        navController.navigate("place_detail/$placeId")
                    }
                )
            }

            composable(MainRoute.Profile.route) {
                ProfileScreen(
                    isGuest = isGuest,
                    onBack = {
                        navController.popBackStack()
                    },
                    onEditProfile = {
                        navController.navigate(MainRoute.EditProfile.route)
                    },
                    onLogout = {
                        onLogout()
                    }
                )
            }

            composable(MainRoute.Districts.route) {
                DistrictsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onOpenDistrict = { districtId ->
                        navController.navigate("district_detail/$districtId")
                    }
                )
            }

            composable(
                route = MainRoute.DistrictDetail.route,
                arguments = listOf(
                    navArgument("districtId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val districtId = backStackEntry.arguments?.getString("districtId").orEmpty()

                DistrictDetailScreen(
                    districtId = districtId,
                    onOpenDistrict = { nextDistrictId ->
                        navController.navigate("district_detail/$nextDistrictId")
                    },
                    onOpenDistrictsList = {
                        navController.navigate(MainRoute.Districts.route)
                    },
                    onOpenMap = {
                        navController.navigate(MainRoute.Map.route)
                    },
                    onOpenStories = {
                        navController.navigate(MainRoute.Stories.route)
                    },
                    onOpenPlace = { placeId ->
                        navController.navigate("place_detail/$placeId")
                    },
                    onPlayAudio = onPlayAudio,
                    onOpenProfile = {
                        navController.navigate(MainRoute.Profile.route)
                    }
                )
            }

            composable(MainRoute.Stories.route) {
                StoriesScreen(
                    onBack = { navController.popBackStack() },
                    onOpenStory = { storyId ->
                        navController.navigate("story_detail/$storyId")
                    }
                )
            }

            composable(
                route = MainRoute.PlaceDetail.route,
                arguments = listOf(
                    navArgument("placeId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getString("placeId").orEmpty()

                PlaceDetailScreen(
                    placeId = placeId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(MainRoute.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "story_detail/{storyId}",
                arguments = listOf(
                    navArgument("storyId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val storyId = backStackEntry.arguments?.getString("storyId").orEmpty()

                StoryDetailScreen(
                    storyId = storyId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}