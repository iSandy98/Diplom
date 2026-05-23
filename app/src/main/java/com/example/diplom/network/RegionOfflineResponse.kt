package com.example.diplom.network

data class RegionOfflineResponse(

    val region: RegionDto,

    val places: List<PointDetailDto>,

    val stories: List<StoryDto>

)