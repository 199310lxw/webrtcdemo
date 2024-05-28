package com.xwl.webrtcdemo

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.orhanobut.logger.Logger

/**
 * @author  lxw
 * @date 2024/5/23
 * descripe
 */
inline fun AppCompatActivity.requestPermission(
    vararg permissions: String,
    crossinline allGranted: (() -> Unit),
    crossinline denied: ((List<String>) -> Unit),
    crossinline explained: ((List<String>) -> Unit)) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
        val deniedList = result.filter { !it.value }.map { it.key}

        when {
            deniedList.isEmpty() ->  allGranted.invoke()
            else -> {
                //对被拒绝全选列表进行分组，分组条件为是否勾选不再询问
                val map = deniedList.groupBy { permission ->
                    if (shouldShowRequestPermissionRationale(permission)) "DENIED" else "EXPLAINED"
                }
                //被拒接且没勾选不再询问
                map["DENIED"]?.let { denied.invoke(it) }
                //被拒接且勾选不再询问
                map["EXPLAINED"]?.let { explained.invoke(it) }
            }
        }
    }.launch(permissions as Array<String>)
}

inline fun Fragment.requestPermission(
    vararg permissions: String,
    crossinline allGranted: (() -> Unit),
    crossinline denied: ((List<String>) -> Unit),
    crossinline explained: ((List<String>) -> Unit)) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
        val deniedList = result.filter { !it.value }.map { it.key}
        when {
            deniedList.isEmpty() ->  allGranted.invoke()
            else -> {
                //对被拒绝全选列表进行分组，分组条件为是否勾选不再询问
                val deniedMap = deniedList.groupBy {permission ->
                    shouldShowRequestPermissionRationale(permission)
                }

                val explainMap = deniedList.groupBy {permission ->
                    !shouldShowRequestPermissionRationale(permission)
                }
                //被拒接且没勾选不再询问
                deniedMap.let { denied.invoke(it.values as List<String>) }

                explainMap.let { explained.invoke(it.values as List<String>) }
            }
        }
    }.launch(permissions as Array<String>)
}