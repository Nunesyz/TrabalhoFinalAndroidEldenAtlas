package com.lucasn.eldenatlas.network
import com.lucasn.eldenatlas.data.*
import retrofit2.http.*
import retrofit2.http.Query as ApiQuery
interface EldenApi {
 @GET("bosses/{id}")
 suspend fun boss(
  @Path("id") id: String
 ): BossDetailResponse

 @GET("bosses")
 suspend fun bosses(
  @ApiQuery("page") page: Int,
  @ApiQuery("name") name: String?,
  @ApiQuery("limit") limit: Int = 20
 ): BossResponse
}


object ApiProvider { val api: EldenApi = retrofit2.Retrofit.Builder().baseUrl("https://eldenring.fanapis.com/api/").addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create()).build().create(EldenApi::class.java) }
