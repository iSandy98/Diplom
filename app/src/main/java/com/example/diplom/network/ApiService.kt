package com.example.diplom.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName


data class RegisterRequest(
    val email: String,
    val password: String,
    val password_confirm: String
)

data class RegisterResponse(
    val user: ProfileDto,
    val refresh: String,
    val access: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    val refresh: String,
    val access: String
)

data class RefreshTokenRequest(
    val refresh: String
)

data class GuestRequest(
    val device_id: String
)

data class GuestResponse(
    val access: String,
    val refresh: String,
    val is_guest: Boolean
)

data class ProfileDto(
    val id: Int,
    val username: String?,
    val email: String?,
    val first_name: String?,
    val last_name: String?,
    val avatar: String?,
    val bio: String?
)

data class RegionDto(
    val id: Int,
    val name: String,
    val description: String?,
    val image: String?,
    val points_count: Int?
)

data class CategoryDto(
    val id: Int,
    val name: String,
    val slug: String?
)

data class PhotoDto(
    val id: Int,
    val image: String?,
    val caption: String?,
    val order: Int?
)

data class AudioGuideDto(

    val id:Int,

    @SerializedName(
        value = "audio_file",
        alternate = ["url"]
    )
    val audio_file:String?,

    val duration_seconds:Int?,

    val language:String?
)

data class PointListDto(

    val id:Int,

    val name:String,

    val description:String?,

    val category_name:String?,

    val region_name:String?,

    val latitude:Double?,

    val longitude:Double?,

    val cover_photo:String?,

    val has_audio:Boolean?,

    val created_at:String?
)

data class PointDetailDto(
    val id: Int,
    val name: String,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val category: CategoryDto?,
    val region: RegionDto?,
    val photos: List<PhotoDto>?,
    val audio_guide: AudioGuideDto?,
    val created_at: String?,
    val updated_at: String?
)

data class PagedResponse<T>(
    val count: Int?,
    val next: String?,
    val previous: String?,
    val results: List<T>
)

data class StoryDto(
    val id: Int,
    val title: String,
    val description: String?,
    val image: String?,
    val created_at: String?
)

data class OfflineRegionBundleDto(

    val version: String?,

    val region: RegionDto,

    val points: List<PointOfflineDto>,

    val routes: List<Any>,

    val stories: List<StoryDto>
)

data class PointOfflineDto(

    val id: Int,

    val name: String,

    val description: String?,

    val latitude: Double?,

    val longitude: Double?,

    val category_slug: String?,

    val photos: List<PhotoOfflineDto>,

    val audio: AudioGuideDto?,

    val updated_at: String?
)

data class PhotoOfflineDto(

    val id: Int,

    val url: String?,

    val caption: String?,

    val order: Int?
)

data class RoutePointDto(

    val order:Int,

    val point_id:Int,

    val name:String,

    val latitude:String?,

    val longitude:String?,

    val note:String?
)

data class RouteDetailDto(

    val id:Int,

    val name:String,

    val description:String?,

    val duration_minutes:Int?,

    val difficulty_display:String?,

    val avg_rating:Double?,

    val points_count:Int?,

    val route_points:List<RoutePointDto>
)

interface ApiService {

    @POST("api/auth/register/")
    suspend fun register(
        @Body body: RegisterRequest
    ): RegisterResponse

    @POST("api/auth/token/")
    suspend fun login(
        @Body body: LoginRequest
    ): TokenResponse

    @POST("api/auth/token/refresh/")
    suspend fun refreshToken(
        @Body body: RefreshTokenRequest
    ): TokenResponse

    @POST("api/auth/guest/")
    suspend fun guestLogin(
        @Body body: GuestRequest
    ): GuestResponse

    @GET("api/auth/profile/")
    suspend fun getProfile(): ProfileDto

    @GET("api/regions/")
    suspend fun getRegions(): PagedResponse<RegionDto>

    @GET("api/regions/{id}/")
    suspend fun getRegionDetail(
        @Path("id") id: Int
    ): RegionDto

    @GET("api/categories/")
    suspend fun getCategories(): List<CategoryDto>

    @GET("api/points/")
    suspend fun getPoints(

        @retrofit2.http.Query(
            "page"
        )
        page:Int

    ):PagedResponse<PointListDto>

    @GET("api/points/{id}/")
    suspend fun getPointDetail(
        @Path("id") id: Int
    ): PointDetailDto

    @GET("api/stories/")
    suspend fun getStories(): PagedResponse<StoryDto>

    @GET("api/stories/{id}/")
    suspend fun getStoryDetail(
        @Path("id") id: Int
    ): StoryDto

    @GET(
        "api/regions/{id}/offline/"
    )
    suspend fun getOfflineRegion(

        @Path("id")
        id:Int

    ): OfflineRegionBundleDto

    @GET("api/routes/")
    suspend fun getRoutes():
            PagedResponse<RouteDetailDto>

    @GET("api/routes/{id}/")
    suspend fun getRouteDetail(

        @Path("id")
        id:Int

    ): RouteDetailDto


}