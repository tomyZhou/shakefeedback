package com.example.shakefeedback;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UploadService {
    @Headers({"appid:b9fb10e5397128ea75c2ce1df967b347d62ba580", "token:f48613e0bc00d8fbf744b13fe6aa802afc1e8ea5"})
    @Multipart
    @POST("upload?output_type=json&type=file&scenes=applog")
    Observable<UploadImageResultBean> upload(@Part MultipartBody.Part file);

    @GET("http://paya.fenqile.com/route0015/paya/support/add_tapd_bug")
    Observable<UploadImageResultBean> uploadInfo(@Query("imgUrl") String imageUrl, @Query("feedbacktxt") String feedbacktxt);

}
