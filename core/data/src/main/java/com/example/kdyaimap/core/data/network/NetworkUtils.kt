package com.example.kdyaimap.core.data.network

import retrofit2.Response
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException

/**
 * 网络响应处理工具类
 */
inline fun <reified T> Response<ApiService.ApiResponse<T>>.handleResponse(): Result<T> {
    return try {
        android.util.Log.d("NetworkUtils", "响应状态: ${code()}, 成功: $isSuccessful")
        
        if (isSuccessful) {
            val body = body()
            if (body != null) {
                android.util.Log.d("NetworkUtils", "响应体: success=${body.success}, message=${body.message}")
                
                if (body.success) {
                    body.data?.let { data ->
                        android.util.Log.d("NetworkUtils", "响应数据解析成功")
                        Result.success(data)
                    } ?: Result.failure(NetworkException("响应数据为空", "DATA_EMPTY"))
                } else {
                    val errorMessage = body.message ?: "请求失败"
                    android.util.Log.w("NetworkUtils", "API返回失败: $errorMessage")
                    Result.failure(NetworkException(errorMessage, "API_ERROR"))
                }
            } else {
                android.util.Log.e("NetworkUtils", "响应体为空")
                Result.failure(NetworkException("响应体为空", "EMPTY_RESPONSE"))
            }
        } else {
            val errorMessage = "HTTP错误: ${code()} - ${message()}"
            android.util.Log.e("NetworkUtils", errorMessage)
            Result.failure(NetworkException(errorMessage, "HTTP_ERROR", code()))
        }
    } catch (e: JsonSyntaxException) {
        android.util.Log.e("NetworkUtils", "JSON语法错误", e)
        Result.failure(NetworkException("数据格式错误", "JSON_SYNTAX_ERROR"))
    } catch (e: MalformedJsonException) {
        android.util.Log.e("NetworkUtils", "JSON格式错误", e)
        Result.failure(NetworkException("数据格式错误", "MALFORMED_JSON"))
    } catch (e: Exception) {
        android.util.Log.e("NetworkUtils", "网络响应处理异常", e)
        Result.failure(NetworkException(e.message ?: "未知错误", "UNKNOWN_ERROR"))
    }
}

/**
 * 自定义网络异常类
 */
class NetworkException(
    message: String,
    val errorCode: String,
    val httpCode: Int = -1
) : Exception(message) {
    
    fun getUserFriendlyMessage(): String? {
        return when (errorCode) {
            "DATA_EMPTY" -> "服务器返回的数据为空，请稍后重试"
            "API_ERROR" -> message
            "EMPTY_RESPONSE" -> "服务器无响应，请检查网络连接"
            "HTTP_ERROR" -> when (httpCode) {
                401 -> "身份验证失败，请重新登录"
                403 -> "权限不足，无法访问该资源"
                404 -> "请求的资源不存在"
                429 -> "请求过于频繁，请稍后重试"
                500 -> "服务器内部错误，请稍后重试"
                502 -> "服务器网关错误，请稍后重试"
                503 -> "服务暂时不可用，请稍后重试"
                504 -> "服务器响应超时，请稍后重试"
                else -> "网络请求失败 (${httpCode})"
            }
            "JSON_SYNTAX_ERROR", "MALFORMED_JSON" -> "数据格式错误，请稍后重试"
            "UNKNOWN_ERROR" -> message
            else -> message
        }
    }
    
    fun isAuthError(): Boolean {
        return httpCode == 401 || errorCode == "AUTH_ERROR"
    }
    
    fun isNetworkError(): Boolean {
        return errorCode in listOf("NETWORK_ERROR", "TIMEOUT", "CONNECTION_ERROR")
    }
    
    fun isServerError(): Boolean {
        return httpCode in 500..599 || errorCode == "SERVER_ERROR"
    }
}

/**
 * 网络异常处理工具类
 */
object NetworkErrorHandler {
    
    /**
     * 处理网络异常，返回用户友好的错误信息
     */
    fun handleError(throwable: Throwable): String? {
        return when (throwable) {
            is NetworkException -> throwable.getUserFriendlyMessage()
            is java.net.SocketTimeoutException -> "网络连接超时，请检查网络设置"
            is java.net.UnknownHostException -> "网络连接失败，请检查网络连接"
            is java.net.ConnectException -> "无法连接到服务器，请稍后重试"
            is java.net.SocketException -> "网络连接异常，请重试"
            is javax.net.ssl.SSLException -> "安全连接失败，请检查网络设置"
            is retrofit2.HttpException -> {
                when (throwable.code()) {
                    401 -> "身份验证失败，请重新登录"
                    403 -> "权限不足，无法访问该资源"
                    404 -> "请求的资源不存在"
                    429 -> "请求过于频繁，请稍后重试"
                    500 -> "服务器内部错误，请稍后重试"
                    502 -> "服务器网关错误，请稍后重试"
                    503 -> "服务暂时不可用，请稍后重试"
                    504 -> "服务器响应超时，请稍后重试"
                    else -> "网络请求失败 (${throwable.code()})"
                }
            }
            is JsonSyntaxException, is MalformedJsonException -> "数据格式错误，请稍后重试"
            else -> throwable.message ?: "未知错误"
        }
    }
    
    /**
     * 判断是否为可重试的错误
     */
    fun isRetryableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is NetworkException -> {
                throwable.isNetworkError() || throwable.isServerError()
            }
            is java.net.SocketTimeoutException,
            is java.net.ConnectException,
            is java.net.SocketException -> true
            is retrofit2.HttpException -> throwable.code() in 500..599
            else -> false
        }
    }
    
    /**
     * 判断是否为认证错误
     */
    fun isAuthError(throwable: Throwable): Boolean {
        return when (throwable) {
            is NetworkException -> throwable.isAuthError()
            is retrofit2.HttpException -> throwable.code() == 401
            else -> false
        }
    }
}