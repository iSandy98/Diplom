package com.example.diplom.ui.theme.screens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DistrictUi(
    val id: String,
    val title: String,
    val imageUrl: String? = null
)

data class StoryUi(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val imageUrl: String? = null
)

data class PlaceUi(

    val id: String,

    val title: String,

    val description: String,

    val locationLabel: String,

    val imageUrl: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null
)

data class HomeUiState(
    val districts: List<DistrictUi> = emptyList(),
    val stories: List<StoryUi> = emptyList(),
    val places: List<PlaceUi> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            districts = listOf(
                DistrictUi("nam", "Намский"),
                DistrictUi("kan", "Кангаласский"),
                DistrictUi("oym", "Оймяконский"),
                DistrictUi("sun", "Сунтарский"),
                DistrictUi("meg", "Мегино-Кангаласский")
            ),
            stories = listOf(
                StoryUi("1", "Название", "Описание"),
                StoryUi("2", "Название", "Описание"),
                StoryUi("3", "Название", "Описание"),
                StoryUi("4", "Название", "Описание"),
                StoryUi("5", "Название", "Описание")
            ),
            places = listOf(
                PlaceUi(
                    id = "mammoth",
                    title = "Музей мамонта",
                    description = "Description duis aute irure dolor in reprehenderit in voluptate velit.",
                    locationLabel = "Якутск • Аудиогид"
                ),
                PlaceUi(
                    id = "bust",
                    title = "Бюст Мусе Джалилю",
                    description = "Description duis aute irure dolor in reprehenderit in voluptate velit.",
                    locationLabel = "Нерюнгри • Аудиогид"
                )
            )
        )
    )

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}