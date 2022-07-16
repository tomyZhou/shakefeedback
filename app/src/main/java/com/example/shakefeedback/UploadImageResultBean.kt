package com.example.shakefeedback

import android.os.Parcelable
import java.io.Serializable


class UploadImageResultBean : Serializable {
    var src: String? = ""
    var domain: String? = ""
    var scenes: String? = ""
    var retmsg: String? = ""
    var url: UrlBean? = null
    var retcode: Int? = 0
}

class UrlBean : Serializable {
    var file: String? = ""
}
