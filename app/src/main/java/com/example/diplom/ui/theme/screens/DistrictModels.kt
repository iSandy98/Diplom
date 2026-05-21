package com.example.diplom.ui.theme.screens

data class DistrictListItemUi(

    val id: String,

    val title: String,

    val description: String,

    val isDownloaded: Boolean = false,

    val isDownloading: Boolean = false,

    val downloadProgress: Float = 0f
)

data class DistrictDetailUi(
    val id: String,
    val title: String,
    val nearbyTitle: String = "Рядом со мной",
    val places: List<PlaceUi> = emptyList(),
    val stories: List<StoryUi> = emptyList(),
    val otherDistricts: List<DistrictUi> = emptyList()
)

object DistrictRepositoryMock {

    private val allDistricts = listOf(
        DistrictListItemUi(
            id = "nam",
            title = "Намский район",
            description = "Supporting line text lorem ipsum dolor sit amet, consectetur."
        ),
        DistrictListItemUi(
            id = "kan",
            title = "Кангаласский район",
            description = "Supporting line text lorem ipsum dolor sit amet, consectetur."
        ),
        DistrictListItemUi(
            id = "oym",
            title = "Оймяконский район",
            description = "Supporting line text lorem ipsum dolor sit amet, consectetur."
        ),
        DistrictListItemUi(
            id = "sun",
            title = "Сунтарский район",
            description = "Supporting line text lorem ipsum dolor sit amet, consectetur."
        ),
        DistrictListItemUi(
            id = "ust",
            title = "Усть-Алданский район",
            description = "Supporting line text lorem ipsum dolor sit amet, consectetur."
        )
    )

    fun getDistricts(): List<DistrictListItemUi> = allDistricts

    fun getDistrictDetail(id: String): DistrictDetailUi {
        val title = allDistricts.firstOrNull { it.id == id }?.title ?: "Район"

        return DistrictDetailUi(
            id = id,
            title = title,
            places = listOf(
                PlaceUi(
                    id = "${id}_place_1",
                    title = "Музей мамонта",
                    description = "Description duis aute irure dolor in reprehenderit in voluptate velit.",
                    locationLabel = "Нерюнгри • Аудиогид"
                ),
                PlaceUi(
                    id = "${id}_place_2",
                    title = "Бюст Мусе Джалилю",
                    description = "Description duis aute irure dolor in reprehenderit in voluptate velit.",
                    locationLabel = "Нерюнгри • Аудиогид"
                )
            ),
            stories = listOf(
                StoryUi("1", "Название", "Описание"),
                StoryUi("2", "Название", "Описание"),
                StoryUi("3", "Название", "Описание")
            ),
            otherDistricts = listOf(
                DistrictUi("nam", "Намский"),
                DistrictUi("kan", "Кангаласский"),
                DistrictUi("oym", "Оймяконский"),
                DistrictUi("sun", "Сунтарский")
            )
        )
    }
}